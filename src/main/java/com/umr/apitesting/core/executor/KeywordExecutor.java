package com.umr.apitesting.core.executor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.umr.apitesting.core.context.TestContext;
import com.umr.apitesting.keywords.BaseKeywords;
import com.umr.apitesting.keywords.Keyword;
import com.umr.apitesting.utils.LoggerUtil;

public class KeywordExecutor {
	private final Map<String, Method> keywordMap;
	private final BaseKeywords baseKeywords;

	public KeywordExecutor() {
		this.keywordMap = new HashMap<>();
		this.baseKeywords = new BaseKeywords();
		initializeKeywords();
	}

	private void initializeKeywords() {
		for (Method method : BaseKeywords.class.getMethods()) {
			if (method.isAnnotationPresent(Keyword.class)) {
				Keyword keyword = method.getAnnotation(Keyword.class);
				keywordMap.put(keyword.value(), method);
			}
		}
	}

	public boolean executeKeyword(String keyword, TestContext context) {
		try {
			Method method = keywordMap.get(keyword);
			if (method == null) {
				throw new IllegalArgumentException("Unknown keyword: " + keyword);
			}

			LoggerUtil.logInfo("Current context: " + context.getTestData(keyword));
			LoggerUtil.logInfo("Executing keyword: " + keyword);
			return (boolean) method.invoke(baseKeywords, context);

		} catch (Exception e) {
			LoggerUtil.logError("Failed to execute keyword: " + keyword, e);
			return false;
		}
	}
}
