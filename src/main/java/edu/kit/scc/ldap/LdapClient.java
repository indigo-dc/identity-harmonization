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
    return ldapPosixUser.getUserDetails(uid);
  }

  /**
   * Gets the POSIX group specified from the LDAP server.
   * 
   * @param cn the group's common name
   * @return a {@link PosixGroup} with the LDAP group information
   */
  public PosixGroup getPosixGroupByCn(String cn) {
    return ldapPosixGroup.getGroupDetails(cn);
  }

  /**
   * Gets the POSIX group specified from the LDAP server.
   * 
   * @param gidNumber the group's gidNumber
   * @return a {@link PosixGroup} with the LDAP group information
   */
  public PosixGroup getPosixGroupByGidNumber(String gidNumber) {
    return ldapPosixGroup.getGroupDetails(Integer.valueOf(gidNumber.trim()));
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
   * @return true if success
   */
  public boolean deleteUser(String uid) {
    PosixUser user = new PosixUser();
    user.setUid(uid);
    return ldapPosixUser.deleteUser(user);
  }

  /**
   * Creates a new LDAP POSIX group.
   * 
   * @param group the {@link PosixGroup} to create
   * @return the created {@link PosixGroup}
   * 
   */
  public PosixGroup createPosixGroup(PosixGroup group) {
    ldapPosixGroup.insertGroup(group);
    return getPosixGroupByCn(group.getCommonName());
  }

  /**
   * Creates a new LDAP POSIX user.
   * 
   * @param user the {@link PosixUser} to create
   * @return the created {@link PosixUser}
   */
  public PosixUser createPosixUser(PosixUser user) {
    ldapPosixUser.insertUser(user);
    return getPosixUser(user.getUid());
  }

  /**
   * Deletes a specific LDAP group.
   * 
   * @param cn the group's common name
   * @return true on success
   */
  public boolean deleteGroup(String cn) {
    PosixGroup group = new PosixGroup();
    group.setCommonName(cn);
    return ldapPosixGroup.deleteGroup(group);
  }

  /**
   * Adds a specific LDAP user to a specific group.
   * 
   * @param cn the group's common name
   * @param memberUid the user's uid
   * @return true on success
   */
  public boolean addGroupMember(String cn, String memberUid) {
    PosixGroup group = new PosixGroup();
    group.setCommonName(cn);
    return ldapPosixGroup.addMember(group, memberUid);
  }

  /**
   * Removes a specific LDAP user from a specific group.
   * 
   * @param cn the group's common name
   * @param memberUid the user's uid
   * @return true on success
   */
  public boolean removeGroupMember(String cn, String memberUid) {
    PosixGroup group = new PosixGroup();
    group.setCommonName(cn);
    return ldapPosixGroup.removeMember(group, memberUid);
  }

  /**
   * Generates a non-conflicting group id number.
   * 
   * @return a new int gidNumber
   */
  @Deprecated
  public int generateGroupIdNumber() {
    int max = 99999;
    int min = 10000;
    Random rand = new Random();
    ArrayList<String> existingGidNumbers = new ArrayList<String>();
    List<PosixGroup> groups = ldapPosixGroup.getAllGroups();
    for (PosixGroup group : groups) {
      existingGidNumbers.add(group.getGidNumber());
    }
    int randomInt = rand.nextInt((max - min) + 1) + min;
    while (existingGidNumbers.contains(String.valueOf(randomInt))) {
      randomInt = rand.nextInt((max - min) + 1) + min;
    }
    return randomInt;
  }

  /**
   * Generates a non-conflicting user id number.
   * 
   * @return a new int uidNumber
   */
  @Deprecated
  public int generateUserIdNumber() {
    int max = 99999;
    int min = 10000;
    Random rand = new Random();
    ArrayList<String> existingUidNumbers = new ArrayList<String>();
    List<PosixUser> users = ldapPosixUser.getAllUsers();
    for (PosixUser user : users) {
      existingUidNumbers.add(user.getUidNumber());
    }
    int randomInt = rand.nextInt((max - min) + 1) + min;
    while (existingUidNumbers.contains(String.valueOf(randomInt))) {
      randomInt = rand.nextInt((max - min) + 1) + min;
    }
    return randomInt;
  }
}
