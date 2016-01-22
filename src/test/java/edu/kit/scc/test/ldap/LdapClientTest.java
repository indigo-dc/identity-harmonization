/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.test.ldap;

import static org.junit.Assert.assertNotNull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.kit.scc.Application;
import edu.kit.scc.dto.GroupDTO;
import edu.kit.scc.dto.UserDTO;
import edu.kit.scc.ldap.LdapClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LdapClientTest {

	private static final Logger log = LoggerFactory.getLogger(LdapClientTest.class);

	@Autowired
	private LdapClient ldapClient;

	@Test
	public void a_createLdapGroupTest() {
		ldapClient.createGroup("newGroup", 3333);
	}

	@Test
	public void b_createLdapUserTest() {
		String cn = "newUser";
		String sn = "newUser";
		String description = "new posix user";
		String homeDirectory = "/home/newUser";
		String uid = "newUser";
		int uidNumber = 6001;
		int gidNumber = 3333;

		ldapClient.createUser(uid, cn, sn, uidNumber, gidNumber, homeDirectory, description);
	}

	@Test
	public void c_getLdapUserTest() {
		UserDTO user = ldapClient.getLdapUser("newUser");

		assertNotNull(user);

		log.debug(user.toString());
	}

	@Test
	public void d_updateUserTest() {
		String cn = "newUser";
		String sn = "newUser";
		String description = "new posix user (update)";
		String homeDirectory = "/home/newUser";
		String uid = "newUser";
		int uidNumber = 6001;
		int gidNumber = 3333;

		ldapClient.updateUser(uid, cn, sn, uidNumber, gidNumber, homeDirectory, description);
	}

	@Test
	public void e_addUserToGroupTest() {
		ldapClient.addGroupMember("newGroup", "newUser");
	}

	@Test
	public void f_getLdapGroupTest() {
		GroupDTO group = ldapClient.getLdapGroup("newGroup");

		assertNotNull(group);

		log.debug(group.toString());
	}

	@Test
	public void g_deleteLdapUserTest() {
		ldapClient.deleteUser("newUser");
	}

	@Test
	public void h_deleteLdapGroupTest() {
		ldapClient.deleteGroup("newGroup");
	}

	@Test
	public void getLdapGroupsTest() {
		ldapClient.getLdapGroups();
	}

	@Test
	public void getLdapUsersTest() {
		ldapClient.getLdapUsers();
	}

	@Test
	public void getNewGidNumber() {
		log.debug("{}", ldapClient.generateGroupId());
	}
}
