/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.saml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.ws.soap.client.BasicSOAPMessageContext;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.soap.client.http.HttpSOAPClient;
import org.opensaml.ws.soap.common.SOAPException;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * SAML client implementation.
 * 
 * @author benjamin
 *
 */
public class SamlClient {

	private static final Logger log = LoggerFactory.getLogger(SamlClient.class);

	private XMLObjectBuilderFactory builderFactory;
	private MarshallerFactory marshallerFactory;

	public SamlClient() {
		try {
			DefaultBootstrap.bootstrap();
			builderFactory = Configuration.getBuilderFactory();
			marshallerFactory = Configuration.getMarshallerFactory();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private String printXml(Element element) {
		String returnString = "";
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer xform;
		try {
			xform = tfactory.newTransformer();

			Source src = new DOMSource(element);
			StringWriter writer = new StringWriter();
			Result out = new StreamResult(writer);

			xform.transform(src, out);
			returnString = writer.toString();
			log.info(returnString);
			try {
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnString;
	}

	public void attributeResponse(String nameId, String issuerHost) {
		NameID nameID = (NameID) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME)
				.buildObject(NameID.DEFAULT_ELEMENT_NAME);
		nameID.setValue(nameId);
		nameID.setFormat(NameID.PERSISTENT);

		Subject subject = (Subject) builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME)
				.buildObject(Subject.DEFAULT_ELEMENT_NAME);
		subject.setNameID(nameID);

		Issuer issuer = (Issuer) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME)
				.buildObject(Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(issuerHost);

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("securityClearance", "C2");
		attributes.put("roles", "editor,reviewer");

		DateTime now = new DateTime();

		Assertion assertion = (Assertion) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME)
				.buildObject(Assertion.DEFAULT_ELEMENT_NAME);
		assertion.setID(getRandomId());
		assertion.setIssueInstant(now);
		assertion.setIssuer(issuer);
		assertion.setSubject(subject);

		Conditions conditions = (Conditions) builderFactory.getBuilder(Conditions.DEFAULT_ELEMENT_NAME)
				.buildObject(Conditions.DEFAULT_ELEMENT_NAME);
		conditions.setNotBefore(now.minusSeconds(10));
		conditions.setNotOnOrAfter(now.plusMinutes(30));
		assertion.setConditions(conditions);

		AttributeStatement statement = (AttributeStatement) builderFactory
				.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME)
				.buildObject(AttributeStatement.DEFAULT_ELEMENT_NAME);

		if (attributes != null)
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				XSAny element = (XSAny) builderFactory.getBuilder(XSAny.TYPE_NAME)
						.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
				element.setTextContent(entry.getValue());

				Attribute attribute = (Attribute) builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME)
						.buildObject(Attribute.DEFAULT_ELEMENT_NAME);
				attribute.setName(entry.getKey());
				attribute.getAttributeValues().add(element);

