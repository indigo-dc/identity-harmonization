/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.ldap;

import edu.kit.scc.dao.PosixGroupDao;
import edu.kit.scc.dto.PosixGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.AttributeInUseException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;

import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;

public class LdapPosixGroupDao implements PosixGroupDao {
  private static final Logger log = LoggerFactory.getLogger(LdapPosixGroupDao.class);

  private LdapTemplate ldapTemplate;

  private String groupBase;

  public void setLdapTemplate(LdapTemplate ldapTemplate) {
    this.ldapTemplate = ldapTemplate;
  }

  public void setGroupBase(String groupBase) {
    this.groupBase = groupBase;
  }

  @Override
  public List<PosixGroup> getAllGroups() {
    return ldapTemplate.search(groupBase, "(objectclass=posixGroup)",
        new LdapPosixGroupAttributeMapper());

  }

  @Override
  public List<PosixGroup> getGroupDetails(String commonName) {
    AndFilter andFilter = new AndFilter();
    andFilter.and(new EqualsFilter("objectclass", "posixGroup"))
        .and(new EqualsFilter("cn", commonName));
    log.debug("LDAP query {}", andFilter.encode());

    return ldapTemplate.search("", andFilter.encode(), new LdapPosixGroupAttributeMapper());
  }

  /**
   * Get group details for the group specified.
   * 
   * @param gidNumber the group's gidNumber
   * @return a list of {@link PosixGroup}
   */
  public List<PosixGroup> getGroupDetails(int gidNumber) {
    AndFilter andFilter = new AndFilter();
    andFilter.and(new EqualsFilter("objectclass", "posixGroup"))
        .and(new EqualsFilter("gidNumber", gidNumber));
    log.debug("LDAP query {}", andFilter.encode());

    return ldapTemplate.search("", andFilter.encode(), new LdapPosixGroupAttributeMapper());
  }

  @Override
  public void insertGroup(PosixGroup group) {
    BasicAttribute posixGroupBasicAttribute = new BasicAttribute("objectclass");
    posixGroupBasicAttribute.add("posixGroup");

    Attributes posixGroupAttributes = new BasicAttributes();
    posixGroupAttributes.put(posixGroupBasicAttribute);
    posixGroupAttributes.put("cn", group.getCommonName());
    posixGroupAttributes.put("gidNumber", String.valueOf(group.getGidNumber()));

    if (group.getUserPassword() != null) {
      posixGroupAttributes.put("userPassword", group.getUserPassword());
    }
    if (group.getDescription() != null) {
      posixGroupAttributes.put("description", group.getDescription());
    }
    LdapName newGroupDn = LdapUtils.emptyLdapName();
    try {
      newGroupDn = new LdapName(groupBase);
      newGroupDn.add("cn=" + group.getCommonName());
      log.debug("Insert {}", newGroupDn.toString());
      ldapTemplate.bind(newGroupDn, null, posixGroupAttributes);
    } catch (NameAlreadyBoundException e) {
      log.error("ERROR {}", e.getMessage());
    } catch (InvalidNameException e) {
      log.error("ERROR {}", e.getMessage());
    }
  }

  @Override
  public void updateGroup(PosixGroup group) {
    BasicAttribute posixGroupBasicAttribute = new BasicAttribute("objectclass");
    posixGroupBasicAttribute.add("posixGroup");

    Attributes posixGroupAttributes = new BasicAttributes();
    posixGroupAttributes.put(posixGroupBasicAttribute);
    posixGroupAttributes.put("cn", group.getCommonName());
    posixGroupAttributes.put("gidNumber", String.valueOf(group.getGidNumber()));

    if (group.getUserPassword() != null) {
      posixGroupAttributes.put("userPassword", group.getUserPassword());
    }
    if (group.getDescription() != null) {
      posixGroupAttributes.put("description", group.getDescription());
    }
    LdapName groupDn = LdapUtils.emptyLdapName();
    try {
      groupDn = new LdapName(groupBase);
      groupDn.add("cn=" + group.getCommonName());
      log.debug("Update {}", groupDn.toString());
      ldapTemplate.bind(groupDn, null, posixGroupAttributes);
    } catch (InvalidNameException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deleteGroup(PosixGroup group) {
    LdapName groupDn = LdapUtils.emptyLdapName();
    try {
      groupDn = new LdapName(groupBase);
      groupDn.add("cn=" + group.getCommonName());
      log.debug("Delete {}", groupDn.toString());
      ldapTemplate.unbind(groupDn);
    } catch (InvalidNameException e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds a POSIX user to the specified POSIX group.
   * 
   * @param group the POSIX group
   * @param memberUid the POSIX user's uid
   */
  public void addMember(PosixGroup group, String memberUid) {
    ModificationItem[] modificationItems = new ModificationItem[] {
        new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", memberUid))};
    LdapName groupDn = LdapUtils.emptyLdapName();
    try {
      groupDn = new LdapName(groupBase);
      groupDn.add("cn=" + group.getCommonName());
      log.debug("Add member {} to {}", memberUid, groupDn.toString());
      ldapTemplate.modifyAttributes(groupDn, modificationItems);
    } catch (AttributeInUseException e) {
      log.error("ERROR {}", e.getMessage());
    } catch (InvalidNameException e) {
      log.error("ERROR {}", e.getMessage());
    }
  }

  /**
   * Removes a POSIX user from the specified POSIX group.
   * 
   * @param group the POSIX group
   * @param memberUid the POSIX user's uid
   */
  public void removeMember(PosixGroup group, String memberUid) {
    ModificationItem[] modificationItems =
        new ModificationItem[] {new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
            new BasicAttribute("memberUid", memberUid))};
    LdapName groupDn = LdapUtils.emptyLdapName();
    try {
      groupDn = new LdapName(groupBase);
      groupDn.add("cn=" + group.getCommonName());
      log.debug("Remove member {} from {}", memberUid, groupDn.toString());
      ldapTemplate.modifyAttributes(groupDn, modificationItems);
    } catch (AttributeInUseException e) {
      log.error("ERROR {}", e.getMessage());
    } catch (InvalidNameException e) {
      log.error("ERROR {}", e.getMessage());
    }
  }

  /**
   * Gets all groups of the specified user.
   * 
   * @param uid the user's uid
   * @return a list of {@link PosixGroup}
   */
  public List<PosixGroup> getUserGroups(String uid) {
    AndFilter andFilter = new AndFilter();
    andFilter.and(new EqualsFilter("objectclass", "posixGroup"))
        .and(new EqualsFilter("memberUid", uid));
    log.debug("LDAP query {}", andFilter.encode());

    return ldapTemplate.search("", andFilter.encode(), new LdapPosixGroupAttributeMapper());
  }
}
