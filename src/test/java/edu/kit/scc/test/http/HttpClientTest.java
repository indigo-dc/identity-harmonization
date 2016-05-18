/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.test.http;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import edu.kit.scc.IdentityHarmonizationService;
import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IdentityHarmonizationService.class)
public class HttpClientTest {
  private static final Logger log = LoggerFactory.getLogger(HttpClientTest.class);

  @Autowired
  private HttpClient httpClient;

  protected static String httpUrl = "http://api.rottentomatoes.com/api/public/v1.0";
  protected static String httpsUrl = "https://launchlibrary.net/1.2/agency/NASA";

  @Test
  public void makeHttpPostRequestTest() {
    HttpResponse httpResponse = httpClient.makeHttpPostRequest("hello", httpUrl);
    assertNull(httpResponse);
  }

  @Test
  public void makeHttpGetRequestTest() {
    HttpResponse httpResponse = httpClient.makeHttpGetRequest(httpUrl);
    assertNotNull(httpResponse);

    log.debug(httpResponse.toString());
  }

  @Test
  public void makeHttpsPostRequestTest() {

    HttpResponse httpResponse = httpClient.makeHttpsPostRequest("hello", httpsUrl);
    assertNull(httpResponse);
  }

  @Test
  public void makeHttpsGetRequestTest() {

    HttpResponse httpResponse = httpClient.makeHttpsGetRequest(httpsUrl);
    assertNotNull(httpResponse);

    log.debug(httpResponse.toString());
  }
}
