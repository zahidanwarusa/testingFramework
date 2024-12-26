package com.umr.apitesting.core.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ConfigManager {
	private static final ConcurrentHashMap<String, Properties> envProperties = new ConcurrentHashMap<>();
	private static String currentEnvironment;
	public static final String CONFIG_PATH = "src/main/resources/config/";

	@PostConstruct
	public void init() {
		loadEnvironmentConfig("dev");
	}

	public static void loadEnvironmentConfig(String environment) {
		String configFile = CONFIG_PATH + environment + ".properties";
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(configFile)) {
			props.load(fis);
			envProperties.put(environment, props);
			currentEnvironment = environment;
		} catch (IOException e) {
			throw new RuntimeException("Failed to load configuration", e);
		}
	}

	public static String getProperty(String key) {
		Properties props = envProperties.get(currentEnvironment);
		if (props == null) {
			throw new RuntimeException("No properties loaded for environment: " + currentEnvironment);
		}
		return props.getProperty(key);
	}

	public static String getCurrentEnvironment() {
		return currentEnvironment;
	}
}