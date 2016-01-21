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

		if (!userList.isEmpty()) {
			user = userList.get(0);
			log.info(user.toString());
		}

		return user;
	}

	/**
	 * Gets all users from the LDAP server.
	 * 
	 * @return a {@link List<UserDTO>} with the LDAP user information
	 */
	public List<UserDTO> getLdapUsers() {
		List<UserDTO> userList = ldapUser.getAllUsers();
		for (int i = 0; i < userList.size(); i++)
			log.info("User name {}", ((UserDTO) userList.get(i)).getCommonName());

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
			log.info("Gropu name {}", ((GroupDTO) groupList.get(i)).getCommonName());

		return groupList;
	}

	/**
	 * Creates a new LDAP user.
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
}
