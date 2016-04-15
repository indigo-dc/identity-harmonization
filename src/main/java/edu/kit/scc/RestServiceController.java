/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import edu.kit.scc.scim.ScimUser;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/rest")
public class RestServiceController {

  private static Logger log = LoggerFactory.getLogger(RestServiceController.class);

  @Value("${rest.serviceUsername}")
  private String restUser;

  @Value("${rest.servicePassword}")
  private String restPassword;

  @Autowired
  private IdentityHarmonizer identityHarmonizer;

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
