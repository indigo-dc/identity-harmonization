/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.dto;

import java.util.List;

public class GroupDTO {

	String commonName;
	int gidNumber;
	List<String> memberUids;

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

	@Override
	public String toString() {
		return "GroupDTO [" + (commonName != null ? "commonName=" + commonName + ", " : "") + "gidNumber=" + gidNumber
				+ ", " + (memberUids != null ? "memberUids=" + memberUids : "") + "]";
	}
}
