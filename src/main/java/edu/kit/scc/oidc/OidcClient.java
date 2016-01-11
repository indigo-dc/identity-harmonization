package edu.kit.scc.oidc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.nimbusds.openid.connect.sdk.UserInfoRequest;

import edu.kit.scc.Utils;
import edu.kit.scc.http.CustomSSLContext;
import edu.kit.scc.http.NullHostNameVerifier;

/**
 * OIDC client implementation.
 * 
 * @author benjamin
 *
 */
public class OidcClient {

	private static final Logger log = LoggerFactory.getLogger(OidcClient.class);

	private SSLContext sslContext;

	private String clientId;
	private String clientSecret;
	private String redirectUri;

	private String oidcTokenEndpoint;
	private String oidcUserInfoEndpoint;

	/**
	 * OIDC client.
	 * 
	 */
	public OidcClient() {
		this.sslContext = CustomSSLContext.initEmptySslContext();

		Properties properties = Utils.loadProperties();

		this.clientId = properties.getProperty("oauth2.clientId");
		this.clientSecret = properties.getProperty("oauth2.clientSecret");
		this.redirectUri = properties.getProperty("oauth2.redirectUri");
		this.oidcTokenEndpoint = properties.getProperty("oidc.tokenEndpoint");
		this.oidcUserInfoEndpoint = properties.getProperty("oidc.userInfoEndpoint");

		log.debug("OAuth2 client id {}", clientId);
		log.debug("OAuth2 client secret {}", clientSecret);
		log.debug("OAuth2 redirect uri {}", redirectUri);
		log.debug("OIDC token endpoint {}", oidcTokenEndpoint);
		log.debug("OIDC user info endpoint {}", oidcUserInfoEndpoint);
	}

	/**
	 * Gets all the user information from the OIDC HTTPS user endpoint.
	 * 
	 * @param accessToken
	 *            the OAuth2 access token
	 * @return a {@link JSONObject} with the OIDC user information
	 */
	public JSONObject requestUserInfo(String accessToken) {
		JSONObject userInfoResponse = null;

		try {
			AccessToken token = AccessToken.parse("Bearer " + accessToken);

			UserInfoRequest request = new UserInfoRequest(new URI(oidcUserInfoEndpoint), (BearerAccessToken) token);

			HTTPResponse response = null;
			response = request.toHTTPRequest().send();

			log.debug(response.getContentAsJSONObject().toJSONString());

			return new JSONObject(response.getContentAsJSONObject().toJSONString());

			// userInfoResponse = UserInfoResponse.parse(response);
			//
			// if (userInfoResponse instanceof UserInfoErrorResponse) {
			// ErrorObject error = ((UserInfoErrorResponse)
			// userInfoResponse).getErrorObject();
			// System.out.println("ERROR " + error.getDescription());
			// return null;
			// }
			//
			// UserInfoSuccessResponse successResponse =
			// (UserInfoSuccessResponse) userInfoResponse;
			// String claims =
			// successResponse.getUserInfo().toJSONObject().toJSONString();
			//
			// System.out.println(claims);
			//
			// return successResponse;

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return userInfoResponse;
	}

	/**
	 * Gets all OIDC tokens from the OIDC HTTPS token endpoint.
	 * 
	 * @param authorizationCode
	 *            the OAuth2 authorization code
	 * @return a {@link Tokens} bundle with all OIDC tokens
	 */
	public Tokens requestTokens(String authorizationCode) {
		AuthorizationCode code = new AuthorizationCode(authorizationCode);

		Tokens tokens = null;

		try {
			URI redirectUri = new URI(this.redirectUri);
			URI tokenEndpoint = new URI(this.oidcTokenEndpoint);

			ClientID clientID = new ClientID(this.clientId);
			Secret clientSecret = new Secret(this.clientSecret);

			ClientAuthentication clientAuthentication = new ClientSecretBasic(clientID, clientSecret);
			AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, redirectUri);

			TokenRequest request = new TokenRequest(tokenEndpoint, clientAuthentication, codeGrant);

			HTTPResponse httpResponse = null;
			HTTPRequest httpRequest = request.toHTTPRequest();

			httpRequest.setDefaultHostnameVerifier(new NullHostNameVerifier());
			httpRequest.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

			httpResponse = httpRequest.send();

			TokenResponse response = null;
			response = OIDCTokenResponseParser.parse(httpResponse);

			if (response instanceof TokenErrorResponse) {
				ErrorObject error = ((TokenErrorResponse) response).getErrorObject();
				log.debug("ERROR " + error.getDescription());
				return null;
			}

			OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) response;

			tokens = oidcTokenResponse.getTokens();

			log.debug(tokens.toJSONObject().toJSONString());

			return tokens;
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return tokens;
	}
}
