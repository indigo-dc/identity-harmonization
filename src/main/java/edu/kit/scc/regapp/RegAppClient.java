/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.regapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;

@Component
public class RegAppClient {

	private static final Logger log = LoggerFactory.getLogger(RegAppClient.class);

	@Value("${regapp.serviceUsername}")
	private String restUser;

	@Value("${regapp.servicePassword}")
	private String restPassword;

	@Value("${regapp.serviceUrl}")
	private String serviceUrl;

	@Autowired
	private HttpClient httpClient;

	public boolean authenticate(String regId, String credentials) {
		String regAppUrl = serviceUrl.replaceAll("/$", "");
		regAppUrl += "/ecp/regid/" + regId;

		HttpResponse response = null;
		if (serviceUrl.startsWith("https")) {
			response = httpClient.makeHttpsPostRequest(restUser, restPassword, credentials, regAppUrl);
		} else {
			response = httpClient.makeHttpPostRequest(restUser, restPassword, credentials, regAppUrl);
		}
		if (response != null && response.statusCode == 200) {
			log.debug("Reg-app authentication success {}", response.toString());
			return true;
		}
		return false;
	}

	public HttpResponse attributeQuery(String regId) {
		String regAppUrl = serviceUrl.replaceAll("/$", "");
		regAppUrl += "/attrq/regid/" + regId;

		HttpResponse response = null;

		if (serviceUrl.startsWith("https")) {
			response = httpClient.makeHttpsGetRequest(restUser, restPassword, regAppUrl);
		} else {
			response = httpClient.makeHttpGetRequest(restUser, restPassword, regAppUrl);
		}
		if (response != null)
			log.debug(response.toString());
		return response;
	}
}