				statement.getAttributes().add(attribute);
			}

		assertion.getStatements().add(statement);

		Marshaller marshaller = marshallerFactory.getMarshaller(assertion);

		Element element;
		try {
			element = marshaller.marshall(assertion);
			log.debug(XMLHelper.prettyPrintXML(element));
		} catch (MarshallingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void attributeQuery(String username, String password, String nameId, String issuerHost) {

		NameID nameID = (NameID) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME)
				.buildObject(NameID.DEFAULT_ELEMENT_NAME);
		nameID.setValue(nameId);
		nameID.setFormat(NameID.PERSISTENT);

		Subject subject = (Subject) builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME)
				.buildObject(Subject.DEFAULT_ELEMENT_NAME);
		subject.setNameID(nameID);

		Issuer issuer = (Issuer) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME)
				.buildObject(Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(issuerHost);

		AttributeQuery attrQuery = (AttributeQuery) builderFactory.getBuilder(AttributeQuery.DEFAULT_ELEMENT_NAME)
				.buildObject(AttributeQuery.DEFAULT_ELEMENT_NAME);
		String id = getRandomId();
		attrQuery.setID(id);
		attrQuery.setSubject(subject);
		attrQuery.setVersion(SAMLVersion.VERSION_20);
		attrQuery.setIssueInstant(new DateTime());
		attrQuery.setIssuer(issuer);

		// AuthnRequest attrQuery = (AuthnRequest)
		// builderFactory.getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME)
		// .buildObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
		// attrQuery.setID(getRandomId());
		// attrQuery.setVersion(SAMLVersion.VERSION_20);
		// attrQuery.setIssueInstant(new DateTime());
		// attrQuery.setIssuer(issuer);
		// attrQuery.setForceAuthn(false);
		// attrQuery.setIsPassive(false);
		// attrQuery.setProtocolBinding(SAMLConstants.SAML2_PAOS_BINDING_URI);
		// attrQuery.setAssertionConsumerServiceURL("https://ldf.data.kit.edu/Shibboleth.sso/SAML2/POST");
		//
		// NameIDPolicy idPolicy =
		// (NameIDPolicy)builderFactory.getBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME)
		// .buildObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
		// idPolicy.setAllowCreate(true);
		// attrQuery.setNameIDPolicy(idPolicy);

		Marshaller marshaller = marshallerFactory.getMarshaller(attrQuery);

		Element element;
		try {
			element = marshaller.marshall(attrQuery);
			log.debug(XMLHelper.prettyPrintXML(element));
		} catch (MarshallingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Envelope envelope = (Envelope) builderFactory.getBuilder(Envelope.DEFAULT_ELEMENT_NAME)
				.buildObject(Envelope.DEFAULT_ELEMENT_NAME);
		Body body = (Body) builderFactory.getBuilder(Body.DEFAULT_ELEMENT_NAME).buildObject(Body.DEFAULT_ELEMENT_NAME);
		body.getUnknownXMLObjects().add(attrQuery);
		envelope.setBody(body);

		BasicSOAPMessageContext soapMessageContext = new BasicSOAPMessageContext();
		soapMessageContext.setOutboundMessage(envelope);

		BasicX509Credential signingCredential;

		X509Certificate cert = getCertificate();
		PrivateKey privKey = getPrivateKey();

		if (cert == null || privKey == null)
			return;

		signingCredential = SecurityHelper.getSimpleCredential(cert, privKey);

		HttpClientBuilder clientBuilder = new HttpClientBuilder();

		Signature signature = (Signature) builderFactory.getBuilder(Signature.DEFAULT_ELEMENT_NAME)
				.buildObject(Signature.DEFAULT_ELEMENT_NAME);
		X509KeyInfoGeneratorFactory keyInfofc = (X509KeyInfoGeneratorFactory) Configuration
				.getGlobalSecurityConfiguration().getKeyInfoGeneratorManager().getDefaultManager()
				.getFactory(signingCredential);
		keyInfofc.setEmitEntityCertificate(false);
		keyInfofc.setEmitEntityCertificateChain(false);
		KeyInfoGenerator keyInfogen = keyInfofc.newInstance();
		try {
			KeyInfo keyInfo = keyInfogen.generate(signingCredential);

			signature.setSigningCredential(signingCredential);
			signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
			signature.setCanonicalizationAlgorithm(SignatureConstants.TRANSFORM_C14N_EXCL_WITH_COMMENTS);
			signature.setKeyInfo(keyInfo);

			attrQuery.setSignature(signature);

			Map<String, Boolean> newFeatures = new HashMap<String, Boolean>();
			newFeatures.put("http://apache.org/xml/features/disallow-doctype-decl", false);
			BasicParserPool pool = new BasicParserPool();
			pool.setNamespaceAware(true);
			pool.setBuilderFeatures(newFeatures);

			org.apache.commons.httpclient.HttpClient httpClient = clientBuilder.buildClient();
			httpClient.getParams().setAuthenticationPreemptive(true);
			httpClient.getState().setCredentials(new AuthScope("idp.scc.kit.edu", 443, AuthScope.ANY_REALM),
					new UsernamePasswordCredentials(username, password));
			// httpClient.getState().addCookie(new Cookie("idp.scc.kit.edu",
			// "JSESSIONID", getRandomId().substring(1),
			// "/idp/profile/SAML2/SOAP/ECP", 3600, true));
			HttpSOAPClient client = new HttpSOAPClient(httpClient, pool);
			// HttpSOAPClient client = new HttpSOAPClient(httpClient, pool);
			client.send("https://idp.scc.kit.edu/idp/profile/SAML2/SOAP/ECP", soapMessageContext);

			Envelope response = (Envelope) soapMessageContext.getInboundMessage();
			Body responseBody = response.getBody();
			List<XMLObject> xmlObjects = responseBody.getUnknownXMLObjects();
			Response re = (Response) xmlObjects.get(0);

			element = marshaller.marshall(re);
			log.info(XMLHelper.prettyPrintXML(element));

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (MarshallingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getRandomId() {
		String randomId = "";
		try {
			SecureRandomIdentifierGenerator randomGen = new SecureRandomIdentifierGenerator();
			randomId = randomGen.generateIdentifier();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return randomId;
	}

	private PrivateKey getPrivateKey() {
		RandomAccessFile raf = null;
		PrivateKey privKey = null;
		try {
			raf = new RandomAccessFile("client.pk8", "r");
			byte[] buf = new byte[(int) raf.length()];
			raf.readFully(buf);
			PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(buf);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			privKey = kf.generatePrivate(kspec);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return privKey;
	}

	private X509Certificate getCertificate() {
		InputStream in = null;
		X509Certificate cert = null;
		try {
			in = new FileInputStream("client.crt");
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) cf.generateCertificate(in);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return cert;
	}
}
