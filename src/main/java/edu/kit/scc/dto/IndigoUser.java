/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.dto;

public class IndigoUser extends PosixUser {

	String indigoId;

	public String getIndigoId() {
		return indigoId;
	}

	public void setIndigoId(String indigoId) {
		this.indigoId = indigoId;
	}

	@Override
	public String toString() {
		return "IndigoUser [" + (indigoId != null ? "indigoId=" + indigoId + ", " : "")
				+ (uid != null ? "uid=" + uid + ", " : "")
				+ (commonName != null ? "commonName=" + commonName + ", " : "")
				+ (surName != null ? "surName=" + surName + ", " : "")
				+ (homeDirectory != null ? "homeDirectory=" + homeDirectory + ", " : "")
				+ (description != null ? "description=" + description + ", " : "")
				+ (gecos != null ? "gecos=" + gecos + ", " : "")
				+ (loginShell != null ? "loginShell=" + loginShell + ", " : "")
				+ (userPassword != null ? "userPassword=" + userPassword + ", " : "") + "uidNumber=" + uidNumber
				+ ", gidNumber=" + gidNumber + "]";
	}
}