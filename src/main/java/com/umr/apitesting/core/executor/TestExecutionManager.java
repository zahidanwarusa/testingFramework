package com.umr.apitesting.core.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.umr.apitesting.utils.LoggerUtil;
import com.umr.apitesting.web.model.ExecutionProgress;
import com.umr.apitesting.web.model.ExecutionRequest;
import com.umr.apitesting.web.model.TestCase;
import com.umr.apitesting.web.model.TestSummary;
import com.umr.apitesting.web.service.TestService;
import com.umr.apitesting.web.websocket.WebSocketHandler;

@Component
public class TestExecutionManager {
	private final TestService testService;
	private final WebSocketHandler wsHandler;
	private final Map<String, ExecutionProgress> executionProgress;

	public TestExecutionManager(TestService testService, WebSocketHandler wsHandler) {
		this.testService = testService;
		this.wsHandler = wsHandler;
		this.executionProgress = new ConcurrentHashMap<>();
	}

	public void executeTests(ExecutionRequest request) {
		try {
			// Get tests based on request criteria
			List<TestCase> tests = testService.getTests(request);

			// Initialize progress tracking
			initializeProgress(tests);

			// Create progress and message callbacks
			ProgressCallback progressCallback = new ProgressCallback() {
				@Override
				public void onProgress(String testId, String status, String message) {
					updateProgress(testId, status, message);
				}
			};

			MessageCallback messageCallback = new MessageCallback() {
				@Override
				public void onMessage(Object message) {
					wsHandler.broadcastMessage(message);
				}
			};

			// Create and execute parallel executor
			ParallelTestExecutor executor = new ParallelTestExecutor(tests, progressCallback, messageCallback);

			executor.execute();

		} catch (Exception e) {
			LoggerUtil.logError("Failed to execute tests", e);
			throw new RuntimeException("Test execution failed", e);
		}
	}

	private void initializeProgress(List<TestCase> tests) {
		tests.forEach(test -> {
			ExecutionProgress progress = ExecutionProgress.builder().testId(test.getTestId()).status("QUEUED")
					.startTime(System.currentTimeMillis()).build();
			executionProgress.put(test.getTestId(), progress);

			// Notify through WebSocket
			wsHandler.broadcastMessage(progress);
		});
	}

	private void updateProgress(String testId, String status, String message) {
		ExecutionProgress progress = executionProgress.get(testId);
		if (progress != null) {
			progress.setStatus(status);
			if (progress.getLogs() == null) {
				progress.setLogs(new ArrayList<>());
			}
			progress.getLogs().add(message);
			wsHandler.broadcastMessage(progress);
		}
	}

	public TestSummary getExecutionSummary() {
		Map<String, Integer> serviceStats = new ConcurrentHashMap<>();
		int total = executionProgress.size();
		int completed = (int) executionProgress.values().stream().filter(p -> "COMPLETED".equals(p.getStatus()))
				.count();

		return TestSummary.builder().testsByService(serviceStats).totalTests(total).status(
				completed == total ? TestSummary.ExecutionStatus.COMPLETED : TestSummary.ExecutionStatus.IN_PROGRESS)
				.build();
	}
}