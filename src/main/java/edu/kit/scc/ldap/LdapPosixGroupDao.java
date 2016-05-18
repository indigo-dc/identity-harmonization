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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.AttributeInUseException;
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
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;

@Component
public class LdapPosixGroupDao {
  private static final Logger log = LoggerFactory.getLogger(LdapPosixGroupDao.class);

  @Autowired
  private LdapTemplate ldapTemplate;

  @Value("${ldap.groupBase}")
  private String groupBase;

  /**
   * Gets all POSIX groups.
   * 
   * @return a {@link List} of {@link PosixGroup}
   */
  public List<PosixGroup> getAllGroups() {
    return ldapTemplate.search(groupBase, "(objectclass=posixGroup)",
        new LdapPosixGroupAttributeMapper());

  }

  /**
   * Gets all POSIX group information.
   * 
   * @param commonName the group's common name
   * @return a {@link PosixGroup}
   */
  public PosixGroup getGroupDetails(String commonName) {
    AndFilter andFilter = new AndFilter();
    andFilter.and(new EqualsFilter("objectclass", "posixGroup"))
        .and(new EqualsFilter("cn", commonName));
    log.debug("LDAP query {}", andFilter.encode());

    List<PosixGroup> results =
        ldapTemplate.search("", andFilter.encode(), new LdapPosixGroupAttributeMapper());
    if (results == null || results.isEmpty()) {
      log.warn("No groups with cn {} found", commonName);
      return null;
    }
    if (results.size() > 1) {
      log.warn("Multiple groups with cn {} found", commonName);
    }
    return results.get(0);
  }

  /**
   * Gets group details for the group specified.
   * 
   * @param gidNumber the group's gidNumber
   * @return a {@link PosixGroup}
   */
  public PosixGroup getGroupDetails(int gidNumber) {
    AndFilter andFilter = new AndFilter();
    andFilter.and(new EqualsFilter("objectclass", "posixGroup"))
        .and(new EqualsFilter("gidNumber", gidNumber));
    log.debug("LDAP query {}", andFilter.encode());

    List<PosixGroup> results =
        ldapTemplate.search("", andFilter.encode(), new LdapPosixGroupAttributeMapper());
    if (results == null || results.isEmpty()) {
      log.warn("No groups with gidNumber {} found", gidNumber);
      return null;
    }
    if (results.size() > 1) {
      log.warn("Multiple groups with gidNumber {} found", gidNumber);
    }
    return results.get(0);
  }

  /**
   * Inserts a new POSIX group into the LDAP directory.
   * 
   * @param group the {@link PosixGroup} to insert
   * @return the {@link PosixGroup} inserted
   */
  public PosixGroup insertGroup(PosixGroup group) {
    if (group.commonName == null || group.gidNumber == null) {
      log.warn("PosixGroup has missing mandatory attributes");
      return null;
    }

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

      return group;
    } catch (NameAlreadyBoundException ex) {
      log.error("ERROR {}", ex.getMessage());
    } catch (InvalidNameException ex) {
      log.error("ERROR {}", ex.getMessage());
    }
    return null;
  }

  /**
   * Updates a POSIX group in the LDAP directory.
   * 
   * @param group the {@link PosixGroup} to update
   * @return the {@link PosixGroup} updated
   */
  public PosixGroup updateGroup(PosixGroup group) {
    BasicAttribute posixGroupBasicAttribute = new BasicAttribute("objectclass");
    posixGroupBasicAttribute.add("posixGroup");

    Attributes posixGroupAttributes = new BasicAttributes();
    posixGroupAttributes.put(posixGroupBasicAttribute);

    if (group.getCommonName() != null) {
      posixGroupAttributes.put("cn", group.getCommonName());
    }
    if (group.getGidNumber() != null) {
      posixGroupAttributes.put("gidNumber", String.valueOf(group.getGidNumber()));
    }
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

      return group;
    } catch (InvalidNameException ex) {
      log.error("ERROR {}", ex.toString());
    }
    return null;
  }

  /**
   * Deletes the {@link PosixGroup} from the LDAP directory.
   * 
   * @param group the {@link PosixGroup} to delete
   * @return true if success
   */
  public boolean deleteGroup(PosixGroup group) {
    LdapName groupDn = LdapUtils.emptyLdapName();
    try {
      groupDn = new LdapName(groupBase);
      groupDn.add("cn=" + group.getCommonName());
      log.debug("Delete {}", groupDn.toString());
      ldapTemplate.unbind(groupDn);

      return true;
    } catch (InvalidNameException ex) {
      log.error("ERROR {}", ex.toString());
      // ex.printStackTrace();
    }
    return false;
  }

  /**
   * Adds a POSIX user to the specified POSIX group.
   * 
   * @param group the POSIX group
   * @param memberUid the POSIX user's uid
   * @return true on success
   */
  public boolean addMember(PosixGroup group, String memberUid) {
    ModificationItem[] modificationItems = new ModificationItem[] {
        new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", memberUid))};
    LdapName groupDn = LdapUtils.emptyLdapName();
    try {
      groupDn = new LdapName(groupBase);
      groupDn.add("cn=" + group.getCommonName());
      log.debug("Add member {} to {}", memberUid, groupDn.toString());
      ldapTemplate.modifyAttributes(groupDn, modificationItems);
      return true;
    } catch (AttributeInUseException ex) {
      log.error("ERROR {}", ex.toString());
    } catch (InvalidNameException ex) {
      log.error("ERROR {}", ex.toString());
    }
    return false;
  }

  /**
   * Removes a POSIX user from the specified POSIX group.
   * 
   * @param group the POSIX group
   * @param memberUid the POSIX user's uid
   * @return true on success
   */
  public boolean removeMember(PosixGroup group, String memberUid) {
    ModificationItem[] modificationItems =
        new ModificationItem[] {new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
            new BasicAttribute("memberUid", memberUid))};
    LdapName groupDn = LdapUtils.emptyLdapName();
    try {
      groupDn = new LdapName(groupBase);
      groupDn.add("cn=" + group.getCommonName());
      log.debug("Remove member {} from {}", memberUid, groupDn.toString());
      ldapTemplate.modifyAttributes(groupDn, modificationItems);
      return true;
    } catch (AttributeInUseException ex) {
      log.error("ERROR {}", ex.toString());
    } catch (InvalidNameException ex) {
      log.error("ERROR {}", ex.toString());
    }
    return false;
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
