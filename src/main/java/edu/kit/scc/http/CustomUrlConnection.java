package edu.kit.scc.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CustomUrlConnection {

	private static final Logger log = LoggerFactory.getLogger(CustomUrlConnection.class);

	private CustomUrlConnection() {
	}

	public static HttpsURLConnection getSecureHttpConnection(boolean checkCertificate, String url) {
		HttpsURLConnection urlConnection = null;
		try {
			log.debug("parse url {}", url);
			URL uri = new URL(url);

			SSLContext sslContext = null;

			if (checkCertificate) {
				// TODO
				HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
				sslContext = CustomSSLContext.initEmptySslContext();
			} else {
				HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
				sslContext = CustomSSLContext.initEmptySslContext();
			}

			urlConnection = (HttpsURLConnection) uri.openConnection();
			urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

		} catch (MalformedURLException e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		} catch (IOException e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		}
		return urlConnection;
	}

	public static HttpURLConnection getHttpConnection(String url) {
		HttpURLConnection urlConnection = null;
		try {
			log.debug("parse url {}", url);
			URL uri = new URL(url);

			urlConnection = (HttpURLConnection) uri.openConnection();

		} catch (MalformedURLException e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		} catch (IOException e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		}
		return urlConnection;
	}
}
