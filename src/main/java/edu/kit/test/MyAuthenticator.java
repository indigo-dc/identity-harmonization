package edu.kit.test;

import java.net.SocketAddress;

import org.apache.directory.api.ldap.model.constants.AuthenticationLevel;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapAuthenticationException;
import org.apache.directory.server.core.api.LdapPrincipal;
import org.apache.directory.server.core.api.entry.ClonedServerEntry;
import org.apache.directory.server.core.api.interceptor.context.BindOperationContext;
import org.apache.directory.server.core.api.interceptor.context.LookupOperationContext;
import org.apache.directory.server.core.authn.AbstractAuthenticator;
import org.apache.directory.server.core.authn.SimpleAuthenticator;
import org.apache.directory.server.i18n.I18n;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAuthenticator extends AbstractAuthenticator {

	private static final Logger LOG = LoggerFactory.getLogger(MyAuthenticator.class);

	private SimpleAuthenticator delegatedAuth;
	private boolean disabled;

	public MyAuthenticator() {
		super(AuthenticationLevel.SIMPLE);
		delegatedAuth = new SimpleAuthenticator();
		LOG.info("MyAuthenticator has been created");
	}

	@Override
	protected void doInit() {
		super.doInit();
		LOG.info("Init called");
		if (getDirectoryService() != null) {
			try {
				delegatedAuth.init(getDirectoryService());
			} catch (Exception e) {
				LOG.error("Exception initializing MyAuthenticator", e);
				disabled = true;
			}
		} else {
			LOG.info("doInit w/o directory service");
		}
	}

	@Override
	public LdapPrincipal authenticate(BindOperationContext bindContext) throws Exception {
		if (disabled) {
			LOG.info("Skipping " + bindContext.getDn());
			if (delegatedAuth == null) {
				LOG.error("Delegated auth is null");
				return null;
			}
			return delegatedAuth.authenticate(bindContext);
		}

		LOG.info("Authenticating " + bindContext.getDn());

		byte[] password = bindContext.getCredentials();

		LOG.info("Password " + new String(password));

		if (true) {
			LookupOperationContext lookupContext = new LookupOperationContext(getDirectoryService().getAdminSession(),
					bindContext.getDn(), SchemaConstants.ALL_USER_ATTRIBUTES,
					SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES);

			Entry userEntry = getDirectoryService().getPartitionNexus().lookup(lookupContext);

			LOG.info(userEntry.toString());

			password = "secret".getBytes();
			LOG.info("New password " + new String(password));

			LdapPrincipal principal = new LdapPrincipal(getDirectoryService().getSchemaManager(), bindContext.getDn(),
					AuthenticationLevel.SIMPLE, password);
			IoSession session = bindContext.getIoSession();
			if (session != null) {
				SocketAddress clientAddress = session.getRemoteAddress();
				principal.setClientAddress(clientAddress);
				SocketAddress serverAddress = session.getServiceAddress();
				principal.setServerAddress(serverAddress);
			}
			bindContext.setEntry(new ClonedServerEntry(userEntry));
			return principal;
		} else {
			String message = I18n.err(I18n.ERR_230, bindContext.getDn().getName());
			LOG.info(message);
			throw new LdapAuthenticationException(message);
		}
	}
}
