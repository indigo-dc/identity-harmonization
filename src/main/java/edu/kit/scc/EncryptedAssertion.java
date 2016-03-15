package edu.kit.scc;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class EncryptedAssertion {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private byte[] key;
	private byte[] iv;
	private String base64Assertion;

	protected EncryptedAssertion() {
	}

	public EncryptedAssertion(byte[] key, byte[] iv, String base64Assertion) {
		this.key = key;
		this.iv = iv;
		this.base64Assertion = base64Assertion;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public byte[] getIv() {
		return iv;
	}

	public void setIv(byte[] iv) {
		this.iv = iv;
	}

	public String getBase64Assertion() {
		return base64Assertion;
	}

	public void setBase64Assertion(String base64Assertion) {
		this.base64Assertion = base64Assertion;
	}

	@Override
	public String toString() {
		return String.format("Assertion[id=%d, assertion='%s']", id, base64Assertion);
	}
}
