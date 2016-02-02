/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.scim;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimUser1_0 {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Name {
		private String familyName, givenName;

		public String getFamilyName() {
			return familyName;
		}

		public void setFamilyName(String familyName) {
			this.familyName = familyName;
		}

		public String getGivenName() {
			return givenName;
		}

		public void setGivenName(String givenName) {
			this.givenName = givenName;
		}

		@Override
		public String toString() {
			return "Name [" + (familyName != null ? "familyName=" + familyName + ", " : "")
					+ (givenName != null ? "givenName=" + givenName : "") + "]";
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Group {
		private String value, display;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getDisplay() {
			return display;
		}

		public void setDisplay(String display) {
			this.display = display;
		}

		@Override
		public String toString() {
			return "Group [" + (value != null ? "value=" + value + ", " : "")
					+ (display != null ? "display=" + display : "") + "]";
		}
	}

	public static class Meta extends HashMap<String, String> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	private String id, userName;
	private List<String> emails;
	private Name name;
	private List<Group> groups;
	private Meta meta;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	@Override
	public String toString() {
		return "ScimUser1_0 [" + (id != null ? "id=" + id + ", " : "")
				+ (userName != null ? "userName=" + userName + ", " : "")
				+ (emails != null ? "emails=" + emails + ", " : "") + (name != null ? "name=" + name + ", " : "")
				+ (groups != null ? "groups=" + groups + ", " : "") + (meta != null ? "meta=" + meta : "") + "]";
	}
}
