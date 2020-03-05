package com.github.antoniomacri.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.Cookie;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;


@QuarkusTest
class JwtGroupsTest {
    private static final String CLAIMS_ARRAY = "/jwt-claims-groups-array.json";
    private static final String CLAIMS_STRING = "/jwt-claims-groups-string.json";


    @Inject
    @ConfigProperty(name = "smallrye.jwt.token.cookie")
    String cookieName;


    @Test
    void groupsArray() {
        given().relaxedHTTPSValidation()
                .spec(jsonWebToken(CLAIMS_ARRAY))
                .when().get("/test")
                .then().spec(resourceAccessed());
    }

    @Test
    void groupsString() {
        given().relaxedHTTPSValidation()
                .spec(jsonWebToken(CLAIMS_STRING))
                .when().get("/test")
                .then().spec(resourceAccessed());
    }


    private RequestSpecification jsonWebToken(String claimsFile) {
        Cookie tokenCookie = generateJsonWebTokenCookie(claimsFile);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new RequestSpecBuilder().addCookie(tokenCookie).build();
    }

    private Cookie generateJsonWebTokenCookie(String claimFile) {
        String token = generateJsonWebToken(claimFile);
        return new Cookie.Builder(cookieName, token)
                .setPath("/")
                .setMaxAge(3600)
                .build();
    }

    private String generateJsonWebToken(String claimFile) {
        try {
            Map<String, Object> timeClaims = new HashMap<>();
            timeClaims.put(Claims.exp.name(), TokenUtils.currentTimeInSecs() + (long) 3600);
            return TokenUtils.generateTokenString(claimFile, timeClaims);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResponseSpecification resourceAccessed() {
        return new ResponseSpecBuilder()
                .expectStatusCode(Response.Status.OK.getStatusCode())
                .expectBody(is("OK"))
                .build();
    }
}
