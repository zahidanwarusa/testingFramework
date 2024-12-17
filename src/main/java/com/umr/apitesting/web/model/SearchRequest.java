package com.umr.apitesting.web.model;

import java.util.List;

import lombok.Data;

@Data
public class SearchRequest {
	public enum SearchType {
		KEYWORD, TEST_ID, SERVICE
	}

	private List<String> keywords;
	private SearchType type;
}