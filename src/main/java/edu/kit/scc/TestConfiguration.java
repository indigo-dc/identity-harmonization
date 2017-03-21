/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import redis.embedded.RedisServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Profile("test")
public class TestConfiguration {

  private static final Logger log = LoggerFactory.getLogger(TestConfiguration.class);

  @Value("${ldap.port}")
  private int ldapPort;

  @Value("${spring.redis.port}")
  private int redisPort;

  private static InMemoryDirectoryServer ds;
  private static RedisServer redisServer;

  /**
   * Initializes in-memory LDAP and redis.
   * 
   * @throws LDAPException in case in-memory LDAP couldn't be created
   * @throws IOException in case in-memory redis couldn't be created
   */
  @PostConstruct
  public void init() throws LDAPException, IOException {
    log.debug("Set-up in-memory LDAP...");
    // set-up in-memory LDAP
    InMemoryDirectoryServerConfig config =
        new InMemoryDirectoryServerConfig("dc=springframework,dc=org");

    // schema config only necessary if the standard
    // schema provided by the library doesn't suit your needs
    config.setSchema(null);

    // listener config only necessary if you want to make sure that the
    // server listens on port 33389, otherwise a free random port will
    // be picked at runtime - which might be even better for tests btw
    config.addAdditionalBindCredentials("cn=admin", "password");
    config.setListenerConfigs(
        new InMemoryListenerConfig("myListener", null, ldapPort, null, null, null));

    ds = new InMemoryDirectoryServer(config);

    try {
      ds.startListening();
    } catch (LDAPException ex) {
      log.warn("LDAP server already running?");
    }

    // import your test data from ldif files
    Resource resource = new ClassPathResource("test-server.ldif");
    BufferedReader br = null;
    LDIFReader reader = null;
    try {
      br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
      reader = new LDIFReader(br);
      ds.importFromLDIF(true, reader);
    } catch (IOException ex) {
      log.error("Could not read test data from ldif file");
    } finally {
      if (br != null) {
        br.close();
      }
      if (reader != null) {
        reader.close();
      }
    }
    log.debug("Set-up in-memory redis...");
    redisServer = new RedisServer(redisPort);
    try {
      redisServer.start();
    } catch (Exception ex) {
      log.warn("Redis servier already running?");
    }
  }

  /**
   * Cleans up in-memory LDAP and redis.
   * 
   */
  @PreDestroy
  public void cleanUp() {
    if (ds != null) {
      log.debug("Shutdown in-memory LDAP");
      ds.shutDown(true);
    }

    if (redisServer != null) {
      log.debug("Shutdown in-memory redis");
      redisServer.stop();
    }
  }

}
