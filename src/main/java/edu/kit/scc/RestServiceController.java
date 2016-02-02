/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc;

import javax.ws.rs.FormParam;

import org.apache.commons.codec.binary.Base64;
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

import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.http.HttpResponse;
import edu.kit.scc.oidc.OidcClient;
import edu.kit.scc.regapp.RegAppClient;
import edu.kit.scc.scim.ScimUser;

@RestController
@RequestMapping("/rest")
public class RestServiceController {

	private static Logger log = LoggerFactory.getLogger(RestServiceController.class);

	@Value("${rest.serviceUsername}")
	private String restUser;

	@Value("${rest.servicePassword}")
	private String restPassword;

	@Autowired
	private RegAppClient regAppClient;

	@Autowired
	private OidcClient oidcClient;

	@Autowired
	private IdentityHarmonizer identityHarmonizer;

	// expected body e.g.
	// password=password
	// password=https%3A%2F%2F512eebd9%3Fk%3D49806e48a5cd2941604eb9dfe321c3bc
	// password=3D49806e48a5cd2941604eb9dfe321c3bc

	@RequestMapping(path = "/ecp/regid/{regId}", method = RequestMethod.POST)
	public ScimUser ecpAuthentication(@PathVariable String regId,
			@RequestHeader("Authorization") String basicAuthorization, @FormParam("username") String username,
			@FormParam("password") String password, @RequestBody String body) {

		verifyAuthorization(basicAuthorization);

		log.debug("Request body {}", body);

		boolean regAppSuccess = false;
		boolean oidcSuccess = false;

		// REG-APP
		log.debug("Try reg-app authentication");
		regAppSuccess = regAppClient.authenticate(regId, body);
		log.debug("Reg-app authentication {}", regAppSuccess);

		HttpResponse regAppQuery = null;
		OIDCTokens tokens = null;

		if (regAppSuccess) {
			regAppQuery = regAppClient.attributeQuery(regId);
			return identityHarmonizer.harmonizeIdentities(username, regAppQuery);
		}

		// OIDC
		log.debug("Try OIDC authentication");
		log.debug("Got token {}", password);
		tokens = oidcClient.requestTokens(password);

		if (tokens != null) {
			oidcSuccess = true;
			log.debug("OIDC authentication {}", oidcSuccess);
			return identityHarmonizer.harmonizeIdentities(username, tokens);
		}
		log.debug("OIDC authentication {}", oidcSuccess);

		// if nothing succeeded, fail ... gracefully
		throw new UnauthorizedException();
	}

	private void verifyAuthorization(String basicAuthorization) {
		String encodedCredentials = basicAuthorization.split(" ")[1];
		String[] credentials = new String(Base64.decodeBase64(encodedCredentials)).split(":");

		if (!credentials[0].equals(restUser) || !credentials[1].equals(restPassword)) {
			log.error("Wrong credentials {} {}", credentials[0], credentials[1]);
			throw new UnauthorizedException();
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
