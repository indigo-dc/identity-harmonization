package edu.kit.scc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

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

		ApplicationContext ctx = new ClassPathXmlApplicationContext("springldap.xml");
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
