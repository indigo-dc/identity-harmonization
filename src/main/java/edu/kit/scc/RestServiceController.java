/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc;

import java.text.ParseException;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.dto.GroupDTO;
import edu.kit.scc.dto.UserDTO;
import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;
import edu.kit.scc.ldap.LdapClient;
import edu.kit.scc.oidc.OidcClient;
import edu.kit.scc.scim.ScimClient;

@RestController
@RequestMapping("/rest")
public class RestServiceController {

	private static Logger log = LoggerFactory.getLogger(RestServiceController.class);

	@Value("${regapp.serviceUsername}")
	private String restUser;

	@Value("${regapp.servicePassword}")
	private String restPassword;

	@Value("${regapp.serviceUrl}")
	private String serviceUrl;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private OidcClient oidcClient;

	@Autowired
	private ScimClient scimClient;

	@Autowired
	private LdapClient ldapClient;

	// expected body e.g.
	// password=password
	// password=https%3A%2F%2F512eebd9%3Fk%3D49806e48a5cd2941604eb9dfe321c3bc
	// password=3D49806e48a5cd2941604eb9dfe321c3bc

	@RequestMapping(path = "/ecp/{regId}", method = RequestMethod.POST)
	public void ecpAuthentication(@PathVariable String regId, @RequestHeader("Authorization") String basicAuthorization,
			@RequestBody String body) {
		String encodedCredentials = basicAuthorization.split(" ")[1];
		String[] credentials = new String(Base64.decodeBase64(encodedCredentials)).split(":");

		if (!credentials[0].equals(restUser) || !credentials[1].equals(restPassword)) {
			log.error("Wrong credentials {} {}", credentials[0], credentials[1]);
			throw new UnauthorizedException();
		}

		log.debug("Request body {}", body);

		// REG-APP
		log.debug("Try reg-app authentication");
		String regAppUrl = serviceUrl.replaceAll("/$", "");
		regAppUrl += "/" + regId;
		HttpResponse response = httpClient.makeHttpPostRequest(restUser, restPassword, body, regAppUrl);
		if (response != null && response.statusCode == 200) {
			log.debug("Reg-app authentication success");
			// TODO harmonize
			// harmonizeIdentities(userName);
			return;
		}

		// OIDC
		log.debug("Try OIDC authentication");
		OIDCTokens tokens = null;
		try {
			String token = body.split("=")[1];
			// oidcJson = oidcClient.requestUserInfo(token);
			tokens = oidcClient.requestTokens(token);
		} catch (ArrayIndexOutOfBoundsException e) {
			log.error(e.getMessage());
			throw new UnauthorizedException();
		}

		if (tokens != null) {
			try {
				JWT jwt = tokens.getIDToken();
				JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
				log.debug(claimsSet.toJSONObject().toJSONString());

				AccessToken accessToken = tokens.getAccessToken();
				oidcClient.requestUserInfo(accessToken.getValue());

				String subject = claimsSet.getSubject();
				log.debug("OIDC authentication success");
				// TODO harmonize
				harmonizeIdentities(subject);
				return;
			} catch (ParseException e) {
				log.error(e.getMessage());
				throw new UnauthorizedException();
			}
		}

		// if nothing succeeded, fail ... gracefully
		throw new UnauthorizedException();
	}

	private void harmonizeIdentities(String subject) {
		// SCIM
		// we are looking for groups in the SCIM response
		log.debug("Try to get SCIM user information");
		JSONObject userJson = scimClient.getUser(subject);
		if (userJson != null) {
			try {
				JSONArray resources = userJson.getJSONArray("Resources");
				JSONObject userResource = resources.getJSONObject(0);

				String userName = userResource.getString("userName");
				JSONObject names = userResource.getJSONObject("name");

				UserDTO existingUser = ldapClient.getLdapUser(userName);

				// there should always be an existing user in the LDAP tree
				if (existingUser != null)
					log.debug(existingUser.toString());
				else {
					throw new UnauthorizedException("no existing LDAP user");
				}

				JSONArray roles = userResource.getJSONArray("groups");
				for (int i = 0; i < roles.length(); i++) {
					JSONObject role = roles.getJSONObject(i);
					String cn = role.getString("display");
					GroupDTO group = ldapClient.getLdapGroup(cn);

					if (group != null) {
						// check/add user
						if (!group.getMemberUids().contains(userName))
							ldapClient.addGroupMember(cn, userName);
					} else {
						// create new group and add user
						ldapClient.createGroup(cn, ldapClient.generateGroupId());
						ldapClient.addGroupMember(cn, userName);
					}
				}
			} catch (JSONException e) {
				// no additional user information
				log.error(e.getMessage());
			}
		}
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public class UnauthorizedException extends RuntimeException {

		private static final long serialVersionUID = 6396195910009296687L;

		public UnauthorizedException() {
			super();
		}

		public UnauthorizedException(String message) {
			super(message);
		}

		public UnauthorizedException(Throwable e) {
			super(e);
		}
	}
}
