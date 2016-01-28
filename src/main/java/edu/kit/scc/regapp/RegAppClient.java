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
		regAppUrl += "/" + regId;
		HttpResponse response = httpClient.makeHttpPostRequest(restUser, restPassword, credentials, regAppUrl);
		if (response != null && response.statusCode == 200) {
			log.debug("Reg-app authentication success");
			return true;
		}
		return false;
	}
}
