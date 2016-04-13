/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.ldap;

import edu.kit.scc.dto.PosixGroup;
import edu.kit.scc.dto.PosixUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * LDAP client implementation.
 * 
 * @author benjamin
 *
 */
@Component
public class LdapClient {

  private static Logger log = LoggerFactory.getLogger(LdapClient.class);

  @Value("${ldap.url}")
  private String url;

  @Value("${ldap.searchBase}")
  private String searchBase;

  @Value("${ldap.userBase}")
  private String userBase;

  @Value("${ldap.groupBase}")
  private String groupBase;

  @Value("${ldap.bindDn}")
  private String bindDn;

  @Value("${ldap.bindPassword}")
  private String password;

  @Bean
  LdapContextSource contextSource() {
    LdapContextSource ldapContextSource = new LdapContextSource();
    ldapContextSource.setUrl(url);
    ldapContextSource.setBase(searchBase);
    ldapContextSource.setUserDn(bindDn);
    ldapContextSource.setPassword(password);
    return ldapContextSource;
  }

  @Bean
  LdapTemplate ldapTemplate(LdapContextSource contextSource) {
    return new LdapTemplate(contextSource);
  }

  @Bean
  LdapPosixUserDao ldapPosixUser(LdapTemplate ldapTemplate) {
    LdapPosixUserDao ldapUserDao = new LdapPosixUserDao();
    ldapUserDao.setLdapTemplate(ldapTemplate);
    ldapUserDao.setUserBase(userBase);
    return ldapUserDao;
  }

  @Bean
  LdapPosixGroupDao ldapPosixGroup(LdapTemplate ldapTemplate) {
    LdapPosixGroupDao ldapGroupDao = new LdapPosixGroupDao();
    ldapGroupDao.setLdapTemplate(ldapTemplate);
    ldapGroupDao.setGroupBase(groupBase);
    return ldapGroupDao;
  }

  @Autowired
  private LdapPosixUserDao ldapPosixUser;

  @Autowired
  private LdapPosixGroupDao ldapPosixGroup;

  /**
   * Gets the POSIX user specified from the LDAP server.
   * 
   * @param uid the user's uid
   * @return a {@link PosixUser} with the LDAP user information
   */
  public PosixUser getPosixUser(String uid) {
    List<PosixUser> userList = ldapPosixUser.getUserDetails(uid);
    PosixUser user = null;

    if (userList != null && !userList.isEmpty()) {
      user = userList.get(0);
      log.debug(user.toString());
    }
    return user;
  }

  /**
   * Gets the POSIX group specified from the LDAP server.
   * 
   * @param cn the group's common name
   * @return a {@link PosixGroup} with the LDAP group information
   */
  public PosixGroup getPosixGroup(String cn) {
    List<PosixGroup> groupList = ldapPosixGroup.getGroupDetails(cn);
    PosixGroup group = null;

    if (groupList != null && !groupList.isEmpty()) {
      group = groupList.get(0);
      log.debug(group.toString());
    }
    return group;
  }

  /**
   * Gets the POSIX group specified from the LDAP server.
   * 
   * @param gidNumber the group's gidNumber
   * @return a {@link PosixGroup} with the LDAP group information
   */
  public PosixGroup getPosixGroup(int gidNumber) {
    List<PosixGroup> groupList = ldapPosixGroup.getGroupDetails(gidNumber);
    PosixGroup group = null;

    if (groupList != null && !groupList.isEmpty()) {
      group = groupList.get(0);
      log.debug(group.toString());
    }
    return group;
  }

  public List<PosixGroup> getUserGroups(String uid) {
    return ldapPosixGroup.getUserGroups(uid);
  }

  /**
   * Gets all POSIX users from the LDAP server.
   * 
   * @return a list of {@link PosixUser} with the LDAP user information
   */
  public List<PosixUser> getPosixUsers() {
    List<PosixUser> userList = ldapPosixUser.getAllUsers();
    for (int i = 0; i < userList.size(); i++) {
      log.debug("User {}", ((PosixUser) userList.get(i)).toString());
    }
    return userList;
  }

  /**
   * Gets all POSIX users from the LDAP server with the specified uidNumber.
   * 
   * @param uidNumber the users' uidNumber
   * @return a list of {@link PosixUser} with the LDAP user information
   */
  public List<PosixUser> getPosixUsers(int uidNumber) {
    List<PosixUser> userList = ldapPosixUser.getAllUsers(uidNumber);
    for (int i = 0; i < userList.size(); i++) {
      log.debug("User {}", ((PosixUser) userList.get(i)).toString());
    }
    return userList;
  }

  /**
   * Gets all POSIX groups from the LDAP server.
   * 
   * @return a list of {@link PosixGroup} with the LDAP group information
   */
  public List<PosixGroup> getPosixGroups() {
    List<PosixGroup> groupList = ldapPosixGroup.getAllGroups();
    for (int i = 0; i < groupList.size(); i++) {
      log.debug("Group {}", ((PosixGroup) groupList.get(i)).toString());
    }
    return groupList;
  }

