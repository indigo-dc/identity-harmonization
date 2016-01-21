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

import edu.kit.scc.dto.UserDTO;

public class UserAttributeMapper implements AttributesMapper<UserDTO> {

	@Override
	public UserDTO mapFromAttributes(Attributes attributes) throws NamingException {
		UserDTO userDTO = new UserDTO();
		String uid = (String) attributes.get("uid").get();
		if (uid != null)
			userDTO.setUid(uid);
		String commonName = (String) attributes.get("cn").get();
		if (commonName != null)
			userDTO.setCommonName(commonName);
		String surName = (String) attributes.get("sn").get();
		if (surName != null)
			userDTO.setSurName(surName);
		String homeDirectory = (String) attributes.get("homeDirectory").get();
		if (homeDirectory != null)
			userDTO.setHomeDirectory(homeDirectory);
		Attribute description = attributes.get("description");
		if (description != null)
			userDTO.setDescription((String) description.get());
		Attribute gidNumber = attributes.get("gidNumber");
		if (gidNumber != null)
			userDTO.setGidNumber(Integer.valueOf((String) gidNumber.get()));
		Attribute uidNumber = attributes.get("uidNumber");
		if (uidNumber != null)
			userDTO.setUidNumber(Integer.valueOf((String) uidNumber.get()));
		return userDTO;
	}

}
