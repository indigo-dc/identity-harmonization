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

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class LdapPosixUserAttributeMapper implements AttributesMapper<PosixUser> {

  @Override
  public PosixUser mapFromAttributes(Attributes attributes) throws NamingException {
    PosixUser posixUser = new PosixUser();

    String uid = (String) attributes.get("uid").get();
    if (uid != null) {
      posixUser.setUid(uid);
    }
    String commonName = (String) attributes.get("cn").get();
    if (commonName != null) {
      posixUser.setCommonName(commonName);
    }
    String surName = (String) attributes.get("sn").get();
    if (surName != null) {
      posixUser.setSurName(surName);
    }
    String homeDirectory = (String) attributes.get("homeDirectory").get();
    if (homeDirectory != null) {
      posixUser.setHomeDirectory(homeDirectory);
    }
    Attribute gidNumber = attributes.get("gidNumber");
    if (gidNumber != null) {
      posixUser.setGidNumber((String) gidNumber.get());
    }
    Attribute uidNumber = attributes.get("uidNumber");
    if (uidNumber != null) {
      posixUser.setUidNumber((String) uidNumber.get());
    }
    Attribute description = attributes.get("description");
    if (description != null) {
      posixUser.setDescription((String) description.get());
    }
    Attribute userPassword = attributes.get("userPassword");
    if (userPassword != null) {
      posixUser.setUserPassword((byte[]) userPassword.get());
    }
    Attribute gecos = attributes.get("gecos");
    if (gecos != null) {
      posixUser.setGecos((String) gecos.get());
    }
    Attribute loginShell = attributes.get("loginShell");
    if (loginShell != null) {
      posixUser.setLoginShell((String) loginShell.get());
    }
    Attribute uniqueIdentifier = attributes.get("uniqueIdentifier");
    if (uniqueIdentifier != null) {
      posixUser.setUniqueIdentifier((String) uniqueIdentifier.get());
    }
    Attribute mail = attributes.get("mail");
    if (mail != null) {
      posixUser.setMail((String) mail.get());
    }
    Attribute givenName = attributes.get("givenName");
    if (givenName != null) {
      posixUser.setGivenName((String) givenName.get());
    }
    return posixUser;
  }

}
