package com.umr.apitesting.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.umr.apitesting.core.executor.ParallelTestExecutor;

@Configuration
public class TestConfig {
	@Autowired
	private ConfigManager configManager;

	@Bean
	public String testDataPath() {
		return ConfigManager.getProperty("test.suite.path");
	}

	@Bean
	public Integer threadCount() {
		return Integer.parseInt(ConfigManager.getProperty("test.thread.count"));
	}

	@Bean
	public ParallelTestExecutor parallelTestExecutor(String testDataPath, Integer threadCount) {
		return new ParallelTestExecutor(testDataPath, threadCount);
	}
}