package com.umr.apitesting.core.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.umr.apitesting.web.model.TestExecutionStatus;

@Service
public class EmailTemplateService {

	public String generateEmailContent(Map<String, TestExecutionStatus> testStatuses, String environment) {
		StringBuilder html = new StringBuilder();
		html.append(getEmailHeader(environment));
		html.append(createSummarySection(testStatuses));
		html.append(createDetailsTable(testStatuses));
		html.append(getEmailFooter());
		return html.toString();
	}

	private String getEmailHeader(String environment) {
		return String.format("""
				<html>
				<head>
				    <style>
				        body { font-family: Arial, sans-serif; margin: 20px; }
				        .summary { background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
				        .stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
				        .stat-card { padding: 15px; border-radius: 5px; text-align: center; }
				        .details-table { width: 100%%; border-collapse: collapse; margin-top: 20px; }
				        .details-table th, .details-table td { padding: 10px; border: 1px solid #dee2e6; }
				        .details-table th { background-color: #f8f9fa; }
				        .success { background-color: #d4edda; }
				        .failure { background-color: #f8d7da; }
				    </style>
				</head>
				<body>
				    <h2>%s Environment - API Test Execution Report</h2>
				    <p>Generated on: %s</p>
				""", environment.toUpperCase(), java.time.LocalDateTime.now()
				.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	}

	private String createSummarySection(Map<String, TestExecutionStatus> testStatuses) {
		int total = testStatuses.size();
		long passed = getPassedCount(testStatuses);
		long failed = total - passed;
		double successRate = total > 0 ? (passed * 100.0) / total : 0;

		return String.format("""
				<div class="summary">
				    <h3>Test Execution Summary</h3>
				    <div class="stats">
				        <div class="stat-card" style="background-color: #e9ecef;">
				            <h4>Total Tests</h4>
				            <div style="font-size: 24px;">%d</div>
				        </div>
				        <div class="stat-card" style="background-color: #d4edda;">
				            <h4>Passed</h4>
				            <div style="font-size: 24px;">%d</div>
				        </div>
				        <div class="stat-card" style="background-color: #f8d7da;">
				            <h4>Failed</h4>
				            <div style="font-size: 24px;">%d</div>
				        </div>
				        <div class="stat-card" style="background-color: #e9ecef;">
				            <h4>Success Rate</h4>
				            <div style="font-size: 24px;">%.1f%%</div>
				        </div>
				    </div>
				</div>
				""", total, passed, failed, successRate);
	}

	private String createDetailsTable(Map<String, TestExecutionStatus> testStatuses) {
		StringBuilder table = new StringBuilder();
		table.append("""
				<h3>Test Execution Details</h3>
				<table class="details-table">
				    <tr>
				        <th>Test ID</th>
				        <th>Status</th>
				        <th>Duration</th>
				        <th>Steps</th>
				        <th>Message</th>
				    </tr>
				""");

		testStatuses.forEach((testId, status) -> {
			String rowClass = status.getStatus() == TestExecutionStatus.Status.COMPLETED ? "success" : "failure";
			long duration = status.getEndTime() - status.getStartTime();

			table.append(String.format("""
					<tr class="%s">
					    <td>%s</td>
					    <td>%s</td>
					    <td>%d ms</td>
					    <td>%d/%d</td>
					    <td>%s</td>
					</tr>
					""", rowClass, testId, status.getStatus(), duration, status.getCompletedSteps(),
					status.getTotalSteps(), status.getMessage() != null ? status.getMessage() : "-"));
		});

		table.append("</table>");
		return table.toString();
	}

	private String getEmailFooter() {
		return """
				<div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #dee2e6;">
				    <p>Please find the detailed HTML report attached.</p>
				    <p style="color: #6c757d;"><small>This is an automated email from the API Testing Framework.</small></p>
				</div>
				</body>
				</html>
				""";
	}

	private long getPassedCount(Map<String, TestExecutionStatus> testStatuses) {
		return testStatuses.values().stream()
				.filter(status -> status.getStatus() == TestExecutionStatus.Status.COMPLETED).count();
	}
}