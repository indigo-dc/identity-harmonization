/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.scim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.kit.scc.ldap.LdapClient;
import edu.kit.scc.scim.ScimUser.Name;

/**
 * SCIM service implementation.
 * 
 * @author benjamin
 *
 */
@Component
public class ScimService {

	@Autowired
	private LdapClient ldapClient;

	/**
	 * Creates a new user according to the provided SCIM object.
	 * 
	 * @param scimUser
	 *            the new user to be created
	 * @return a {@link ScimUser} with the user information of the created user.
	 */
	public ScimUser createUser(ScimUser scimUser) {
		String uid = scimUser.getUserName();
		Name name = scimUser.getName();

		String cn = name.getGivenName();
		String sn = name.getFamilyName();

		String homeDirectory = "/home/" + uid;

		String description = "INDIGO-DataCloud user";

		int uidNumber, gidNumber;

		return null;
	}
}
