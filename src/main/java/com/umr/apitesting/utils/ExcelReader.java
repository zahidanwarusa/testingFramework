package com.umr.apitesting.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
	private String filePath;
	private Workbook workbook;
	private Sheet sheet;
	private Map<String, Integer> columns;

	public ExcelReader(String filePath) {
		this.filePath = filePath;
		this.columns = new HashMap<>();
		initializeWorkbook();
	}

	private void initializeWorkbook() {
		try (FileInputStream fis = new FileInputStream(filePath)) {
			workbook = new XSSFWorkbook(fis);
			LoggerUtil.logInfo("Successfully initialized workbook: " + filePath);
		} catch (IOException e) {
			LoggerUtil.logError("Failed to initialize workbook: " + filePath, e);
			throw new RuntimeException("Failed to initialize workbook", e);
		}
	}

	public void setSheet(String sheetName) {
		sheet = workbook.getSheet(sheetName);
		if (sheet == null) {
			throw new RuntimeException("Sheet not found: " + sheetName);
		}
		initializeColumns();
	}

	private void initializeColumns() {
		Row headerRow = sheet.getRow(0);
		for (Cell cell : headerRow) {
			columns.put(cell.getStringCellValue(), cell.getColumnIndex());
		}
	}

	public List<Map<String, String>> readData() {
		List<Map<String, String>> data = new ArrayList<>();

		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row != null) {
				Map<String, String> rowData = new HashMap<>();
				for (Map.Entry<String, Integer> column : columns.entrySet()) {
					Cell cell = row.getCell(column.getValue());
					rowData.put(column.getKey(), getCellValue(cell));
				}
				data.add(rowData);
			}
		}

		return data;
	}

	private String getCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}

		return switch (cell.getCellType()) {
		case STRING -> cell.getStringCellValue();
		case NUMERIC -> String.valueOf(cell.getNumericCellValue());
		case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
		case FORMULA -> cell.getCellFormula();
		default -> "";
		};
	}

	public void close() {
		try {
			if (workbook != null) {
				workbook.close();
			}
		} catch (IOException e) {
			LoggerUtil.logError("Failed to close workbook", e);
		}
	}
}