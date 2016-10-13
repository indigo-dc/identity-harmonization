package edu.kit.scc.test.rest;

import static com.jayway.restassured.RestAssured.given;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import edu.kit.scc.IdentityHarmonizationService;
import edu.kit.scc.scim.ScimUser;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IdentityHarmonizationService.class)
@WebIntegrationTest
public class RestServiceTest {

  private static final Logger log = LoggerFactory.getLogger(RestServiceTest.class);

  @Value("${rest.serviceUsername}")
  private String restUser;

  @Value("${rest.servicePassword}")
  private String restPassword;

  @Value("${server.port}")
  private int serverPort;

  @Before
  public void setUpEach() {
    RestAssured.baseURI = "https://localhost:" + String.valueOf(serverPort);
    RestAssured.useRelaxedHTTPSValidation();
  }

  @Test
  public void initTest() {

  }

  // @Test
  public void canCreateUserWithScim() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());

    ScimUser scimUser = new ScimUser();
    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/scim+json").and().body(scimUser).when().post("/Users")
        .then().statusCode(HttpStatus.SC_CREATED).extract().response();

    log.debug("Response {}", response.asString());
  }

  // @Test
  public void canFetchUserWithId() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());

    ScimUser scimUser = new ScimUser();
    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/scim+json").and().body(scimUser).when().post("/Users")
        .then().statusCode(HttpStatus.SC_CREATED).extract().response();

    log.debug("Response {}", response.asString());

    JSONObject json = new JSONObject(response.asString());
    log.debug("JSON {}", json.toString());

    String id = json.getString("id");

    given().header("Authorization", "Basic " + authString).when().get("/Users/{id}", id).then()
        .statusCode(HttpStatus.SC_OK).body("externalId", Matchers.is(id));
  }

  // @Test
  public void canHarmonizeUsers() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());

    ScimUser scimUser = new ScimUser();
    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/scim+json").and().body(scimUser).when().post("/Users")
        .then().statusCode(HttpStatus.SC_CREATED).extract().response();

    JSONObject json1 = new JSONObject(response.asString());

    scimUser = new ScimUser();
    response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/scim+json").and().body(scimUser).when().post("/Users")
        .then().statusCode(HttpStatus.SC_CREATED).extract().response();

    JSONObject json2 = new JSONObject(response.asString());

    JSONArray linkUsers = new JSONArray();
    linkUsers.put(json1);
    linkUsers.put(json2);

    response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/scim+json").and().body(linkUsers.toString()).when()
        .post("/link").then().statusCode(HttpStatus.SC_OK).extract().response();

    log.debug("Unlink information {}", response.asString());
    JSONArray unlinkUsers = new JSONArray(response.asString());

    response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/scim+json").and().body(unlinkUsers.toString()).when()
        .post("/unlink").then().statusCode(HttpStatus.SC_OK).extract().response();
  }
}
