package com.umr.apitesting.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.umr.apitesting.core.config.ConfigManager;
import com.umr.apitesting.core.context.TestContext;
import com.umr.apitesting.core.http.RestClientBuilder;
import com.umr.apitesting.utils.LoggerUtil;

import io.restassured.response.Response;

public class FrameworkSetupTest {

	@BeforeClass
	public void setup() {
		ConfigManager.loadEnvironmentConfig("dev");
	}

	@Test
	public void testConfigurationLoading() {
		String baseUrl = ConfigManager.getProperty("base.url");
		assertNotNull(baseUrl, "Base URL should be loaded from configuration");
		LoggerUtil.logInfo("Successfully loaded base URL: " + baseUrl);
	}

	@Test
	public void testRestClient() {
		Response response = new RestClientBuilder().setBaseUrl("https://jsonplaceholder.typicode.com")
				.setEndpoint("/posts/1").setMethod("GET").addHeader("Content-Type", "application/json").execute();

		assertEquals(response.getStatusCode(), 200, "API request should be successful");
	}

	@Test
	public void testTestContext() {
		TestContext context = new TestContext("TC001");
		context.setTestData("key1", "value1");
		context.setRuntimeData("key2", "value2");

		assertEquals(context.getTestData("key1"), "value1", "Test data should be retrievable");
		assertEquals(context.getRuntimeData("key2"), "value2", "Runtime data should be retrievable");
	}
}