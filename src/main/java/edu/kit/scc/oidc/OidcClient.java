/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.oidc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.http.CustomSSLContext;
import edu.kit.scc.http.NullHostNameVerifier;

/**
 * OIDC client implementation.
 * 
 * @author benjamin
 *
 */
@Component
public class OidcClient {

	private static final Logger log = LoggerFactory.getLogger(OidcClient.class);

	private SSLContext sslContext = CustomSSLContext.initEmptySslContext();

	@Value("${oauth2.clientId}")
	private String clientId;

	@Value("${oauth2.clientSecret}")
	private String clientSecret;

	@Value("${oauth2.redirectUri}")
	private String redirectUri;

	@Value("${oidc.tokenEndpoint}")
	private String oidcTokenEndpoint;

	@Value("${oidc.userInfoEndpoint}")
	private String oidcUserInfoEndpoint;

	/**
	 * Gets all the user information from the OIDC HTTPS user endpoint.
	 * 
	 * @param accessToken
	 *            the OAuth2 access token
	 * @return a {@link JSONObject} with the OIDC user information
	 */
	@SuppressWarnings("static-access")
	public UserInfo requestUserInfo(String accessToken, JWTClaimsSet claimsSet) {
		UserInfo userInfo = null;
		try {
			AccessToken token = AccessToken.parse("Bearer " + accessToken);

			URI uri = new URI(oidcUserInfoEndpoint);
			UserInfoRequest request = new UserInfoRequest(uri, (BearerAccessToken) token);
			HTTPRequest httpRequest = request.toHTTPRequest();

			httpRequest.setDefaultHostnameVerifier(new NullHostNameVerifier());
			httpRequest.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

			HTTPResponse response = null;

			// DEBUG
			logHttpRequest(httpRequest);

			response = request.toHTTPRequest().send();

			// DEBUG
			logHttpResponse(response);

			net.minidev.json.JSONObject jsonResponse = response.getContentAsJSONObject();
			jsonResponse.put("sub", claimsSet.getSubject());

			response.setContent(jsonResponse.toJSONString());

			UserInfoResponse userInfoResponse = UserInfoResponse.parse(response);

			if (userInfoResponse instanceof UserInfoErrorResponse) {
				UserInfoErrorResponse errorResponse = (UserInfoErrorResponse) userInfoResponse;

				ErrorObject error = ((UserInfoErrorResponse) errorResponse).getErrorObject();
				log.warn("ERROR HTTP {} code {}", error.getHTTPStatusCode(), error.getCode());
				log.warn("ERROR " + error.getDescription());
				return null;
			}

			UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;

			userInfo = successResponse.getUserInfo();

			log.debug(userInfo.toJSONObject().toJSONString());

			return userInfo;
		} catch (ParseException e) {
			log.error("ERROR {}", e.getMessage());
		} catch (URISyntaxException e) {
			log.error("ERROR {}", e.getMessage());
		} catch (IOException e) {
			log.error("ERROR {}", e.getMessage());
		}

		return userInfo;
	}

	/**
	 * Gets all OIDC tokens from the OIDC HTTPS token endpoint.
	 * 
	 * @param authorizationCode
	 *            the OAuth2 authorization code
	 * @return a {@link Tokens} bundle with all OIDC tokens
	 */
	@SuppressWarnings("static-access")
	public OIDCTokens requestTokens(String authorizationCode) {
		AuthorizationCode code = new AuthorizationCode(authorizationCode);

		OIDCTokens tokens = null;

		try {
			URI redirectUri = new URI(this.redirectUri);
			URI tokenEndpoint = new URI(this.oidcTokenEndpoint);

			ClientID clientID = new ClientID(this.clientId);
			Secret clientSecret = new Secret(this.clientSecret);

			ClientAuthentication clientAuthentication = new ClientSecretBasic(clientID, clientSecret);
			AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, redirectUri);

			// codeGrant.toParameters().put("client_id", this.clientId);
			// codeGrant.toParameters().put("client_secret", this.clientSecret);

			// for (Entry<String, String> entry :
			// codeGrant.toParameters().entrySet())
			// log.debug("{} {}", entry.getKey(), entry.getValue());

			TokenRequest request = new TokenRequest(tokenEndpoint, clientAuthentication, codeGrant);

			HTTPResponse httpResponse = null;
			HTTPRequest httpRequest = request.toHTTPRequest();
			// httpRequest.setAccept("*/*");

			httpRequest.setDefaultHostnameVerifier(new NullHostNameVerifier());
			httpRequest.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

			// DEBUG
			logHttpRequest(httpRequest);

			httpResponse = httpRequest.send();

			TokenResponse response = null;
			response = OIDCTokenResponseParser.parse(httpResponse);

			// DEBUG
			logHttpResponse(httpResponse);

			if (response instanceof TokenErrorResponse) {

				TokenErrorResponse tokenErrorResponse = (TokenErrorResponse) response;

				log.warn("ERROR {}", tokenErrorResponse.toJSONObject().toJSONString());

				ErrorObject error = ((TokenErrorResponse) response).getErrorObject();
				log.warn("ERROR HTTP {} code {}", error.getHTTPStatusCode(), error.getCode());
				log.warn("ERROR " + error.getDescription());
				return null;
			}

			OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) response;

			tokens = oidcTokenResponse.getOIDCTokens();

			log.debug(tokens.toJSONObject().toJSONString());

			return tokens;
		} catch (ParseException e) {
			log.error("ERROR {}", e.getMessage());
		} catch (IOException e) {
			log.error("ERROR {}", e.getMessage());
		} catch (URISyntaxException e) {
			log.error("ERROR {}", e.getMessage());
		}
		return tokens;
	}

	private void logHttpRequest(HTTPRequest httpRequest) {
		log.debug("------HTTP REQUEST DEBUG------");
		for (Entry<String, String> e : httpRequest.getHeaders().entrySet())
			log.debug("{} {}", e.getKey(), e.getValue());
		log.debug("Method {}", httpRequest.getMethod());
		log.debug("Query {}", httpRequest.getQuery());
		log.debug("Url {}", httpRequest.getURL());
		log.debug("------HTTP REQUEST DEBUG------");
	}

	private void logHttpResponse(HTTPResponse httpResponse) {
		log.debug("------HTTP RESPONSE DEBUG------");
		for (Entry<String, String> e : httpResponse.getHeaders().entrySet())
			log.debug("{} {}", e.getKey(), e.getValue());
		log.debug("Status code {}", httpResponse.getStatusCode());
		log.debug("Content {}", httpResponse.getContent());
		log.debug("------HTTP RESPONSE DEBUG------");
	}
}
