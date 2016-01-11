package edu.kit.scc.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * HTTP client implementation.
 * 
 * @author benjamin
 *
 */
public class HttpClient {

	private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

	/**
	 * Makes a HTTP GET request.
	 * 
	 * @param url
	 *            the URL for the request
	 * @return a {@link edu.kit.scc.http.HttpResponse} with the request's
	 *         response code and response stream as {@link byte[]}
	 * 
	 */
	public edu.kit.scc.http.HttpResponse makeHTTPGetRequest(String url) {
		return makeHTTPGetRequest(null, null, url);
	}

	/**
	 * Makes a HTTP GET request with basic authorization.
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
	public edu.kit.scc.http.HttpResponse makeHTTPGetRequest(String user, String password, String url) {
		edu.kit.scc.http.HttpResponse response = null;
		InputStream in = null;
		try {
			HttpURLConnection urlConnection = CustomUrlConnection.getHttpConnection(url);

			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Accept", "*/*");

			if (user != null && !user.isEmpty()) {
				String value = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
				log.debug("Authorization: Basic {}", value);
				urlConnection.setRequestProperty("Authorization", "Basic " + value);
			}

			urlConnection.connect();
			in = urlConnection.getInputStream();

			response = new edu.kit.scc.http.HttpResponse(urlConnection.getResponseCode(), ByteStreams.toByteArray(in));

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
	 * Makes a HTTP POST request.
	 * 
	 * @param body
	 *            the body for the HTTP POST request
	 * @param url
	 *            the URL for the request
	 * @return a {@link edu.kit.scc.http.HttpResponse} with the request's
	 *         response code and response stream as {@link byte[]}
	 */
	public edu.kit.scc.http.HttpResponse makeHTTPPostRequest(String body, String url) {
		return makeHTTPPostRequest(null, null, body, url);
	}

	/**
	 * Makes a HTTP POST request with basic authorization.
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
	public edu.kit.scc.http.HttpResponse makeHTTPPostRequest(String user, String password, String body, String url) {
		edu.kit.scc.http.HttpResponse response = null;
		OutputStream out = null;
		InputStream in = null;
		try {
			HttpURLConnection urlConnection = CustomUrlConnection.getHttpConnection(url);

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

			response = new edu.kit.scc.http.HttpResponse(urlConnection.getResponseCode(), ByteStreams.toByteArray(in));

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

	@Deprecated
	public void makePOST(String serviceHost, int servicePort, String serviceUsername, String servicePassword,
			String serviceUrl) {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		httpClient.getCredentialsProvider().setCredentials(new AuthScope(serviceHost, servicePort),
				new UsernamePasswordCredentials(serviceUsername, servicePassword));

		HttpPost post;
		try {
			log.debug(serviceUrl + "/9999");
			post = new HttpPost(serviceUrl + "/9999");

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("password", servicePassword));
			post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			log.debug(post.getEntity().toString());

			HttpResponse response = httpClient.execute(post);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String responseString = EntityUtils.toString(response.getEntity());
				log.debug(responseString);
			} else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				log.info("User {} is not authorized by idp", "9999");
			} else {
				log.warn("Statuscode bad: {}", response.getStatusLine());
				String responseString = EntityUtils.toString(response.getEntity());
				log.debug(responseString);
			}
		} catch (UnsupportedEncodingException e) {
			log.warn("UnsupportedEncodingException", e);
		} catch (ClientProtocolException e) {
			log.warn("ClientProtocolException", e);
		} catch (ParseException e) {
			log.warn("ParseException", e);
		} catch (IOException e) {
			log.warn("IOException", e);
		}
	}

	@Deprecated
	public DefaultHttpClient getHttpClient(String serviceProtocol, Boolean checkCert) {
		// TODO - remove in production
		// HTTP support for testing
		if (serviceProtocol.toLowerCase().equals("http")) {
			return new DefaultHttpClient();
		}
		if (checkCert) {
			return new DefaultHttpClient();
		} else {
			try {
				SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new TrustManager[] { new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}

					public void checkServerTrusted(X509Certificate[] certs, String authType) {
					}
				} }, new SecureRandom());

				SSLSocketFactory sf = new SSLSocketFactory(sslContext, new AllowAllHostnameVerifier());
				Scheme httpsScheme = new Scheme("https", 443, sf);
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(httpsScheme);

				// apache HttpClient version >4.2 should use
				// BasicClientConnectionManager
				ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
				return new DefaultHttpClient(cm);
			} catch (KeyManagementException e) {
				log.warn("Problem while createing no cert check http client", e);
				return new DefaultHttpClient();
			} catch (NoSuchAlgorithmException e) {
				log.warn("Problem while createing no cert check http client", e);
				return new DefaultHttpClient();
			}
		}
	}
}
