/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.test;

import static org.junit.Assert.assertNotNull;

import edu.kit.scc.IdentityHarmonizationService;
import edu.kit.scc.IdentityManager;
import edu.kit.scc.scim.ScimGroup;
import edu.kit.scc.scim.ScimUser;
import edu.kit.scc.scim.ScimUser.Email;
import edu.kit.scc.scim.ScimUser.Meta;
import edu.kit.scc.scim.ScimUser.Name;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IdentityHarmonizationService.class)
@ActiveProfiles("test")
public class CreatePosixUserTest {

  private static final Logger log = LoggerFactory.getLogger(CreatePosixUserTest.class);

  @Autowired
  private IdentityManager identityManager;

  private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  private static String generateString(Random rng, String characters, int length) {
    char[] text = new char[length];
    for (int i = 0; i < length; i++) {
      text[i] = characters.charAt(rng.nextInt(characters.length()));
    }
    return new String(text);
  }

  @Test
  public void testCreateUser(){
    ScimUser scimUser = new ScimUser();
    
    ScimUser createdUser = identityManager.createUser(scimUser);
    
    assertNotNull(createdUser);
  }
  
  // @Test
  public void generateNewDefaultPosixUserTest() {
    ScimUser scimUser = identityManager.createUser(new ScimUser());
    log.debug(scimUser.toString());

    assertNotNull(scimUser);
  }

  // @Test
  public void generateNewPosixUserTestWithExternalId() {
    ScimUser scimUser = new ScimUser();
    String externalId = UUID.randomUUID().toString();

    log.debug("External id {}", externalId);

    scimUser.setExternalId(externalId);
    ScimUser createdUser = identityManager.createUser(scimUser);
    log.debug(createdUser.toString());

    assertNotNull(createdUser);
  }

  //@Test
  public void generateNewPosixUserFromScim() {
    Random rng = new Random();
    int length = 6;

    ScimUser scimUser = new ScimUser();
    String externalId = UUID.randomUUID().toString();

    scimUser.setExternalId(externalId);
    scimUser.setUserName(generateString(rng, characters, length));

    Name name = new Name();
    name.setFamilyName(generateString(rng, characters, length));
    name.setGivenName(generateString(rng, characters, length));

    scimUser.setName(name);

    Email email = new Email();
    email.setValue(generateString(rng, characters, 3) + "@" + generateString(rng, characters, 3)
        + "." + generateString(rng, characters, 2));

    scimUser.setEmails(Arrays.asList(email));

    ScimGroup group1 = new ScimGroup();
    group1.setValue(UUID.randomUUID().toString());
    group1.setDisplay(generateString(rng, characters, length));
    ScimGroup group2 = new ScimGroup();
    group2.setValue(UUID.randomUUID().toString());
    group2.setDisplay(generateString(rng, characters, length));

    scimUser.setGroups(Arrays.asList(group1, group2));

    Meta meta = new Meta();
    meta.put("organisation_name", "indigo-dc");

    scimUser.setMeta(meta);

    log.debug(scimUser.toString());
    ScimUser createdUser = identityManager.createUser(scimUser);
    log.debug(createdUser.toString());

    assertNotNull(createdUser);
  }
}
