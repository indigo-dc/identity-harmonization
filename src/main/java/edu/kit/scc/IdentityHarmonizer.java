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

	public ScimUser harmonizeIdentities(String username, OIDCTokens tokens) {
		ScimUser scimUser = new ScimUser();
		scimUser.setUserName(username);

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

		if (scimUser.getUserName() != null && !scimUser.getUserName().equals(username)) {
			log.warn("provided username {} does not equal oidc username {}", username, scimUser.getUserName());
			// the google case ..
			if (scimUser.getEmails() != null && !scimUser.getEmails().isEmpty()) {
				String emailAddress = scimUser.getEmails().get(0).getValue();
				String[] scopedEmail = emailAddress.split("@");
				try {
					String userId = scopedEmail[0];
					log.warn("overwrite username {} with {}", scimUser.getUserName(), userId);

					scimUser.setUserName(userId);
				} catch (Exception e) {
					log.warn("ERROR parsing email {}", e.getMessage());
				}
			}
		} else {
			log.warn("provided username {} does not equal oidc subject {}", username, scimUser.getExternalId());
			log.warn("overwrite username {} with {}", username, scimUser.getExternalId());
			scimUser.setUserName(scimUser.getExternalId());
		}

		// SCIM
		log.debug("Try to get SCIM user information");
		JSONObject userJson = scimClient.getUser(scimUser.getUserName());
		if (userJson != null) {
			log.debug("SCIM user info {}", userJson.toString());
		}

		// LDAP
		// TODO

		// REGAPP
		// TODO

		// TODO merge

		log.debug("Aggregated SCIM user information {}", scimUser.toString());
		return scimUser;
	}
}
