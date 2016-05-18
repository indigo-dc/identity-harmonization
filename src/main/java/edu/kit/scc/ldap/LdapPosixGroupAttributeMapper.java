/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.ldap;

import org.springframework.ldap.core.AttributesMapper;

import java.util.ArrayList;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class LdapPosixGroupAttributeMapper implements AttributesMapper<PosixGroup> {

  @Override
  public PosixGroup mapFromAttributes(Attributes attributes) throws NamingException {
    PosixGroup posixGroup = new PosixGroup();
    String commonName = (String) attributes.get("cn").get();
    if (commonName != null) {
      posixGroup.setCommonName(commonName);
    }
    Attribute gidNumber = attributes.get("gidNumber");
    if (gidNumber != null) {
      posixGroup.setGidNumber((String) gidNumber.get());
    }
    Attribute memberUids = attributes.get("memberUid");
    if (memberUids != null) {
      posixGroup.setMemberUids(new ArrayList<String>());
      for (int i = 0; i < memberUids.size(); i++) {
        posixGroup.getMemberUids().add((String) memberUids.get(i));
      }
    }

    Attribute description = attributes.get("description");
    if (description != null) {
      posixGroup.setDescription((String) description.get());
    }
    Attribute userPassword = attributes.get("userPassword");
    if (userPassword != null) {
      posixGroup.setUserPassword((byte[]) userPassword.get());
    }
    return posixGroup;
  }

}
