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
import edu.kit.scc.scim.ScimUser.Email;
import edu.kit.scc.scim.ScimUser.Meta;
import edu.kit.scc.scim.ScimUser.Name;
import edu.kit.scc.scim.ScimUserDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class PosixUserDao implements ScimUserDao {

  private static final Logger log = LoggerFactory.getLogger(PosixUserDao.class);

  @Value("${ldap.default.gidNumber}")
  String defaultGidNumber;

  @Autowired
  private RedisClient redisClient;

  @Autowired
  private LdapClient ldapClient;

  @Override
  public ScimUser createUser(String userBase, ScimUser scimUser) {
    // check if default group exists
    PosixGroup defaultGroup = ldapClient.getPosixGroupByGidNumber(defaultGidNumber);
    if (defaultGroup == null) {
      log.error("default group {} does not exists", defaultGidNumber);
      return null;
    }

    String uniqueIdentifier = scimUser.getExternalId();

    // if no unique identifier provided generate a random one
    if (uniqueIdentifier == null) {
      uniqueIdentifier = UUID.randomUUID().toString();
    }

    // create a new user id number
    String uidNumber = redisClient.createUser(uniqueIdentifier);
    if (uidNumber == null) {
      log.error("could not get uidNumber for user {}", uniqueIdentifier);
      return null;
    }

    // create a default user id
    String uid = scimUser.getUserName();

    // populate user with default values
    PosixUser localUser = new PosixUser();
    localUser.setCommonName(uid);
    localUser.setDescription("user created by IdH");
    localUser.setGidNumber(defaultGidNumber);
    localUser.setHomeDirectory("/home/" + uid);
    localUser.setSurName(uid);
    localUser.setUid(uid);
    localUser.setUidNumber(uidNumber);
    localUser.setUniqueIdentifier(uniqueIdentifier);

    log.debug("User defaults to {}", localUser.toString());

    // overwrite with provided values
    if (scimUser.getUserName() != null) {
      // check for conflicting uid
      if (ldapClient.getPosixUser(scimUser.getUserName()) == null) {
        localUser.setUid(scimUser.getUserName());
      } else {
        log.warn("user {} already exists, use default uid", scimUser.getUserName());
      }
    }

    List<Email> emails = scimUser.getEmails();
    if (emails != null && !emails.isEmpty()) {
      if (emails.get(0).getValue() != null) {
        localUser.setMail(emails.get(0).getValue());
        localUser.setCommonName(emails.get(0).getValue());
      }
    }

    Name name = scimUser.getName();
    if (name != null) {
      if (name.getFamilyName() != null) {
        localUser.setSurName(name.getFamilyName());
      }
      if (name.getGivenName() != null) {
        localUser.setGivenName(name.getGivenName());
      }
    }

    // create the user locally
    PosixUser posixUser = ldapClient.createPosixUser(userBase, localUser);
    if (posixUser == null) {
      log.error("could not create user in the LDAP directory");
      return null;
    }
    log.debug("User created {}", posixUser.toString());

    // add user to default group
    ldapClient.addGroupMember(defaultGroup.getCommonName(), localUser.getUid());

    ScimGroup defaultScimGroup = new ScimGroup();
    defaultScimGroup.setDisplay(defaultGroup.getCommonName());
    defaultScimGroup.setRef(defaultGroup.getGidNumber());

    ScimUser createdUser = scimUserFromPosixUser(posixUser);
    createdUser.getGroups().add(defaultScimGroup);

    // TODO group unique identifiers
    // create local groups, add user
    if (scimUser.getGroups() != null) {
      for (ScimGroup group : scimUser.getGroups()) {
        if (group.getValue() != null && group.getDisplay() != null) {
          // check if group already exists
          PosixGroup localGroup = ldapClient.getPosixGroupByCn(group.getDisplay());

          // create group
          if (localGroup == null) {
            String groupNumber = redisClient.createGroup(group.getValue());
            localGroup = new PosixGroup();
            localGroup.setGidNumber(groupNumber);
            localGroup.setCommonName(group.getDisplay());
            localGroup.setDescription("group created by IdH");

            localGroup = ldapClient.createPosixGroup(localGroup);

            if (localGroup == null) {
              log.error("could not create group in the LDAP directory");
              break;
            } else {
              log.debug("Created group {}", localGroup.toString());
            }
          } else {
            log.debug("Found existing group {}", localGroup.toString());;
          }

          // add user
          boolean userAdded =
              ldapClient.addGroupMember(localGroup.getCommonName(), localUser.getUid());
          if (userAdded) {
            ScimGroup scimGroup = new ScimGroup();
            scimGroup.setDisplay(localGroup.getCommonName());
            scimGroup.setValue(group.getValue());
            scimGroup.setRef(localGroup.getGidNumber());

            createdUser.getGroups().add(scimGroup);

            log.debug("Added user {} to group {}", localUser.getUid(), localGroup.getCommonName());
          }
        }
      }
    }

    return createdUser;
  }

  /**
   * Converts a {@link PosixUser} object to a {@link ScimUser} object.
   * 
   * @param posixUser the {@PosixUser} object
   * @return the posixUser as {@link ScimUser} object
   */
  public ScimUser scimUserFromPosixUser(PosixUser posixUser) {
    ScimUser scimUser = new ScimUser();
    scimUser.setSchemas(Arrays.asList(ScimUser.USER_SCHEMA_2_0));

    scimUser.setExternalId(posixUser.getUniqueIdentifier());
    scimUser.setId(posixUser.getUid());
    scimUser.setUserName(posixUser.getUid());

    Email email = new Email();
    email.setValue(posixUser.getMail());
    scimUser.setEmails(Arrays.asList(email));

    Meta meta = new Meta();
    meta.put("description", posixUser.getDescription());
    meta.put("homeDirectory", posixUser.getHomeDirectory());
    meta.put("gecos", posixUser.getGecos());
    meta.put("loginShell", posixUser.getLoginShell());
    meta.put("uidNumber", posixUser.getUidNumber());
    meta.put("uniqueIdentifier", posixUser.getUniqueIdentifier());

    scimUser.setMeta(meta);

    scimUser.setGroups(new ArrayList<ScimGroup>());

    Name name = new Name();
    name.setFamilyName(posixUser.getSurName());
    name.setGivenName(posixUser.getGivenName());

    // scimUser.setPassword(posixUser.getUserPassword());

    scimUser.setActive(true);

    return scimUser;
  }

}
