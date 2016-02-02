/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.test.scim;

import static org.junit.Assert.assertNotNull;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.kit.scc.Application;
import edu.kit.scc.scim.ScimClient;
import edu.kit.scc.scim.ScimUser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class ScimClientTest {

	@Autowired
	private ScimClient scimClient;

	private static final Logger log = LoggerFactory.getLogger(ScimClientTest.class);

	@Test
	public void getUsersTest() {
		JSONObject jsonUsers = scimClient.getUsers();
		assertNotNull(jsonUsers);

		log.debug(jsonUsers.toString());
	}

	@Test
	public void getGroupsTest() {
		JSONObject jsonGroups = scimClient.getGroups();
		assertNotNull(jsonGroups);

		log.debug(jsonGroups.toString());
	}

	@Test
	public void getUserTest() {
		JSONObject jsonUser = scimClient.getUser("user1");
		assertNotNull(jsonUser);

		log.debug(jsonUser.toString());
	}

	@Test
	public void getScimUserTest() {
		ScimUser scimUser = scimClient.getScimUser("user1");
		assertNotNull(scimUser);

		log.debug(scimUser.toString());
	}
}
