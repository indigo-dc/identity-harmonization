/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.ldap;

import java.util.Arrays;

public class PosixUser {
  String uniqueIdentifier;
  String uid;
  String commonName;
  String givenName;
  String surName;
  String homeDirectory;
  String description;
  String gecos;
  String loginShell;
  String mail;
  byte[] userPassword;

  String uidNumber;
  String gidNumber;

  public String getGecos() {
    return gecos;
  }

  public void setGecos(String gecos) {
    this.gecos = gecos;
  }

  public String getLoginShell() {
    return loginShell;
  }

  public void setLoginShell(String loginShell) {
    this.loginShell = loginShell;
  }

  public byte[] getUserPassword() {
    return userPassword;
  }

  public void setUserPassword(byte[] userPassword) {
    this.userPassword = userPassword;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public String getSurName() {
    return surName;
  }

  public void setSurName(String surName) {
    this.surName = surName;
  }

  public String getHomeDirectory() {
    return homeDirectory;
  }

  public void setHomeDirectory(String homeDirectory) {
    this.homeDirectory = homeDirectory;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getUidNumber() {
    return uidNumber;
  }

  public void setUidNumber(String uidNumber) {
    this.uidNumber = uidNumber;
  }

  public String getGidNumber() {
    return gidNumber;
  }

  public void setGidNumber(String gidNumber) {
    this.gidNumber = gidNumber;
  }

  public String getUniqueIdentifier() {
    return uniqueIdentifier;
  }

  public void setUniqueIdentifier(String uniqueIdentifier) {
    this.uniqueIdentifier = uniqueIdentifier;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getMail() {
    return mail;
  }

  public void setMail(String mail) {
    this.mail = mail;
  }

  @Override
  public String toString() {
    return "PosixUser ["
        + (uniqueIdentifier != null ? "uniqueIdentifier=" + uniqueIdentifier + ", " : "")
        + (uid != null ? "uid=" + uid + ", " : "")
        + (commonName != null ? "commonName=" + commonName + ", " : "")
        + (givenName != null ? "givenName=" + givenName + ", " : "")
        + (surName != null ? "surName=" + surName + ", " : "")
        + (homeDirectory != null ? "homeDirectory=" + homeDirectory + ", " : "")
        + (description != null ? "description=" + description + ", " : "")
        + (gecos != null ? "gecos=" + gecos + ", " : "")
        + (loginShell != null ? "loginShell=" + loginShell + ", " : "")
        + (mail != null ? "mail=" + mail + ", " : "")
        + (userPassword != null ? "userPassword=" + Arrays.toString(userPassword) + ", " : "")
        + "uidNumber=" + uidNumber + ", gidNumber=" + gidNumber + "]";
  }
}
