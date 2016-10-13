/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.test.redis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import edu.kit.scc.IdentityHarmonizationService;
import edu.kit.scc.redis.RedisClient;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IdentityHarmonizationService.class)
@ActiveProfiles("development")
public class RedisClientTest {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(RedisClientTest.class);

  @Autowired
  private RedisClient redisClient;

  @Test
  public void createUserTest() {
    String uuid = UUID.randomUUID().toString();

    String uidNumber = redisClient.createUser(uuid);

    // assertNotNull(uidNumber);

    uidNumber = redisClient.createUser(uuid);

    // assertNull(uidNumber);
  }

  @Test
  public void createGroupTest() {
    String uuid = UUID.randomUUID().toString();

    String gidNumber = redisClient.createGroup(uuid);

    assertNotNull(gidNumber);

    gidNumber = redisClient.createGroup(uuid);

    assertNull(gidNumber);
  }
}
