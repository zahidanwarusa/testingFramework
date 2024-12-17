package com.umr.apitesting.web.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.umr.apitesting.utils.ExcelReader;
import com.umr.apitesting.web.model.ExecutionRequest;
import com.umr.apitesting.web.model.TestCase;

@Service
public class TestService {
	private final String TEST_SUITE_PATH = "src/main/resources/testdata/TestSuite.xlsx";

	public List<TestCase> getTests(ExecutionRequest request) {
		ExcelReader reader = new ExcelReader(TEST_SUITE_PATH);
		reader.setSheet("TestCases");

		List<Map<String, String>> testData = reader.readData();
		return filterTestsByRequest(testData, request);
	}

	public List<TestCase> getActiveTests() {
		ExcelReader reader = new ExcelReader(TEST_SUITE_PATH);
		reader.setSheet("TestCases");

		List<Map<String, String>> testData = reader.readData();
		return testData.stream().filter(row -> "Y".equalsIgnoreCase(row.get("Execute"))).map(this::mapToTestCase)
				.collect(Collectors.toList());
	}

	private List<TestCase> filterTestsByRequest(List<Map<String, String>> testData, ExecutionRequest request) {
		return testData.stream().filter(row -> filterByExecutionType(row, request)).map(this::mapToTestCase)
				.collect(Collectors.toList());
	}

	private boolean filterByExecutionType(Map<String, String> row, ExecutionRequest request) {
		switch (request.getType()) {
		case ALL:
			return true;
		case EXECUTABLE:
			return "Y".equalsIgnoreCase(row.get("Execute"));
		case FILTERED:
			return filterByKeywords(row, request.getKeywords());
		default:
			return false;
		}
	}

	private boolean filterByKeywords(Map<String, String> row, List<String> keywords) {
		if (keywords == null || keywords.isEmpty()) {
			return true;
		}
		String testId = row.get("TestID");
		return keywords.stream().anyMatch(keyword -> testId.contains(keyword));
	}

	private TestCase mapToTestCase(Map<String, String> row) {
		TestCase testCase = new TestCase();
		testCase.setTestId(row.get("TestID"));
		testCase.setTestName(row.get("TestName"));
		testCase.setDescription(row.get("Description"));
		testCase.setExecute(row.get("Execute"));
		testCase.setPriority(row.get("Priority"));
		testCase.setAuthRequired(row.get("AuthRequired"));
		return testCase;
	}
}