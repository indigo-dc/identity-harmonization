/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.http;

public class HttpResponse {

  public int statusCode;

  public byte[] response;

  /**
   * Base class for HTTP responses.
   * 
   * @param statusCode the responses' status code
   * @param response the response
   */
  public HttpResponse(int statusCode, byte[] response) {
    super();
    this.statusCode = statusCode;
    this.response = response;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public byte[] getResponse() {
    return response;
  }

  public String getResponseString() {
    return new String(response);
  }

  @Override
  public String toString() {
    return "HttpResponse [statusCode=" + statusCode + ", "
        + (response != null ? "response=" + new String(response) : "") + "]";
  }

}
