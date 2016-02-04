/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.dto;

import java.util.List;

public class PosixGroup {

	String commonName;
	int gidNumber;
	List<String> memberUids;
	String description;
	String userPassword;

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public int getGidNumber() {
		return gidNumber;
	}

	public void setGidNumber(int gidNumber) {
		this.gidNumber = gidNumber;
	}

	public List<String> getMemberUids() {
		return memberUids;
	}

	public void setMemberUids(List<String> memberUids) {
		this.memberUids = memberUids;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	@Override
	public String toString() {
		return "PosixGroup [" + (commonName != null ? "commonName=" + commonName + ", " : "") + "gidNumber=" + gidNumber
				+ ", " + (memberUids != null ? "memberUids=" + memberUids + ", " : "")
				+ (description != null ? "description=" + description + ", " : "")
				+ (userPassword != null ? "userPassword=" + userPassword : "") + "]";
	}
}
