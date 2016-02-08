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

import edu.kit.scc.dao.PosixUserDAO;
import edu.kit.scc.dto.PosixUser;

public class LdapPosixUserDAO implements PosixUserDAO {
	private static final Logger log = LoggerFactory.getLogger(LdapPosixUserDAO.class);

	private LdapTemplate ldapTemplate;

	private String userBase;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setUserBase(String userBase) {
		this.userBase = userBase;
	}

	@Override
	public List<PosixUser> getAllUsers() {
		return ldapTemplate.search(userBase, "(objectclass=posixAccount)", new LdapPosixUserAttributeMapper());
	}

	@Override
	public List<PosixUser> getUserDetails(String uid) {
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "posixAccount")).and(new EqualsFilter("uid", uid));
		log.debug("LDAP query {}", andFilter.encode());

		return ldapTemplate.search("", andFilter.encode(), new LdapPosixUserAttributeMapper());
	}

	@Override
	public void insertUser(PosixUser posixUser) {
		BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
		personBasicAttribute.add("person");
		personBasicAttribute.add("posixAccount");

		Attributes personAttributes = new BasicAttributes();
		personAttributes.put(personBasicAttribute);
		personAttributes.put("cn", posixUser.getCommonName());
		personAttributes.put("sn", posixUser.getSurName());
		personAttributes.put("uid", posixUser.getUid());
		personAttributes.put("uidNumber", String.valueOf(posixUser.getUidNumber()));
		personAttributes.put("gidNumber", String.valueOf(posixUser.getGidNumber()));
		personAttributes.put("homeDirectory", posixUser.getHomeDirectory());

		if (posixUser.getDescription() != null)
			personAttributes.put("description", posixUser.getDescription());
		if (posixUser.getGecos() != null)
			personAttributes.put("gecos", posixUser.getGecos());
		if (posixUser.getLoginShell() != null)
			personAttributes.put("loginShell", posixUser.getLoginShell());
		if (posixUser.getUserPassword() != null)
			personAttributes.put("userPassword", posixUser.getUserPassword());

		LdapName newUserDN = LdapUtils.emptyLdapName();
		try {
			newUserDN = new LdapName(userBase);
			newUserDN.add("uid=" + posixUser.getUid());
			log.debug("Insert {}", newUserDN.toString());
			ldapTemplate.bind(newUserDN, null, personAttributes);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateUser(PosixUser posixUser) {
		BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
		personBasicAttribute.add("person");
		personBasicAttribute.add("posixAccount");

		Attributes personAttributes = new BasicAttributes();
		personAttributes.put(personBasicAttribute);
		personAttributes.put("cn", posixUser.getCommonName());
		personAttributes.put("sn", posixUser.getSurName());
		personAttributes.put("description", posixUser.getDescription());
		personAttributes.put("uid", posixUser.getUid());
		personAttributes.put("uidNumber", String.valueOf(posixUser.getUidNumber()));
		personAttributes.put("gidNumber", String.valueOf(posixUser.getGidNumber()));
		personAttributes.put("homeDirectory", posixUser.getHomeDirectory());

		if (posixUser.getDescription() != null)
			personAttributes.put("description", posixUser.getDescription());
		if (posixUser.getGecos() != null)
			personAttributes.put("gecos", posixUser.getGecos());
		if (posixUser.getLoginShell() != null)
			personAttributes.put("loginShell", posixUser.getLoginShell());
		if (posixUser.getUserPassword() != null)
			personAttributes.put("userPassword", posixUser.getUserPassword());

		LdapName userDN = LdapUtils.emptyLdapName();
		try {
			userDN = new LdapName(userBase);
			userDN.add("uid=" + posixUser.getUid());
			log.debug("Update {}", userDN.toString());
			ldapTemplate.rebind(userDN, null, personAttributes);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteUser(PosixUser posixUser) {
		LdapName userDN = LdapUtils.emptyLdapName();
		try {
			userDN = new LdapName(userBase);
			userDN.add("uid=" + posixUser.getUid());
			log.debug("Delete {}", userDN.toString());
			ldapTemplate.unbind(userDN);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
	}
}
