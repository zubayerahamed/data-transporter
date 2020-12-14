package com.asl.service;

import java.util.Date;
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
	 * GET LATEST RECORD
	 * @return {@link Date}
	 */
	public Object getLatestRecordFromPrimaryDB();

	/**
	 * GET DATA FROM SOURCE TABLE
	 * @param date
	 * @return List&lt;Map&lt;String, Object>> results
	 */
	public List<Map<String, Object>> readDataFromSourceTable(String latestRecordValue);

	/**
	 * SEARCH AND FETCH ALL UPDATABLE DATA FROM DESTINATION DB
	 * @param tripNumbers
	 * @return
	 */
	public List<Map<String, Object>> searcAndGethUpdatableData(List<String> tripNumbers);

	/**
	 * INSERT SOURCE DATA TO PRIMARY DB FOR RECORD TRACKING
	 * @param sourceData
	 * @return {@link int}
	 */
	public int insertSourceDataToPrimaryDB(List<Map<String, Object>> sourceData);

	/**
	 * UPDATE SOURCE DATA TO PRIMARY DB
	 * @param sourceData
	 * @return
	 */
	public int updateSourceDataToPrimaryDB(List<Map<String, Object>> sourceData);
	
	
	/**
	 * UPDATE DATA TO DESTINATION DB
	 * @param sourceData
	 * @return
	 */
	public int updateSourceDataToDestination(List<Map<String, Object>> sourceData);

	/**
	 * INSERT SOURCE DATA TO DESTINATION DB
	 * @param sourceData
	 * @return {@link int} Number of record inserted
	 */
	public int insertSourceDataToDestination(List<Map<String, Object>> sourceData);

	public int insertDummyDataToSourceTable();

	
}
