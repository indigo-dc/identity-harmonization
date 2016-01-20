package edu.kit.scc.scim;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;

/**
 * SCIM client implementation.
 * 
 * @author benjamin
 *
 */
@Component
public class ScimClient {

	private static final Logger log = LoggerFactory.getLogger(ScimClient.class);

	@Value("${scim.userEndpoint}")
	private String userEndpoint;

	@Value("${scim.groupEndpoint}")
	private String groupEndpoint;

	@Value("${scim.user}")
	private String user;

	@Value("${scim.password}")
	private String password;

	/**
	 * Gets all user information from the SCIM HTTPS user endpoint for a
	 * specific user.
	 * 
	 * @param name
	 *            the user's name
	 * 
	 * @return a {@link JSONObject} with the SCIM user information
	 */
	public JSONObject getUser(String name) {
		JSONObject json = null;
		HttpClient client = new HttpClient();
		String url = userEndpoint + "?filter=userNameEq" + name;
		HttpResponse response = client.makeHttpsGetRequest(user, password, url);

		if (response != null) {
			log.debug(response.toString());

			json = new JSONObject(new String(response.getResponse()));
		}

		return json;
	}

	/**
	 * Gets all user information from the SCIM HTTPS user endpoint.
	 * 
	 * @return a {@link JSONObject} with the SCIM user information
	 */
	public JSONObject getUsers() {
		JSONObject json = null;
		HttpClient client = new HttpClient();
		HttpResponse response = client.makeHttpsGetRequest(user, password, userEndpoint);

		if (response != null) {
			log.debug(response.toString());

			json = new JSONObject(new String(response.getResponse()));
		}

		return json;
	}

	/**
	 * Gets all group information from the SCIM HTTPS group endpoint.
	 * 
	 * @return a {@link JSONObject} with the SCIM group information
	 */
	public JSONObject getGroups(String user, String password) {
		JSONObject json = null;
		HttpClient client = new HttpClient();
		HttpResponse response = client.makeHttpsGetRequest(user, password, groupEndpoint);

		if (response != null) {
			log.debug(response.toString());

			json = new JSONObject(new String(response.getResponse()));
		}

		return json;
	}

}
