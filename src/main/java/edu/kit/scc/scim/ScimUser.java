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

import java.util.HashMap;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimUser {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Name {
    private String formatted;
    private String familyName;
    private String givenName;
    private String middleName;
    private String honorificPrefix;
    private String honorificSufix;

    public String getFormatted() {
      return formatted;
    }

    public void setFormatted(String formatted) {
      this.formatted = formatted;
    }

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

    public String getMiddleName() {
      return middleName;
    }

    public void setMiddleName(String middleName) {
      this.middleName = middleName;
    }

    public String getHonorificPrefix() {
      return honorificPrefix;
    }

    public void setHonorificPrefix(String honorificPrefix) {
      this.honorificPrefix = honorificPrefix;
    }

    public String getHonorificSufix() {
      return honorificSufix;
    }

    public void setHonorificSufix(String honorificSufix) {
      this.honorificSufix = honorificSufix;
    }

    @Override
    public String toString() {
      return "Name [" + (formatted != null ? "formatted=" + formatted + ", " : "")
          + (familyName != null ? "familyName=" + familyName + ", " : "")
          + (givenName != null ? "givenName=" + givenName + ", " : "")
          + (middleName != null ? "middleName=" + middleName + ", " : "")
          + (honorificPrefix != null ? "honorificPrefix=" + honorificPrefix + ", " : "")
          + (honorificSufix != null ? "honorificSufix=" + honorificSufix : "") + "]";
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Email {
    private String value;
    private String type;
    private boolean primary;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public boolean isPrimary() {
      return primary;
    }

    public void setPrimary(boolean primary) {
      this.primary = primary;
    }

    @Override
    public String toString() {
      return "Email [" + (value != null ? "value=" + value + ", " : "")
          + (type != null ? "type=" + type + ", " : "") + "primary=" + primary + "]";
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Address {
    private String type;
    private String streetAddress;
    private String locality;
    private String region;
    private String postalCode;
    private String country;
    private String formatted;
    private boolean primary;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getStreetAddress() {
      return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
      this.streetAddress = streetAddress;
    }

    public String getLocality() {
      return locality;
    }

    public void setLocality(String locality) {
      this.locality = locality;
    }

    public String getRegion() {
      return region;
    }

    public void setRegion(String region) {
      this.region = region;
    }

    public String getPostalCode() {
      return postalCode;
    }

    public void setPostalCode(String postalCode) {
      this.postalCode = postalCode;
    }

    public String getCountry() {
      return country;
    }

    public void setCountry(String country) {
      this.country = country;
    }

    public String getFormatted() {
      return formatted;
    }

    public void setFormatted(String formatted) {
      this.formatted = formatted;
    }

    public boolean isPrimary() {
      return primary;
    }

    public void setPrimary(boolean primary) {
      this.primary = primary;
    }

    @Override
    public String toString() {
      return "Address [" + (type != null ? "type=" + type + ", " : "")
          + (streetAddress != null ? "streetAddress=" + streetAddress + ", " : "")
          + (locality != null ? "locality=" + locality + ", " : "")
          + (region != null ? "region=" + region + ", " : "")
          + (postalCode != null ? "postalCode=" + postalCode + ", " : "")
          + (country != null ? "country=" + country + ", " : "")
          + (formatted != null ? "formatted=" + formatted + ", " : "") + "primary=" + primary + "]";
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class PhoneNumber {
    private String value;
    private String type;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return "PhoneNumber [" + (value != null ? "value=" + value + ", " : "")
          + (type != null ? "type=" + type : "") + "]";
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Ims {
    private String value;
    private String type;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return "Ims [" + (value != null ? "value=" + value + ", " : "")
          + (type != null ? "type=" + type : "") + "]";
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Photo {
    private String value;
    private String type;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return "Photo [" + (value != null ? "value=" + value + ", " : "")
          + (type != null ? "type=" + type : "") + "]";
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class X509Certificate {
    private String value;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return "x509Certificate [" + (value != null ? "value=" + value : "") + "]";
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Meta extends HashMap<String, String> {

    /**
     * Default serial id.
     * 
     */
    private static final long serialVersionUID = 1L;

  }

  @JsonIgnore
  public static final String CORE_SCHEMA_1_0 = "urn:scim:schemas:core:1.0";

  @JsonIgnore
  public static final String USER_SCHEMA_2_0 = "urn:ietf:params:scim:schemas:core:2.0:User";

  private List<String> schemas;
  private String id;
  private String externalId;
  private String userName;
  private String displayName;
  private String nickName;
  private String profileUrl;
  private String userType;
  private String title;
  private String preferredLanguage;
  private String locale;
  private String timezone;
  private String password;
  private boolean active;
  private Name name;
  private List<Email> emails;
  private List<Address> addresses;
  private List<PhoneNumber> phoneNumbers;
  private List<Ims> ims;
  private List<Photo> photos;
  private List<ScimGroup> groups;
  private Meta meta;

  public Name getName() {
    return name;
  }

  public void setName(Name name) {
    this.name = name;
  }

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

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }

  public String getProfileUrl() {
    return profileUrl;
  }

  public void setProfileUrl(String profileUrl) {
    this.profileUrl = profileUrl;
  }

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPreferredLanguage() {
    return preferredLanguage;
  }

  public void setPreferredLanguage(String preferredLanguage) {
    this.preferredLanguage = preferredLanguage;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public List<Email> getEmails() {
    return emails;
  }

  public void setEmails(List<Email> emails) {
    this.emails = emails;
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Address> addresses) {
    this.addresses = addresses;
  }

  public List<PhoneNumber> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public List<Ims> getIms() {
    return ims;
  }

  public void setIms(List<Ims> ims) {
    this.ims = ims;
  }

  public List<Photo> getPhotos() {
    return photos;
  }

  public void setPhotos(List<Photo> photos) {
    this.photos = photos;
  }

  public List<ScimGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<ScimGroup> groups) {
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
    return "ScimUser [" + (schemas != null ? "schemas=" + schemas + ", " : "")
        + (id != null ? "id=" + id + ", " : "")
        + (externalId != null ? "externalId=" + externalId + ", " : "")
        + (userName != null ? "userName=" + userName + ", " : "")
        + (displayName != null ? "displayName=" + displayName + ", " : "")
        + (nickName != null ? "nickName=" + nickName + ", " : "")
        + (profileUrl != null ? "profileUrl=" + profileUrl + ", " : "")
        + (userType != null ? "userType=" + userType + ", " : "")
        + (title != null ? "title=" + title + ", " : "")
        + (preferredLanguage != null ? "preferredLanguage=" + preferredLanguage + ", " : "")
        + (locale != null ? "locale=" + locale + ", " : "")
        + (timezone != null ? "timezone=" + timezone + ", " : "")
        + (password != null ? "password=" + password + ", " : "") + "active=" + active + ", "
        + (name != null ? "name=" + name + ", " : "")
        + (emails != null ? "emails=" + emails + ", " : "")
        + (addresses != null ? "addresses=" + addresses + ", " : "")
        + (phoneNumbers != null ? "phoneNumbers=" + phoneNumbers + ", " : "")
        + (ims != null ? "ims=" + ims + ", " : "")
        + (photos != null ? "photos=" + photos + ", " : "")
        + (groups != null ? "groups=" + groups + ", " : "") + (meta != null ? "meta=" + meta : "")
        + "]";
  }
}
