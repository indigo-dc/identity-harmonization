/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.saml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.ecp.Request;
import org.opensaml.saml2.ecp.impl.ResponseMarshaller;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.security.MetadataCriteria;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.ws.soap.client.BasicSOAPMessageContext;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.soap.client.http.HttpSOAPClient;
import org.opensaml.ws.soap.client.http.TLSProtocolSocketFactory;
import org.opensaml.ws.soap.common.SOAPException;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.security.keyinfo.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.keyinfo.provider.RSAKeyValueProvider;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.X509SubjectName;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

/**
 * SAML client implementation.
 * 
 * @author benjamin
 *
 */
@Component
public class SamlClient {

	private static final Logger log = LoggerFactory.getLogger(SamlClient.class);
	private X509Certificate certificate;
	private PrivateKey pK;
	private PublicKey pubK;

	public SamlClient() {
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void logXmlElement(XMLObject object) {
		Element element;
		try {
			element = Configuration.getMarshallerFactory().getMarshaller(object).marshall(object);
			log.debug("Build {}\n{}", object.getClass().getName(), XMLHelper.prettyPrintXML(element));
		} catch (MarshallingException e) {
			e.printStackTrace();
		}
	}

	public Assertion buildAssertion() {
		Assertion assertion = (Assertion) Configuration.getBuilderFactory().getBuilder(Assertion.DEFAULT_ELEMENT_NAME)
				.buildObject(Assertion.DEFAULT_ELEMENT_NAME);

		Issuer issuer = (Issuer) Configuration.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME)
				.buildObject(Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue("reg-app");

		assertion.setIssuer(issuer);

		logXmlElement(assertion);

		return assertion;
	}

	public X509Certificate readIdpCert() {
		URL url = Thread.currentThread().getContextClassLoader().getResource("idp.scc.kit.edu");
		Path certificateFile = Paths.get(url.getPath());
		try {
			byte[] certificateBytes = Files.readAllBytes(certificateFile);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(certificateBytes);// cert.getBytes(StandardCharsets.UTF_8));
			Certificate ca = cf.generateCertificate(in);

			return (X509Certificate) ca;
		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
		}
		return null;
	}

	public Credential getSigningCredential() {
		try {
			URL url = Thread.currentThread().getContextClassLoader().getResource("pkcs8_key");
			Path privateKeyFile = Paths.get(url.getPath());

			url = Thread.currentThread().getContextClassLoader().getResource("cert.pem");
			Path certificateFile = Paths.get(url.getPath());

			byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile);

			byte[] certificateBytes = Files.readAllBytes(certificateFile);

			log.debug("Private key: {}", bytesToHex(privateKeyBytes));
			log.debug("Certificate: {}", bytesToHex(certificateBytes));

			// log.debug("{}", bytesToHex(privateKeyBytes));
			// privateKeyBytes = Base64.getDecoder().decode(new
			// String(privateKeyBytes).getBytes(StandardCharsets.UTF_8));
			// log.debug("{}", privateKeyBytes);
			// publicKeyBytes = Base64.getDecoder().decode(new
			// String(publicKeyBytes).getBytes(StandardCharsets.UTF_8));

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(certificateBytes);// cert.getBytes(StandardCharsets.UTF_8));
			Certificate ca = cf.generateCertificate(in);
			PublicKey publicKey = ca.getPublicKey();

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			// byte[] privateKeyBytes = key.getBytes();
			// byte[] pkBytes = Base64.getDecoder().decode(privateKeyBytes);
			KeySpec pkKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey = keyFactory.generatePrivate(pkKeySpec);

			// String keyStoreType = KeyStore.getDefaultType();
			// KeyStore keystore = KeyStore.getInstance(keyStoreType);
			// keystore.load(null, null);
			// keystore.setCertificateEntry("ca", ca);

			certificate = (X509Certificate) ca;
			BasicX509Credential credential = new BasicX509Credential();
			credential.setEntityCertificate(certificate);
			credential.setPublicKey(publicKey);
			credential.setPrivateKey(privateKey);

			Credential signingCredential = credential;

			// log.debug("private key {}", pk.toString());
			// log.debug("signing credential {}", signingCredential.toString());

			log.debug("Signing credential for {}\nPrivKey {}\nPubKey {}", signingCredential.getEntityId(),
					bytesToHex(signingCredential.getPrivateKey().getEncoded()),
					bytesToHex(signingCredential.getPublicKey().getEncoded()));

			log.debug("==== CHECK DUMMY SIGNATURE START ====");
			pK = signingCredential.getPrivateKey();
			pubK = signingCredential.getPublicKey();

			java.security.Signature dummySig = java.security.Signature.getInstance("SHA256withRSA");
			dummySig.initSign(pK);
			dummySig.update("TEST".getBytes());
			byte[] sigBytes = dummySig.sign();
			log.debug("DUMMY SIG {}", bytesToHex(sigBytes));
			dummySig.initVerify(pubK);
			dummySig.update("TEST".getBytes());
			log.debug("DUMMY SIG VALIDATE {}", dummySig.verify(sigBytes));
			log.debug("==== CHECK DUMMY SIGNATURE END ====");

			log.debug("Certificate\n{}", certificate);

			return signingCredential;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Assertion signAssertion(Assertion assertion) {
		Credential signingCredential = getSigningCredential();

		Signature signature = (Signature) Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME)
				.buildObject(Signature.DEFAULT_ELEMENT_NAME);
		signature.setSigningCredential(signingCredential);
		signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
		signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

		KeyInfo keyInfo = (KeyInfo) Configuration.getBuilderFactory().getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME)
				.buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);

