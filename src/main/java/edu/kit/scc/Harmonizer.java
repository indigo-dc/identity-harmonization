/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc;

import java.text.ParseException;
import java.util.Arrays;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.oidc.OidcClient;
import edu.kit.scc.scim.ScimClient;
import edu.kit.scc.scim.ScimUser;

@Component
public class Harmonizer {

	private static final Logger log = LoggerFactory.getLogger(Harmonizer.class);

	@Autowired
	private ScimClient scimClient;

	@Autowired
	private OidcClient oidcClient;

	public ScimUser harmonizeIdentities(String subject, OIDCTokens tokens) {
		ScimUser scimUser = new ScimUser();
		scimUser.setSchemas(Arrays.asList(scimUser.USER_SCHEMA));
		scimUser.setUserName(subject);

		// OIDC
		log.debug("Try to get OIDC user information");
		JSONObject userInfo = null;
		if (tokens != null) {
			try {
				JWT jwt = tokens.getIDToken();
				JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();

				log.debug("Claims set {}", claimsSet.toJSONObject().toJSONString());

				AccessToken accessToken = tokens.getAccessToken();

				userInfo = oidcClient.requestUserInfo(accessToken.getValue());

				log.debug("User info {}", userInfo.toString());

			} catch (ParseException e) {
				log.error(e.getMessage());
			}
		}

		// SCIM
		log.debug("Try to get SCIM user information");
		JSONObject userJson = scimClient.getUser(subject);
		log.debug("SCIM user info {}", userJson.toString());

		// LDAP
		// TODO

		log.debug("Aggregated SCIM user information {}", scimUser.toString());
		return scimUser;
	}
}
