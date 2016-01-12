package edu.kit.scc.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

import edu.kit.scc.dto.UserDTO;

public class UserAttributeMapper implements AttributesMapper<UserDTO> {

	@Override
	public UserDTO mapFromAttributes(Attributes attributes) throws NamingException {
		UserDTO userDTO = new UserDTO();
		String commonName = (String) attributes.get("cn").get();
		if (commonName != null)
			userDTO.setCommonName(commonName);
		String lastName = (String) attributes.get("sn").get();
		if (lastName != null)
			userDTO.setLastName(lastName);
		Attribute description = attributes.get("description");
		if (description != null)
			userDTO.setDescription((String) description.get());
		return userDTO;
	}

}
