package edu.kit.scc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {

	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	private Utils() {
	}

	public static String encodeBase64(String string) {
		return new String(Base64.encodeBase64(string.getBytes()));
	}

	public static Properties loadProperties() {
		InputStream in = null;
		Properties properties = new Properties();
		try {
			// load properties from configuration.properties file
			in = Main.class.getClassLoader().getResourceAsStream("config.properties");
			if (in != null) {
				properties.load(in);
				log.debug("loaded properties from {} file", "config.properties");
			}
			// load properties from System.getProperty, overwrite
			// configuration.properties
			if (System.getProperty("registerApp.serviceUrl") != null)
				properties.put("regapp.serviceUrl", System.getProperty("registerApp.serviceUrl"));
			if (System.getProperty("registerApp.serviceUsername") != null)
				properties.put("regapp.serviceUsername", System.getProperty("registerApp.serviceUsername"));
			if (System.getProperty("registerApp.servicePassword") != null)
				properties.put("regapp.servicePassword", System.getProperty("registerApp.servicePassword"));
			if (System.getProperty("registerApp.checkCert") != null)
				properties.put("regapp.checkCert", System.getProperty("registerApp.serviceUrl"));
		} catch (IOException e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// e.printStackTrace();
					log.error(e.getMessage());
				}
			}
		}
		return properties;
	}

	public static void printProperties() {
		Properties properties = loadProperties();
		for (Entry<Object, Object> e : properties.entrySet())
			log.info("{}: {}", new Object[] { e.getKey(), e.getValue() });
	}
}
