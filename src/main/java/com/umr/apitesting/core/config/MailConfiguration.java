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
		mailSender.setUsername(emailConfig.getUsername());
		mailSender.setPassword(emailConfig.getPassword());

		// Configure additional properties
		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "false");

		return mailSender;
	}
}