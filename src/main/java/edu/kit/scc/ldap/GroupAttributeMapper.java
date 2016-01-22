/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.ldap;

import java.util.ArrayList;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

import edu.kit.scc.dto.GroupDTO;

public class GroupAttributeMapper implements AttributesMapper<GroupDTO> {

	@Override
	public GroupDTO mapFromAttributes(Attributes attributes) throws NamingException {
		GroupDTO groupDTO = new GroupDTO();
		String commonName = (String) attributes.get("cn").get();
		if (commonName != null)
			groupDTO.setCommonName(commonName);
		Attribute gidNumber = attributes.get("gidNumber");
		if (gidNumber != null)
			groupDTO.setGidNumber(Integer.valueOf((String) gidNumber.get()));
		Attribute memberUids = attributes.get("memberUid");
		if (memberUids != null) {
			groupDTO.setMemberUids(new ArrayList<String>());
			for (int i = 0; i < memberUids.size(); i++)
				groupDTO.getMemberUids().add((String) memberUids.get(i));
		}
		return groupDTO;
	}

}
