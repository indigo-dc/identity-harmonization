package edu.kit.scc.scim;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimError {

  @JsonIgnore
  public static final String ERROR_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:Error";

  private List<String> schemas;
  private String status;
  private String scimType;
  private String detail;

  /**
   * Get schemas.
   * 
   * @return the schemas
   */
  public List<String> getSchemas() {
    return schemas;
  }

  /**
   * Set schemas.
   * 
   * @param schemas the schemas to set
   */
  public void setSchemas(List<String> schemas) {
    this.schemas = schemas;
  }

  /**
   * Get status.
   * 
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Set status.
   * 
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Get scimType.
   * 
   * @return the scimType
   */
  public String getScimType() {
    return scimType;
  }

  /**
   * Set scimType.
   * 
   * @param scimType the scimType to set
   */
  public void setScimType(String scimType) {
    this.scimType = scimType;
  }

  /**
   * Get detail.
   * 
   * @return the detail
   */
  public String getDetail() {
    return detail;
  }

  /**
   * Set detail.
   * 
   * @param detail the detail to set
   */
  public void setDetail(String detail) {
    this.detail = detail;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ScimError [" + (schemas != null ? "schemas=" + schemas + ", " : "")
        + (status != null ? "status=" + status + ", " : "")
        + (scimType != null ? "scimType=" + scimType + ", " : "")
        + (detail != null ? "detail=" + detail : "") + "]";
  }
}
