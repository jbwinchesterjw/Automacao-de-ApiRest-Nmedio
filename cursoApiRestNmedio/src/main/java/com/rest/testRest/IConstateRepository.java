package com.rest.testRest;

import io.restassured.http.ContentType;

public interface IConstateRepository {

	String APP_BASE_URL = "https://barrigarest.wcaquino.me";
	Integer APP_PORT = 443;//atenção nas URL https = 443, http = 80
	String APP_BASE_PATH = "";
	
	ContentType APP_CONTENT_TYPE = ContentType.JSON;
	Long Max_TIMEOUT = 5000L;//time max de cada requisição
}
