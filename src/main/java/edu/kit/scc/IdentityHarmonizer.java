/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc;

import java.text.ParseException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.http.HttpResponse;
import edu.kit.scc.oidc.OidcClient;
import edu.kit.scc.scim.ScimClient;
import edu.kit.scc.scim.ScimListResponse;
import edu.kit.scc.scim.ScimUser;
import edu.kit.scc.scim.ScimUserAttributeMapper;

@Component
public class IdentityHarmonizer {

	private static final Logger log = LoggerFactory.getLogger(IdentityHarmonizer.class);

	@Autowired
	private ScimClient scimClient;

	@Autowired
	private OidcClient oidcClient;

	public ScimListResponse harmonizeIdentities(String username, OIDCTokens tokens) {
		int identityCount = 0;

		ScimUser scimUser = scimClient.getScimUser(username);
		if (scimUser != null)
			identityCount++;

		ScimUser scimUserFromJWT = null;
		ScimUserAttributeMapper mapper = new ScimUserAttributeMapper();

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
			} catch (ParseException e) {
				log.error(e.getMessage());
			}
		}

		if (userInfo != null) {
			log.debug("User info {}", userInfo.toJSONObject().toJSONString());
			scimUserFromJWT = mapper.mapFromUserInfo(userInfo);
			if (scimUserFromJWT != null)
				identityCount++;
		}

		ScimListResponse scimListResponse = new ScimListResponse();
		scimListResponse
				.setSchemas(Arrays.asList(scimListResponse.LIST_RESPONSE_SCHEMA, new ScimUser().USER_SCHEMA_2_0));
		scimListResponse.setResources(Arrays.asList(scimUser, scimUserFromJWT));
		scimListResponse.setTotalResults(identityCount);

		log.debug("SCIM query response {}", scimListResponse.toString());

		return scimListResponse;
	}

	// Example Reg-App HttpResponse
	// {"eppn":"ym0762@partner.kit.edu","last_update":"2016-02-02
	// 11:47:49.489","email":"ym0762@partner.kit.edu"}
	public ScimListResponse harmonizeIdentities(String username, HttpResponse regAppQuery) {
		int identityCount = 0;
		ScimUser scimUser = scimClient.getScimUser(username);
		if (scimUser != null)
			identityCount++;

		ScimUser scimUserFromQuery = null;
		ScimUserAttributeMapper mapper = new ScimUserAttributeMapper();

		if (regAppQuery != null) {
			log.debug("Reg-app query response {}", regAppQuery.toString());
			scimUserFromQuery = mapper.mapFromRegAppQuery(regAppQuery.getResponseString());
			if (scimUserFromQuery != null)
				identityCount++;
		}

		ScimListResponse scimListResponse = new ScimListResponse();
		scimListResponse.setSchemas(Arrays.asList(scimListResponse.LIST_RESPONSE_SCHEMA, scimUser.USER_SCHEMA_2_0));
		scimListResponse.setResources(Arrays.asList(scimUser, scimUserFromQuery));
		scimListResponse.setTotalResults(identityCount);

		log.debug("SCIM query response {}", scimListResponse.toString());

		return scimListResponse;

	}
}
