package edu.kit.scc;

import java.util.List;

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

import edu.kit.scc.dao.UserDAO;
import edu.kit.scc.dto.UserDTO;
import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;
import edu.kit.scc.ldap.LDAPUserDAO;

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

	public static void main(String[] args) {

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
		ApplicationContext ctx = new AnnotationConfigApplicationContext(Main.class);
		UserDAO ldapUser = (LDAPUserDAO) ctx.getBean("ldapUser");
		List<UserDTO> userList = ldapUser.getAllUserNames();
		for (int i = 0; i < userList.size(); i++)
			log.info("User name {}", ((UserDTO) userList.get(i)).getCommonName());
		List<UserDTO> userDetails = ldapUser.getUserDetails("John Smith", "Smith");
		for (int i = 0; i < userDetails.size(); i++)
			log.info("Description {}", ((UserDTO) userDetails.get(i)).getDescription());

		UserDTO newUser = new UserDTO();
		newUser.setCommonName("me");
		newUser.setLastName("too");
		ldapUser.insertUser(newUser);

		((AbstractApplicationContext) ctx).close();
	}
}
