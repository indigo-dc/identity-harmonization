package edu.kit.scc.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * HTTPS client implementation.
 * 
 * @author benjamin
 *
 */
public class HttpsClient {

	private static final Logger log = LoggerFactory.getLogger(HttpsClient.class);

	/**
	 * Makes a HTTPS GET request.
	 * 
	 * @param url
	 *            the URL for the request
	 * @return a {@link edu.kit.scc.http.HttpResponse} with the request's
	 *         response code and response stream as {@link byte[]}
	 */
	public HttpResponse makeHTTPSGetRequest(String url) {
		return makeHTTPSGetRequest(null, null, url);
	}

	/**
	 * Makes a HTTPS GET request with basic authorization.
	 * 
	 * @param user
	 *            the user for basic HTTP authorization
	 * @param password
	 *            the user's password for basic HTTP authorization
	 * @param url
	 *            the URL for the request
	 * @return a {@link edu.kit.scc.http.HttpResponse} with the request's
	 *         response code and response stream as {@link byte[]}
	 */
	public HttpResponse makeHTTPSGetRequest(String user, String password, String url) {
		HttpResponse response = null;
		InputStream in = null;
		try {

			HttpsURLConnection urlConnection = CustomUrlConnection.getSecureHttpConnection(false, url);

			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Accept", "*/*");

			if (user != null && !user.isEmpty()) {
				String value = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
				log.debug("Authorization: Basic {}", value);
				urlConnection.setRequestProperty("Authorization", "Basic " + value);
			}

			urlConnection.connect();
			in = urlConnection.getInputStream();

			response = new HttpResponse(urlConnection.getResponseCode(), ByteStreams.toByteArray(in));

		} catch (IOException e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		} catch (Exception e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// e.printStackTrace();
					log.error(e.getMessage());
				}
		}
		return response;
	}

	/**
	 * Makes a HTTPS POST request.
	 * 
	 * @param body
	 *            the body for the HTTP POST request
	 * @param url
	 *            the URL for the request
	 * @return a {@link edu.kit.scc.http.HttpResponse} with the request's
	 *         response code and response stream as {@link byte[]}
	 */
	public HttpResponse makeHTTPSPostRequest(String body, String url) {
		return makeHTTPSPostRequest(null, null, body, url);
	}

	/**
	 * Makes a HTTPS POST request with basic authorization.
	 * 
	 * @param user
	 *            the user for basic HTTP authorization
	 * @param password
	 *            the user's password for basic HTTP authorization
	 * @param body
	 *            the body for the HTTP POST request
	 * @param url
	 *            the URL for the request
	 * @return a {@link edu.kit.scc.http.HttpResponse} with the request's
	 *         response code and response stream as {@link byte[]}
	 */
	public HttpResponse makeHTTPSPostRequest(String user, String password, String body, String url) {
		HttpResponse response = null;
		OutputStream out = null;
		InputStream in = null;
		try {

			HttpsURLConnection urlConnection = CustomUrlConnection.getSecureHttpConnection(false, url);

			urlConnection.setRequestMethod("POST");
			// urlConnection.setRequestProperty("Accept", "*/*");
			urlConnection.setDoOutput(true);

			if (user != null && !user.isEmpty()) {
				String value = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
				log.debug("Authorization: Basic {}", value);
				urlConnection.setRequestProperty("Authorization", "Basic " + value);
			}

			byte[] bodyBytes = body.getBytes("UTF-8");
			out = urlConnection.getOutputStream();
			out.write(bodyBytes);

			urlConnection.connect();
			in = urlConnection.getInputStream();

			response = new HttpResponse(urlConnection.getResponseCode(), ByteStreams.toByteArray(in));

		} catch (IOException e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		} catch (Exception e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// e.printStackTrace();
					log.error(e.getMessage());
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// e.printStackTrace();
					log.error(e.getMessage());
				}
		}
		return response;
	}
}
