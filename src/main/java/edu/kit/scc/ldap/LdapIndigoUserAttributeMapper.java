/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

import edu.kit.scc.dto.IndigoUser;

public class LdapIndigoUserAttributeMapper implements AttributesMapper<IndigoUser> {

	@Override
	public IndigoUser mapFromAttributes(Attributes attributes) throws NamingException {
		IndigoUser indigoUser = new IndigoUser();
		String indigoId = (String) attributes.get("indigoId").get();
		if (indigoId != null)
			indigoUser.setIndigoId(indigoId);
		String uid = (String) attributes.get("uid").get();
		if (uid != null)
			indigoUser.setUid(uid);
		String commonName = (String) attributes.get("cn").get();
		if (commonName != null)
			indigoUser.setCommonName(commonName);
		String surName = (String) attributes.get("sn").get();
		if (surName != null)
			indigoUser.setSurName(surName);
		String homeDirectory = (String) attributes.get("homeDirectory").get();
		if (homeDirectory != null)
			indigoUser.setHomeDirectory(homeDirectory);
		Attribute gidNumber = attributes.get("gidNumber");
		if (gidNumber != null)
			indigoUser.setGidNumber(Integer.valueOf((String) gidNumber.get()));
		Attribute uidNumber = attributes.get("uidNumber");
		if (uidNumber != null)
			indigoUser.setUidNumber(Integer.valueOf((String) uidNumber.get()));

		Attribute description = attributes.get("description");
		if (description != null)
			indigoUser.setDescription((String) description.get());
		Attribute userPassword = attributes.get("userPassword");
		if (userPassword != null)
			indigoUser.setUserPassword((String) userPassword.get());
		Attribute gecos = attributes.get("gecos");
		if (gecos != null)
			indigoUser.setGecos((String) gecos.get());
		Attribute loginShell = attributes.get("loginShell");
		if (loginShell != null)
			indigoUser.setLoginShell((String) loginShell.get());

		return indigoUser;
	}
}