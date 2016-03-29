/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
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
import edu.kit.scc.scim.ScimListResponse;
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

	@RequestMapping(path = "/link", method = RequestMethod.POST)
	public List<ScimUser> linkUsers(@RequestHeader("Authorization") String basicAuthorization,
			@RequestBody List<ScimUser> scimUsers, HttpServletResponse response) {

		verifyAuthorization(basicAuthorization);

		log.debug("Request body {}", scimUsers);

		List<ScimUser> modifiedUsers = identityHarmonizer.harmonizeIdentities(scimUsers);
		if (!modifiedUsers.isEmpty())
			return modifiedUsers;

		throw new ConflictException();
	}

	@RequestMapping(path = "/unlink", method = RequestMethod.POST)
	public List<ScimUser> unlinkUsers(@RequestHeader("Authorization") String basicAuthorization,
			@RequestBody List<ScimUser> scimUsers, HttpServletResponse response) {

		verifyAuthorization(basicAuthorization);

		log.debug("Request body {}", scimUsers);

		List<ScimUser> modifiedUsers = identityHarmonizer.unlinkUsers(scimUsers);
		if (!modifiedUsers.isEmpty())
			return modifiedUsers;

		throw new ConflictException();
	}

	@RequestMapping(path = "/scim/Users", method = RequestMethod.POST, produces = "application/scim+json")
	@ResponseStatus(value = HttpStatus.CREATED)
	public ScimUser scimAddUser(@RequestHeader("Authorization") String basicAuthorization,
			@RequestBody ScimUser scimUser, HttpServletResponse response) {

		verifyAuthorization(basicAuthorization);

		log.debug("Request body {}", scimUser);

		ScimUser createdScimUser = scimUser; // scimService.createLdapIndigoUser(scimUser);

		if (createdScimUser != null) {
			response.addHeader("Location", "");
			return createdScimUser;
		}

		throw new ConflictException();
	}

	@RequestMapping(path = "/ecp/regid/{regId}", method = RequestMethod.POST)
	public ScimListResponse ecpAuthentication(@PathVariable String regId,
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
			log.debug("{}", regAppQuery);
			// return identityHarmonizer.harmonizeIdentities(username,
			// regAppQuery);
		}

		// OIDC
		log.debug("Try OIDC authentication");
		log.debug("Got token {}", password);
		tokens = oidcClient.requestTokens(password);

		if (tokens != null) {
			oidcSuccess = true;
			log.debug("OIDC authentication {}", oidcSuccess);
			// return identityHarmonizer.harmonizeIdentities(username, tokens);
		}
		log.debug("OIDC authentication {}", oidcSuccess);

		// if nothing succeeded, fail
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

	@ResponseStatus(value = HttpStatus.CONFLICT)
	public class ConflictException extends RuntimeException {

		private static final long serialVersionUID = -9070725142810603956L;

		public ConflictException() {
			super();
		}

		public ConflictException(String message) {
			super(message);
		}

		public ConflictException(Throwable e) {
			super(e);
		}
	}
}