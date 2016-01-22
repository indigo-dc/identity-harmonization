/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.ldap;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

import edu.kit.scc.dto.GroupDTO;
import edu.kit.scc.dto.UserDTO;

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
	LdapUserDAO ldapUser(LdapTemplate ldapTemplate) {
		LdapUserDAO ldapUserDAO = new LdapUserDAO();
		ldapUserDAO.setLdapTemplate(ldapTemplate);
		ldapUserDAO.setUserBase(userBase);
		return ldapUserDAO;
	}

	@Bean
	LdapGroupDAO ldapGroup(LdapTemplate ldapTemplate) {
		LdapGroupDAO ldapGroupDAO = new LdapGroupDAO();
		ldapGroupDAO.setLdapTemplate(ldapTemplate);
		ldapGroupDAO.setGroupBase(groupBase);
		return ldapGroupDAO;
	}

	@Autowired
	private LdapUserDAO ldapUser;

	@Autowired
	private LdapGroupDAO ldapGroup;

	/**
	 * Gets the user specified from the LDAP server.
	 * 
	 * @param uid
	 *            the user's uid
	 * @return a {@link UserDTO} with the LDAP user information
	 */
	public UserDTO getLdapUser(String uid) {
		List<UserDTO> userList = ldapUser.getUserDetails(uid);
		UserDTO user = null;

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
	 * @return a {@link GroupDTO} with the LDAP group information
	 */
	public GroupDTO getLdapGroup(String cn) {
		List<GroupDTO> groupList = ldapGroup.getGroupDetails(cn);
		GroupDTO group = null;

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
	public List<UserDTO> getLdapUsers() {
		List<UserDTO> userList = ldapUser.getAllUsers();
		for (int i = 0; i < userList.size(); i++)
			log.debug("User {}", ((UserDTO) userList.get(i)).toString());

		return userList;
	}

	/**
	 * Gets all groups from the LDAP server.
	 * 
	 * @return a {@link List<GroupDTO>} with the LDAP group information
	 */
	public List<GroupDTO> getLdapGroups() {
		List<GroupDTO> groupList = ldapGroup.getAllGroups();
		for (int i = 0; i < groupList.size(); i++)
			log.debug("Group {}", ((GroupDTO) groupList.get(i)).toString());

		return groupList;
	}

	/**
	 * Creates a new LDAP POSIX user.
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
	 */
	public void createUser(String uid, String cn, String sn, int uidNumber, int gidNumber, String homeDirectory,
			String description) {
		UserDTO user = new UserDTO();
		user.setCommonName(cn);
		user.setDescription(description);
		user.setSurName(sn);
		user.setUid(uid);
		user.setGidNumber(gidNumber);
		user.setUidNumber(uidNumber);
		user.setHomeDirectory(homeDirectory);
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
	 */
	public void updateUser(String uid, String cn, String sn, int uidNumber, int gidNumber, String homeDirectory,
			String description) {
		UserDTO user = new UserDTO();
		user.setCommonName(cn);
		user.setDescription(description);
		user.setSurName(sn);
		user.setUid(uid);
		user.setGidNumber(gidNumber);
		user.setUidNumber(uidNumber);
		user.setHomeDirectory(homeDirectory);
		ldapUser.updateUser(user);
	}

	/**
	 * Deletes a specific LDAP POSIX user.
	 * 
	 * @param uid
	 *            the user's uid
	 */
	public void deleteUser(String uid) {
		UserDTO user = new UserDTO();
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
	 */
	public void createGroup(String cn, int gidNumber) {
		GroupDTO group = new GroupDTO();
		group.setCommonName(cn);
		group.setGidNumber(gidNumber);
		ldapGroup.insertGroup(group);
	}

	/**
	 * Updates a specific LDAP POSIX group.
	 * 
	 * @param cn
	 *            the group's common name
	 * @param gidNumber
	 *            the group's gid number
	 */
	public void updateGroup(String cn, int gidNumber) {
		GroupDTO group = new GroupDTO();
		group.setCommonName(cn);
		group.setGidNumber(gidNumber);
		ldapGroup.updateGroup(group);
	}

	/**
	 * Deletes a specific LDAP POSIX group.
	 * 
	 * @param cn
	 *            the group's common name
	 */
	public void deleteGroup(String cn) {
		GroupDTO group = new GroupDTO();
		group.setCommonName(cn);
		ldapGroup.deleteGroup(group);
	}

	public void addGroupMember(String cn, String memberUid) {
		GroupDTO group = new GroupDTO();
		group.setCommonName(cn);
		ldapGroup.addMember(group, memberUid);
	}
}