		KeyName keyName = (KeyName) Configuration.getBuilderFactory().getBuilder(KeyName.DEFAULT_ELEMENT_NAME)
				.buildObject(KeyName.DEFAULT_ELEMENT_NAME);
		keyName.setValue("reg-app");

		X509Data x509Data = (X509Data) Configuration.getBuilderFactory().getBuilder(X509Data.DEFAULT_ELEMENT_NAME)
				.buildObject(X509Data.DEFAULT_ELEMENT_NAME);

		X509SubjectName x509SubjectName = (X509SubjectName) Configuration.getBuilderFactory()
				.getBuilder(X509SubjectName.DEFAULT_ELEMENT_NAME).buildObject(X509SubjectName.DEFAULT_ELEMENT_NAME);
		x509SubjectName.setValue(certificate.getSubjectDN().getName());

		org.opensaml.xml.signature.X509Certificate x509Cert = (org.opensaml.xml.signature.X509Certificate) Configuration
				.getBuilderFactory().getBuilder(org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME)
				.buildObject(org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
		try {
			x509Cert.setValue(Base64.getEncoder().encodeToString(certificate.getEncoded()));
		} catch (CertificateEncodingException e1) {
			e1.printStackTrace();
		}

		x509Data.getX509SubjectNames().add(x509SubjectName);
		x509Data.getX509Certificates().add(x509Cert);

		keyInfo.getKeyNames().add(keyName);
		keyInfo.getX509Datas().add(x509Data);

		signature.setKeyInfo(keyInfo);

		// SecurityConfiguration secConfig =
		// Configuration.getGlobalSecurityConfiguration();
		// String keyInfoGeneratorProfile = "XMLSignature";
		//
		// try {
		// SecurityHelper.prepareSignatureParams(signature, signingCredential,
		// secConfig, null);
		// } catch (SecurityException e) {
		// e.printStackTrace();
		// }
		//
		// // Response resp = (Response)
		// //
		// Configuration.getBuilderFactory().getBuilder(Response.DEFAULT_ELEMENT_NAME)
		// // .buildObject(Response.DEFAULT_ELEMENT_NAME);

		assertion.setSignature(signature);
		((SAMLObjectContentReference) signature.getContentReferences().get(0))
				.setDigestAlgorithm(EncryptionConstants.ALGO_ID_DIGEST_SHA256);
		try {
			Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
		} catch (MarshallingException e) {
			e.printStackTrace();
		}

		try {
			Signer.signObject(signature);
		} catch (SignatureException e) {
			e.printStackTrace();
		}

		logXmlElement(assertion);

		return assertion;
	}

