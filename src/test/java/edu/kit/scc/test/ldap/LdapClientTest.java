/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.test.ldap;

import static org.junit.Assert.assertNotNull;

import edu.kit.scc.IdentityHarmonizationService;
import edu.kit.scc.ldap.LdapClient;
import edu.kit.scc.ldap.PosixGroup;
import edu.kit.scc.ldap.PosixUser;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IdentityHarmonizationService.class)
@ActiveProfiles("development")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LdapClientTest {

  private static final Logger log = LoggerFactory.getLogger(LdapClientTest.class);

  @Autowired
  private LdapClient ldapClient;

  protected static PosixGroup posixGroup;
  protected static PosixUser posixUser;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    String userCn = "newUser";
    String userSn = "newUser";
    String description = "new posix user";
    String homeDirectory = "/home/newUser";
    String uid = "newUser";
    String uidNumber = "6001";
    String userGidNumber = "3333";

    posixUser = new PosixUser();
    posixUser.setCommonName(userCn);
    posixUser.setSurName(userSn);
    posixUser.setHomeDirectory(homeDirectory);
    posixUser.setDescription(description);
    posixUser.setUid(uid);
    posixUser.setUidNumber(uidNumber);
    posixUser.setGidNumber(userGidNumber);

    String groupCn = "newGroup";
    String gidNumber = "3333";

    posixGroup = new PosixGroup();
    posixGroup.setCommonName(groupCn);
    posixGroup.setGidNumber(gidNumber);
  }

  @Test
  public void a_createLdapGroupTest() {
    PosixGroup group = ldapClient.createPosixGroup(posixGroup);
    assertNotNull(group);

    log.debug(group.toString());
  }

  @Test
  public void b_createLdapUserTest() {
    PosixUser user = ldapClient.createPosixUser(posixUser);
    assertNotNull(user);

    log.debug(user.toString());
  }

  @Test
  public void c_getLdapUserTest() {
    PosixUser user = ldapClient.getPosixUser(posixUser.getUid());
    assertNotNull(user);

    log.debug(user.toString());
  }

  @Test
  public void d_updateUserTest() {
    posixUser.setDescription("new posix user (update)");
    PosixUser user = ldapClient.updatePosixUser(posixUser);
    assertNotNull(user);

    log.debug(user.toString());
  }

  @Test
  public void e_addUserToGroupTest() {
    ldapClient.addGroupMember(posixGroup.getCommonName(), posixUser.getUid());
  }

  @Test
  public void f_removeUserFromGroupTest() {
    ldapClient.removeGroupMember(posixGroup.getCommonName(), posixUser.getUid());
  }

  @Test
  public void f_getLdapGroupTest() {
    PosixGroup group = ldapClient.getPosixGroupByCn(posixGroup.getCommonName());

    assertNotNull(group);

    log.debug(group.toString());
  }

  @Test
  public void h_deleteLdapUserTest() {
    ldapClient.deleteUser(posixUser.getUid());
  }

  @Test
  public void i_deleteLdapGroupTest() {
    ldapClient.deleteGroup(posixGroup.getCommonName());
  }

  @Test
  public void j_getLdapGroupsTest() {
    List<PosixGroup> groups = ldapClient.getPosixGroups();
    assertNotNull(groups);
  }

  @Test
  public void k_getLdapUsersTest() {
    List<PosixUser> users = ldapClient.getPosixUsers();
    assertNotNull(users);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void getNewGidNumber() {
    assertNotNull(ldapClient.generateGroupIdNumber());
  }
}
