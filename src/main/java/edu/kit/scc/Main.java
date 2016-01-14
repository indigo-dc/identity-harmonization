package edu.kit.scc;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.ssl.X509CertificateChainBuilder;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.ws.soap.client.BasicSOAPMessageContext;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.soap.client.http.HttpSOAPClient;
import org.opensaml.ws.soap.common.SOAPException;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.w3c.dom.Element;

import edu.kit.scc.dao.UserDAO;
import edu.kit.scc.dto.UserDTO;
import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;
import edu.kit.scc.ldap.LDAPUserDAO;
import edu.kit.scc.saml.HttpSignableSoapClient;
import edu.kit.scc.saml.SamlClient;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	// private static String authorizationCode =
	// "997fc6c123b3224b3ade247ea8376164";
	// private static String accessToken = "88e3bd6a549e385926378129b330c";

	@Bean
	LdapContextSource contextSource() {
		LdapContextSource ldapContextSource = new LdapContextSource();
		ldapContextSource.setUrl("ldap://192.168.122.202:10389");
		ldapContextSource.setBase("o=sshService");
		ldapContextSource.setUserDn("uid=admin,ou=system");
		ldapContextSource.setPassword("secret");
		return ldapContextSource;
	}

	@Bean
	LdapTemplate ldapTemplate(LdapContextSource contextSource) {
		return new LdapTemplate(contextSource);
	}

	@Bean
	LDAPUserDAO ldapUser(LdapTemplate ldapTemplate) {
		LDAPUserDAO ldapUserDAO = new LDAPUserDAO();
		ldapUserDAO.setLdapTemplate(ldapTemplate);
		return ldapUserDAO;
	}

	public static void main(String[] args) throws ConfigurationException, MarshallingException, TransformerException,
			CertificateException, javax.security.cert.CertificateException, IOException, NoSuchAlgorithmException,
			InvalidKeySpecException, SecurityException, SOAPException {

		// OidcClient oidcClient = new OidcClient(clientId, clientSecret,
		// redirectUri, oidcTokenEndpoint,
		// oidcUserInfoEndpoint);
		//
		// Tokens tokens = oidcClient.requestTokens(authorizationCode);
		//
		// oidcClient.requestUserInfo(tokens.getAccessToken().getValue());

		// Utils.printProperties();
		// ScimClient scimClient = new ScimClient();
		// scimClient.getUsers("admin", "admin");
		// scimClient.getGroups("admin", "admin");

		// Utils.printProperties();
		// HttpClient client = new HttpClient();
		// HttpResponse response =
		// client.makeHTTPPostRequest("password=password",
		// "http://localhost:50070");
		// log.debug(response.toString());
		// client.makePOST("localhost", 50070, "user", "password",
		// "http://localhost:50070");

		// Resource resource = new ClassPathResource("springldap.xml");
		// BeanFactory factory = new XmlBeanFactory(resource);

		// ApplicationContext ctx = new
		// ClassPathXmlApplicationContext("springldap.xml");
		// ApplicationContext ctx = new
		// AnnotationConfigApplicationContext(Main.class);
		// UserDAO ldapUser = (LDAPUserDAO) ctx.getBean("ldapUser");
		// List<UserDTO> userList = ldapUser.getAllUserNames();
		// for (int i = 0; i < userList.size(); i++)
		// log.info("User name {}", ((UserDTO)
		// userList.get(i)).getCommonName());
		// List<UserDTO> userDetails = ldapUser.getUserDetails("John Smith",
		// "Smith");
		// for (int i = 0; i < userDetails.size(); i++)
		// log.info("Description {}", ((UserDTO)
		// userDetails.get(i)).getDescription());
		//
		// UserDTO newUser = new UserDTO();
		// newUser.setCommonName("me");
		// newUser.setLastName("too");
		// ldapUser.insertUser(newUser);
		//
		// ((AbstractApplicationContext) ctx).close();

		SamlClient client = new SamlClient();
		client.attributeQuery("ym0762", "oo22-.22", "MdWhcFYwml0vPFMscox33AYkkgs=", "https://ldf.data.kit.edu/sp");
	}
}
