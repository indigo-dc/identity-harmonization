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
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.oidc.OidcClient;
import edu.kit.scc.scim.ScimClient;
import edu.kit.scc.scim.ScimUser;
import edu.kit.scc.scim.ScimUserAttributeMapper;

@Component
public class IdentityHarmonizer {

	private static final Logger log = LoggerFactory.getLogger(IdentityHarmonizer.class);

	@Autowired
	private ScimClient scimClient;

	@Autowired
	private OidcClient oidcClient;

	public ScimUser harmonizeIdentities(String subject, OIDCTokens tokens) {
		ScimUser scimUser = new ScimUser();
		scimUser.setUserName(subject);

		// OIDC
		log.debug("Try to get OIDC user information");
		UserInfo userInfo = null;
		if (tokens != null) {
			try {
				JWT jwt = tokens.getIDToken();
				JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();

				log.debug("Claims set {}", claimsSet.toJSONObject().toJSONString());

				AccessToken accessToken = tokens.getAccessToken();

				userInfo = oidcClient.requestUserInfo(accessToken.getValue(), claimsSet);

				if (userInfo != null) {
					log.debug("User info {}", userInfo.toJSONObject().toJSONString());
					ScimUserAttributeMapper attributeMapper = new ScimUserAttributeMapper();

					scimUser = attributeMapper.mapFromUserInfo(userInfo);
					scimUser.setSchemas(Arrays.asList(scimUser.USER_SCHEMA));

				}

			} catch (ParseException e) {
				log.error(e.getMessage());
			}
		}

		// SCIM
		log.debug("Try to get SCIM user information");
		JSONObject userJson = scimClient.getUser(scimUser.getUserName());
		if (userJson != null) {
			log.debug("SCIM user info {}", userJson.toString());

			// TODO merge with SCIM user
		}

		// LDAP
		// TODO

		// REGAPP
		// TODO

		log.debug("Aggregated SCIM user information {}", scimUser.toString());
		return scimUser;
	}
}
