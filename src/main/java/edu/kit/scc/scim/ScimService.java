/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.scim;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.kit.scc.dto.IndigoUser;
import edu.kit.scc.dto.PosixGroup;
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

	private static final String DEFAULT_HOME_DIRECTORY = "/home";
	private static final String DEFAULT_DESCRIPTION = "INDIGO-DataCloud";
	private static final String DEFAULT_INDIGO_GROUP = "indigo";

	/**
	 * Creates a new LDAP INDIGO user according to the provided SCIM object.
	 * 
	 * @param scimUser
	 *            the new user to be created
	 * @return a {@link ScimUser} with the user information of the created user.
	 */
	public ScimUser createLdapIndigoUser(ScimUser scimUser) {
		ScimUser createdUser = null;

		ScimUserAttributeMapper mapper = new ScimUserAttributeMapper();
		IndigoUser indigoUser = mapper.mapToIndigoUser(scimUser);

		if (indigoUser.getUid() == null)
			return null;
		if (indigoUser.getIndigoId() == null)
			return null;
		if (indigoUser.getCommonName() == null)
			return null;
		if (indigoUser.getSurName() == null)
			return null;
		if (indigoUser.getHomeDirectory() == null)
			indigoUser.setHomeDirectory(DEFAULT_HOME_DIRECTORY + "/" + indigoUser.getUid());
		if (indigoUser.getDescription() == null)
			indigoUser.setDescription(DEFAULT_DESCRIPTION);

		int claimedPrimaryGidNumber = indigoUser.getGidNumber();
		int clamiedPrimaryUidNumber = indigoUser.getUidNumber();
		List<ScimGroup> scimGroups = scimUser.getGroups();

		List<PosixGroup> localGroups = ldapClient.getPosixGroups();

		boolean matchingPrimaryGidNumber = false;
		if (scimGroups != null) {
			for (ScimGroup scimGroup : scimGroups) {
				boolean matchingLocalGroup = false;

				int gidNumber = Integer.valueOf(scimGroup.getValue());
				String commonName = scimGroup.getDisplay();

				try {
					matchingLocalGroup = ldapClient.equalGroups(ldapClient.getPosixGroup(gidNumber),
							ldapClient.getPosixGroup(commonName));
				} catch (Exception e) {
					log.error("ERROR {}", e.getMessage());
					e.printStackTrace();
				}

				if (gidNumber == claimedPrimaryGidNumber)
					matchingPrimaryGidNumber = true;

			}
		}

		try {
			IndigoUser ldapUser = ldapClient.createIndigoUser(indigoUser.getUid(), indigoUser.getCommonName(),
					indigoUser.getSurName(), indigoUser.getIndigoId(), indigoUser.getUidNumber(),
					indigoUser.getGidNumber(), indigoUser.getHomeDirectory(), indigoUser.getDescription(),
					indigoUser.getGecos(), indigoUser.getLoginShell(), null);

			createdUser = mapper.mapFromIndigoUser(ldapUser);
		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
			// e.printStackTrace();
		}
		return createdUser;
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
