/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.scim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.kit.scc.dto.IndigoUser;
import edu.kit.scc.dto.PosixGroup;
import edu.kit.scc.dto.PosixUser;
import edu.kit.scc.ldap.LdapClient;

/**
 * SCIM service implementation.
 * 
 * @author benjamin
 *
 */
@Component
public class ScimService {

	private static final Logger log = LoggerFactory.getLogger(ScimService.class);

	@Autowired
	private LdapClient ldapClient;

	public static final String DEFAULT_HOME_DIRECTORY = "/home";
	public static final String DEFAULT_DESCRIPTION = "INDIGO-DataCloud";
	public static final String DEFAULT_INDIGO_GROUP = "indigo";

	/**
	 * Creates a new LDAP INDIGO user according to the provided SCIM object.
	 * 
	 * @param scimUser
	 *            the new user to be created
	 * @return a {@link ScimUser} with the user information of the created user.
	 */
	public ScimUser createLdapIndigoUser(ScimUser scimUser) {
		ScimUser createdUser = null;
		ScimUserAttributeMapper userMapper = new ScimUserAttributeMapper();
		ScimGroupAttributeMapper groupMapper = new ScimGroupAttributeMapper();

		IndigoUser indigoUser = userMapper.mapToIndigoUser(scimUser);

		if (indigoUser == null)
			return createdUser;

		List<ScimGroup> scimGroups = scimUser.getGroups();

		int clamiedPrimaryUidNumber = indigoUser.getUidNumber();
		if (clamiedPrimaryUidNumber != 0) {
			// user claims to have local user id
			log.debug("User claimed local uidNumber {}", clamiedPrimaryUidNumber);
			createdUser = createOrGetDefaultUser(indigoUser);
			log.debug("Created user {}", createdUser.toString());
			// - verify local user
			// - modify created user's uidNumber
			IndigoUser localUser = userMapper.mapToIndigoUser(createdUser);
			log.debug("Map from {} to {}", createdUser.toString(), localUser.toString());

			PosixUser verifiedLocalUser = getVerifiedLocalUser(clamiedPrimaryUidNumber);
			log.debug("Found verified local user {}", verifiedLocalUser.toString());

			int verifiedLocalUidNumber = verifiedLocalUser.getUidNumber();
			String verifiedLocalHomeDirectory = verifiedLocalUser.getHomeDirectory();

			IndigoUser updatedUser = ldapClient.updateIndigoUser(localUser.getUid(), localUser.getCommonName(),
					localUser.getSurName(), localUser.getIndigoId(), verifiedLocalUidNumber, localUser.getGidNumber(),
					verifiedLocalHomeDirectory, localUser.getDescription(), localUser.getGecos(),
					localUser.getLoginShell(),
					(localUser.getUserPassword() != null ? new String(localUser.getUserPassword()) : null));

			log.debug("Updated user {}", updatedUser.toString());
			createdUser = userMapper.mapFromIndigoUser(updatedUser);
		} else {
			// user has no local user id, use default
			log.debug("User has no local uidNumber");
			createdUser = createOrGetDefaultUser(indigoUser);
		}

		if (scimGroups != null) {
			HashMap<String, Integer> verifiedLocalGroups = getVerifiedLocalGroups(scimGroups);
			for (String group : verifiedLocalGroups.keySet()) {
				log.debug("Add user {} to group {}", createdUser.getId(), group);
				ldapClient.addGroupMember(group, createdUser.getId());
			}
			int claimedPrimaryGidNumber = indigoUser.getGidNumber();
			if (claimedPrimaryGidNumber != 0) {
				// user claims to have local primary group
				log.debug("User claimed primary group {}", claimedPrimaryGidNumber);
			} else {
				// user claims to have no local primary group
				log.debug("User has no primary local group, use default {}", indigoUser.getGidNumber());
			}
		}

		List<PosixGroup> userGroups = ldapClient.getUserGroups(createdUser.getUserName());
		createdUser.setGroups(new ArrayList<ScimGroup>());
		for (PosixGroup group : userGroups) {
			log.debug("User is member of group {}", group.toString());
			createdUser.getGroups().add(groupMapper.mapFromPosixGroup(group));
		}

		return createdUser;
	}

