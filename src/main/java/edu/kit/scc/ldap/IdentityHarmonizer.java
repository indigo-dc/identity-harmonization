/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.ldap;

import edu.kit.scc.redis.RedisClient;
import edu.kit.scc.scim.ScimGroup;
import edu.kit.scc.scim.ScimUser;
import edu.kit.scc.scim.ScimUser.Meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IdentityHarmonizer {

  private static final Logger log = LoggerFactory.getLogger(IdentityHarmonizer.class);

  @Autowired
  private LdapClient ldapClient;

  @Autowired
  private PosixUserDao posixUserDao;

  @Autowired
  private RedisClient redisClient;


  /**
   * Links the users represented in the JSON serialized list of SCIM user's via LDAP locally.
   * 
   * @param scimUsers the SCIM user's to link
   * @return a list of JSON serialized SCIM user's containing the modification information
   */
  public List<ScimUser> harmonizeIdentities(List<ScimUser> scimUsers) {

    ArrayList<ScimUser> linkedUsers = null;
    ScimUser primaryUser = null;

    for (ScimUser user : scimUsers) {
      if (user.isActive()) {
        primaryUser = user;
        log.debug("Primary user {}", primaryUser.toString());
        break;
      }
    }

    if (scimUsers.remove(primaryUser)) {
      log.debug("LDAP lookup for user {}", primaryUser.getUserName());
      PosixUser primaryPosixUser = ldapClient.getPosixUser(primaryUser.getUserName());
      log.debug("Primary user {}", primaryPosixUser.toString());

      linkedUsers = new ArrayList<>();
      linkedUsers.add(posixUserDao.scimUserFromPosixUser(primaryPosixUser));

      // store uid number and home directory for unlinking
      redisClient.setUidNumber(primaryPosixUser.getUniqueIdentifier(),
          primaryPosixUser.getUidNumber());
      redisClient.setUserHome(primaryPosixUser.getUniqueIdentifier(),
          primaryPosixUser.getHomeDirectory());

      for (ScimUser user : scimUsers) {
        log.debug("LDAP lookup for user {}", user.getUserName());
        PosixUser posixUser = ldapClient.getPosixUser(user.getUserName());
        log.debug("User {}", posixUser.toString());

        linkedUsers.add(posixUserDao.scimUserFromPosixUser(posixUser));

        // store uid number and home directory for unlinking
        redisClient.setUidNumber(posixUser.getUniqueIdentifier(), posixUser.getUidNumber());
        redisClient.setUserHome(posixUser.getUniqueIdentifier(), posixUser.getHomeDirectory());

        posixUser.setUidNumber(primaryPosixUser.getUidNumber());
        posixUser.setHomeDirectory(primaryPosixUser.getHomeDirectory());

        PosixUser updatedUser = ldapClient.updatePosixUser(posixUser);

        if (updatedUser == null) {
          log.debug("Could not update LDAP user");
          linkedUsers.remove(linkedUsers.size() - 1);
        } else {
          log.debug("Modified LDAP user {}", updatedUser.toString());
        }
      }
    }

    return linkedUsers;
  }

  @SuppressWarnings("unused")
  private List<ScimUser> harmonizeIdentities_Old(List<ScimUser> scimUsers) {
    ArrayList<ScimUser> linkedUsers = new ArrayList<>();
    ScimUser primaryUser = null;

    for (ScimUser user : scimUsers) {
      if (user.isActive()) {
        primaryUser = user;
        log.debug("Primary user {}", primaryUser.toString());
        break;
      }
    }

    if (scimUsers.remove(primaryUser)) {
      log.debug("LDAP lookup for user {}", primaryUser.getUserName());
      PosixUser primaryPosixUser = ldapClient.getPosixUser(primaryUser.getUserName());
      log.debug("Primary user {}", primaryPosixUser.toString());

      Meta metaData = new Meta();
      metaData.put("homeDirectory", primaryPosixUser.getHomeDirectory());
      metaData.put("cn", primaryPosixUser.getCommonName());
      metaData.put("gidNumber", String.valueOf(primaryPosixUser.getGidNumber()));
      metaData.put("uid", primaryPosixUser.getUid());
      metaData.put("uidNumber", String.valueOf(primaryPosixUser.getUidNumber()));

      primaryUser.setMeta(metaData);

      List<PosixGroup> primaryGroups = ldapClient.getUserGroups(primaryUser.getUserName());
      log.debug("Primary groups {}", primaryGroups.toString());

      primaryUser.setGroups(new ArrayList<>());

      for (ScimUser secondaryUser : scimUsers) {
        PosixUser secondaryPosixUser = ldapClient.getPosixUser(secondaryUser.getUserName());
        log.debug("Secondary user {}", secondaryUser.toString());

        metaData = new Meta();
        metaData.put("homeDirectory", secondaryPosixUser.getHomeDirectory());
        metaData.put("cn", secondaryPosixUser.getCommonName());
        metaData.put("gidNumber", String.valueOf(secondaryPosixUser.getGidNumber()));
        metaData.put("uid", secondaryPosixUser.getUid());
        metaData.put("uidNumber", String.valueOf(secondaryPosixUser.getUidNumber()));

        secondaryUser.setMeta(metaData);

        List<PosixGroup> secondaryGroups = ldapClient.getUserGroups(secondaryUser.getUserName());
        log.debug("Secondary groups {}", secondaryGroups.toString());

        secondaryUser.setGroups(new ArrayList<>());

        for (PosixGroup group : primaryGroups) {
          List<String> members = group.getMemberUids();
          log.debug("Group {} members {}", group.getCommonName(), members);
          if (!members.contains(secondaryUser.getUserName())) {
            ldapClient.addGroupMember(group.getCommonName(), secondaryUser.getUserName());

            ScimGroup scimGroup = new ScimGroup();
            scimGroup.setDisplay(group.getCommonName());
            scimGroup.setValue(String.valueOf(group.getGidNumber()));
            secondaryUser.getGroups().add(scimGroup);

            log.debug("Adding user {} to group {}", secondaryUser.getUserName(),
                group.getCommonName());
          }
        }

        for (PosixGroup group : secondaryGroups) {
          List<String> members = group.getMemberUids();
          log.debug("Group members {}", members);
          if (!members.contains(primaryUser.getUserName())) {
            ldapClient.addGroupMember(group.getCommonName(), primaryUser.getUserName());

            ScimGroup scimGroup = new ScimGroup();
            scimGroup.setDisplay(group.getCommonName());
            scimGroup.setValue(String.valueOf(group.getGidNumber()));
            primaryUser.getGroups().add(scimGroup);

            log.debug("Adding user {} to group {}", primaryUser.getUserName(),
                group.getCommonName());
          }
        }

        linkedUsers.add(secondaryUser);

        secondaryPosixUser.setUidNumber(primaryPosixUser.getUidNumber());
        secondaryPosixUser.setHomeDirectory(primaryPosixUser.getHomeDirectory());
        ldapClient.updatePosixUser(secondaryPosixUser);

        log.debug("Modified LDAP user {}", secondaryUser.toString());

      }

      linkedUsers.add(primaryUser);

    }
    return linkedUsers;
  }

  /**
   * Unlinks the users represented in the JSON serialized list of SCIM user's via LDAP locally.
   * 
   * @param scimUsers the SCIM user's to unlink
   * @return a list of JSON serialized SCIM user's containing the user's information after unlinking
   */
  public List<ScimUser> unlinkUsers(List<ScimUser> scimUsers) {
    ArrayList<ScimUser> unlinkedUsers = new ArrayList<>();

    for (ScimUser user : scimUsers) {
      PosixUser posixUser = ldapClient.getPosixUser(user.getUserName());
      log.debug("Posix user {}", posixUser.toString());

      // restore uidNumber and homDirectory from redis
      String uidNumber = redisClient.getUidNumber(posixUser.getUniqueIdentifier());
      String homeDirectory = redisClient.getUserHome(posixUser.getUniqueIdentifier());

      posixUser.setHomeDirectory(homeDirectory);
      posixUser.setUidNumber(uidNumber);

      PosixUser updatedUser = ldapClient.updatePosixUser(posixUser);

      if (updatedUser != null) {
        log.debug("Modified LDAP user {}", updatedUser.toString());
        unlinkedUsers.add(posixUserDao.scimUserFromPosixUser(updatedUser));
      }
    }

    return unlinkedUsers;
  }


  @SuppressWarnings("unused")
  private List<ScimUser> unlinkUsers_Old(List<ScimUser> scimUsers) {
    ArrayList<ScimUser> unlinkedUsers = new ArrayList<>();

    for (ScimUser user : scimUsers) {
      PosixUser posixUser = ldapClient.getPosixUser(user.getUserName());
      log.debug("Posix user {}", posixUser.toString());

      for (ScimGroup group : user.getGroups()) {
        ldapClient.removeGroupMember(group.getDisplay(), user.getUserName());
        log.debug("Remove user {} from group {}", user.getUserName(), group.getDisplay());
      }

      if (!user.isActive() && user.getMeta() != null) {
        posixUser.setHomeDirectory(user.getMeta().get("homeDirectory"));
        posixUser.setUidNumber(user.getMeta().get("uidNumber"));

        ldapClient.updatePosixUser(posixUser);

        log.debug("Modified LDAP user {}", posixUser.toString());
      }

      posixUser = ldapClient.getPosixUser(user.getUserName());
      Meta metaData = new Meta();
      metaData.put("homeDirectory", posixUser.getHomeDirectory());
      metaData.put("cn", posixUser.getCommonName());
      metaData.put("gidNumber", String.valueOf(posixUser.getGidNumber()));
      metaData.put("uid", posixUser.getUid());
      metaData.put("uidNumber", String.valueOf(posixUser.getUidNumber()));

      user.setGroups(new ArrayList<>());
      List<PosixGroup> posixGroups = ldapClient.getUserGroups(user.getUserName());
      for (PosixGroup group : posixGroups) {
        ScimGroup scimGroup = new ScimGroup();
        scimGroup.setDisplay(group.getCommonName());
        scimGroup.setValue(String.valueOf(group.getGidNumber()));
        user.getGroups().add(scimGroup);
      }
      user.setActive(true);
      unlinkedUsers.add(user);
    }
    return unlinkedUsers;
  }
}
