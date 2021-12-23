package com.asl.service.impl;

import java.util.ArrayList;
import java.util.Collections;
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
@Profile({"!file & !api"})
public class DBDataTransportService extends AbstractDataTransportService {

	@Autowired @Qualifier("jdbcTemplatePrimary") private JdbcTemplate jdbcTemplatePrimary;

	// PRIMARY TABLES CONFIG
	@Value("${primary.table.name:recordstatus}")
	private String primaryDBTable;
	@Value("#{${primary.table.values.columns}}")
	private Map<String,String> primaryTableValuesColumns;
	@Value("${primary.table.insert.columns}")
	private List<String> primaryTableInsertColumns;
	@Value("${primary.table.read.orderby}")
	private String pOrderBy;
	@Value("${primary.table.read.condition.column}")
	private String fetchConditionColumn;
	@Value("${primary.table.read.condition.column.type}")
	private String fetchConditionColumnType;
	@Value("${primary.table.updatable.search.column}")
	private String updatableSearchColumn;

	@Override
	public void doDataTransport() {
		log.info("===> DB Profile is activated");

		// GET LAST RECORD DATE FROM PPRIMARY DB
		String latestRecordValue = null;
		if("DATE".equalsIgnoreCase(fetchConditionColumnType)) {
			latestRecordValue = getLatestRecordFromPrimaryDB() != null ? SDF.format((Date) getLatestRecordFromPrimaryDB()) : null;
		} else {
			latestRecordValue = getLatestRecordFromPrimaryDB() != null ? (String) getLatestRecordFromPrimaryDB() : null;
		}

		// search source data
		List<Map<String, Object>> sourceData = readDataFromSourceTable(latestRecordValue);
		if(sourceData == null || sourceData.isEmpty()) log.info("====> No source data found");

		// search updatable
		List<String> transactionIds = new ArrayList<>();
		sourceData.stream().forEach(r -> {
			String trids = ((Integer) r.get("TRANSACTION_ID")).toString();
			transactionIds.add(trids);
		});
		List<Map<String, Object>> updatableData = searcAndGethUpdatableData(transactionIds);
		List<String> updatableIds = new ArrayList<>();
		updatableData.stream().forEach(r -> {
			String trids = (String) r.get("TRANSACTION_ID");
			updatableIds.add(trids);
		});

		// Seperate updatable & insertable data
		List<Map<String,Object>> newdata = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> olddata = new ArrayList<Map<String,Object>>();
		sourceData.stream().forEach(s -> {
			if(updatableIds.contains(((Integer) s.get("TRANSACTION_ID")).toString())) {
				olddata.add(s);
			} else {
				newdata.add(s);
			}
		});

		log.info("===> new data : {}", newdata.size());
		log.info("===> old data : {}", olddata.size());

		// Insert new source data to primary db
		int pdbNewInserted = insertSourceDataToPrimaryDB(newdata);
		log.info("Total new data inserted to PDB : {}", pdbNewInserted);

		// Insert new source data to source db
		int sodNewInserted = insertSourceDataToDestination(newdata);
		log.info("Total new data inserted to SOD : {}", sodNewInserted);

		// update old source data to primary db
		int pdbOldUpdated = updateSourceDataToPrimaryDB(olddata);
		log.info("Total old data updated to PDB : {}", pdbOldUpdated);

		// update old source data to source db
		int sodOldUpdated = updateSourceDataToDestination(olddata);
		log.info("Total old data updated to SOD : {}", sodOldUpdated);
	}

	/**
	 * GET LATEST RECORD
	 * @return {@link Date}
	 */
	@Override
	public Object getLatestRecordFromPrimaryDB() {
		StringBuilder sql = new StringBuilder("SELECT * FROM " + primaryDBTable + " ORDER BY "+ pOrderBy +" DESC LIMIT 1");

		List<Map<String, Object>> result = jdbcTemplatePrimary.queryForList(sql.toString());

		if(!result.isEmpty()) {
			Map<String, Object> rowMap = result.stream().findFirst().orElse(null);
			return rowMap.get(fetchConditionColumn);
		}
		return null;
	}

	
	
	@Transactional
	@Override
	public int insertSourceDataToPrimaryDB(List<Map<String, Object>> sourceData) {
		if(sourceData == null || sourceData.isEmpty()) return 0;

		int totalInserted = 0;
		for(Map<String, Object> map : sourceData) {
			StringBuilder sql = new StringBuilder("INSERT INTO " + primaryDBTable + " ("+ getColumnsString(primaryTableInsertColumns) +") VALUES ("+ generateValuesFromMap(primaryTableValuesColumns, map) +") ");
			int count = jdbcTemplatePrimary.update(sql.toString());
			totalInserted += count;
		}

		log.info("===> Total data inserted to Primary DB , {}", totalInserted);
		return totalInserted;
	}

	@Transactional
	@Override
	public int updateSourceDataToPrimaryDB(List<Map<String, Object>> olddata) {
		if(olddata == null || olddata.isEmpty()) return 0;

		int totalUpdated = 0;
		for(Map<String, Object> map : olddata) {
			String transId =  map.get("TRANSACTION_ID") != null ? ((Integer) map.get("TRANSACTION_ID")).toString() : null;
			String lightWeightDate = map.get("LIGHT_WEIGHT_TIME_STAMP") != null ? SDF.format(map.get("LIGHT_WEIGHT_TIME_STAMP")) : null;
			String loadWeightDate = map.get("LOAD_WEIGHT_TIME_STAMP") != null ? SDF.format(map.get("LOAD_WEIGHT_TIME_STAMP")) : null;
			String finalDate = map.get("ORDER_BY_COL") != null ? SDF.format(map.get("ORDER_BY_COL")) : null;

			StringBuilder sql = new StringBuilder("UPDATE " + primaryDBTable + " SET LIGHT_WEIGHT_TIME_STAMP='"+ lightWeightDate +"', LOAD_WEIGHT_TIME_STAMP='"+ loadWeightDate +"', FINAL_DATE='"+ finalDate +"' WHERE TRANSACTION_ID='"+ transId +"' ");
			log.info("===> Update query for primaryDB : {}", sql.toString());
			int count = jdbcTemplatePrimary.update(sql.toString());
			totalUpdated += count;
		}

		log.info("===> Total data update to Primary DB , {}", totalUpdated);
		return totalUpdated;
	}


	@Override
	public List<Map<String, Object>> searcAndGethUpdatableData(List<String> trids) {
		if(trids == null || trids.isEmpty()) return Collections.emptyList();

		StringBuilder sql = new StringBuilder("SELECT * FROM " + primaryDBTable + " WHERE " + updatableSearchColumn + " IN ("+ getValuesString(trids) +") ");

		log.info("===> Search for updatable data query : {}", sql.toString());
		return jdbcTemplatePrimary.queryForList(sql.toString());
	}

	
}
