package com.umr.apitesting.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerUtil {
	private static final Logger LOG = LogManager.getLogger(LoggerUtil.class);

	public static void logInfo(String message) {
		LOG.info(message);
	}

	public static void logDebug(String message) {
		LOG.debug(message);
	}

	public static void logError(String message, Throwable throwable) {
		LOG.error(message, throwable);
	}

	public static void logWarning(String message) {
		LOG.warn(message);
	}
}