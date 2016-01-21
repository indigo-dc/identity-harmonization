/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.ldap;

import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;

import edu.kit.scc.dao.UserDAO;
import edu.kit.scc.dto.UserDTO;

public class LdapUserDAO implements UserDAO {

	private static final Logger log = LoggerFactory.getLogger(LdapUserDAO.class);

	private LdapTemplate ldapTemplate;

	private String userBase;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setUserBase(String userBase) {
		this.userBase = userBase;
	}

	@Override
	public List<UserDTO> getAllUsers() {
		return ldapTemplate.search(userBase, "(objectclass=inetOrgPerson)", new UserAttributeMapper());
	}

	@Override
	public List<UserDTO> getUserDetails(String uid) {
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "inetOrgPerson")).and(new EqualsFilter("uid", uid));
		log.debug("LDAP query {}", andFilter.encode());

		return ldapTemplate.search("", andFilter.encode(), new UserAttributeMapper());
	}

	@Override
	public void insertUser(UserDTO userDTO) {
		BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
		personBasicAttribute.add("inetOrgPerson");

		Attributes personAttributes = new BasicAttributes();
		personAttributes.put(personBasicAttribute);
		personAttributes.put("cn", userDTO.getCommonName());
		personAttributes.put("sn", userDTO.getLastName());
		personAttributes.put("description", userDTO.getDescription());
		personAttributes.put("uid", userDTO.getUid());

		LdapName newUserDN = LdapUtils.emptyLdapName();
		try {
			newUserDN = new LdapName(userBase);
			newUserDN.add("uid=" + userDTO.getUid());
			log.debug(newUserDN.toString());
			ldapTemplate.bind(newUserDN, null, personAttributes);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateUser(UserDTO userDTO) {
		BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
		personBasicAttribute.add("inetOrgPerson");

		Attributes personAttributes = new BasicAttributes();
		personAttributes.put(personBasicAttribute);
		personAttributes.put("cn", userDTO.getCommonName());
		personAttributes.put("sn", userDTO.getLastName());
		personAttributes.put("description", userDTO.getDescription());
		personAttributes.put("uid", userDTO.getUid());

		LdapName newUserDN = LdapUtils.emptyLdapName();
		try {
			newUserDN = new LdapName(userBase);
			newUserDN.add("uid=" + userDTO.getUid());
			log.debug(newUserDN.toString());
			ldapTemplate.rebind(newUserDN, null, personAttributes);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void deleteUser(UserDTO userDTO) {
		LdapName newUserDN = LdapUtils.emptyLdapName();
		try {
			newUserDN = new LdapName(userBase);
			newUserDN.add("uid=" + userDTO.getUid());
			log.debug(newUserDN.toString());
			ldapTemplate.unbind(newUserDN);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
