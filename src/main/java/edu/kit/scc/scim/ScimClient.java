package edu.kit.scc.scim;

import java.util.Properties;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.Utils;
import edu.kit.scc.http.HttpResponse;
import edu.kit.scc.http.HttpsClient;

/**
 * SCIM client implementation.
 * 
 * @author benjamin
 *
 */
public class ScimClient {

	private static final Logger log = LoggerFactory.getLogger(ScimClient.class);

	private String userEndpoint;
	private String groupEndpoint;

	/**
	 * SCIM client.
	 * 
	 */
	public ScimClient() {
		Properties properties = Utils.loadProperties();
		this.userEndpoint = properties.getProperty("scim.userEndpoint");
		this.groupEndpoint = properties.getProperty("scim.groupEndpoint");

		log.debug("SCIM user endpoint {}", userEndpoint);
		log.debug("SCIM group endpoint {}", groupEndpoint);
	}

	/**
	 * Gets all user information from the SCIM HTTPS user endpoint.
	 * 
	 * @param user
	 *            the user for basic HTTP authorization
	 * @param password
	 *            the user's password for basic HTTP authorization
	 * @return a {@link JSONObject} with the SCIM user information
	 */
	public JSONObject getUsers(String user, String password) {
		JSONObject json = null;
		HttpsClient client = new HttpsClient();
		HttpResponse response = client.makeHTTPSGetRequest(user, password, userEndpoint);

		if (response != null) {
			log.debug(response.toString());

			json = new JSONObject(new String(response.getResponse()));
		}

		return json;
	}

	/**
	 * Gets all group information from the SCIM HTTPS group endpoint.
	 * 
	 * @param user
	 *            the user for basic HTTP authorization
	 * @param password
	 *            the user's password for basic HTTP authorization
	 * @return a {@link JSONObject} with the SCIM group information
	 */
	public JSONObject getGroups(String user, String password) {
		JSONObject json = null;
		HttpsClient client = new HttpsClient();
		HttpResponse response = client.makeHTTPSGetRequest(user, password, groupEndpoint);

		if (response != null) {
			log.debug(response.toString());

			json = new JSONObject(new String(response.getResponse()));
		}

		return json;
	}

}
