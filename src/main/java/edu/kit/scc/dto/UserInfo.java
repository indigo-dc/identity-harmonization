/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {

	private List<String> userIdentities;

	private List<String> userGroups;

	public List<String> getUserIdentities() {
		return userIdentities;
	}

	public UserInfo setUserIdentities(List<String> userIdentities) {
		this.userIdentities = userIdentities;
		return this;
	}

	public List<String> getUserGroups() {
		return userGroups;
	}

	public UserInfo setUserGroups(List<String> userGroups) {
		this.userGroups = userGroups;
		return this;
	}

	@Override
	public String toString() {
		return "UserInfo [" + (userIdentities != null ? "userIdentities=" + userIdentities + ", " : "")
				+ (userGroups != null ? "userGroups=" + userGroups : "") + "]";
	}
}
