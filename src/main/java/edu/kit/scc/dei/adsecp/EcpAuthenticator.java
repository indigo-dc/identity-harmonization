package edu.kit.scc.dei.adsecp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.directory.api.ldap.model.constants.AuthenticationLevel;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.StringValue;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapAuthenticationException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.StringConstants;
import org.apache.directory.server.core.api.LdapPrincipal;
import org.apache.directory.server.core.api.entry.ClonedServerEntry;
import org.apache.directory.server.core.api.interceptor.context.BindOperationContext;
import org.apache.directory.server.core.api.interceptor.context.LookupOperationContext;
import org.apache.directory.server.core.authn.SimpleAuthenticator;
import org.apache.directory.server.i18n.I18n;
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

public class EcpAuthenticator extends SimpleAuthenticator {

	private static final Logger logger = LoggerFactory.getLogger(EcpAuthenticator.class);

	public EcpAuthenticator() {
		super();

		logger.info("Constructing EcpAuthenticator");
	}

	@Override
	public LdapPrincipal authenticate(BindOperationContext bindContext) throws LdapException {
		logger.debug("Starting ECP Auth routine for {}", bindContext.getDn());

		try {
			LdapPrincipal principal = super.authenticate(bindContext);

			logger.debug("SimpleAuthentication succeded, no ECP auth");
			return principal;
		} catch (LdapException e) {
			logger.debug("ECP Authenticate called");

			if (bindContext.getEntry() == null || bindContext.getEntry().get("description") == null)
				lookupUserPassword(bindContext);

			Object o = bindContext.getEntry().get("description").get();
			String regId;
			if (o instanceof StringValue)
				regId = ((StringValue) o).getValue();
			else
				throw new IllegalStateException("description not of type String");

			byte[] credentials = bindContext.getCredentials();
			String password;
			try {
				password = new String(credentials, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				logger.error("Unsupported encoding: UTF-8");
				throw new LdapAuthenticationException("Internal server error");
			}

			logger.debug("trying login for regId {}", regId);

			String serviceUrl = "https://localhost/rest/ecp/regid/";
			String serviceUsername = "ldaprest";
			String servicePassword = "qwertz";
			String serviceHost;
			Boolean checkCert = Boolean.TRUE;

			if (System.getProperty("registerApp.serviceUrl") != null)
				serviceUrl = System.getProperty("registerApp.serviceUrl");
			if (System.getProperty("registerApp.serviceUsername") != null)
				serviceUsername = System.getProperty("registerApp.serviceUsername");
			if (System.getProperty("registerApp.servicePassword") != null)
				servicePassword = System.getProperty("registerApp.servicePassword");
			if (System.getProperty("registerApp.checkCert") != null)
				checkCert = Boolean.parseBoolean(System.getProperty("registerApp.checkCert"));

			try {
				serviceHost = new URI(serviceUrl).getHost();
			} catch (URISyntaxException e2) {
				logger.warn("Service URL is misconfigured", e2);
				throw new LdapException(e2);
			}

			logger.debug("ECPAuth Config: url {}, user {}, pass {}, host {}, cert {}",
					new Object[] { serviceUrl, serviceUsername, servicePassword, serviceHost, checkCert });

			DefaultHttpClient httpClient = getHttpClient(checkCert);

			httpClient.getCredentialsProvider().setCredentials(new AuthScope(serviceHost, 443),
					new UsernamePasswordCredentials(serviceUsername, servicePassword));

			HttpPost post;
			try {
				post = new HttpPost(serviceUrl + URLEncoder.encode(regId, "UTF-8"));
			} catch (UnsupportedEncodingException e2) {
				logger.warn("UnsupportedEncodingException", e2);
				throw new LdapException(e2);
			}

			try {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("password", password));
				post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				logger.debug(post.getEntity().toString());
				HttpResponse response = httpClient.execute(post);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String responseString = EntityUtils.toString(response.getEntity());
					logger.debug(responseString);
					return new LdapPrincipal(getDirectoryService().getSchemaManager(), bindContext.getDn(),
							AuthenticationLevel.SIMPLE);
				} else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
					logger.info("User {} is not authorized by idp", regId);
				} else {
					logger.warn("Statuscode bad: {}", response.getStatusLine());
					String responseString = EntityUtils.toString(response.getEntity());
					logger.debug(responseString);
				}
			} catch (UnsupportedEncodingException e1) {
				logger.warn("UnsupportedEncodingException", e1);
			} catch (ClientProtocolException e1) {
				logger.warn("ClientProtocolException", e1);
			} catch (ParseException e1) {
				logger.warn("ParseException", e1);
			} catch (IOException e1) {
				logger.warn("IOException", e1);
			}

			String message = I18n.err(I18n.ERR_230, bindContext.getDn().getName());
			logger.info(message);
			throw new LdapAuthenticationException(message);
		}
	}

	private DefaultHttpClient getHttpClient(Boolean checkCert) {
		if (!checkCert) {
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
				logger.warn("Problem while createing no cert check http client", e);
				return new DefaultHttpClient();
			} catch (NoSuchAlgorithmException e) {
				logger.warn("Problem while createing no cert check http client", e);
				return new DefaultHttpClient();
			}
		}
	}

	private byte[] lookupUserPassword(BindOperationContext bindContext) throws LdapException {
		// ---- lookup the principal entry's userPassword attribute
		Entry userEntry;

		try {
			/*
			 * NOTE: at this point the BindOperationContext does not has a null
			 * session since the user has not yet authenticated so we cannot use
			 * lookup() yet. This is a very special case where we cannot rely on
			 * the bindContext to perform a new sub operation. We request all
			 * the attributes
			 */
			LookupOperationContext lookupContext = new LookupOperationContext(getDirectoryService().getAdminSession(),
					bindContext.getDn(), SchemaConstants.ALL_USER_ATTRIBUTES,
					SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES);

			userEntry = getDirectoryService().getPartitionNexus().lookup(lookupContext);

			if (userEntry == null) {
				Dn dn = bindContext.getDn();
				String upDn = (dn == null ? "" : dn.getName());

				throw new LdapAuthenticationException(I18n.err(I18n.ERR_231, upDn));
			}
		} catch (Exception cause) {
			LOG.error(I18n.err(I18n.ERR_6, cause.getLocalizedMessage()));
			LdapAuthenticationException e = new LdapAuthenticationException(cause.getLocalizedMessage());
			e.initCause(e);
			throw e;
		}

		checkPwdPolicy(userEntry);

		Value<?> userPassword;

		Attribute userPasswordAttr = userEntry.get(SchemaConstants.USER_PASSWORD_AT);

		bindContext.setEntry(new ClonedServerEntry(userEntry));

		// ---- assert that credentials match
		if (userPasswordAttr == null) {
			return StringConstants.EMPTY_BYTES;
		} else {
			userPassword = userPasswordAttr.get();

			return userPassword.getBytes();
		}
	}

}