	public void validateSignatuer(Assertion assertion) {
		SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
		try {
			profileValidator.validate(assertion.getSignature());
		} catch (ValidationException e) {
			e.printStackTrace();
		}

		Credential verificationCredential = getSigningCredential();
		SignatureValidator sigValidator = new SignatureValidator(verificationCredential);

		try {
			log.debug("Validate assertion ...");
			logXmlElement(assertion);

			sigValidator.validate(assertion.getSignature());

			log.debug("Validate signature success!");
		} catch (ValidationException e) {
			e.printStackTrace();
		}
	}

	public void validate(Assertion assertion) {
		URL url = Thread.currentThread().getContextClassLoader().getResource("reg-app-sp.xml");
		File idpMetadata = new File(url.getPath());

		log.debug("Read metadata from file {}", idpMetadata.exists());

		try {
			FilesystemMetadataProvider mdProvider = new FilesystemMetadataProvider(idpMetadata);
			mdProvider.setRequireValidMetadata(true);
			mdProvider.setParserPool(new BasicParserPool());
			mdProvider.initialize();

			// EntityDescriptor idpEntityDescriptor =
			// mdProvider.getEntityDescriptor("reg-app");
			// Element element =
			// Configuration.getMarshallerFactory().getMarshaller(idpEntityDescriptor)
			// .marshall(idpEntityDescriptor);
			// log.debug("Entity descriptor from metadata provider\n{}",
			// XMLHelper.prettyPrintXML(element));

			String idpEntityID = "reg-app";
			QName idpRole = SPSSODescriptor.DEFAULT_ELEMENT_NAME;

			MetadataCredentialResolver mdCredResolver = new MetadataCredentialResolver(mdProvider);

			CriteriaSet criteriaSet = new CriteriaSet(new EntityIDCriteria(idpEntityID));
			criteriaSet.add(new MetadataCriteria(idpRole, null));

			for (Credential cred : mdCredResolver.resolve(criteriaSet)) {
				log.debug("Matching credentials {}", cred.getEntityId());
			}

			BasicX509Credential credential = new BasicX509Credential();
			credential.setEntityCertificate(certificate);
			credential.setPublicKey(pubK);
			Credential signingCredential = credential;

			// StaticCredentialResolver credResolver = new
			// StaticCredentialResolver(signingCredential);
			KeyInfoCredentialResolver keyInfoCredResolver = Configuration.getGlobalSecurityConfiguration()
					.getDefaultKeyInfoCredentialResolver();

			ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(mdCredResolver,
					keyInfoCredResolver);

			SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
			try {
				profileValidator.validate(assertion.getSignature());
			} catch (ValidationException e) {
				e.printStackTrace();
			}

			try {
				log.debug("Validate assertion ...");
				logXmlElement(assertion);

				if (!trustEngine.validate(assertion.getSignature(), criteriaSet)) {
					log.error("Signature was either invalid or signing key could not be established as trusted");
				} else {
					log.debug("Validation success!");
				}
			} catch (SecurityException e) {
				// Indicates processing error evaluating the signature
				e.printStackTrace();
			}

		} catch (MetadataProviderException e) {
			e.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public String getAuthNAssertion(String forIdP, String toAssertionConsumerServiceURL, String fromSP) {
		String reqString = "";

		try {
			SecureRandomIdentifierGenerator idGenerator = new SecureRandomIdentifierGenerator();
			DateTime now = new DateTime();

			AuthnRequest request = (AuthnRequest) Configuration.getBuilderFactory()
					.getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME).buildObject(AuthnRequest.DEFAULT_ELEMENT_NAME);

			request.setAssertionConsumerServiceURL(toAssertionConsumerServiceURL);
			// request.setProviderName("sp.scc.kit.edu");
			request.setID(idGenerator.generateIdentifier());
			request.setVersion(SAMLVersion.VERSION_20);
			request.setIssueInstant(now);
			request.setDestination(forIdP);
			request.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");

			Issuer issuer = (Issuer) Configuration.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME)
					.buildObject(Issuer.DEFAULT_ELEMENT_NAME);
			issuer.setValue(fromSP);

			request.setIssuer(issuer);

			NameIDPolicy nameIDPolicy = (NameIDPolicy) Configuration.getBuilderFactory()
					.getBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME).buildObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
			nameIDPolicy.setAllowCreate(true);
			nameIDPolicy.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
			nameIDPolicy.setSPNameQualifier("Issuer");
			request.setNameIDPolicy(nameIDPolicy);

			RequestedAuthnContext authNContext = (RequestedAuthnContext) Configuration.getBuilderFactory()
					.getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME)
					.buildObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);

			authNContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);

			AuthnContextClassRef authNContextClassRef = (AuthnContextClassRef) Configuration.getBuilderFactory()
					.getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME)
					.buildObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);

			authNContextClassRef
					.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

			authNContext.getAuthnContextClassRefs().add(authNContextClassRef);

			request.setRequestedAuthnContext(authNContext);

			// Subject subject = (Subject)
			// Configuration.getBuilderFactory().getBuilder(Subject.DEFAULT_ELEMENT_NAME)
			// .buildObject(Subject.DEFAULT_ELEMENT_NAME);
			// NameID nameID = (NameID)
			// Configuration.getBuilderFactory().getBuilder(NameID.DEFAULT_ELEMENT_NAME)
			// .buildObject(NameID.DEFAULT_ELEMENT_NAME);
			//
			// nameID.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
			// nameID.setValue("benjamin");
			//
			// subject.setNameID(nameID);
			//
			// request.setSubject(subject);

			// Signature signature = (Signature)
			// Configuration.getBuilderFactory()
			// .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
			// signature.setSigningCredential(getSigningCredential());
			// signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
			// signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
			//
			// request.setSignature(signature);
			//
			// Signer.signObject(signature);

			logXmlElement(request);

			Element element;
			element = Configuration.getMarshallerFactory().getMarshaller(request).marshall(request);
			reqString = XMLHelper.nodeToString(element);
			log.debug("REQUEST {}", reqString);
			return reqString;
		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
		}

		return reqString;
	}

	public void testAssertionSignature() {
		try {
			SecureRandomIdentifierGenerator idGenerator = new SecureRandomIdentifierGenerator();
			DateTime now = new DateTime();

			Assertion assertion = (Assertion) Configuration.getBuilderFactory()
					.getBuilder(Assertion.DEFAULT_ELEMENT_NAME).buildObject(Assertion.DEFAULT_ELEMENT_NAME);
			assertion.setVersion(SAMLVersion.VERSION_20);
			assertion.setID(idGenerator.generateIdentifier());
			assertion.setIssueInstant(now);

			Issuer issuer = (Issuer) Configuration.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME)
					.buildObject(Issuer.DEFAULT_ELEMENT_NAME);
			issuer.setValue("reg-app");

			assertion.setIssuer(issuer);

			AuthnStatement authnStmt = (AuthnStatement) Configuration.getBuilderFactory()
					.getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME).buildObject(AuthnStatement.DEFAULT_ELEMENT_NAME);
			authnStmt.setAuthnInstant(now);

			assertion.getAuthnStatements().add(authnStmt);

			Signature signature = (Signature) Configuration.getBuilderFactory()
					.getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
			signature.setSigningCredential(getSigningCredential());
			signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
			signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

			assertion.setSignature(signature);

			Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
			Signer.signObject(signature);

			logXmlElement(assertion);

			Assertion signedAssertion = (Assertion) Configuration.getUnmarshallerFactory()
					.getUnmarshaller(assertion.getDOM()).unmarshall(assertion.getDOM());

			StaticCredentialResolver credResolver = new StaticCredentialResolver(getSigningCredential());
			KeyInfoCredentialResolver kiResolver = Configuration.getGlobalSecurityConfiguration()
					.getDefaultKeyInfoCredentialResolver();// SecurityTestHelper.buildBasicInlineKeyInfoResolver();
			ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(credResolver, kiResolver);

			CriteriaSet criteriaSet = new CriteriaSet(new EntityIDCriteria("reg-app"));

			if (trustEngine.validate(signedAssertion.getSignature(), criteriaSet)) {
				log.debug("Validation success!");
			} else {
				log.error("Validation fail!");
			}

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MarshallingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnmarshallingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void canonicalizeSign(Assertion assertion) {
		try {

			logXmlElement(assertion);

			Canonicalizer c14n = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
			Element element = Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);

			byte outputBytes[] = c14n.canonicalizeSubtree(element);
			log.debug("Canonicalization {}", bytesToHex(outputBytes));

			java.security.Signature sig = java.security.Signature.getInstance("SHA256withRSA");
			sig.initSign(pK);
			sig.update(outputBytes);
			byte[] sigBytes = sig.sign();
			log.debug("SIG {}", bytesToHex(sigBytes));
			sig.initVerify(pubK);
			sig.update(outputBytes);
			log.debug("SIG VALIDATE {}", sig.verify(sigBytes));
			log.debug("SIG BASE64 {}", new String(Base64.getEncoder().encode(sigBytes)));
		} catch (InvalidCanonicalizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MarshallingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CanonicalizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.security.SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendAuthNRequest() {
		try {
			SecureRandomIdentifierGenerator idGenerator = new SecureRandomIdentifierGenerator();
			DateTime now = new DateTime();

			Assertion assertion = (Assertion) Configuration.getBuilderFactory()
					.getBuilder(Assertion.DEFAULT_ELEMENT_NAME).buildObject(Assertion.DEFAULT_ELEMENT_NAME);
			assertion.setVersion(SAMLVersion.VERSION_20);
			assertion.setID(idGenerator.generateIdentifier());
			assertion.setIssueInstant(now);

			Issuer issuer = (Issuer) Configuration.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME)
					.buildObject(Issuer.DEFAULT_ELEMENT_NAME);
			issuer.setValue("https://ldf.data.kit.edu/sp");

			assertion.setIssuer(issuer);

			AuthnStatement authnStmt = (AuthnStatement) Configuration.getBuilderFactory()
					.getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME).buildObject(AuthnStatement.DEFAULT_ELEMENT_NAME);
			authnStmt.setAuthnInstant(now);

			assertion.getAuthnStatements().add(authnStmt);

			Signature signature = (Signature) Configuration.getBuilderFactory()
					.getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
			signature.setSigningCredential(getSigningCredential());
			signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
			signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

			assertion.setSignature(signature);

			Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
			Signer.signObject(signature);

			logXmlElement(assertion);

			Envelope envelope = (Envelope) Configuration.getBuilderFactory().getBuilder(Envelope.DEFAULT_ELEMENT_NAME)
					.buildObject(Envelope.DEFAULT_ELEMENT_NAME);
			Body body = (Body) Configuration.getBuilderFactory().getBuilder(Body.DEFAULT_ELEMENT_NAME)
					.buildObject(Body.DEFAULT_ELEMENT_NAME);

			body.getUnknownXMLObjects().add(assertion);
			envelope.setBody(body);

			BasicSOAPMessageContext soapContext = new BasicSOAPMessageContext();
			soapContext.setOutboundMessage(envelope);

			logXmlElement(envelope);

			X509Credential clientTLSCred = SecurityHelper.getSimpleCredential(certificate, pK);

			StaticClientKeyManager keyManager = new StaticClientKeyManager(clientTLSCred.getPrivateKey(),
					clientTLSCred.getEntityCertificate());

			HttpClientBuilder clientBuilder = new HttpClientBuilder();
			clientBuilder
					.setHttpsProtocolSocketFactory(new TLSProtocolSocketFactory(keyManager, new X509TrustManager() {
						@Override
						public void checkServerTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
						}

						@Override
						public void checkClientTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
						}

						@Override
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[] { readIdpCert(), certificate };
						}
					}));

			BasicParserPool parserPool = new BasicParserPool();
			parserPool.setNamespaceAware(true);

			HttpClient httpClient = clientBuilder.buildClient();
			httpClient.getState().setCredentials(new AuthScope("idp.scc.kit.edu", 443),
					new UsernamePasswordCredentials("ym0762", "oo22-.22"));

			HttpSOAPClient soapClient = new HttpSOAPClient(httpClient, parserPool);

			String serverEndpoint = "https://idp.scc.kit.edu/idp/profile/SAML2/SOAP/ECP";

			soapClient.send(serverEndpoint, soapContext);

			Envelope soapResponse = (Envelope) soapContext.getInboundMessage();

			System.out.println("SOAP Response was:");
			System.out.println(XMLHelper.prettyPrintXML(soapResponse.getDOM()));

		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
		}
	}

	public void sendAttributeQuery() {
		try {
			SecureRandomIdentifierGenerator idGenerator = new SecureRandomIdentifierGenerator();
			DateTime now = new DateTime();

			// Assertion assertion = (Assertion)
			// Configuration.getBuilderFactory()
			// .getBuilder(Assertion.DEFAULT_ELEMENT_NAME).buildObject(Assertion.DEFAULT_ELEMENT_NAME);
			// assertion.setVersion(SAMLVersion.VERSION_20);
			// assertion.setID(idGenerator.generateIdentifier());
			// assertion.setIssueInstant(now);

			Issuer issuer = (Issuer) Configuration.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME)
					.buildObject(Issuer.DEFAULT_ELEMENT_NAME);
			issuer.setValue("https://ldf.data.kit.edu/sp");

			// assertion.setIssuer(issuer);

			// AuthnStatement authnStmt = (AuthnStatement)
			// Configuration.getBuilderFactory()
			// .getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME).buildObject(AuthnStatement.DEFAULT_ELEMENT_NAME);
			// authnStmt.setAuthnInstant(now);

			// assertion.getAuthnStatements().add(authnStmt);

			Signature signature = (Signature) Configuration.getBuilderFactory()
					.getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
			signature.setSigningCredential(getSigningCredential());
			signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
			signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

			// assertion.setSignature(signature);

			// Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
			// Signer.signObject(signature);
			//
			// logXmlElement(assertion);

			AttributeQuery attributeQuery = (AttributeQuery) Configuration.getBuilderFactory()
					.getBuilder(AttributeQuery.DEFAULT_ELEMENT_NAME).buildObject(AttributeQuery.DEFAULT_ELEMENT_NAME);
			attributeQuery.setVersion(SAMLVersion.VERSION_20);
			attributeQuery.setID(idGenerator.generateIdentifier());
			attributeQuery.setIssueInstant(now);

			attributeQuery.setIssuer(issuer);

			Subject subject = (Subject) Configuration.getBuilderFactory().getBuilder(Subject.DEFAULT_ELEMENT_NAME)
					.buildObject(Subject.DEFAULT_ELEMENT_NAME);

			NameID nameID = (NameID) Configuration.getBuilderFactory().getBuilder(NameID.DEFAULT_ELEMENT_NAME)
					.buildObject(NameID.DEFAULT_ELEMENT_NAME);

			nameID.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
			nameID.setValue("MdWhcFYwml0vPFMscox33AYkkgs=");

			subject.setNameID(nameID);

			attributeQuery.setSubject(subject);

			attributeQuery.setSignature(signature);

			Configuration.getMarshallerFactory().getMarshaller(attributeQuery).marshall(attributeQuery);
			Signer.signObject(signature);

			Envelope envelope = (Envelope) Configuration.getBuilderFactory().getBuilder(Envelope.DEFAULT_ELEMENT_NAME)
					.buildObject(Envelope.DEFAULT_ELEMENT_NAME);
			Body body = (Body) Configuration.getBuilderFactory().getBuilder(Body.DEFAULT_ELEMENT_NAME)
					.buildObject(Body.DEFAULT_ELEMENT_NAME);

			body.getUnknownXMLObjects().add(attributeQuery);
			envelope.setBody(body);

			BasicSOAPMessageContext soapContext = new BasicSOAPMessageContext();
			soapContext.setOutboundMessage(envelope);

			logXmlElement(envelope);

			X509Credential clientTLSCred = SecurityHelper.getSimpleCredential(certificate, pK);

			StaticClientKeyManager keyManager = new StaticClientKeyManager(clientTLSCred.getPrivateKey(),
					clientTLSCred.getEntityCertificate());

			HttpClientBuilder clientBuilder = new HttpClientBuilder();
			clientBuilder
					.setHttpsProtocolSocketFactory(new TLSProtocolSocketFactory(keyManager, new X509TrustManager() {
						@Override
						public void checkServerTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
						}

						@Override
						public void checkClientTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
						}

						@Override
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[] { readIdpCert(), certificate };
						}
					}));

			BasicParserPool parserPool = new BasicParserPool();
			parserPool.setNamespaceAware(true);

			HttpClient httpClient = clientBuilder.buildClient();
			// httpClient.getState().setCredentials(
			// new AuthScope("idp.scc.kit.edu", 8443),
			// new UsernamePasswordCredentials("admin", "admin"));

			HttpSOAPClient soapClient = new HttpSOAPClient(httpClient, parserPool);

			String serverEndpoint = "https://idp.scc.kit.edu:8443/idp/profile/SAML2/SOAP/AttributeQuery";

			soapClient.send(serverEndpoint, soapContext);

			Envelope soapResponse = (Envelope) soapContext.getInboundMessage();

			System.out.println("SOAP Response was:");
			System.out.println(XMLHelper.prettyPrintXML(soapResponse.getDOM()));

		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
		}
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	class StaticClientKeyManager implements X509KeyManager {

		private static final String clientAlias = "myStaticAlias";

		private PrivateKey privateKey;
		private X509Certificate cert;

		public StaticClientKeyManager(PrivateKey newPrivateKey, X509Certificate newCert) {
			privateKey = newPrivateKey;
			cert = newCert;
		}

		/** {@inheritDoc} */
		public String chooseClientAlias(String[] as, Principal[] aprincipal, Socket socket) {
			System.out.println("chooseClientAlias");
			return clientAlias;
		}

		/** {@inheritDoc} */
		public String chooseServerAlias(String s, Principal[] aprincipal, Socket socket) {
			System.out.println("chooseServerAlias");
			return null;
		}

		/** {@inheritDoc} */
		public X509Certificate[] getCertificateChain(String s) {
			System.out.println("getCertificateChain");
			return new X509Certificate[] { cert };
		}

		/** {@inheritDoc} */
		public String[] getClientAliases(String s, Principal[] aprincipal) {
			System.out.println("getClientAliases");
			return new String[] { clientAlias };
		}

		/** {@inheritDoc} */
		public PrivateKey getPrivateKey(String s) {
			System.out.println("getPrivateKey");
			return privateKey;
		}

		/** {@inheritDoc} */
		public String[] getServerAliases(String s, Principal[] aprincipal) {
			System.out.println("getServerAliases");
			return null;
		}

	}
}
