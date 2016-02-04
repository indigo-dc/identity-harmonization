/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.scim;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimListResponse {

	private List<String> schemas;
	private int totalResults;
	private List<ScimUser> Resources;

	@JsonIgnore
	public final String LIST_RESPONSE_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:ListResponse";

	public List<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public List<ScimUser> getResources() {
		return Resources;
	}

	public void setResources(List<ScimUser> resources) {
		Resources = resources;
	}

	@Override
	public String toString() {
		return "ScimListResponse [" + (schemas != null ? "schemas=" + schemas + ", " : "") + "totalResults="
				+ totalResults + ", " + (Resources != null ? "Resources=" + Resources : "") + "]";
	}
}
