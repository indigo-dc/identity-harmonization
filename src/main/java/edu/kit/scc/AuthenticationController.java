/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.HandlerMapping;

import edu.kit.scc.saml.SamlClient;

@Controller
public class AuthenticationController {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

	@Value("${oauth2.authorizeUri}")
	private String oauth2AuthorizeUri;

	@Value("${oauth2.redirectUri}")
	private String oauth2RedirectUri;

	@Value("${oauth2.clientId}")
	private String oauth2ClientId;

	@Autowired
	SamlClient samlClient;

	@Autowired
	EncryptedAssertionRepository repository;

	@RequestMapping(path = "/assertions/**") // , produces =
												// "application/octet-stream")
	public void getAssertion(HttpServletRequest request, HttpServletResponse response) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		log.debug(path);

		path = path.replace("/assertions/", "");
		log.debug(path);

		Path p = Paths.get("assertions", path);

		try {
			byte[] content = Files.readAllBytes(p);

			response.setContentLength(content.length);

			OutputStream outStream = response.getOutputStream();
			outStream.write(content);

			outStream.close();

		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
			e.printStackTrace();
		}
	}

	@RequestMapping(path = "/SAML2/POST", method = RequestMethod.POST)
	public String saml(@RequestParam("SAMLResponse") String samlResponse, HttpServletRequest request, Model model) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		log.debug(path);

		// for (Enumeration<String> e = request.getAttributeNames();
		// e.hasMoreElements();) {
		// String attribute = e.nextElement();
		// log.debug("{} {}", attribute, request.getAttribute(attribute));
		// }

		for (Entry entry : request.getParameterMap().entrySet())
			log.debug("{} {}", entry.getKey(), entry.getValue());

		String decodedAuthnRequestXML = "";

		try {
			Base64 base64Decoder = new Base64();
			byte[] xmlBytes = samlResponse.getBytes("UTF-8");
			byte[] base64DecodedByteArray = base64Decoder.decode(xmlBytes);

			try {
				Inflater inflater = new Inflater(true);
				inflater.setInput(base64DecodedByteArray);
				byte[] xmlMessageBytes = new byte[5000];
				int resultLength = inflater.inflate(xmlMessageBytes);

				if (!inflater.finished()) {
					throw new RuntimeException("didn't allocate enough space to hold " + "decompressed data");
				}

				inflater.end();
				decodedAuthnRequestXML = new String(xmlMessageBytes, 0, resultLength, "UTF-8");

			} catch (DataFormatException e) {
				log.warn("WARNING {}", e.getMessage());

				ByteArrayInputStream bais = new ByteArrayInputStream(base64DecodedByteArray);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				InflaterInputStream iis = new InflaterInputStream(bais);
				byte[] buf = new byte[1024];
				int count = iis.read(buf);
				while (count != -1) {
					baos.write(buf, 0, count);
					count = iis.read(buf);
				}
				iis.close();
				decodedAuthnRequestXML = new String(baos.toByteArray());
			}

		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
			// e.printStackTrace();
		}

		if (decodedAuthnRequestXML.equals("")) {
			try {
				Base64 base64Decoder = new Base64();
				byte[] xmlBytes = samlResponse.getBytes("UTF-8");
				byte[] base64DecodedByteArray = base64Decoder.decode(xmlBytes);

				decodedAuthnRequestXML = new String(base64DecodedByteArray);

			} catch (Exception e) {
				log.error("ERROR {}", e.getMessage());
				e.printStackTrace();
			}
		}

		SAXBuilder builder = new SAXBuilder();
		String assertionXMLCompact = "";
		try {
			Document document = builder.build(new ByteArrayInputStream(decodedAuthnRequestXML.getBytes()));
			String[] samlRequestAttributes = new String[4];

			Element rootElement = document.getRootElement();

			samlRequestAttributes[0] = rootElement.getAttributeValue("IssueInstant");
			samlRequestAttributes[1] = rootElement.getAttributeValue("ProviderName");
			samlRequestAttributes[2] = rootElement.getAttributeValue("AssertionConsumerServiceURL");
			samlRequestAttributes[3] = rootElement.getAttributeValue("ID");

			ElementFilter filter = new ElementFilter("Assertion");
			for (Element e : rootElement.getDescendants(filter)) {

				XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
				String assertionXMLPretty = outputter.outputString(e);
				outputter.setFormat(Format.getCompactFormat());

				assertionXMLCompact = outputter.outputString(e);

				model.addAttribute("Assertion", assertionXMLPretty);
				model.addAttribute("Delegate", assertionXMLCompact);
			}
			model.addAttribute("IssueInstant", samlRequestAttributes[0]);
			model.addAttribute("ProviderName", samlRequestAttributes[1]);
			model.addAttribute("AssertionConsumerServiceURL", samlRequestAttributes[2]);
			model.addAttribute("ID", samlRequestAttributes[3]);

			try {
				Security.addProvider(new BouncyCastleProvider());

				byte[] byteArray = assertionXMLCompact.getBytes();

				SecureRandom secRnd = new SecureRandom();

				char[] fileChars = new char[8];
				char[] VALID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

				for (int i = 0; i < fileChars.length; i++)
					fileChars[i] = VALID_CHARACTERS[secRnd.nextInt(fileChars.length)];

				byte[] iv = new byte[16];
				secRnd.nextBytes(iv);
				byte[] key = new byte[16];
				secRnd.nextBytes(key);
				IvParameterSpec init_vector = new IvParameterSpec(iv);
				SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
				Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding");
				c.init(Cipher.ENCRYPT_MODE, secretKey, init_vector);

				byte[] encryptedBytes = c.doFinal(byteArray);

				log.debug("Init vector {}", DatatypeConverter.printHexBinary(iv));
				model.addAttribute("iv", DatatypeConverter.printHexBinary(iv));
				log.debug("Key {}", DatatypeConverter.printHexBinary(key));
				model.addAttribute("key", DatatypeConverter.printHexBinary(key));

				byte[] returnBytes = new byte[encryptedBytes.length + iv.length];
				System.arraycopy(iv, 0, returnBytes, 0, iv.length);
				System.arraycopy(encryptedBytes, 0, returnBytes, iv.length, encryptedBytes.length);

				log.debug("Encrypted assertion {}", DatatypeConverter.printHexBinary(returnBytes));
				model.addAttribute("encassertion", DatatypeConverter.printHexBinary(returnBytes));

				Base64 b64 = new Base64(true);
				log.debug("Encoded assertion {}", new String(b64.encode(returnBytes)));
				model.addAttribute("delegate", new String(b64.encode(returnBytes)));

				// repository.save(new EncryptedAssertion(key, iv, new
				// String(b64.encode(returnBytes))));

				Path p = Paths.get("assertions", new String(fileChars));
				Files.write(p, new String(b64.encode(returnBytes)).getBytes(), StandardOpenOption.WRITE,
						StandardOpenOption.CREATE_NEW);

				model.addAttribute("file", new String(fileChars));
			} catch (Exception e) {
				log.warn("ERROR {}", e.getMessage());
				e.printStackTrace();
			}

		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
			e.printStackTrace();
		}

		model.addAttribute("samlResponse", samlResponse);
		model.addAttribute("decodedAuthnRequestXML", decodedAuthnRequestXML);

		return "saml";
	}

	@RequestMapping("/login")
	public String login(HttpServletResponse response, Model model) throws UnsupportedEncodingException {
		// String redirectUrl = oauth2AuthorizeUri.replaceAll("/$", "");
		// redirectUrl += "?response_type=code&scope=openid%20email&client_id=";
		// redirectUrl += oauth2ClientId;
		// redirectUrl += "&redirect_uri=";
		// redirectUrl += oauth2RedirectUri;
		// log.debug("Redirect to {}", redirectUrl);

		String request = samlClient.getAuthNAssertion("https://192.168.122.99:9443/samlsso",
				"https://192.168.122.1:8443/SAML2/POST", "sp.scc.kit.edu");
		try {
			byte[] xmlBytes = request.getBytes(StandardCharsets.UTF_8);
			log.debug("XML request bytes {}", DatatypeConverter.printHexBinary(xmlBytes));
//			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
//			DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteOutputStream);
//			deflaterOutputStream.write(xmlBytes, 0, xmlBytes.length);
//			deflaterOutputStream.close();
//			log.debug("XML deflated request bytes {}",
//					DatatypeConverter.printHexBinary(byteOutputStream.toByteArray()));

			Base64 base64Encoder = new Base64();
//			byte[] base64EncodedByteArray = base64Encoder.encode(byteOutputStream.toByteArray());
			
			byte[] base64EncodedByteArray = base64Encoder.encode(xmlBytes);
			log.debug("XML deflated and base64 encoded {}", DatatypeConverter.printHexBinary(base64EncodedByteArray));
			String base64EncodedMessage = new String(base64EncodedByteArray);
			log.debug("XML deflated and base64 encoded {}", base64EncodedMessage);
			String urlEncodedMessage = URLEncoder.encode(base64EncodedMessage, StandardCharsets.UTF_8.name());

			request = urlEncodedMessage;

			SecureRandom secRnd = new SecureRandom();
			char[] VALID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
			char[] chars = new char[16];
			for (int i = 0; i < chars.length; i++)
				chars[i] = VALID_CHARACTERS[secRnd.nextInt(chars.length)];

			model.addAttribute("samlrequest", urlEncodedMessage);
			model.addAttribute("relaystate", new String(chars));

			log.debug("REQUEST {}", urlEncodedMessage);
		} catch (Exception e) {
			log.error("ERROR {}", e.getMessage());
		}
		String redirectUrl = "https://192.168.122.99:9443/samlsso?SAMLRequest=";

		model.addAttribute("url", "https://192.168.122.99:9443/samlsso");
		// response.addHeader("Referer", "https://192.168.122.1:8443/");

		return "form";
	}

	@RequestMapping(path = "/oauth2")
	public String oauth2Authentication(@RequestParam(value = "code", required = true) String code, Model model) {
		log.debug(code);
		model.addAttribute("code", code);
		return "index";
	}
}
