package com.umr.apitesting.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "mail")
public class EmailConfig {
	private String host;
	private int port;
	private String username;
	private String password;
	private String from;
	private String[] recipients;
	private boolean enabled;
	private String subject;
}
