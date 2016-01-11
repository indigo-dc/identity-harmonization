package edu.kit.scc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	// private static String authorizationCode =
	// "997fc6c123b3224b3ade247ea8376164";
	// private static String accessToken = "88e3bd6a549e385926378129b330c";

	public static void main(String[] args) {

		// OidcClient oidcClient = new OidcClient(clientId, clientSecret,
		// redirectUri, oidcTokenEndpoint,
		// oidcUserInfoEndpoint);
		//
		// Tokens tokens = oidcClient.requestTokens(authorizationCode);
		//
		// oidcClient.requestUserInfo(tokens.getAccessToken().getValue());

		// Utils.printProperties();
		// ScimClient scimClient = new ScimClient();
		// scimClient.getUsers("admin", "admin");
		// scimClient.getGroups("admin", "admin");

		Utils.printProperties();
		HttpClient client = new HttpClient();
		HttpResponse response = client.makeHTTPPostRequest("password=password", "http://localhost:50070");
		log.debug(response.toString());
		// client.makePOST("localhost", 50070, "user", "password",
		// "http://localhost:50070");
	}
}
