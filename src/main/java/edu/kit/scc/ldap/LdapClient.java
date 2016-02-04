/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

import edu.kit.scc.dto.PosixGroup;
import edu.kit.scc.dto.IndigoUser;

/**
 * LDAP client implementation.
 * 
 * @author benjamin
 *
 */
@Component
public class LdapClient {

	private static Logger log = LoggerFactory.getLogger(LdapClient.class);

	@Value("${ldap.url}")
	private String url;

	@Value("${ldap.searchBase}")
	private String searchBase;

	@Value("${ldap.userBase}")
	private String userBase;

	@Value("${ldap.groupBase}")
	private String groupBase;

	@Value("${ldap.bindDn}")
	private String bindDn;

	@Value("${ldap.bindPassword}")
	private String password;

	@Bean
	LdapContextSource contextSource() {
		LdapContextSource ldapContextSource = new LdapContextSource();
		ldapContextSource.setUrl(url);
		ldapContextSource.setBase(searchBase);
		ldapContextSource.setUserDn(bindDn);
		ldapContextSource.setPassword(password);
		return ldapContextSource;
	}

	@Bean
	LdapTemplate ldapTemplate(LdapContextSource contextSource) {
		return new LdapTemplate(contextSource);
	}

	@Bean
	LdapIndigoUserDAO ldapUser(LdapTemplate ldapTemplate) {
		LdapIndigoUserDAO ldapUserDAO = new LdapIndigoUserDAO();
		ldapUserDAO.setLdapTemplate(ldapTemplate);
		ldapUserDAO.setUserBase(userBase);
		return ldapUserDAO;
	}

	@Bean
	LdapPosixGroupDAO ldapGroup(LdapTemplate ldapTemplate) {
		LdapPosixGroupDAO ldapGroupDAO = new LdapPosixGroupDAO();
		ldapGroupDAO.setLdapTemplate(ldapTemplate);
		ldapGroupDAO.setGroupBase(groupBase);
		return ldapGroupDAO;
	}

	@Autowired
	private LdapIndigoUserDAO ldapUser;

	@Autowired
	private LdapPosixGroupDAO ldapGroup;

	/**
	 * Gets the user specified from the LDAP server.
	 * 
	 * @param uid
	 *            the user's uid
	 * @return a {@link IndigoUser} with the LDAP user information
	 */
	public IndigoUser getIndigoUser(String uid) {
		List<IndigoUser> userList = ldapUser.getUserDetails(uid);
		IndigoUser user = null;

		if (userList != null && !userList.isEmpty()) {
			user = userList.get(0);
			log.debug(user.toString());
		}
		return user;
	}

	/**
	 * Gets the group specified from the LDAP server.
	 * 
	 * @param cn
	 *            the group's common name
	 * @return a {@link PosixGroup} with the LDAP group information
	 */
	public PosixGroup getPosixGroup(String cn) {
		List<PosixGroup> groupList = ldapGroup.getGroupDetails(cn);
		PosixGroup group = null;

		if (groupList != null && !groupList.isEmpty()) {
			group = groupList.get(0);
			log.debug(group.toString());
		}
		return group;
	}

	/**
	 * Gets all users from the LDAP server.
	 * 
	 * @return a {@link List<UserDTO>} with the LDAP user information
	 */
	public List<IndigoUser> getIndigoUsers() {
		List<IndigoUser> userList = ldapUser.getAllUsers();
		for (int i = 0; i < userList.size(); i++)
			log.debug("User {}", ((IndigoUser) userList.get(i)).toString());

		return userList;
	}

	/**
	 * Gets all groups from the LDAP server.
	 * 
	 * @return a {@link List<GroupDTO>} with the LDAP group information
	 */
	public List<PosixGroup> getPosixGroups() {
		List<PosixGroup> groupList = ldapGroup.getAllGroups();
		for (int i = 0; i < groupList.size(); i++)
			log.debug("Group {}", ((PosixGroup) groupList.get(i)).toString());

		return groupList;
	}

	/**
	 * Creates a new LDAP INDIGO POSIX user.
	 * 
	 * @param uid
	 *            the user's uid
	 * @param cn
	 *            the user's common name
	 * @param sn
	 *            the user's sure name
	 * @param uidNumber
	 *            the user's uid number
	 * @param gidNumber
	 *            the user's gid number
	 * @param homeDirectory
	 *            the user's home directory
	 * @param description
	 *            the user's description
	 * @param gecos
	 *            the user's general comprehensive operating system information
	 * @param loginShell
	 *            the user's login shell
	 * @param userPassword
	 *            the user's password
	 */
	public void createIndigoUser(String uid, String cn, String sn, String indigoId, int uidNumber, int gidNumber,
			String homeDirectory, String description, String gecos, String loginShell, String userPassword) {
		IndigoUser user = new IndigoUser();
		user.setCommonName(cn);
		user.setDescription(description);
		user.setSurName(sn);
		user.setUid(uid);
		user.setGecos(gecos);
		user.setIndigoId(indigoId);
		user.setGidNumber(gidNumber);
		user.setUidNumber(uidNumber);
		user.setHomeDirectory(homeDirectory);
		user.setLoginShell(loginShell);
		user.setUserPassword(userPassword);
		ldapUser.insertUser(user);
	}

