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
   * Stores a new user id number for the given external id.
   * 
   * @param externalId the external id
   * @return the user id number
   */
  public String createUser(String externalId) {
    boolean counterInit = template.opsForValue().setIfAbsent("uidNumber", minUidNumber);
    if (counterInit) {
      log.debug("Redis uidNumber counter initialized");
    }

    String uidNumber = template.opsForValue().get("uidNumber");

    // max user id number reached
    if (Integer.valueOf(uidNumber) <= Integer.valueOf(maxUidNumber)) {
      return setUidNumber(externalId, uidNumber);
    }
    return null;
  }

  /**
   * Stores a new group id number for the given external id.
   * 
   * @param value the external id
   * @return the group id number
   */
  public String createGroup(String value) {
    boolean counterInit = template.opsForValue().setIfAbsent("gidNumber", minGidNumber);
    if (counterInit) {
      log.debug("Redis gidNumber counter initialized");
    }

    String gidNumber = template.opsForValue().get("gidNumber");

    // max user id number reached
    if (Integer.valueOf(gidNumber) <= Integer.valueOf(maxGidNumber)) {

      boolean groupCreated = template.opsForValue().setIfAbsent("group:" + value, gidNumber);

      if (groupCreated) {
        log.debug("Group group:{} = {} created {}", value, gidNumber, groupCreated);
        template.opsForValue().increment("gidNumber", 1);
        return gidNumber;
      } else {
        log.warn("Group group:{} already exists", value);
      }
    }
    return null;
  }

  /**
   * Stores the user's uidNumber.
   * 
   * @param externalId the external id
   * @param uidNumber the uidNumber
   * @return the uidNumber
   */
  public String setUidNumber(String externalId, String uidNumber) {
    boolean created = template.opsForValue().setIfAbsent("user:" + externalId, uidNumber);

    if (created) {
      log.debug("User user:{} = {} created {}", externalId, uidNumber, created);
      template.opsForValue().increment("uidNumber", 1);
      return uidNumber;
    } else {
      log.warn("User user:{} already exists", externalId);
      return template.opsForValue().get("user:" + externalId);
    }
  }

  /**
   * Gets the user's uidNumber.
   * 
   * @param externalId the external id
   * @return the uidNumber
   */
  public String getUidNumber(String externalId) {
    return template.opsForValue().get("user:" + externalId);
  }

  /**
   * Stores the user's home directory.
   * 
   * @param externalId the external id
   * @param homeDirectory the home directory
   * @return the uidNumber
   */
  public String setUserHome(String externalId, String homeDirectory) {
    boolean created =
        template.opsForValue().setIfAbsent("user:" + externalId + ":" + "home", homeDirectory);

    if (created) {
      log.debug("User user:{} = {} created {}", externalId, homeDirectory, created);
      return homeDirectory;
    } else {
      log.warn("User user:{} home exists", externalId);
      return template.opsForValue().get("user:" + externalId + ":" + "home");
    }
  }

  /**
   * Gets the user's home directory.
   * 
   * @param externalId the external id
   * @return the homeDirectory
   */
  public String getUserHome(String externalId) {
    return template.opsForValue().get("user:" + externalId + ":" + "home");
  }
}
