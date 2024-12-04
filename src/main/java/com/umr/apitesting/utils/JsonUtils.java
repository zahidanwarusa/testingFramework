package com.umr.apitesting.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUtils {
	private static final ObjectMapper mapper = new ObjectMapper();

	public static String makeJson(String keys, String values) {
		if (keys == null || values == null) {
			return null;
		}

		try {
			String[] keyArray = keys.split(",");
			String[] valueArray = values.split(",");

			if (keyArray.length != valueArray.length) {
				LoggerUtil.logError("Keys and values count mismatch", null);
				return null;
			}

			ObjectNode jsonNode = mapper.createObjectNode();

			for (int i = 0; i < keyArray.length; i++) {
				String key = keyArray[i].trim();
				String value = valueArray[i].trim();

				// Handle nested objects
				if (key.contains(".")) {
					handleNestedObject(jsonNode, key, value);
				} else {
					jsonNode.put(key, value);
				}
			}

			return mapper.writeValueAsString(jsonNode);

		} catch (Exception e) {
			LoggerUtil.logError("Failed to create JSON", e);
			return null;
		}
	}

	private static void handleNestedObject(ObjectNode parentNode, String key, String value) {
		String[] parts = key.split("\\.");
		ObjectNode currentNode = parentNode;

		// Create nested objects for all but the last part
		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i].trim();
			if (!currentNode.has(part)) {
				currentNode.putObject(part);
			}
			currentNode = (ObjectNode) currentNode.get(part);
		}

		// Set the value at the deepest level
		currentNode.put(parts[parts.length - 1].trim(), value);
	}
}