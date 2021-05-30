package com.rest.refatory;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;

import io.restassured.RestAssured;

public class ClassRefatorada {
	@BeforeClass
	public void login() {
		Map<String, String> login = new HashMap<String, String>();
		login.put("emal", "");
		login.put("senha", "");
		
		String TOKEM = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)//atenção api mas antiga usa o JWT token, e as mas novas usa o bearer
			.extract().path("token");
		;
		RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEM);
		RestAssured.get("/reset").then().statusCode(200);
	}
}
