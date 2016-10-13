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
import edu.kit.scc.ldap.IdentityHarmonizer;
import edu.kit.scc.ldap.LdapClient;
import edu.kit.scc.ldap.PosixUser;
import edu.kit.scc.ldap.PosixUserDao;
import edu.kit.scc.scim.ScimError;
import edu.kit.scc.scim.ScimUser;
import edu.kit.scc.scim.ScimUser.Meta;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class RestServiceController {

  private static Logger log = LoggerFactory.getLogger(RestServiceController.class);

  @Value("${server.port}")
  private int port;

  @Value("${rest.serviceUsername}")
  private String restUser;

  @Value("${rest.servicePassword}")
  private String restPassword;

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
  private IdentityHarmonizer identityHarmonizer;

  @Autowired
  private HttpClient httpClient;

  @Autowired
  private LdapClient ldapClient;

  @Autowired
  private PosixUserDao posixUserDao;

  /**
   * SCIM get user endpoint.
   * 
   * @param authorizationHeader basic or bearer HTTP authorization
   * @return the {@link ScimUser}
   */
  @RequestMapping(path = "scim/Users", method = RequestMethod.POST,
      consumes = {"application/scim+json"}, produces = "application/json")
  public ResponseEntity<?> createScimUser(
      @RequestHeader("Authorization") String authorizationHeader,
      @RequestHeader("Content-Type") String contentType, @RequestBody ScimUser scimUser,
      HttpServletRequest request) {

    if (!verifyAuthorization(authorizationHeader)) {
      return new ResponseEntity<String>("REST Service Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    ScimUser createdUser = posixUserDao.createUser("ou=users", scimUser);

    if (createdUser == null) {
      ScimError scimError = new ScimError();
      scimError.setSchemas(Arrays.asList(ScimError.ERROR_SCHEMA));
      scimError.setStatus(HttpStatus.CONFLICT.toString() + " (Conflict)");
      scimError.setDetail("User already exists");
      return new ResponseEntity<>(scimError, responseHeaders, HttpStatus.CONFLICT);
    }

    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    try {
      String host = InetAddress.getLocalHost().getHostAddress();
      URI uri = new URI(
          "https://" + host + ":" + String.valueOf(port) + path + "/" + createdUser.getUserName());
      log.debug(uri.toString());
      responseHeaders.setLocation(uri);
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String time = DateTime.now().toString();
    if (createdUser.getMeta() == null) {
      createdUser.setMeta(new Meta());
    }
    createdUser.getMeta().put("resourceType", "User");
    createdUser.getMeta().put("created", time);
    createdUser.getMeta().put("lastModified", time);
    createdUser.getMeta().put("location", responseHeaders.getLocation().toString());

    return new ResponseEntity<>(createdUser, responseHeaders, HttpStatus.CREATED);
  }

  /**
   * IAM SCIM get user endpoint.
   * 
   * @param authorizationHeader basic or bearer HTTP authorization
   * @return the {@link ScimUser}
   */
  @RequestMapping(path = "IAM/Users/{name}", method = RequestMethod.GET,
      produces = "application/json")
  public ResponseEntity<?> getIamScimUser(
      @RequestHeader("Authorization") String authorizationHeader, @PathVariable String name) {

    if (!verifyAuthorization(authorizationHeader)) {
      return new ResponseEntity<String>("REST Service Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    // get access token
    String body = "grant_type=client_credentials&client_id=" + oidcClientId + "&client_secret="
        + oidcClientSecret + "&scope=scim:read";

    HttpResponse response = httpClient.makeHttpsPostRequest(body, oidcTokenEndpoint);
    JSONObject json = new JSONObject(response.getResponse());

    String accessToken = json.getString("access_token");

    // get SCIM user
    HttpResponse scimResponse =
        httpClient.makeHttpsGetRequest(null, accessToken, scimUsersEndpoint);

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
   * SCIM get user endpoint.
   * 
   * @param authorizationHeader basic or bearer HTTP authorization
   * @return the LDAP user
   */
  @RequestMapping(path = "scim/Users/{name}", method = RequestMethod.GET,
      produces = "application/json")
  public ResponseEntity<?> getScimUser(@RequestHeader("Authorization") String authorizationHeader,
      @PathVariable String name) {

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    if (!verifyAuthorization(authorizationHeader)) {
      return new ResponseEntity<String>("REST Service Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    PosixUser posixUser = ldapClient.getPosixUser(name);

    if (posixUser != null) {
      ScimUser scimUser = posixUserDao.scimUserFromPosixUser(posixUser);

      return new ResponseEntity<>(scimUser, responseHeaders, HttpStatus.OK);
    }

    ScimError scimError = new ScimError();
    scimError.setSchemas(Arrays.asList(ScimError.ERROR_SCHEMA));
    scimError.setStatus(HttpStatus.NOT_FOUND.toString() + " (Not Found)");
    scimError.setDetail("User not found");

    return new ResponseEntity<>(scimError, responseHeaders, HttpStatus.NOT_FOUND);
  }

  /**
   * LDAP get user endpoint.
   * 
   * @param authorizationHeader basic or bearer HTTP authorization
   * @return the LDAP user
   */
  @RequestMapping(path = "ldap/Users/{name}", method = RequestMethod.GET,
      produces = "application/json")
  public ResponseEntity<?> getLdapUser(@RequestHeader("Authorization") String authorizationHeader,
      @PathVariable String name) {

    if (!verifyAuthorization(authorizationHeader)) {
      return new ResponseEntity<String>("REST Service Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    PosixUser posixUser = ldapClient.getPosixUser(name);

    return new ResponseEntity<>(posixUser, HttpStatus.OK);
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

  /**
   * Verifies the authorization.
   * 
   * @param basicAuthorization the authorization to verify
   * @return true if the authorization could be verified, false otherwise
   */
  public boolean verifyAuthorization(String basicAuthorization) {
    String encodedCredentials = basicAuthorization.split(" ")[1];
    String[] credentials = new String(Base64.decodeBase64(encodedCredentials)).split(":");

    if (credentials[0].equals(restUser) && credentials[1].equals(restPassword)) {
      return true;
    }
    log.error("Wrong credentials {} {}", credentials[0], credentials[1]);
    return false;
  }
}
