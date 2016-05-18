/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import edu.kit.scc.scim.ScimGroup;
import edu.kit.scc.scim.ScimUser;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

@RestController
public class RestServiceController {

  private static Logger log = LoggerFactory.getLogger(RestServiceController.class);

  @Value("${rest.serviceUsername}")
  private String restUser;

  @Value("${rest.servicePassword}")
  private String restPassword;

  @Autowired
  private UserGenerator userGenerator;

  /**
   * SCIM create user endpoint.
   * 
   * @param authorizationHeader basic or bearer HTTP authorization
   * @param scimUser (optional) the SCIM user to create
   * @return the created {@link ScimUser}
   */
  @RequestMapping(path = "/Users", method = RequestMethod.POST, consumes = "application/scim+json",
      produces = "application/scim+json")
  public ResponseEntity<?> createUser(@RequestHeader("Authorization") String authorizationHeader,
      @RequestBody(required = false) ScimUser scimUser) {

    ScimUser user = userGenerator.createUser(scimUser);

    if (user != null) {
      return new ResponseEntity<ScimUser>(user, HttpStatus.CREATED);
    }
    return new ResponseEntity<ScimUser>(scimUser, HttpStatus.BAD_REQUEST);
  }

  /**
   * SCIM get user endpoint.
   * 
   * @param authorizationHeader basic or bearer HTTP authorization
   * @param id (optional) the SCIM user's id
   * @return the {@link ScimUser}
   */
  @RequestMapping(path = "/Users/{id}", method = RequestMethod.GET,
      produces = "application/scim+json")
  public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authorizationHeader,
      @PathVariable String id) {

    ScimUser scimUser = new ScimUser();
    scimUser.setSchemas(Arrays.asList(ScimUser.USER_SCHEMA_2_0));
    scimUser.setActive(true);
    scimUser.setUserName(id);
    scimUser.setId(id);
    scimUser.setExternalId(id);

    return new ResponseEntity<ScimUser>(scimUser, HttpStatus.OK);
  }

  /**
   * SCIM get group endpoint.
   * 
   * @param authorizationHeader basic or bearer HTTP authorization
   * @param id (optional) the SCIM group's id
   * @return the {@link ScimGroup}
   */
  @RequestMapping(path = "/Groups/{id}", method = RequestMethod.GET,
      produces = "application/scim+json")
  public ResponseEntity<?> getGroup(@RequestHeader("Authorization") String authorizationHeader,
      @PathVariable String id) {

    ScimUser scimUser = new ScimUser();
    scimUser.setSchemas(Arrays.asList(ScimUser.USER_SCHEMA_2_0));
    scimUser.setActive(true);
    scimUser.setUserName(id);
    scimUser.setId(id);
    scimUser.setExternalId(UUID.randomUUID().toString());

    return new ResponseEntity<ScimUser>(scimUser, HttpStatus.OK);
  }

  /**
   * Linking endpoint.
   * 
   * @param basicAuthorization authorization header value
   * @param scimUsers a JSON serialized list of SCIM users for linking
   * @param response the HttpServletResponse
   * @return a JSON serialized list of SCIM users containing the modifications done
   */
  @RequestMapping(path = "/link", method = RequestMethod.POST, consumes = "application/scim+json",
      produces = "application/scim+json")
  public ResponseEntity<?> linkUsers(@RequestHeader("Authorization") String basicAuthorization,
      @RequestBody List<ScimUser> scimUsers, HttpServletResponse response) {

    if (!verifyAuthorization(basicAuthorization)) {
      return new ResponseEntity<String>("REST Service Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    log.debug("Request body {}", scimUsers);

    IdentityHarmonizer identityHarmonizer = new IdentityHarmonizer();
    List<ScimUser> modifiedUsers = identityHarmonizer.harmonizeIdentities(scimUsers);
    if (!modifiedUsers.isEmpty()) {
      return new ResponseEntity<List<ScimUser>>(modifiedUsers, HttpStatus.OK);
    }

    return new ResponseEntity<String>("Conflicting information", HttpStatus.CONFLICT);
  }

  /**
   * Unlinking endpoint.
   * 
   * @param basicAuthorization authorization header value
   * @param scimUsers a JSON serialized list of SCIM users for unlinking
   * @param response the HttpServletResponse
   * @return A JSON serialized list of SCIM users containing the local user information.
   */
  @RequestMapping(path = "/unlink", method = RequestMethod.POST, consumes = "application/scim+json",
      produces = "application/scim+json")
  public ResponseEntity<?> unlinkUsers(@RequestHeader("Authorization") String basicAuthorization,
      @RequestBody List<ScimUser> scimUsers, HttpServletResponse response) {

    if (!verifyAuthorization(basicAuthorization)) {
      return new ResponseEntity<String>("REST Service Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    log.debug("Request body {}", scimUsers);

    IdentityHarmonizer identityHarmonizer = new IdentityHarmonizer();
    List<ScimUser> modifiedUsers = identityHarmonizer.unlinkUsers(scimUsers);
    if (!modifiedUsers.isEmpty()) {
      return new ResponseEntity<List<ScimUser>>(modifiedUsers, HttpStatus.OK);
    }

    return new ResponseEntity<String>("Conflicting information", HttpStatus.CONFLICT);
  }

  private boolean verifyAuthorization(String basicAuthorization) {
    String encodedCredentials = basicAuthorization.split(" ")[1];
    String[] credentials = new String(Base64.decodeBase64(encodedCredentials)).split(":");

    if (credentials[0].equals(restUser) && credentials[1].equals(restPassword)) {
      return true;
    }
    log.error("Wrong credentials {} {}", credentials[0], credentials[1]);
    return false;
  }
}
