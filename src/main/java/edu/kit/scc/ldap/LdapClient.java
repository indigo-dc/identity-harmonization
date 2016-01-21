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
}
