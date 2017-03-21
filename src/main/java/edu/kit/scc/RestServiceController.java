/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;
import edu.kit.scc.scim.ScimUser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

@RestController
public class RestServiceController {

  private static Logger log = LoggerFactory.getLogger(RestServiceController.class);

  @Value("${oidc.token}")
  private String oidcTokenEndpoint;

  @Value("${oidc.clientid}")
  private String oidcClientId;

  @Value("${oidc.clientsecret}")
  private String oidcClientSecret;

  @Value("${scim.users}")
  private String scimUsersEndpoint;

  @Value("${scim.groups}")
  private String scimGroupsEndpoint;

  @Autowired
  private IdentityManager identityHarmonizer;

  @Autowired
  private HttpClient httpClient;

  /**
   * IAM SCIM get user endpoint.
   * 
   * @return the {@link ScimUser}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "IAM/Users/{name}", method = RequestMethod.GET,
      produces = "application/json")
  public ResponseEntity<?> getIamScimUser(@PathVariable String name) {

    // get access token
    String body = "grant_type=client_credentials&client_id=" + oidcClientId + "&client_secret="
        + oidcClientSecret + "&scope=scim:read";

    HttpResponse response = httpClient.makeHttpsPostRequest(body, oidcTokenEndpoint);
    JSONObject json = new JSONObject(response.getResponse());

    String accessToken = json.getString("access_token");
    log.debug("Access token {}", accessToken);

    // get SCIM user
    HttpResponse scimResponse =
        httpClient.makeHttpsGetRequest(accessToken, null, scimUsersEndpoint);

    JSONObject scimUsers = new JSONObject(scimResponse.getResponse());
    JSONArray scimResources = scimUsers.getJSONArray("Resources");

    JSONObject returnObj = new JSONObject();

    for (int i = 0; i < scimResources.length(); i++) {
      JSONObject obj = scimResources.getJSONObject(i);

      if (obj.getString("displayName").equals(name)) {
        returnObj = obj;
        break;
      } else {
        returnObj.put("error", "not found");
      }
    }

    return new ResponseEntity<>(returnObj.toString(), HttpStatus.OK);
  }

  /**
   * Linking endpoint.
   * 
   * @param scimUsers a JSON serialized list of SCIM users for linking
   * @param response the HttpServletResponse
   * @return a JSON serialized list of SCIM users containing the modifications done
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/link", method = RequestMethod.POST, consumes = "application/scim+json",
      produces = "application/scim+json")
  public ResponseEntity<?> linkUsers(@RequestBody List<ScimUser> scimUsers,
      HttpServletResponse response) {

    log.debug("Request body {}", scimUsers);

    List<ScimUser> modifiedUsers = scimUsers;
    if (!modifiedUsers.isEmpty()) {
      return new ResponseEntity<List<ScimUser>>(modifiedUsers, HttpStatus.OK);
    }

    return new ResponseEntity<String>("Conflicting information", HttpStatus.CONFLICT);
  }

  /**
   * Unlinking endpoint.
   * 
   * @param scimUsers a JSON serialized list of SCIM users for unlinking
   * @param response the HttpServletResponse
   * @return A JSON serialized list of SCIM users containing the local user information.
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/unlink", method = RequestMethod.POST, consumes = "application/scim+json",
      produces = "application/scim+json")
  public ResponseEntity<?> unlinkUsers(@RequestBody List<ScimUser> scimUsers,
      HttpServletResponse response) {

    log.debug("Request body {}", scimUsers);

    List<ScimUser> modifiedUsers = identityHarmonizer.unlinkUsers(scimUsers);
    if (!modifiedUsers.isEmpty()) {
      return new ResponseEntity<List<ScimUser>>(modifiedUsers, HttpStatus.OK);
    }

    return new ResponseEntity<String>("Conflicting information", HttpStatus.CONFLICT);
  }
}
