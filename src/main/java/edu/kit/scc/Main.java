package edu.kit.scc;

import java.net.URISyntaxException;

import com.nimbusds.oauth2.sdk.ParseException;

import edu.kit.scc.oidc.OidcClient;

public class Main {

	private static String authorizationCode = "4/RRRRiWO4Avp1olNeXv2NyJUljPD4_5NqMHQQg0WY_-w";
	private static String accessToken = "ya29.YAJ0rCj_mZbvfXKtqYSDYjwZE-vc0MNqEpQcT931T3vWv-5tdXKD85iVC2lxIq1HYHG8";

	public static void main(String[] args) {
		OidcClient oidcClient = new OidcClient();

		try {
			oidcClient.requestUserInfo(accessToken);
		} catch (URISyntaxException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
