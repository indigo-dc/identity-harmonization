/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.test.oidc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.kit.scc.Application;
import edu.kit.scc.oidc.OidcClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class OidcClientTest {

	@Autowired
	private OidcClient oidcClient;

	@Test
	public void requestOIDCTokenTest() {
		String authorizationCode = "";
		oidcClient.requestTokens(authorizationCode);
	}

	@Test
	public void requestUserInfoTest() {
		String accessToken = "";
		oidcClient.requestUserInfo(accessToken);
	}

}
