package com.umr.apitesting.core.service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.umr.apitesting.core.config.EmailConfig;
import com.umr.apitesting.utils.LoggerUtil;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private EmailConfig emailConfig;

	public void sendTestExecutionReport(String reportPath, Map<String, Object> executionStats) {
		if (!emailConfig.isEnabled()) {
			LoggerUtil.logInfo("Email notifications are disabled");
			return;
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(emailConfig.getFrom());
			helper.setTo(emailConfig.getRecipients());
			helper.setSubject(formatSubject());

			String content = createEmailContent(executionStats);
			helper.setText(content, true);

			File reportFile = new File(reportPath);
			if (reportFile.exists()) {
				helper.addAttachment("TestExecutionReport.html", reportFile);
			}

			// Add retry mechanism
			int maxRetries = 3;
			int retryCount = 0;
			while (retryCount < maxRetries) {
				try {
					mailSender.send(message);
					LoggerUtil.logInfo("Test execution report email sent successfully");
					break;
				} catch (MailException e) {
					retryCount++;
					if (retryCount == maxRetries) {
						throw e;
					}
					LoggerUtil.logWarning("Email send attempt " + retryCount + " failed, retrying...");
					Thread.sleep(2000); // Wait 2 seconds before retry
				}
			}
		} catch (Exception e) {
			LoggerUtil.logError("Failed to send test execution report email", e);
		}
	}

	private String formatSubject() {
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
		return String.format("%s - %s", emailConfig.getSubject(), date);
	}

	private String createEmailContent(Map<String, Object> stats) {
		StringBuilder content = new StringBuilder();
		content.append("<html><body>");
		content.append("<h2>Test Execution Summary</h2>");
		content.append("<hr/>");

		// Add stats
		content.append("<table style='border-collapse: collapse; width: 60%;'>");
		content.append("<tr><td>Total Tests:</td><td>").append(stats.get("totalTests")).append("</td></tr>");
		content.append("<tr><td>Passed:</td><td>").append(stats.get("passed")).append("</td></tr>");
		content.append("<tr><td>Failed:</td><td>").append(stats.get("failed")).append("</td></tr>");
		content.append("<tr><td>Success Rate:</td><td>").append(stats.get("successRate")).append("%</td></tr>");
		content.append("</table>");

		// Add failed test details if any
		if (stats.containsKey("failedTests")) {
			content.append("<h3>Failed Test Details:</h3>");
			content.append("<ul>");
			@SuppressWarnings("unchecked")
			Map<String, String> failedTests = (Map<String, String>) stats.get("failedTests");
			failedTests.forEach((testId, reason) -> content.append("<li>").append(testId).append(": ").append(reason)
					.append("</li>"));
			content.append("</ul>");
		}

		content.append("<p>Please find detailed test report attached.</p>");
		content.append("</body></html>");

		return content.toString();
	}
}