  /**
   * Updates a POSIX user.
   * 
   * @param posixUser the POSIX user to update
   * @return the updated {@link PosixUser}
   */
  public PosixUser updatePosixUser(PosixUser posixUser) {
    ldapPosixUser.updateUser(posixUser);

    return getPosixUser(posixUser.getUid());
  }

  /**
   * Deletes a specific LDAP user.
   * 
   * @param uid the user's uid
   */
  public void deleteUser(String uid) {
    PosixUser user = new PosixUser();
    user.setUid(uid);
    ldapPosixUser.deleteUser(user);
  }

  /**
   * Creates a new LDAP POSIX group.
   * 
   * @param cn the group's common name
   * @param gidNumber the group's gid number
   * @param description the group's description
   * @param userPassword the group's user password
   * @return the created {@link PosixGroup}
   * 
   */
  public PosixGroup createPosixGroup(String cn, int gidNumber, String description,
      String userPassword) {
    PosixGroup group = new PosixGroup();
    group.setCommonName(cn);
    group.setGidNumber(gidNumber);
    group.setDescription(description);
    if (userPassword != null) {
      group.setUserPassword(userPassword.getBytes());
    }
    ldapPosixGroup.insertGroup(group);

    return getPosixGroup(cn);
  }

  /**
   * Updates a specific LDAP POSIX group.
   * 
   * @param cn the group's common name
   * @param gidNumber the group's gid number
   * @param description the group's description
   * @param userPassword the group's user password
   * @return the updated {@link PosixGroup}
   */
  public PosixGroup updatePosixGroup(String cn, int gidNumber, String description,
      String userPassword) {
    PosixGroup group = new PosixGroup();
    group.setCommonName(cn);
    group.setGidNumber(gidNumber);
    group.setDescription(description);
    if (userPassword != null) {
      group.setUserPassword(userPassword.getBytes());
    }
    ldapPosixGroup.updateGroup(group);

    return getPosixGroup(cn);
  }

  /**
   * Deletes a specific LDAP group.
   * 
   * @param cn the group's common name
   */
  public void deleteGroup(String cn) {
    PosixGroup group = new PosixGroup();
    group.setCommonName(cn);
    ldapPosixGroup.deleteGroup(group);
  }

  /**
   * Adds a specific LDAP user to a specific group.
   * 
   * @param cn the group's common name
   * @param memberUid the user's uid
   */
  public void addGroupMember(String cn, String memberUid) {
    PosixGroup group = new PosixGroup();
    group.setCommonName(cn);
    ldapPosixGroup.addMember(group, memberUid);
  }

  /**
   * Removes a specific LDAP user from a specific group.
   * 
   * @param cn the group's common name
   * @param memberUid the user's uid
   */
  public void removeGroupMember(String cn, String memberUid) {
    PosixGroup group = new PosixGroup();
    group.setCommonName(cn);
    ldapPosixGroup.removeMember(group, memberUid);
  }

  /**
   * Compares two POSIX LDAP groups.
   * 
   * @param group1 {@link PosixGroup} group one
   * @param group2 {@link PosixGroup} group two
   * @return true if groups are equal (name and gidNumber)
   * 
   */
  public boolean equalGroups(PosixGroup group1, PosixGroup group2) {
    if (group1.getGidNumber() == group2.getGidNumber()) {
      if (group1.getCommonName().equals(group2.getCommonName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generates a non-conflicting group id number.
   * 
   * @return a new int gidNumber
   */
  public int generateGroupIdNumber() {
    int max = 99999;
    int min = 10000;
    Random rand = new Random();
    ArrayList<Integer> existingGidNumbers = new ArrayList<Integer>();
    List<PosixGroup> groups = ldapPosixGroup.getAllGroups();
    for (PosixGroup group : groups) {
      existingGidNumbers.add(group.getGidNumber());
    }
    int randomInt = rand.nextInt((max - min) + 1) + min;
    while (existingGidNumbers.contains(randomInt)) {
      randomInt = rand.nextInt((max - min) + 1) + min;
    }
    return randomInt;
  }

  /**
   * Generates a non-conflicting user id number.
   * 
   * @return a new int uidNumber
   */
  public int generateUserIdNumber() {
    int max = 99999;
    int min = 10000;
    Random rand = new Random();
    ArrayList<Integer> existingUidNumbers = new ArrayList<Integer>();
    List<PosixUser> users = ldapPosixUser.getAllUsers();
    for (PosixUser user : users) {
      existingUidNumbers.add(user.getUidNumber());
    }
    int randomInt = rand.nextInt((max - min) + 1) + min;
    while (existingUidNumbers.contains(randomInt)) {
      randomInt = rand.nextInt((max - min) + 1) + min;
    }
    return randomInt;
  }
}
