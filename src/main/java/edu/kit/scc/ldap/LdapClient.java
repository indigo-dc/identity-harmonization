package edu.kit.scc.ldap;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

import edu.kit.scc.dto.UserDTO;

@Component
public class LdapClient {

	private static Logger log = LoggerFactory.getLogger(LdapClient.class);

	@Value("${ldap.url}")
	private String url;

	@Value("${ldap.searchBase}")
	private String base;

	@Value("${ldap.bindDn}")
	private String dn;

	@Value("${ldap.bindPassword}")
	private String password;

	@Bean
	LdapContextSource contextSource() {
		LdapContextSource ldapContextSource = new LdapContextSource();
		ldapContextSource.setUrl(url);
		ldapContextSource.setBase(base);
		ldapContextSource.setUserDn(dn);
		ldapContextSource.setPassword(password);
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

	@Autowired
	private LDAPUserDAO ldapUser;

	public void getLdapUser() {
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
	}
}
