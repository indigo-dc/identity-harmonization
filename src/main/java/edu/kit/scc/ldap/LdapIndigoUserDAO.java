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

import edu.kit.scc.dao.IndigoUserDAO;
import edu.kit.scc.dto.IndigoUser;

public class LdapIndigoUserDAO implements IndigoUserDAO {

	private static final Logger log = LoggerFactory.getLogger(LdapIndigoUserDAO.class);

	private LdapTemplate ldapTemplate;

	private String userBase;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setUserBase(String userBase) {
		this.userBase = userBase;
	}

	@Override
	public List<IndigoUser> getAllUsers() {
		return ldapTemplate.search(userBase, "(objectclass=indigoUser)", new LdapIndigoUserAttributeMapper());
	}

	@Override
	public List<IndigoUser> getUserDetails(String indigoId) {
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "indigoUser")).and(new EqualsFilter("indigoId", indigoId));
		log.debug("LDAP query {}", andFilter.encode());

		return ldapTemplate.search("", andFilter.encode(), new LdapIndigoUserAttributeMapper());
	}

	@Override
	public void insertUser(IndigoUser indigoUser) {
		BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
		personBasicAttribute.add("person");
		personBasicAttribute.add("posixAccount");
		personBasicAttribute.add("indigoUser");

		Attributes personAttributes = new BasicAttributes();
		personAttributes.put(personBasicAttribute);
		personAttributes.put("indigoId", indigoUser.getIndigoId());
		personAttributes.put("cn", indigoUser.getCommonName());
		personAttributes.put("sn", indigoUser.getSurName());
		personAttributes.put("uid", indigoUser.getUid());
		personAttributes.put("uidNumber", String.valueOf(indigoUser.getUidNumber()));
		personAttributes.put("gidNumber", String.valueOf(indigoUser.getGidNumber()));
		personAttributes.put("homeDirectory", indigoUser.getHomeDirectory());

		if (indigoUser.getDescription() != null)
			personAttributes.put("description", indigoUser.getDescription());
		if (indigoUser.getGecos() != null)
			personAttributes.put("gecos", indigoUser.getGecos());
		if (indigoUser.getLoginShell() != null)
			personAttributes.put("loginShell", indigoUser.getLoginShell());
		if (indigoUser.getUserPassword() != null)
			personAttributes.put("userPassword", indigoUser.getUserPassword());

		LdapName newUserDN = LdapUtils.emptyLdapName();
		try {
			newUserDN = new LdapName(userBase);
			newUserDN.add("uid=" + indigoUser.getUid());
			log.debug("Insert {}", newUserDN.toString());
			ldapTemplate.bind(newUserDN, null, personAttributes);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateUser(IndigoUser indigoUser) {
		BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
		personBasicAttribute.add("person");
		personBasicAttribute.add("posixAccount");
		personBasicAttribute.add("indigoUser");

		Attributes personAttributes = new BasicAttributes();
		personAttributes.put(personBasicAttribute);
		personAttributes.put("indigoId", indigoUser.getIndigoId());
		personAttributes.put("cn", indigoUser.getCommonName());
		personAttributes.put("sn", indigoUser.getSurName());
		personAttributes.put("description", indigoUser.getDescription());
		personAttributes.put("uid", indigoUser.getUid());
		personAttributes.put("uidNumber", String.valueOf(indigoUser.getUidNumber()));
		personAttributes.put("gidNumber", String.valueOf(indigoUser.getGidNumber()));
		personAttributes.put("homeDirectory", indigoUser.getHomeDirectory());

		if (indigoUser.getDescription() != null)
			personAttributes.put("description", indigoUser.getDescription());
		if (indigoUser.getGecos() != null)
			personAttributes.put("gecos", indigoUser.getGecos());
		if (indigoUser.getLoginShell() != null)
			personAttributes.put("loginShell", indigoUser.getLoginShell());
		if (indigoUser.getUserPassword() != null)
			personAttributes.put("userPassword", indigoUser.getUserPassword());

		LdapName userDN = LdapUtils.emptyLdapName();
		try {
			userDN = new LdapName(userBase);
			userDN.add("uid=" + indigoUser.getUid());
			log.debug("Update {}", userDN.toString());
			ldapTemplate.rebind(userDN, null, personAttributes);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteUser(IndigoUser indigoUser) {
		LdapName userDN = LdapUtils.emptyLdapName();
		try {
			userDN = new LdapName(userBase);
			userDN.add("uid=" + indigoUser.getUid());
			log.debug("Delete {}", userDN.toString());
			ldapTemplate.unbind(userDN);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
	}
}