	/**
	 * Updates a specific LDAP POSIX user.
	 * 
	 * @param uid
	 *            the user's uid
	 * @param cn
	 *            the user's common name
	 * @param sn
	 *            the user's sure name
	 * @param uidNumber
	 *            the user's uid number
	 * @param gidNumber
	 *            the user's gid number
	 * @param homeDirectory
	 *            the user's home directory
	 * @param description
	 *            the user's description
	 * @param gecos
	 *            the user's general comprehensive operating system information
	 * @param loginShell
	 *            the user's login shell
	 * @param userPassword
	 *            the user's password
	 */
	public void updateIndigoUser(String uid, String cn, String sn, String indigoId, int uidNumber, int gidNumber,
			String homeDirectory, String description, String gecos, String loginShell, String userPassword) {
		IndigoUser user = new IndigoUser();
		user.setCommonName(cn);
		user.setDescription(description);
		user.setSurName(sn);
		user.setUid(uid);
		user.setGecos(gecos);
		user.setIndigoId(indigoId);
		user.setGidNumber(gidNumber);
		user.setUidNumber(uidNumber);
		user.setHomeDirectory(homeDirectory);
		user.setLoginShell(loginShell);
		user.setUserPassword(userPassword);
		ldapUser.updateUser(user);
	}

	/**
	 * Deletes a specific LDAP user.
	 * 
	 * @param uid
	 *            the user's uid
	 */
	public void deleteUser(String uid) {
		IndigoUser user = new IndigoUser();
		user.setUid(uid);
		ldapUser.deleteUser(user);
	}

	/**
	 * Creates a new LDAP POSIX group.
	 * 
	 * @param cn
	 *            the group's common name
	 * @param gidNumber
	 *            the group's gid number
	 * @param the
	 *            group's description
	 * @param the
	 *            group's user password
	 * 
	 */
	public void createPosixGroup(String cn, int gidNumber, String description, String userPassword) {
		PosixGroup group = new PosixGroup();
		group.setCommonName(cn);
		group.setGidNumber(gidNumber);
		group.setDescription(description);
		group.setUserPassword(userPassword);
		ldapGroup.insertGroup(group);
	}

	/**
	 * Updates a specific LDAP POSIX group.
	 * 
	 * @param cn
	 *            the group's common name
	 * @param gidNumber
	 *            the group's gid number
	 * @param the
	 *            group's description
	 * @param the
	 *            group's user password
	 * 
	 */
	public void updatePosixGroup(String cn, int gidNumber, String description, String userPassword) {
		PosixGroup group = new PosixGroup();
		group.setCommonName(cn);
		group.setGidNumber(gidNumber);
		group.setDescription(description);
		group.setUserPassword(userPassword);
		ldapGroup.updateGroup(group);
	}

	/**
	 * Deletes a specific LDAP group.
	 * 
	 * @param cn
	 *            the group's common name
	 */
	public void deleteGroup(String cn) {
		PosixGroup group = new PosixGroup();
		group.setCommonName(cn);
		ldapGroup.deleteGroup(group);
	}

	/**
	 * Adds a specific LDAP user to a specific group.
	 * 
	 * @param cn
	 *            the group's common name
	 * @param memberUid
	 *            the user's uid
	 */
	public void addGroupMember(String cn, String memberUid) {
		PosixGroup group = new PosixGroup();
		group.setCommonName(cn);
		ldapGroup.addMember(group, memberUid);
	}

	/**
	 * Generates a non-conflicting group id.
	 * 
	 * @return a new int gidNumber
	 */
	public int generateGroupId() {
		int max = 99999;
		int min = 10000;
		Random rand = new Random();
		ArrayList<Integer> existingGidNumbers = new ArrayList<Integer>();
		List<PosixGroup> groups = ldapGroup.getAllGroups();
		for (PosixGroup group : groups)
			existingGidNumbers.add(group.getGidNumber());

		int randomInt = rand.nextInt((max - min) + 1) + min;
		while (existingGidNumbers.contains(randomInt))
			randomInt = rand.nextInt((max - min) + 1) + min;

		return randomInt;
	}
}
