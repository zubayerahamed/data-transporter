package com.asl.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 9, 2020
 */
@Slf4j
@Service
@Profile("!file")
public class DBDataTransportService extends AbstractDataTransportService {

	@Autowired @Qualifier("jdbcTemplatePrimary") private JdbcTemplate jdbcTemplatePrimary;

	// PRIMARY TABLES CONFIG
	@Value("${primary.table.name:recordstatus}")
	private String primaryDBTable;
	@Value("${primary.table.columns}")
	private List<String> primaryTableColumns;
	@Value("#{${primary.table.values.columns}}")
	private Map<String,String> primaryTableValuesColumns;
	@Value("${primary.table.insert.columns}")
	private List<String> primaryTableInsertColumns;
	@Value("${primary.table.read.orderby}")
	private String pOrderBy;
	@Value("${primary.table.read.condition.column}")
	private String fetchConditionColumn;

	@Override
	public void doDataTransport() {
		log.info("===> DB Profile is activated");

		// GET LAST RECORD DATE FROM PPRIMARY DB
		String latestRecordValue = null;
		if("date".equalsIgnoreCase(fetchConditionColumn)) {
			latestRecordValue = getLatestRecord() != null ? SDF.format((Date) getLatestRecord()) : null;
		} else {
			latestRecordValue = getLatestRecord() != null ? (String) getLatestRecord() : null;
		}

		// READ DATA FROM SOURCE DB
		List<Map<String, Object>> sourceData = readDataFromSourceTable(latestRecordValue);
		if(sourceData.isEmpty()) {
			log.info("===> No new source data found to do transport");
			return;
		}

		// INSERT SOURCE DATA TO DESTINATION DB
		int rows = insertSourceDataToDestination(sourceData);
		if(rows < 1) {
			log.info("===> No record is inserted to Destination DB");
			return;
		}

		// INSERT LATEST SOURCE DATA TO PRIMARY TABLE FOR RECORD TRACKING
		int statRow = insertLatestSourceDataToPrimaryDB(sourceData);
		if(statRow != 1) log.info("===> Latest record is not inserted into primary DB");
	}

	/**
	 * GET LATEST RECORD DATE
	 * @return {@link Date}
	 */
	private Object getLatestRecord() {
		StringBuilder sql = new StringBuilder("SELECT * FROM " + primaryDBTable + " ORDER BY "+ pOrderBy +" DESC LIMIT 1");

		List<Map<String, Object>> result = jdbcTemplatePrimary.queryForList(sql.toString());

		if(!result.isEmpty()) {
			Map<String, Object> rowMap = result.stream().findFirst().orElse(null);
			return rowMap.get(fetchConditionColumn);
		}

		return null;
	}

	
	/**
	 * INSERT LATEST SOURCE DATA TO PRIMARY TABLE FOR RECORD TRACKING
	 * @param sourceData
	 * @return {@link int}
	 */
	@Transactional
	private int insertLatestSourceDataToPrimaryDB(List<Map<String, Object>> sourceData) {
		if(sourceData == null || sourceData.isEmpty()) return 0;

		Map<String, Object> map = sourceData.get(sourceData.size() - 1);

		StringBuilder sql = new StringBuilder("INSERT INTO " + primaryDBTable + " ("+ getColumnsString(primaryTableInsertColumns) +") VALUES ("+ generateValuesFromMap(primaryTableValuesColumns, map) +") ");

		return jdbcTemplatePrimary.update(sql.toString());
	}

}
