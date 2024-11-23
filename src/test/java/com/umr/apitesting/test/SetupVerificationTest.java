package com.umr.apitesting.test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.umr.apitesting.utils.LoggerUtil;

import io.restassured.response.Response;

public class SetupVerificationTest {

	@Test
	public void verifySetup() {
		LoggerUtil.logInfo("Starting API test verification");

		try {
			Response response = given().get("https://jsonplaceholder.typicode.com/posts/1");

			assertEquals(response.getStatusCode(), 200);
			LoggerUtil.logInfo("API test verification completed successfully");

		} catch (Exception e) {
			LoggerUtil.logError("API test verification failed", e);
			throw e;
		}
	}
}