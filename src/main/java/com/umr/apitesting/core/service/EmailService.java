package com.umr.apitesting.core.service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

	private static final int MAX_RETRIES = 3;
	private static final long RETRY_DELAY = 2000; // 2 seconds

	public void sendTestExecutionReport(String reportPath, Map<String, Object> executionStats) {
		if (!emailConfig.isEnabled()) {
			LoggerUtil.logInfo("Email notifications are disabled");
			return;
		}

		int retries = 0;
		Exception lastException = null;

		while (retries < MAX_RETRIES) {
			try {
				sendEmail(reportPath, executionStats);
				LoggerUtil.logInfo("Test execution report email sent successfully");
				return;
			} catch (Exception e) {
				lastException = e;
				retries++;
				LoggerUtil.logWarning("Email send attempt " + retries + " failed, retrying ...");

				if (retries < MAX_RETRIES) {
					try {
						Thread.sleep(RETRY_DELAY);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}

		LoggerUtil.logError("Failed to send test execution report email after " + MAX_RETRIES + " attempts",
				lastException);
		// Continue execution even if email fails
	}

	private void sendEmail(String reportPath, Map<String, Object> stats) throws Exception {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		// Set email metadata
		helper.setFrom(emailConfig.getFrom());
		helper.setTo(emailConfig.getRecipients());
		helper.setSubject(formatSubject());

		// Create email content
		String content = createEmailContent(stats);
		helper.setText(content, true);

		// Attach report if it exists
		File reportFile = new File(reportPath);
		if (reportFile.exists()) {
			helper.addAttachment("TestExecutionReport.html", reportFile);
		}

		// Send email
		mailSender.send(message);
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