package com.umr.apitesting.core.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfiguration {

	@Autowired
	private EmailConfig emailConfig;

	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		// Configure mail sender
		mailSender.setHost(emailConfig.getHost());
		mailSender.setPort(emailConfig.getPort());

		// Set username/password if authentication is required
		if (emailConfig.getUsername() != null && !emailConfig.getUsername().isEmpty()) {
			mailSender.setUsername(emailConfig.getUsername());
			mailSender.setPassword(emailConfig.getPassword());
		}

		// Configure additional properties
		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "false"); // Set to true if auth is needed
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");

		// Enable connection timeouts
		props.put("mail.smtp.connectiontimeout", "5000");
		props.put("mail.smtp.timeout", "5000");
		props.put("mail.smtp.writetimeout", "5000");

		// Enable debug if needed
		props.put("mail.debug", "true");

		// Disable certificate validation (use only in controlled corporate
		// environments)
		props.put("mail.smtp.ssl.checkserveridentity", "false");

		return mailSender;
	}
}