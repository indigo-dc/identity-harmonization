package edu.kit.scc.oidc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
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

public class OidcClient {

	// private static String authorizationCode =
	// "4/E8EPEfj8V5NsK4fuJ8u5zTF9HPj1Ms58Om1tNC6PJnQ";
	private static String _redirectUri = "http://localhost:5000";
	private static String _clientId = "214831481800-rrtfq9e5ug04sprel2aa45eougp6lqeh.apps.googleusercontent.com";
	private static String _clientSecret = "mtN_7xlXXBpOQPVHOOCvbXv6";

	private static String oidcUserInfoEndpoint = "https://www.googleapis.com/oauth2/v1/userinfo";
	private static String oidcTokenEndpoint = "https://accounts.google.com/o/oauth2/token";

	public UserInfoResponse requestUserInfo(String accessToken) throws URISyntaxException, ParseException {
		AccessToken token = AccessToken.parse("Bearer " + accessToken);
		UserInfoRequest request = new UserInfoRequest(new URI(oidcUserInfoEndpoint), (BearerAccessToken) token);

		HTTPResponse response = null;
		try {
			response = request.toHTTPRequest().send();
		} catch (IOException e) {
		}

		System.out.println(response.getContentAsJSONObject().toJSONString());
		
		UserInfoResponse userInfoResponse = null;
		try {
			userInfoResponse = UserInfoResponse.parse(response);
		} catch (ParseException e) {
		}

		if (userInfoResponse instanceof UserInfoErrorResponse) {
			ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
			System.out.println("ERROR " + error.getDescription());

			return null;
		}

		UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;
		String claims = successResponse.getUserInfo().toJSONObject().toJSONString();

		System.out.println(claims);

		return successResponse;
	}

	public Tokens requestToken(String authorizationCode) throws URISyntaxException {
		AuthorizationCode code = new AuthorizationCode(authorizationCode);

		URI redirectUri = new URI(_redirectUri);
		System.out.println(redirectUri.toString());
		AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, redirectUri);

		ClientID clientID = new ClientID(_clientId);
		Secret clientSecret = new Secret(_clientSecret);

		ClientAuthentication clientAuthentication = new ClientSecretBasic(clientID, clientSecret);

		URI tokenEndpoint = new URI(oidcTokenEndpoint);

		TokenRequest request = new TokenRequest(tokenEndpoint, clientAuthentication, codeGrant);

		HTTPResponse httpResponse = null;
		try {
			httpResponse = request.toHTTPRequest().send();
		} catch (IOException e) {
		}

		TokenResponse response = null;
		try {
			response = OIDCTokenResponseParser.parse(httpResponse);
		} catch (ParseException e) {
		}

		if (response instanceof TokenErrorResponse) {
			ErrorObject error = ((TokenErrorResponse) response).getErrorObject();
			System.out.println("ERROR " + error.getDescription());
			return null;
		}

		OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) response;

		Tokens tokens = oidcTokenResponse.getTokens();

		System.out.println(tokens.toJSONObject().toJSONString());

		return tokens;
	}
}
