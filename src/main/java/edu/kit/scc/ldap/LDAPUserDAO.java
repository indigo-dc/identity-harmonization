package edu.kit.scc.ldap;

import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;

import edu.kit.scc.dao.UserDAO;
import edu.kit.scc.dto.UserDTO;

public class LDAPUserDAO implements UserDAO {

	private static final Logger log = LoggerFactory.getLogger(LDAPUserDAO.class);

	private LdapTemplate ldapTemplate;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	@Override
	public List<UserDTO> getAllUserNames() {
		return ldapTemplate.search("", "(objectclass=person)", new UserAttributeMapper());
	}

	@Override
	public List<UserDTO> getUserDetails(String commonName, String lastName) {
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("cn", commonName))
				.and(new EqualsFilter("sn", lastName));
		log.debug("LDAP query {}", andFilter.encode());

		return ldapTemplate.search("", andFilter.encode(), new UserAttributeMapper());
	}

	@Override
	public void insertUser(UserDTO userDTO) {

		BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
		personBasicAttribute.add("person");

		Attributes personAttributes = new BasicAttributes();
		personAttributes.put(personBasicAttribute);
		personAttributes.put("cn", userDTO.getCommonName());
		personAttributes.put("sn", userDTO.getLastName());
		personAttributes.put("description", userDTO.getDescription());

		LdapName newUserDN = LdapUtils.emptyLdapName();
		try {
			newUserDN = new LdapName("o=sshService");
			newUserDN.add("uid=" + userDTO.getCommonName());
			log.debug(newUserDN.toString());
			// ldapTemplate.bind(newUserDN, null, personAttributes);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateUser(UserDTO userDTO) {
		BasicAttribute personBasicAttribute = new BasicAttribute("objectclass");
		personBasicAttribute.add("person");

		Attributes personAttributes = new BasicAttributes();
		personAttributes.put(personBasicAttribute);
		personAttributes.put("cn", userDTO.getCommonName());
		personAttributes.put("sn", userDTO.getLastName());
		personAttributes.put("description", userDTO.getDescription());

		LdapName newUserDN = LdapUtils.emptyLdapName();
		try {
			newUserDN = new LdapName("o=sshService");
			newUserDN.add("uid=" + userDTO.getCommonName());
			log.debug(newUserDN.toString());
			// ldapTemplate.rebind(newUserDN, null, personAttributes);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void deleteUser(UserDTO userDTO) {
		LdapName newUserDN = LdapUtils.emptyLdapName();
		try {
			newUserDN = new LdapName("o=sshService");
			newUserDN.add("uid=" + userDTO.getCommonName());
			log.debug(newUserDN.toString());
			// ldapTemplate.unbind(newUserDN);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
