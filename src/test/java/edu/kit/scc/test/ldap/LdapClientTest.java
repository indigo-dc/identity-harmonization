/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.test.ldap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.kit.scc.Application;
import edu.kit.scc.ldap.LdapClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class LdapClientTest {

	@Autowired
	private LdapClient ldapClient;

	@Test
	public void getLdapGroupsTest() {
		ldapClient.getLdapGroups();
	}

	@Test
	public void getLdapUsersTest() {
		ldapClient.getLdapUsers();
	}

	@Test
	public void createLdapUserTest() {
		String cn = "newPosixUser1";
		String sn = "newPosixUser1";
		String description = "new posix user";
		String homeDirectory = "/home/newPosixUser1";
		String uid = "newPosixUser1";
		int uidNumber = 6001;
		int gidNumber = 2222;

		ldapClient.createUser(uid, cn, sn, uidNumber, gidNumber, homeDirectory, description);
	}
}
