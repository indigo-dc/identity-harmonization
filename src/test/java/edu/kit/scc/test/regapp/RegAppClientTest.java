/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.test.regapp;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.kit.scc.Application;
import edu.kit.scc.http.HttpResponse;
import edu.kit.scc.regapp.RegAppClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class RegAppClientTest {

	private static final Logger log = LoggerFactory.getLogger(RegAppClientTest.class);

	@Autowired
	private RegAppClient regAppClient;

	private String regId = "";
	private String credentials = "";

	@Test
	public void authenticateTest() {
		assertTrue(regAppClient.authenticate(regId, credentials));
	}

	@Test
	public void attributeQueryTest() {
		HttpResponse response = regAppClient.attributeQuery(regId);
		assertNotNull(response);
		log.debug(response.toString());
	}

}
