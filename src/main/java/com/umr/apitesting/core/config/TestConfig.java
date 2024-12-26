package com.umr.apitesting.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.umr.apitesting.core.executor.ParallelTestExecutor;

@Configuration
public class TestConfig {
	private final ConfigManager configManager;

	public TestConfig(ConfigManager configManager) { // Constructor injection
		this.configManager = configManager;
	}

	@Bean
	public String testDataPath() {
		return configManager.getProperty("test.suite.path");
	}

	@Bean
	public Integer threadCount() {
		return Integer.parseInt(configManager.getProperty("test.thread.count"));
	}

	@Bean
	public ParallelTestExecutor parallelTestExecutor(String testDataPath, Integer threadCount) {
		return new ParallelTestExecutor(testDataPath, threadCount);
	}
}