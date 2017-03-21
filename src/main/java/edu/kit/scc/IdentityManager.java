/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import edu.kit.scc.ldap.LdapClient;
import edu.kit.scc.ldap.PosixUser;
import edu.kit.scc.redis.RedisClient;
import edu.kit.scc.scim.ScimUser;
import edu.kit.scc.scim.ScimUser.Email;
import edu.kit.scc.scim.ScimUser.Meta;
import edu.kit.scc.scim.ScimUser.Name;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class IdentityManager {

  private static final Logger log = LoggerFactory.getLogger(IdentityManager.class);

  @Value("${ldap.default.gidNumber}")
  private String defaultGidNumber;

  @Autowired
  private LdapClient ldapClient;

  @Autowired
  private RedisClient redisClient;


  /**
   * Creates a new {@link PosixUser} from the given {@link ScimUser}.
   * 
   * @param scimUser the {@link ScimUser}
   * @return the created {@link ScimUser}
   */
  @SuppressWarnings("unchecked")
  public ScimUser createUser(ScimUser scimUser) {
    // try to create all user identities from INDIGO schema
    Map<String, Object> indigo = scimUser.getIndigo();
    if (indigo != null && !indigo.isEmpty()) {
      // OIDC identities
      ArrayList<Object> oidcIds = (ArrayList<Object>) indigo.get("oidcIds");
      if (oidcIds != null && !oidcIds.isEmpty()) {
        for (int i = 0; i < oidcIds.size(); i++) {
          Map<String, String> oidcId = (Map<String, String>) oidcIds.get(i);

          String issuer = oidcId.get("issuer");
          String subject = oidcId.get("subject");
          log.debug("issuer {}", issuer);
          log.debug("subject {}", subject);


          PosixUser localUser =
              createPosixUser("ou=users,ou=" + issuer, subject, scimUser.getExternalId());
          if (localUser != null) {
            log.debug("created user {}", localUser.toString());
          }
        }
      }
      // OIDC identities
      ArrayList<Object> samlIds = (ArrayList<Object>) indigo.get("samlIds");
      if (samlIds != null && !samlIds.isEmpty()) {
        for (int i = 0; i < samlIds.size(); i++) {
          Map<String, String> samlId = (Map<String, String>) samlIds.get(i);

          String issuer = samlId.get("idpId");
          String subject = samlId.get("userId");
          log.debug("issuer {}", issuer);
          log.debug("subject {}", subject);


          PosixUser localUser =
              createPosixUser("ou=users,ou=" + issuer, subject, scimUser.getExternalId());
          if (localUser != null) {
            log.debug("created user {}", localUser.toString());
          }
        }
      }
    }

    // try to create local user
    PosixUser localUser =
        createPosixUser("ou=users", scimUser.getUserName(), scimUser.getExternalId());

    ScimUser createdUser = scimUserFromPosixUser(localUser);

    String time = DateTime.now().toString();
    createdUser.getMeta().put("resourceType", "User");
    createdUser.getMeta().put("created", time);
    createdUser.getMeta().put("lastModified", time);

    return createdUser;
  }

  /**
   * Create a {@link PosixUser} from userBase, uid and externalId.
   * 
   * @param userBase the LDAP userBase to write to
   * @param uid the user's uid
   * @param externalId the user's externalId
   * @return the created {@link PosixUser}
   */
  public PosixUser createPosixUser(String userBase, String uid, String externalId) {

    if (uid == null || uid.isEmpty()) {
      Random rng = new Random();
      int length = 6;

      uid = generateString(rng, characters, length);
    }

    String id = redisClient.createUser(userBase + ":" + uid);
    String uidNumber = redisClient.getUidNumber(id);

    PosixUser posixUser = new PosixUser();
    posixUser.setUidNumber(uidNumber);
    posixUser.setUniqueIdentifier((externalId) == null ? id : externalId);

    posixUser.setGidNumber(defaultGidNumber);

    posixUser.setUid(uid);
    posixUser.setSurName(uid);
    posixUser.setCommonName(uid);
    posixUser.setHomeDirectory("/home/" + uid);

    posixUser.setDescription("user created by IdH");

    log.debug("Try to create user {}", posixUser.toString());
    PosixUser createdUser = ldapClient.createPosixUser(userBase, posixUser);

    return createdUser;
  }

  /**
   * Converts a {@link PosixUser} object to a {@link ScimUser} object.
   * 
   * @param posixUser the {@link PosixUser} object
   * @return the posixUser as {@link ScimUser} object
   */
  public ScimUser scimUserFromPosixUser(PosixUser posixUser) {
    ScimUser scimUser = new ScimUser();
    scimUser.setSchemas(Arrays.asList(ScimUser.USER_SCHEMA_2_0));

    scimUser.setExternalId(posixUser.getUniqueIdentifier());
    scimUser.setId(posixUser.getUid());
    scimUser.setUserName(posixUser.getUid());

    if (posixUser.getMail() != null) {
      Email email = new Email();
      email.setValue(posixUser.getMail());
      scimUser.setEmails(Arrays.asList(email));
    }

    Meta meta = new Meta();
    meta.put("description", posixUser.getDescription());
    meta.put("homeDirectory", posixUser.getHomeDirectory());
    meta.put("gecos", posixUser.getGecos());
    meta.put("loginShell", posixUser.getLoginShell());
    meta.put("uidNumber", posixUser.getUidNumber());
    meta.put("uniqueIdentifier", posixUser.getUniqueIdentifier());

    scimUser.setMeta(meta);

    Name name = new Name();
    name.setFamilyName(posixUser.getSurName());
    name.setGivenName(posixUser.getGivenName());

    // scimUser.setPassword(posixUser.getUserPassword());

    return scimUser;
  }

  /**
   * Links the users represented in the JSON serialized list of SCIM user's via LDAP locally.
   * 
   * @param scimUsers the SCIM user's to link
   * @return a list of JSON serialized SCIM user's containing the modification information
   */
  public List<ScimUser> linkUsers(List<ScimUser> scimUsers) {

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
      linkedUsers.add(scimUserFromPosixUser(primaryPosixUser));

      // store uid number and home directory for unlinking
      redisClient.setUidNumber(primaryPosixUser.getUniqueIdentifier(),
          primaryPosixUser.getUidNumber());
      redisClient.setUserHome(primaryPosixUser.getUniqueIdentifier(),
          primaryPosixUser.getHomeDirectory());

      for (ScimUser user : scimUsers) {
        log.debug("LDAP lookup for user {}", user.getUserName());
        PosixUser posixUser = ldapClient.getPosixUser(user.getUserName());
        log.debug("User {}", posixUser.toString());

        linkedUsers.add(scimUserFromPosixUser(posixUser));

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
        unlinkedUsers.add(scimUserFromPosixUser(updatedUser));
      }
    }

    return unlinkedUsers;
  }

  private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  private static String generateString(Random rng, String characters, int length) {
    char[] text = new char[length];
    for (int i = 0; i < length; i++) {
      text[i] = characters.charAt(rng.nextInt(characters.length()));
    }
    return new String(text);
  }
}
