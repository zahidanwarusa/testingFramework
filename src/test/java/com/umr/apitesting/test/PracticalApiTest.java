package com.umr.apitesting.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.umr.apitesting.core.executor.TestExecutor;
import com.umr.apitesting.utils.LoggerUtil;

public class PracticalApiTest {

	private TestExecutor testExecutor;

	@BeforeClass
	public void setup() {
		testExecutor = new TestExecutor("src/main/resources/testdata/TestSuite.xlsx");
		LoggerUtil.logInfo("Test Suite initialized");
	}

	@Test(description = "Get post details from JSONPlaceholder")
	public void testGetPost() {
		testExecutor.executeTest("TC001");
	}

	@Test(description = "Create user using ReqRes API")
	public void testCreateUser() {
		testExecutor.executeTest("TC002");
	}

	@Test(description = "Get user details from ReqRes")
	public void testGetUser() {
		testExecutor.executeTest("TC003");
	}

	@Test(description = "Get country details")
	public void testGetCountry() {
		testExecutor.executeTest("TC004");
	}

	@Test(description = "Get weather data")
	public void testGetWeather() {
		testExecutor.executeTest("TC005");
	}

	@Test(description = "Get GitHub repository info")
	public void testGetRepoInfo() {
		testExecutor.executeTest("TC006");
	}
}