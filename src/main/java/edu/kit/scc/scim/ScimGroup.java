/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.scim;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimGroup {

  @JsonIgnore
  public static final String GROUP_SCHEMA_2_0 = "urn:ietf:params:scim:schemas:core:2.0:Group";

  private List<String> schemas;
  private String id;
  private String externalId;
  private String displayName;
  @JsonProperty(value = "$ref")
  private String ref;
  private String value;
  private String display;
  private HashMap<String, String> metadata;
  private List<Object> members;

  public List<String> getSchemas() {
    return schemas;
  }

  public void setSchemas(List<String> schemas) {
    this.schemas = schemas;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

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

  public HashMap<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(HashMap<String, String> metadata) {
    this.metadata = metadata;
  }

  public List<Object> getMembers() {
    return members;
  }

  public void setMembers(List<Object> members) {
    this.members = members;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ScimGroup [" + (schemas != null ? "schemas=" + schemas + ", " : "")
        + (id != null ? "id=" + id + ", " : "")
        + (externalId != null ? "externalId=" + externalId + ", " : "")
        + (displayName != null ? "displayName=" + displayName + ", " : "")
        + (ref != null ? "ref=" + ref + ", " : "") + (value != null ? "value=" + value + ", " : "")
        + (display != null ? "display=" + display + ", " : "")
        + (metadata != null ? "metadata=" + metadata + ", " : "")
        + (members != null ? "members=" + members : "") + "]";
  }
}
