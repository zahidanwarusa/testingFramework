
package com.umr.apitesting.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umr.apitesting.core.executor.TestExecutor;
import com.umr.apitesting.utils.LoggerUtil;
import com.umr.apitesting.web.model.TestCase;
import com.umr.apitesting.web.model.TestExecutionStatus;
import com.umr.apitesting.web.service.TestService;
import com.umr.apitesting.web.websocket.WebSocketHandler;

@RestController
@RequestMapping("/v1/tests")
public class TestController {

	private final TestExecutor testExecutor;
	private final WebSocketHandler webSocketHandler;
	private final TestService testService;

	public TestController(TestExecutor testExecutor, WebSocketHandler webSocketHandler, TestService testService) {
		this.testExecutor = testExecutor;
		this.webSocketHandler = webSocketHandler;
		this.testService = testService;
	}

	@GetMapping
	public ResponseEntity<List<TestCase>> getAllTests() {
		try {
			List<TestCase> tests = testService.getActiveTests();
			return ResponseEntity.ok(tests);
		} catch (Exception e) {
			LoggerUtil.logError("Error fetching tests", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/execute/{testId}")
	public ResponseEntity<?> executeTest(@PathVariable String testId) {
		try {
			// Send initial status through WebSocket
			webSocketHandler.broadcastMessage(Map.of("type", "TEST_STARTED", "testId", testId, "status", "RUNNING"));

			testExecutor.executeTest(testId);

			// Send completion status
			webSocketHandler
					.broadcastMessage(Map.of("type", "TEST_COMPLETED", "testId", testId, "status", "COMPLETED"));

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			LoggerUtil.logError("Error executing test: " + testId, e);

			// Send error status
			webSocketHandler.broadcastMessage(
					Map.of("type", "TEST_ERROR", "testId", testId, "status", "ERROR", "message", e.getMessage()));

			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}

	@GetMapping("/status/{testId}")
	public ResponseEntity<?> getTestStatus(@PathVariable String testId) {
		try {
			// Get test execution status from TestExecutor
			TestExecutionStatus status = testExecutor.getTestStatus(testId);
			return ResponseEntity.ok(status);
		} catch (Exception e) {
			LoggerUtil.logError("Error getting test status: " + testId, e);
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}
}