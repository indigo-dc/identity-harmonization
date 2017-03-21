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
import edu.kit.scc.scim.ScimUser.Meta;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;


@RestController
public class ScimServiceController {

  @Value("${server.location}")
  private String location;

  private static final Logger log = LoggerFactory.getLogger(ScimServiceController.class);

  /**
   * Get a user.
   * 
   * @param request the {@link HttpServletRequest}
   * @param id the user's id
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Users/{id}", method = RequestMethod.GET,
      produces = "application/scim+json")
  public ResponseEntity<?> getScimUser(HttpServletRequest request, @PathVariable String id) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    try {
      responseHeaders.setLocation(new URI(location + "/Users/" + id));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    ScimUser scimUser = new ScimUser();
    scimUser.setUserName("username");
    scimUser.setId(id);
    scimUser.setExternalId(UUID.randomUUID().toString());
    scimUser.setActive(true);

    String time = DateTime.now().toString();
    Meta metadata = new Meta();
    metadata.put("resourceType", "User");
    metadata.put("created", time);
    metadata.put("lastModified", time);
    metadata.put("location", responseHeaders.getLocation().toString());

    try {
      MessageDigest hash = MessageDigest.getInstance("MD5");
      hash.update(scimUser.toString().getBytes());

      String etag = Base64.getEncoder().encodeToString(hash.digest());
      metadata.put("version", "W/" + "\"" + etag + "\"");

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    scimUser.setMeta(metadata);
    responseHeaders.setETag(metadata.get("version"));
    return new ResponseEntity<>(scimUser, responseHeaders, HttpStatus.OK);
  }

  /**
   * Create a user via HTTP POST.
   * 
   * @param request the {@link HttpServletRequest}
   * @param user the {@link ScimUser}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Users", method = RequestMethod.POST, consumes = "application/scim+json",
      produces = "application/scim+json")
  public ResponseEntity<?> postScimUser(HttpServletRequest request, @RequestBody ScimUser user) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    log.debug("try to createt user {} from {}", user, request.getRemoteHost());

    ScimUser scimUser = new ScimUser();
    
    try {
      responseHeaders.setLocation(new URI(location + "/Users/" + scimUser.getId()));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    scimUser.getMeta().put("location", responseHeaders.getLocation().toString());

    try {
      MessageDigest hash = MessageDigest.getInstance("MD5");
      hash.update(scimUser.toString().getBytes());

      String etag = Base64.getEncoder().encodeToString(hash.digest());
      scimUser.getMeta().put("version", "W/" + "\"" + etag + "\"");

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    responseHeaders.setETag(scimUser.getMeta().get("version"));
    return new ResponseEntity<>(scimUser, responseHeaders, HttpStatus.CREATED);
  }

  /**
   * Replace a user via HTTP PUT.
   * 
   * @param request the {@link HttpServletRequest}
   * @param id the user's id
   * @param user the {@link ScimUser}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Users/{id}", method = RequestMethod.PUT,
      consumes = "application/scim+json", produces = "application/scim+json")
  public ResponseEntity<?> putScimUser(HttpServletRequest request, @PathVariable String id,
      @RequestBody ScimUser user) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    ScimUser scimUser = new ScimUser();
    scimUser.setUserName(user.getUserName());
    scimUser.setId(UUID.randomUUID().toString());
    scimUser.setExternalId(user.getExternalId());
    scimUser.setActive(true);

    try {
      responseHeaders.setLocation(new URI(location + "/Users/" + scimUser.getId()));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    String time = DateTime.now().toString();
    Meta metadata = new Meta();
    metadata.put("resourceType", "User");
    metadata.put("created", time);
    metadata.put("lastModified", time);
    metadata.put("location", responseHeaders.getLocation().toString());

    try {
      MessageDigest hash = MessageDigest.getInstance("MD5");
      hash.update(scimUser.toString().getBytes());

      String etag = Base64.getEncoder().encodeToString(hash.digest());
      metadata.put("version", "W/" + "\"" + etag + "\"");

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    scimUser.setMeta(metadata);
    responseHeaders.setETag(metadata.get("version"));
    return new ResponseEntity<>(scimUser, responseHeaders, HttpStatus.OK);
  }

  /**
   * Delete a user.
   * 
   * @param request the {@link HttpServletRequest}
   * @param id the user's id
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Users/{id}", method = RequestMethod.DELETE,
      produces = "application/scim+json")
  public ResponseEntity<?> deleteScimUser(HttpServletRequest request, @PathVariable String id) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NO_CONTENT);
  }

  /**
   * Update a user via HTTP PATCH.
   * <p>
   * HTTP PATCH is an OPTIONAL server function that enables clients to update one or more attributes
   * of a SCIM resource using a sequence of operations to "add", "remove", or "replace" values.
   * </p>
   * 
   * @param request the {@link HttpServletRequest}
   * @param id the user's id
   * @param user the {@link ScimUser}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Users/{id}", method = RequestMethod.PATCH,
      consumes = "application/scim+json", produces = "application/scim+json")
  public ResponseEntity<?> patchScimUser(HttpServletRequest request, @PathVariable String id,
      @RequestBody ScimUser user) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * Get a group.
   * 
   * @param request the {@link HttpServletRequest}
   * @param id the group's id
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Groups/{id}", method = RequestMethod.GET,
      produces = "application/scim+json")
  public ResponseEntity<?> getScimGroup(HttpServletRequest request, @PathVariable String id) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    try {
      responseHeaders.setLocation(new URI(location + "/Groups/" + id));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    ScimGroup scimGroup = new ScimGroup();
    scimGroup.setSchemas(Arrays.asList(ScimGroup.GROUP_SCHEMA_2_0));
    scimGroup.setDisplayName("default");
    scimGroup.setId(id);
    scimGroup.setExternalId(UUID.randomUUID().toString());

    String time = DateTime.now().toString();
    HashMap<String, String> metadata = new HashMap<String, String>();
    metadata.put("resourceType", "User");
    metadata.put("created", time);
    metadata.put("lastModified", time);
    metadata.put("location", responseHeaders.getLocation().toString());

    try {
      MessageDigest hash = MessageDigest.getInstance("MD5");
      hash.update(scimGroup.toString().getBytes());

      String etag = Base64.getEncoder().encodeToString(hash.digest());
      metadata.put("version", "W/" + "\"" + etag + "\"");

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    scimGroup.setMetadata(metadata);
    responseHeaders.setETag(metadata.get("version"));
    return new ResponseEntity<>(scimGroup, responseHeaders, HttpStatus.OK);
  }

  /**
   * Create a group via HTTP POST.
   * 
   * @param request the {@link HttpServletRequest}
   * @param group the {@link ScimGroup}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Groups", method = RequestMethod.POST, consumes = "application/scim+json",
      produces = "application/scim+json")
  public ResponseEntity<?> postScimGroup(HttpServletRequest request, @RequestBody ScimGroup group) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    ScimGroup scimGroup = new ScimGroup();
    scimGroup.setDisplayName(group.getDisplayName());
    scimGroup.setId(UUID.randomUUID().toString());
    scimGroup.setExternalId(group.getExternalId());

    try {
      responseHeaders.setLocation(new URI(location + "/Groups/" + scimGroup.getId()));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    String time = DateTime.now().toString();
    HashMap<String, String> metadata = new HashMap<String, String>();
    metadata.put("resourceType", "User");
    metadata.put("created", time);
    metadata.put("lastModified", time);
    metadata.put("location", responseHeaders.getLocation().toString());

    try {
      MessageDigest hash = MessageDigest.getInstance("MD5");
      hash.update(scimGroup.toString().getBytes());

      String etag = Base64.getEncoder().encodeToString(hash.digest());
      metadata.put("version", "W/" + "\"" + etag + "\"");

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    scimGroup.setMetadata(metadata);
    responseHeaders.setETag(metadata.get("version"));
    return new ResponseEntity<>(scimGroup, responseHeaders, HttpStatus.CREATED);
  }

  /**
   * Replace a group via HTTP PUT.
   * 
   * @param request the {@link HttpServletRequest}
   * @param id the group's id
   * @param group the {@link ScimGroup}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Groups/{id}", method = RequestMethod.PUT,
      consumes = "application/scim+json", produces = "application/scim+json")
  public ResponseEntity<?> putScimGroup(HttpServletRequest request, @PathVariable String id,
      @RequestBody ScimGroup group) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    ScimGroup scimGroup = new ScimGroup();
    scimGroup.setDisplayName(group.getDisplayName());
    scimGroup.setId(id);
    scimGroup.setExternalId(group.getExternalId());

    try {
      responseHeaders.setLocation(new URI(location + "/Groups/" + scimGroup.getId()));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    String time = DateTime.now().toString();
    HashMap<String, String> metadata = new HashMap<String, String>();
    metadata.put("resourceType", "User");
    metadata.put("created", time);
    metadata.put("lastModified", time);
    metadata.put("location", responseHeaders.getLocation().toString());

    try {
      MessageDigest hash = MessageDigest.getInstance("MD5");
      hash.update(scimGroup.toString().getBytes());

      String etag = Base64.getEncoder().encodeToString(hash.digest());
      metadata.put("version", "W/" + "\"" + etag + "\"");

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    scimGroup.setMetadata(metadata);
    responseHeaders.setETag(metadata.get("version"));
    return new ResponseEntity<>(scimGroup, responseHeaders, HttpStatus.OK);
  }

  /**
   * Delete a group.
   * 
   * @param request the {@link HttpServletRequest}
   * @param id the group's id
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Groups/{id}", method = RequestMethod.DELETE,
      produces = "application/scim+json")
  public ResponseEntity<?> deleteScimGroup(HttpServletRequest request, @PathVariable String id) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NO_CONTENT);
  }

  /**
   * Update a group via HTTP PATCH.
   * <p>
   * HTTP PATCH is an OPTIONAL server function that enables clients to update one or more attributes
   * of a SCIM resource using a sequence of operations to "add", "remove", or "replace" values.
   * </p>
   * 
   * @param request the {@link HttpServletRequest}
   * @param id the group's id
   * @param group the {@link ScimGroup}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Groups/{id}", method = RequestMethod.PATCH,
      consumes = "application/scim+json", produces = "application/scim+json")
  public ResponseEntity<?> patchScimGroup(HttpServletRequest request, @PathVariable String id,
      @RequestBody ScimGroup group) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * CRUD operations for SCIM "/Me" endpoint.
   * <p>
   * A client MAY use a URL of the form "/Me" as a URI alias for the User or other resource
   * associated with the currently authenticated subject for any SCIM operation.
   * </p>
   * 
   * @param request the {@link HttpServletRequest}
   * @param user the {@link ScimUser}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Me",
      method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
          RequestMethod.DELETE},
      consumes = "application/scim+json", produces = "application/scim+json")
  public ResponseEntity<?> doMe(HttpServletRequest request, @RequestBody ScimUser user) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * Get SCIM service provider configuration.
   * <p>
   * An HTTP GET to this endpoint will return a JSON structure that describes the SCIM specification
   * features available on a service provider.
   * </p>
   * 
   * @param request the {@link HttpServletRequest}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/ServiceProviderConfig", method = RequestMethod.GET,
      produces = "application/scim+json")
  public ResponseEntity<?> getServiceProviderConfig(HttpServletRequest request) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * Get SCIM resource types supported.
   * <p>
   * An HTTP GET to this endpoint is used to discover the types of resources available on a SCIM
   * service provider (e.g., Users and Groups).
   * </p>
   * 
   * @param request the {@link HttpServletRequest}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/ResourceTypes", method = RequestMethod.GET,
      produces = "application/scim+json")
  public ResponseEntity<?> getResourceTypes(HttpServletRequest request) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * Get SCIM schemas supported.
   * <p>
   * An HTTP GET to this endpoint is used to retrieve information about resource schemas supported
   * by a SCIM service provider.
   * </p>
   * 
   * @param request the {@link HttpServletRequest}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Schemas", method = RequestMethod.GET, produces = "application/scim+json")
  public ResponseEntity<?> getSchemas(HttpServletRequest request) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * Do SCIM bulk operation.
   * <p>
   * The SCIM bulk operation is an optional server feature that enables clients to send a
   * potentially large collection of resource operations in a single request. Support for bulk
   * requests can be discovered by querying the service provider configuration (see Section 4). The
   * body of a bulk operation contains a set of HTTP resource operations using one of the HTTP
   * methods supported by the API, i.e., POST, PUT, PATCH, or DELETE.
   * </p>
   * 
   * @param request the {@link HttpServletRequest}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/Bulk", method = RequestMethod.POST, consumes = "application/scim+json",
      produces = "application/scim+json")
  public ResponseEntity<?> doBulk(HttpServletRequest request) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * Do SCIM search operation.
   * <p>
   * Clients MAY execute queries without passing parameters on the URL by using the HTTP POST verb
   * combined with the "/.search" path extension. The inclusion of "/.search" on the end of a valid
   * SCIM endpoint SHALL be used to indicate that the HTTP POST verb is intended to be a query
   * operation.
   * </p>
   * 
   * @param request the {@link HttpServletRequest}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(path = "/.search", method = RequestMethod.POST,
      consumes = "application/scim+json", produces = "application/scim+json")
  public ResponseEntity<?> doSearch(HttpServletRequest request) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "scim+json"));

    return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_IMPLEMENTED);
  }
}