	private PosixUser getVerifiedLocalUser(int claimedPrimaryUidNumber) {
		PosixUser user = null;
		List<PosixUser> users = ldapClient.getPosixUsers(claimedPrimaryUidNumber);
		if (users != null && !users.isEmpty()) {
			if (users.size() == 1) {
				user = users.get(0);
			} else {
				// multiple users with the same uidNumber
				user = users.get(0);
			}
		}
		return user;
	}

	private HashMap<String, Integer> getVerifiedLocalGroups(List<ScimGroup> groups) {
		HashMap<String, Integer> localGroups = new HashMap<String, Integer>();

		if (groups != null) {
			for (ScimGroup scimGroup : groups) {

				int gidNumber = Integer.valueOf(scimGroup.getValue());
				String commonName = scimGroup.getDisplay();

				try {
					if (ldapClient.equalGroups(ldapClient.getPosixGroup(gidNumber),
							ldapClient.getPosixGroup(commonName))) {
						log.debug("Found matching local group {} {}", commonName, gidNumber);
						localGroups.put(commonName, gidNumber);
					}
				} catch (Exception e) {
					log.error("ERROR {}", e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return localGroups;
	}

	private ScimUser createOrGetDefaultUser(IndigoUser indigoUser) {
		ScimUser user = null;
		ScimUserAttributeMapper mapper = new ScimUserAttributeMapper();
		ScimGroup indigoGroup = createOrGetDefaultGroup();

		try {
			indigoUser.setUidNumber(ldapClient.generateUserIdNumber());
			indigoUser.setGidNumber(Integer.valueOf(indigoGroup.getValue()));
			indigoUser.setHomeDirectory(DEFAULT_HOME_DIRECTORY + "/" + indigoUser.getUid());

			log.debug("Create INDIGO user {}", indigoUser.toString());

			IndigoUser ldapUser = ldapClient.createIndigoUser(indigoUser.getUid(), indigoUser.getCommonName(),
					indigoUser.getSurName(), indigoUser.getIndigoId(), indigoUser.getUidNumber(),
					indigoUser.getGidNumber(), indigoUser.getHomeDirectory(), indigoUser.getDescription(),
					indigoUser.getGecos(), indigoUser.getLoginShell(), null);

			ldapClient.addGroupMember(indigoGroup.getDisplay(), ldapUser.getUid());

			log.debug("Created/got LDAP INDIGO user {}", ldapUser.toString());

			user = mapper.mapFromIndigoUser(ldapUser);
		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
			// e.printStackTrace();
		}
		log.debug("Created/got user {}", user.toString());
		return user;
	}

	private ScimGroup createOrGetDefaultGroup() {
		ScimGroup createdGroup = null;
		ScimGroupAttributeMapper mapper = new ScimGroupAttributeMapper();
		try {
			int gidNumber = ldapClient.generateGroupIdNumber();

			log.debug("Create INDIGO Group {} {}", DEFAULT_INDIGO_GROUP, gidNumber);

			PosixGroup ldapGroup = ldapClient.createPosixGroup(DEFAULT_INDIGO_GROUP, gidNumber, DEFAULT_DESCRIPTION,
					null);

			log.debug("Created/got LDAP group {}", ldapGroup.toString());

			createdGroup = mapper.mapFromPosixGroup(ldapGroup);
		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
			// e.printStackTrace();
		}
		return createdGroup;
	}

	/**
	 * Creates a new LDAP POSIX group according to the provided SCIM object.
	 * 
	 * @param scimGroup
	 *            the new group to be created
	 * @return a {@link ScimGroup} with the group information of the created
	 *         group.
	 */
	public ScimGroup createLdapPosixGroup(ScimGroup scimGroup) {
		ScimGroup createdGroup = null;
		ScimGroupAttributeMapper mapper = new ScimGroupAttributeMapper();

		PosixGroup posixGroup = mapper.mapToPosixGroup(scimGroup);
		if (posixGroup.getCommonName() == null)
			return null;

		posixGroup.setGidNumber(ldapClient.generateGroupIdNumber());

		try {
			PosixGroup ldapGroup = ldapClient.createPosixGroup(posixGroup.getCommonName(), posixGroup.getGidNumber(),
					posixGroup.getDescription(), null);

			createdGroup = mapper.mapFromPosixGroup(ldapGroup);
		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
			e.printStackTrace();
		}
		return createdGroup;
	}
}
