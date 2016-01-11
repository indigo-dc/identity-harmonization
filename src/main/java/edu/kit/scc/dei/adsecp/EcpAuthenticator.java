package edu.kit.scc.dei.adsecp;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.directory.api.ldap.model.constants.AuthenticationLevel;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
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
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.Utils;
import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;
import edu.kit.scc.http.HttpsClient;

public class EcpAuthenticator extends SimpleAuthenticator {

	private static final Logger logger = LoggerFactory.getLogger(EcpAuthenticator.class);

	// Reg-App connection properties, overwritten by system properties
	//
	// config.properties regapp.serviceUrl or registerApp.serviceUrl
	private String serviceUrl;
	// config.properties regapp.serviceUsername or registerApp.serviceUsername
	private String serviceUsername;
	// config.properties regapp.servicePassword or registerApp.servicePassword
	private String servicePassword;
	// config.properties regapp.checkCert or registerApp.checkCert
	private Boolean checkCert;

	// from registerApp.serviceUrl
	private String serviceHost;
	// from registerApp.serviceUrl
	private int servicePort;
	// from registerApp.serviceUrl
	private String serviceProtocol;

	public EcpAuthenticator() {
		super();

		Properties properties = Utils.loadProperties();
		this.serviceUrl = properties.getProperty("regapp.serviceUrl");
		this.serviceUsername = properties.getProperty("regapp.serviceUsername");
		this.servicePassword = properties.getProperty("regapp.servicePassword");
		this.checkCert = Boolean.valueOf(properties.getProperty("regapp.checkCert"));

		logger.info("Constructing EcpAuthenticator");
		logger.debug("RegApp service url {}", serviceUrl);
		logger.debug("RegApp service username {}", serviceUsername);
		logger.debug("RegApp service password {}", servicePassword);
		logger.debug("RegApp check certificate {}", checkCert);
	}

	@Override
	public LdapPrincipal authenticate(BindOperationContext bindContext) throws LdapException {
		try {
			logger.debug("Starting ECP Auth routine for {}", bindContext.getDn());

			LdapPrincipal principal = super.authenticate(bindContext);

			logger.debug("SimpleAuthentication succeded, no ECP auth");

			return principal;
		} catch (NullPointerException e) {
			logger.warn(e.getMessage());
		} catch (LdapException e) {
			logger.warn("LdapException", e);
		}

		logger.debug("ECP authenticate called");

		// TODO - needs to be reconsidered
		//
		// bindContext.getEntry() == null -> no ldap entry for dn
		// bindContext.getEntry().get("description") == null -> no description
		// attribute for ldap dn entry, associated with the user's Reg-App Id
		//
		// if (bindContext.getEntry() == null ||
		// bindContext.getEntry().get("description") == null)
		// lookupUserPassword(bindContext);

		String regId = "007";

		try {
			regId = (String) bindContext.getEntry().get("description").get().getValue();
		} catch (NullPointerException e) {
			logger.error("Entry has no description", e);
			// throw new IllegalStateException("entry has no description");
		} catch (ClassCastException e) {
			logger.error("Description not of type String", e);
			throw new IllegalStateException("description not of type String");
		}

		String password = "";
		try {
			byte[] credentials = bindContext.getCredentials();
			password = new String(credentials, "UTF-8");
		} catch (NullPointerException e) {
			logger.error("Context has no password", e);
			// throw new IllegalStateException("entry has no description");
		} catch (UnsupportedEncodingException e) {
			logger.error("Unsupported encoding: UTF-8", e);
			throw new LdapAuthenticationException("Internal server error");
		}

		logger.debug("trying login for regId {}", regId);

		try {
			serviceHost = new URI(serviceUrl).getHost();
			servicePort = new URI(serviceUrl).getPort();
			serviceProtocol = new URI(serviceUrl).getScheme();
		} catch (URISyntaxException e) {
			logger.warn("Service URL is misconfigured", e);
			throw new LdapException(e);
		}

		logger.debug("ECPAuth Config: url {}, user {}, pass {}, host {}, port {}, protocol {}, cert {}", new Object[] {
				serviceUrl, serviceUsername, servicePassword, serviceHost, servicePort, serviceProtocol, checkCert });

		serviceUrl = serviceUrl.replaceAll("/$", "");
		serviceUrl += "/" + regId;

		HttpResponse response = null;
		if (serviceProtocol.toLowerCase().equals("http")) {
			HttpClient httpClient = new HttpClient();
			response = httpClient.makeHTTPPostRequest(serviceUsername, servicePassword, "password=" + password,
					serviceUrl);
			logger.debug(String.valueOf(response.toString()));
		} else if (serviceProtocol.toLowerCase().equals("https")) {
			HttpsClient httpsClient = new HttpsClient();
			response = httpsClient.makeHTTPSPostRequest(serviceUsername, servicePassword, "password=" + password,
					serviceUrl);
			logger.debug(String.valueOf(response.toString()));
		} else {
			logger.error("Unsupported protocol: {}", serviceProtocol.toLowerCase());
			throw new LdapAuthenticationException("Internal server error");
		}

		if (response.getStatusCode() == HttpStatus.SC_OK) {

			return new LdapPrincipal(getDirectoryService().getSchemaManager(), bindContext.getDn(),
					AuthenticationLevel.SIMPLE);

		} else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
			logger.info("User {} is not authorized by idp", regId);
		} else {
			logger.warn("Statuscode bad: {}", response.getStatusCode());
			String responseString = response.getResponseString();
			logger.debug(responseString);
		}

		String message = I18n.err(I18n.ERR_230, bindContext.getDn().getName());
		logger.info(message);
		throw new LdapAuthenticationException(message);

	}

	@SuppressWarnings("unused")
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