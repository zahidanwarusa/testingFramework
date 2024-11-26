package com.umr.apitesting.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.umr.apitesting.core.config.ConfigManager;
import com.umr.apitesting.core.executor.TestExecutor;

public class KeywordFrameworkTest {

	private TestExecutor testExecutor;

	@BeforeClass
	public void setup() {
		ConfigManager.loadEnvironmentConfig("dev");
		testExecutor = new TestExecutor("src/main/resources/testdata/TestSuite.xlsx");
	}

	@Test
	public void testLoginAPI() {
		testExecutor.executeTest("TC001");
	}

	@Test
	public void testGetUserAPI() {
		testExecutor.executeTest("TC002");
	}
}