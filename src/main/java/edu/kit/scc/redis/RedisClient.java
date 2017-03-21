/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RedisClient {

  private static final Logger log = LoggerFactory.getLogger(RedisClient.class);

  @Value("${ldap.uidNumber.start}")
  String minUidNumber;

  @Value("${ldap.uidNumber.end}")
  String maxUidNumber;

  @Value("${ldap.gidNumber.start}")
  String minGidNumber;

  @Value("${ldap.gidNumber.end}")
  String maxGidNumber;

  @Autowired
  private StringRedisTemplate template;


  /**
   * Gets the user's uidNumber.
   * 
   * @param id the user's id
   * @return the uidNumber
   */
  public String getUidNumber(String id) {

    String uidNumber = template.opsForValue().get("user:" + id + ":uidNumber");

    // create a new uid number
    if (uidNumber == null) {
      boolean counterInit = template.opsForValue().setIfAbsent("uidNumber", minUidNumber);
      if (counterInit) {
        log.debug("Redis uidNumber counter initialized");
      }
      Long newUidNumber = template.opsForValue().increment("uidNumber", 1);
      // check if max user id number reached
      if (newUidNumber <= Long.valueOf(maxUidNumber)) {
        template.opsForValue().set("user:" + id + ":uidNumber", newUidNumber.toString());
      } else {
        log.error("Can not create user, maximum uid number reached");
      }
    }
    return template.opsForValue().get("user:" + id + ":uidNumber");
  }

  /**
   * Gets the user's home directory.
   * 
   * @param id the external id
   * @return the homeDirectory
   */
  public String getUserHome(String id) {
    return template.opsForValue().get("user:" + id + ":homeDirectory");
  }

  /**
   * Stores a new user for the given external id.
   * 
   * @param externalId the external id
   * @return the user id number
   */
  public String createUser(String externalId) {
    String id = UUID.randomUUID().toString();
    boolean userCreated = template.opsForValue().setIfAbsent("user:" + externalId, id);
    if (userCreated) {
      log.debug("User {} created with id {}", externalId, id);
    } else {
      log.warn("User {} already exists", externalId);
    }
    return template.opsForValue().get("user:" + externalId);
  }

  /**
   * Sets the user's uidNumber.
   * 
   * @param id the user's id
   * @param uidNumber the uidNumber
   */
  public void setUidNumber(String id, String uidNumber) {
    template.opsForValue().set("user:" + id + ":uidNumber", uidNumber);
  }

  /**
   * Sets the user's home directory.
   * 
   * @param id the user's id
   * @param homeDirectory the home directory
   */
  public void setUserHome(String id, String homeDirectory) {
    template.opsForValue().set("user:" + id + ":homeDirectory", homeDirectory);
  }
}
