/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

@Component
public class LdapPosixUserDao {
  private static final Logger log = LoggerFactory.getLogger(LdapPosixUserDao.class);

  @Autowired
  private LdapTemplate ldapTemplate;

  /**
   * Gets all POSIX account LDAP entries.
   * 
   * @return a {@link List} of {@link PosixUser}
   */
  public List<PosixUser> getAllUsers(String userBase) {
    return ldapTemplate.search(userBase, "(objectclass=posixAccount)",
        new LdapPosixUserAttributeMapper());
  }

  /**
   * Gets all users with the specified uidNumber.
   * 
   * @param uidNumber the user's uidNumber
   * @return a {@link List} {@link edu.kit.scc.ldap.PosixUser}
   */
  public List<PosixUser> getAllUsers(int uidNumber) {
    AndFilter andFilter = new AndFilter();
    andFilter.and(new EqualsFilter("objectclass", "posixAccount"))
        .and(new EqualsFilter("uidNumber", uidNumber));
    log.debug("LDAP query {}", andFilter.encode());

    return ldapTemplate.search("", andFilter.encode(), new LdapPosixUserAttributeMapper());
  }

  /**
   * Gets the user's details.
   * 
   * @param uid the user's uid
   * @return a {@link PosixUser}
   */
  public PosixUser getUserDetails(String uid) {
    AndFilter andFilter = new AndFilter();
    andFilter.and(new EqualsFilter("objectclass", "posixAccount"))
        .and(new EqualsFilter("uid", uid));
    log.debug("LDAP query {}", andFilter.encode());

    List<PosixUser> results =
        ldapTemplate.search("", andFilter.encode(), new LdapPosixUserAttributeMapper());
    if (results == null || results.isEmpty()) {
      log.warn("No users with uid {} found", uid);
      return null;
    }
    if (results.size() > 1) {
      log.warn("Multiple users with uid {} found", uid);
    }
    return results.get(0);
  }

  /**
   * Inserts a new POSIX user into the LDAP directory.
   * 
   * @param posixUser the {@link PosixUser} to insert
   * @return the {@link PosixUser} inserted
   */
  public PosixUser insertUser(String userBase, PosixUser posixUser) {
    if (posixUser.commonName == null || posixUser.gidNumber == null
        || posixUser.homeDirectory == null || posixUser.surName == null || posixUser.uid == null
        || posixUser.uidNumber == null) {
      log.warn("PosixUser has missing mandatory attributes");
      return null;
    }

    BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
    personBasicAttribute.add("extensibleObject");
    personBasicAttribute.add("inetOrgPerson");
    personBasicAttribute.add("organizationalPerson");
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

    if (posixUser.getUniqueIdentifier() != null) {
      personAttributes.put("uniqueIdentifier", posixUser.getUniqueIdentifier());
    }
    if (posixUser.getDescription() != null) {
      personAttributes.put("description", posixUser.getDescription());
    }
    if (posixUser.getGecos() != null) {
      personAttributes.put("gecos", posixUser.getGecos());
    }
    if (posixUser.getLoginShell() != null) {
      personAttributes.put("loginShell", posixUser.getLoginShell());
    }
    if (posixUser.getUserPassword() != null) {
      personAttributes.put("userPassword", posixUser.getUserPassword());
    }
    if (posixUser.getGivenName() != null) {
      personAttributes.put("givenName", posixUser.getGivenName());
    }
    if (posixUser.getMail() != null) {
      personAttributes.put("mail", posixUser.getMail());
    }

    LdapName newUserDn = LdapUtils.emptyLdapName();
    try {
      newUserDn = new LdapName(userBase);
      newUserDn.add("uid=" + posixUser.getUid());
      log.debug("Insert {}", newUserDn.toString());
      ldapTemplate.bind(newUserDn, null, personAttributes);

      return posixUser;
    } catch (InvalidNameException ex) {
      log.error("ERROR {}", ex.toString());
      // ex.printStackTrace();
    } catch (NameAlreadyBoundException ex) {
      log.error("ERROR {}", ex.toString());
    }
    return null;
  }

  /**
   * Updates a POSIX user in the LDAP directory.
   * 
   * @param posixUser the {@link PosixUser} to update
   * @return the {@link PosixUser} updated
   */
  public PosixUser updateUser(String userBase, PosixUser posixUser) {
    BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
    personBasicAttribute.add("extensibleObject");
    personBasicAttribute.add("inetOrgPerson");
    personBasicAttribute.add("organizationalPerson");
    personBasicAttribute.add("person");
    personBasicAttribute.add("posixAccount");

    Attributes personAttributes = new BasicAttributes();
    personAttributes.put(personBasicAttribute);

    if (posixUser.getCommonName() != null) {
      personAttributes.put("cn", posixUser.getCommonName());
    }
    if (posixUser.getSurName() != null) {
      personAttributes.put("sn", posixUser.getSurName());
    }
    if (posixUser.getUid() != null) {
      personAttributes.put("uid", posixUser.getUid());
    }
    if (posixUser.getUidNumber() != null) {
      personAttributes.put("uidNumber", String.valueOf(posixUser.getUidNumber()));
    }
    if (posixUser.getGidNumber() != null) {
      personAttributes.put("gidNumber", String.valueOf(posixUser.getGidNumber()));
    }
    if (posixUser.getHomeDirectory() != null) {
      personAttributes.put("homeDirectory", posixUser.getHomeDirectory());
    }
    if (posixUser.getUniqueIdentifier() != null) {
      personAttributes.put("uniqueIdentifier", posixUser.getUniqueIdentifier());
    }
    if (posixUser.getDescription() != null) {
      personAttributes.put("description", posixUser.getDescription());
    }
    if (posixUser.getGecos() != null) {
      personAttributes.put("gecos", posixUser.getGecos());
    }
    if (posixUser.getLoginShell() != null) {
      personAttributes.put("loginShell", posixUser.getLoginShell());
    }
    if (posixUser.getUserPassword() != null) {
      personAttributes.put("userPassword", posixUser.getUserPassword());
    }
    if (posixUser.getGivenName() != null) {
      personAttributes.put("givenName", posixUser.getGivenName());
    }
    if (posixUser.getMail() != null) {
      personAttributes.put("mail", posixUser.getMail());
    }

    LdapName userDn = LdapUtils.emptyLdapName();
    try {
      userDn = new LdapName(userBase);
      userDn.add("uid=" + posixUser.getUid());
      log.debug("Update {}", userDn.toString());
      ldapTemplate.rebind(userDn, null, personAttributes);

      return posixUser;
    } catch (InvalidNameException ex) {
      log.error("ERROR {}", ex.toString());
      // ex.printStackTrace();
    }
    return null;
  }

  /**
   * Deletes a POSIX user from the LDAP directory.
   * 
   * @param posixUser the {@link PosixUser} to delete
   * @return true if success
   */
  public boolean deleteUser(String userBase, PosixUser posixUser) {
    LdapName userDn = LdapUtils.emptyLdapName();
    try {
      userDn = new LdapName(userBase);
      userDn.add("uid=" + posixUser.getUid());
      log.debug("Delete {}", userDn.toString());
      ldapTemplate.unbind(userDn);

      return true;
    } catch (InvalidNameException ex) {
      log.error("ERROR {}", ex.toString());
      // ex.printStackTrace();
    }
    return false;
  }
}
