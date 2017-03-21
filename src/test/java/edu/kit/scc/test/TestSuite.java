/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.test;

import edu.kit.scc.test.http.HttpClientTest;
import edu.kit.scc.test.ldap.LdapClientTest;
import edu.kit.scc.test.redis.RedisClientTest;
import edu.kit.scc.test.rest.RestServiceTest;
import edu.kit.scc.test.scim.ScimModelTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({CreatePosixUserTest.class, LdapClientTest.class, RedisClientTest.class,
    HttpClientTest.class, RestServiceTest.class, ScimModelTest.class})
public class TestSuite {

}
