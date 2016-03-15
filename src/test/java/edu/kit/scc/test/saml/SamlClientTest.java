package edu.kit.scc.test.saml;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml2.core.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.kit.scc.Application;
import edu.kit.scc.saml.SamlClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class SamlClientTest {

	@Autowired
	private SamlClient samlClient;

	@Test
	public void buildAssertionTest() {
	//	Assertion assertion = samlClient.buildAssertion();

//		samlClient.validateSignatuer(samlClient.signAssertion(assertion));

		// Assertion newAssertion = samlClient.buildAssertion();
		// samlClient.canonicalizeSign(newAssertion);
		//samlClient.validate(samlClient.signAssertion(assertion));
		
//		samlClient.testAssertionSignature();
		
		samlClient.sendAuthNRequest();
	}

}
