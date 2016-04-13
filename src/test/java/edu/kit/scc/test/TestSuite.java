/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import edu.kit.scc.test.ldap.LdapClientTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LdapClientTest.class})
public class TestSuite {

}
