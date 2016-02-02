/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.scim;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

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
		String url = userEndpoint.replaceAll("/$", "");
		try {
			url += "?filter=userNameEq" + URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("ERROR {}", e.getMessage());
		}

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
		String url = userEndpoint.replaceAll("/$", "");
		HttpResponse response = client.makeHttpsGetRequest(user, password, url);

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
	public JSONObject getGroups() {
		JSONObject json = null;
		HttpClient client = new HttpClient();
		String url = groupEndpoint.replaceAll("/$", "");
		HttpResponse response = client.makeHttpsGetRequest(user, password, url);

		if (response != null) {
			log.debug(response.toString());

			json = new JSONObject(new String(response.getResponse()));
		}

		return json;
	}

	/**
	 * Gets all user information from the SCIM provider.
	 * 
	 * @param username
	 *            the user's username
	 * @return a {@link ScimUser} with the SCIM user information
	 *         urn:ietf:params:scim:schemas:core:2.0:User formatted
	 */
	public ScimUser getScimUser(String username) {
		ScimUser scimUser = new ScimUser();

		log.debug("Try to get SCIM user information");
		JSONObject scimJson = getUser(username);

		try {
			log.debug("Got SCIM {}", scimJson.toString());
			JSONArray schemas = scimJson.getJSONArray("schemas");
			if (schemas != null && schemas.length() > 0) {
				String schema = schemas.getString(0);

				ObjectMapper mapper = new ObjectMapper();

				if (schema.equals(scimUser.CORE_SCHEMA_1_0)) {
					JSONArray resources = scimJson.getJSONArray("Resources");
					JSONObject scim1Json = resources.getJSONObject(0);

					log.debug("{} {}", scimUser.CORE_SCHEMA_1_0, scim1Json.toString());

					ScimUser1_0 scim1User = mapper.readValue(scim1Json.toString(), ScimUser1_0.class);
					ScimUserAttributeMapper attributeMapper = new ScimUserAttributeMapper();

					scimUser = attributeMapper.mapFromScim1User(scim1User);
				}

				if (schema.equals(scimUser.USER_SCHEMA_2_0)) {

					log.debug("{} {}", scimUser.USER_SCHEMA_2_0, scimJson.toString());

					scimUser = mapper.readValue(scimJson.toString(), ScimUser.class);
				}
			}

		} catch (Exception e) {
			log.warn("ERROR {}", e.getMessage());
			scimUser = null;
		}

		return scimUser;
	}
}
