package edu.kit.scc.http;

public class HttpResponse {

	public int statusCode;

	public byte[] response;

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
