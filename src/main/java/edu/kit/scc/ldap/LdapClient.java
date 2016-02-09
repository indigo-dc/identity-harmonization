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
import edu.kit.scc.dto.PosixUser;
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
	LdapIndigoUserDAO ldapIndigoUser(LdapTemplate ldapTemplate) {
		LdapIndigoUserDAO ldapUserDAO = new LdapIndigoUserDAO();
		ldapUserDAO.setLdapTemplate(ldapTemplate);
		ldapUserDAO.setUserBase(userBase);
		return ldapUserDAO;
	}

	@Bean
	LdapPosixUserDAO ldapPosixUser(LdapTemplate ldapTemplate) {
		LdapPosixUserDAO ldapUserDAO = new LdapPosixUserDAO();
		ldapUserDAO.setLdapTemplate(ldapTemplate);
		ldapUserDAO.setUserBase("");
		return ldapUserDAO;
	}

	@Bean
	LdapPosixGroupDAO ldapPosixGroup(LdapTemplate ldapTemplate) {
		LdapPosixGroupDAO ldapGroupDAO = new LdapPosixGroupDAO();
		ldapGroupDAO.setLdapTemplate(ldapTemplate);
		ldapGroupDAO.setGroupBase(groupBase);
		return ldapGroupDAO;
	}

	@Autowired
	private LdapIndigoUserDAO ldapIndigoUser;

	@Autowired
	private LdapPosixUserDAO ldapPosixUser;

	@Autowired
	private LdapPosixGroupDAO ldapPosixGroup;

	/**
	 * Gets the INDIGO user specified from the LDAP server.
	 * 
	 * @param uid
	 *            the user's uid
	 * @return a {@link IndigoUser} with the LDAP user information
	 */
	public IndigoUser getIndigoUser(String uid) {
		List<IndigoUser> userList = ldapIndigoUser.getUserDetails(uid);
		IndigoUser user = null;

		if (userList != null && !userList.isEmpty()) {
			user = userList.get(0);
			log.debug(user.toString());
		}
		return user;
	}

	/**
	 * Gets the POSIX group specified from the LDAP server.
	 * 
	 * @param cn
	 *            the group's common name
	 * @return a {@link PosixGroup} with the LDAP group information
	 */
	public PosixGroup getPosixGroup(String cn) {
		List<PosixGroup> groupList = ldapPosixGroup.getGroupDetails(cn);
		PosixGroup group = null;

		if (groupList != null && !groupList.isEmpty()) {
			group = groupList.get(0);
			log.debug(group.toString());
		}
		return group;
	}

	/**
	 * Gets the POSIX group specified from the LDAP server.
	 * 
	 * @param gidNumber
	 *            the group's gidNumber
	 * @return a {@link PosixGroup} with the LDAP group information
	 */
	public PosixGroup getPosixGroup(int gidNumber) {
		List<PosixGroup> groupList = ldapPosixGroup.getGroupDetails(gidNumber);
		PosixGroup group = null;

		if (groupList != null && !groupList.isEmpty()) {
			group = groupList.get(0);
			log.debug(group.toString());
		}
		return group;
	}

	public List<PosixGroup> getUserGroups(String uid) {
		return ldapPosixGroup.getUserGroups(uid);
	}

	/**
	 * Gets all INDIGO users from the LDAP server.
	 * 
	 * @return a {@link List<IndigoUser>} with the LDAP user information
	 */
	public List<IndigoUser> getIndigoUsers() {
		List<IndigoUser> userList = ldapIndigoUser.getAllUsers();
		for (int i = 0; i < userList.size(); i++)
			log.debug("User {}", ((IndigoUser) userList.get(i)).toString());

		return userList;
	}

	/**
	 * Gets all POSIX users from the LDAP server.
	 * 
	 * @return a {@link List<PosixUser>} with the LDAP user information
	 */
	public List<PosixUser> getPosixUsers() {
		List<PosixUser> userList = ldapPosixUser.getAllUsers();
		for (int i = 0; i < userList.size(); i++)
			log.debug("User {}", ((PosixUser) userList.get(i)).toString());

		return userList;
	}

	/**
	 * Gets all POSIX groups from the LDAP server.
	 * 
	 * @return a {@link List<PosixGroup>} with the LDAP group information
	 */
	public List<PosixGroup> getPosixGroups() {
		List<PosixGroup> groupList = ldapPosixGroup.getAllGroups();
		for (int i = 0; i < groupList.size(); i++)
			log.debug("Group {}", ((PosixGroup) groupList.get(i)).toString());

		return groupList;
	}

	/**
	 * Creates a new LDAP INDIGO user.
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
	 * @return the created {@link IndigoUser}
	 */
	public IndigoUser createIndigoUser(String uid, String cn, String sn, String indigoId, int uidNumber, int gidNumber,
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
		if (userPassword != null)
			user.setUserPassword(userPassword.getBytes());
		ldapIndigoUser.insertUser(user);

		return getIndigoUser(uid);
	}

	/**
	 * Updates a specific LDAP INDIGO user.
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
	 * @return the updated {@link IndigoUser}
	 */
	public IndigoUser updateIndigoUser(String uid, String cn, String sn, String indigoId, int uidNumber, int gidNumber,
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
		if (userPassword != null)
			user.setUserPassword(userPassword.getBytes());
		ldapIndigoUser.updateUser(user);

		return getIndigoUser(uid);
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
		ldapPosixUser.deleteUser(user);
	}

	/**
	 * Deletes a specific INDIGO LDAP user.
	 * 
	 * @param uid
	 *            the user's uid
	 */
	public void deleteIndigoUser(String uid) {
		IndigoUser user = new IndigoUser();
		user.setUid(uid);
		ldapIndigoUser.deleteUser(user);
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
	 * @return the created {@link PosixGroup}
	 * 
	 */
	public PosixGroup createPosixGroup(String cn, int gidNumber, String description, String userPassword) {
		PosixGroup group = new PosixGroup();
		group.setCommonName(cn);
		group.setGidNumber(gidNumber);
		group.setDescription(description);
		if (userPassword != null)
			group.setUserPassword(userPassword.getBytes());
		ldapPosixGroup.insertGroup(group);

		return getPosixGroup(cn);
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
	 * @return the updated {@link PosixGroup}
	 */
	public PosixGroup updatePosixGroup(String cn, int gidNumber, String description, String userPassword) {
		PosixGroup group = new PosixGroup();
		group.setCommonName(cn);
		group.setGidNumber(gidNumber);
		group.setDescription(description);
		if (userPassword != null)
			group.setUserPassword(userPassword.getBytes());
		ldapPosixGroup.updateGroup(group);

		return getPosixGroup(cn);
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
		ldapPosixGroup.deleteGroup(group);
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
		ldapPosixGroup.addMember(group, memberUid);
	}

	/**
	 * Compares two POSIX LDAP groups.
	 * 
	 * @param {@link
	 * 			PosixGroup} group one
	 * @param {@link
	 * 			PosixGroup} group two
	 * @return true if groups are equal (name and gidNumber)
	 * 
	 */
	public boolean equalGroups(PosixGroup group1, PosixGroup group2) {
		if (group1.getGidNumber() == group2.getGidNumber())
			if (group1.getCommonName().equals(group2.getCommonName()))
				return true;
		return false;
	}

	/**
	 * Generates a non-conflicting group id number.
	 * 
	 * @return a new int gidNumber
	 */
	public int generateGroupIdNumber() {
		int max = 99999;
		int min = 10000;
		Random rand = new Random();
		ArrayList<Integer> existingGidNumbers = new ArrayList<Integer>();
		List<PosixGroup> groups = ldapPosixGroup.getAllGroups();
		for (PosixGroup group : groups)
			existingGidNumbers.add(group.getGidNumber());

		int randomInt = rand.nextInt((max - min) + 1) + min;
		while (existingGidNumbers.contains(randomInt))
			randomInt = rand.nextInt((max - min) + 1) + min;

		return randomInt;
	}

	/**
	 * Generates a non-conflicting user id number.
	 * 
	 * @return a new int uidNumber
	 */
	public int generateUserIdNumber() {
		int max = 99999;
		int min = 10000;
		Random rand = new Random();
		ArrayList<Integer> existingUidNumbers = new ArrayList<Integer>();
		List<PosixUser> users = ldapPosixUser.getAllUsers();
		for (PosixUser user : users)
			existingUidNumbers.add(user.getUidNumber());

		int randomInt = rand.nextInt((max - min) + 1) + min;
		while (existingUidNumbers.contains(randomInt))
			randomInt = rand.nextInt((max - min) + 1) + min;

		return randomInt;
	}
}
