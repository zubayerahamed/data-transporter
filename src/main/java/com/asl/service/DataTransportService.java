package com.asl.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 9, 2020
 */
@Component
public interface DataTransportService {

	/**
	 * Data transport start point
	 */
	public void doDataTransport();

	/**
	 * GET DATA FROM SOURCE TABLE
	 * @param date
	 * @return List&lt;Map&lt;String, Object>> results
	 */
	public List<Map<String, Object>> readDataFromSourceTable(String latestRecordValue);

	public int insertDummyDataToSourceTable();
}
