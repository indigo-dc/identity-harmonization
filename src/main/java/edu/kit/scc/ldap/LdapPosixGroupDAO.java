/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.ldap;

import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.AttributeInUseException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;

import edu.kit.scc.dao.PosixGroupDAO;
import edu.kit.scc.dto.PosixGroup;

public class LdapPosixGroupDAO implements PosixGroupDAO {
	private static final Logger log = LoggerFactory.getLogger(LdapPosixGroupDAO.class);

	private LdapTemplate ldapTemplate;

	private String groupBase;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setGroupBase(String groupBase) {
		this.groupBase = groupBase;
	}

	@Override
	public List<PosixGroup> getAllGroups() {
		return ldapTemplate.search(groupBase, "(objectclass=posixGroup)", new LdapPosixGroupAttributeMapper());

	}

	@Override
	public List<PosixGroup> getGroupDetails(String commonName) {
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "posixGroup")).and(new EqualsFilter("cn", commonName));
		log.debug("LDAP query {}", andFilter.encode());

		return ldapTemplate.search("", andFilter.encode(), new LdapPosixGroupAttributeMapper());
	}

	public List<PosixGroup> getGroupDetails(int gidNumber) {
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "posixGroup")).and(new EqualsFilter("gidNumber", gidNumber));
		log.debug("LDAP query {}", andFilter.encode());

		return ldapTemplate.search("", andFilter.encode(), new LdapPosixGroupAttributeMapper());
	}

	@Override
	public void insertGroup(PosixGroup group) {
		BasicAttribute posixGroupBasicAttribute = new BasicAttribute("objectclass");
		posixGroupBasicAttribute.add("posixGroup");

		Attributes posixGroupAttributes = new BasicAttributes();
		posixGroupAttributes.put(posixGroupBasicAttribute);
		posixGroupAttributes.put("cn", group.getCommonName());
		posixGroupAttributes.put("gidNumber", String.valueOf(group.getGidNumber()));

		if (group.getUserPassword() != null)
			posixGroupAttributes.put("userPassword", group.getUserPassword());
		if (group.getDescription() != null)
			posixGroupAttributes.put("description", group.getDescription());

		LdapName newGroupDN = LdapUtils.emptyLdapName();
		try {
			newGroupDN = new LdapName(groupBase);
			newGroupDN.add("cn=" + group.getCommonName());
			log.debug("Insert {}", newGroupDN.toString());
			ldapTemplate.bind(newGroupDN, null, posixGroupAttributes);
		} catch (NameAlreadyBoundException e) {
			log.error("ERROR {}", e.getMessage());
		} catch (InvalidNameException e) {
			log.error("ERROR {}", e.getMessage());
		}
	}

	@Override
	public void updateGroup(PosixGroup group) {
		BasicAttribute posixGroupBasicAttribute = new BasicAttribute("objectclass");
		posixGroupBasicAttribute.add("posixGroup");

		Attributes posixGroupAttributes = new BasicAttributes();
		posixGroupAttributes.put(posixGroupBasicAttribute);
		posixGroupAttributes.put("cn", group.getCommonName());
		posixGroupAttributes.put("gidNumber", String.valueOf(group.getGidNumber()));

		if (group.getUserPassword() != null)
			posixGroupAttributes.put("userPassword", group.getUserPassword());
		if (group.getDescription() != null)
			posixGroupAttributes.put("description", group.getDescription());

		LdapName groupDN = LdapUtils.emptyLdapName();
		try {
			groupDN = new LdapName(groupBase);
			groupDN.add("cn=" + group.getCommonName());
			log.debug("Update {}", groupDN.toString());
			ldapTemplate.bind(groupDN, null, posixGroupAttributes);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteGroup(PosixGroup group) {
		LdapName groupDN = LdapUtils.emptyLdapName();
		try {
			groupDN = new LdapName(groupBase);
			groupDN.add("cn=" + group.getCommonName());
			log.debug("Delete {}", groupDN.toString());
			ldapTemplate.unbind(groupDN);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
	}

	public void addMember(PosixGroup group, String memberUid) {
		ModificationItem[] modificationItems = new ModificationItem[] {
				new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", memberUid)) };
		LdapName groupDN = LdapUtils.emptyLdapName();
		try {
			groupDN = new LdapName(groupBase);
			groupDN.add("cn=" + group.getCommonName());
			log.debug("Add member {} to {}", memberUid, groupDN.toString());
			ldapTemplate.modifyAttributes(groupDN, modificationItems);
		} catch (AttributeInUseException e) {
			log.error("ERROR {}", e.getMessage());
		} catch (InvalidNameException e) {
			log.error("ERROR {}", e.getMessage());
		}
	}

	public void removeMember(PosixGroup group, String memberUid) {
		ModificationItem[] modificationItems = new ModificationItem[] {
				new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", memberUid)) };
		LdapName groupDN = LdapUtils.emptyLdapName();
		try {
			groupDN = new LdapName(groupBase);
			groupDN.add("cn=" + group.getCommonName());
			log.debug("Remove member {} from {}", memberUid, groupDN.toString());
			ldapTemplate.modifyAttributes(groupDN, modificationItems);
		} catch (AttributeInUseException e) {
			log.error("ERROR {}", e.getMessage());
		} catch (InvalidNameException e) {
			log.error("ERROR {}", e.getMessage());
		}
	}

	public List<PosixGroup> getUserGroups(String uid) {
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "posixGroup")).and(new EqualsFilter("memberUid", uid));
		log.debug("LDAP query {}", andFilter.encode());

		return ldapTemplate.search("", andFilter.encode(), new LdapPosixGroupAttributeMapper());
	}
}
