package edu.kit.scc.test;

import static com.jayway.restassured.RestAssured.given;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import edu.kit.scc.IdentityHarmonizationService;
import edu.kit.scc.scim.ScimUser;

import org.apache.commons.httpclient.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IdentityHarmonizationService.class)
@WebIntegrationTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("development")
public class RestServiceControllerTest {

  private static final Logger log = LoggerFactory.getLogger(RestServiceControllerTest.class);

  String id = UUID.randomUUID().toString();

  @Before
  public void setUpEach() {
    RestAssured.baseURI = "https://localhost:8443";
    RestAssured.useRelaxedHTTPSValidation();
  }

  @Test
  public void canCreateUserWithScim() {
    ScimUser scimUser = new ScimUser();
    Response response = given().header("Authorization", "Basic asdfagdf").and()
        .header("Content-Type", "application/scim+json").and().body(scimUser).when().post("/Users")
        .then().statusCode(HttpStatus.SC_CREATED).extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void canFetchUserWithId() {

    given().header("Authorization", "Basic asdfagdf").when().get("/Users/{id}", id).then()
        .statusCode(HttpStatus.SC_OK).body("externalId", Matchers.is(id));
  }
}
