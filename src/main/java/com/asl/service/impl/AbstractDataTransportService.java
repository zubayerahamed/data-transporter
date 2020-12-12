package com.asl.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asl.service.ASLSessionManager;
import com.asl.service.DataTransportService;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 12, 2020
 */
@Slf4j
public abstract class AbstractDataTransportService implements DataTransportService {

	protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired @Qualifier("jdbcTemplateFrom") protected JdbcTemplate jdbcTemplateFrom;
	@Autowired @Qualifier("jdbcTemplateTo") protected JdbcTemplate jdbcTemplateTo;
	@Autowired protected ASLSessionManager sessionManager;

	// SOURCE TABLE CONFIG
	@Value("${from.table.name:fromtable}")
	protected String fromDBTable;
	@Value("#{${from.table.columns}}")
	protected Map<String,String> fromTableColumns;
	@Value("#{${from.table.read.condition.column}}")
	protected Map<String, String> fromTableConditionColumn;

	// DESTINATION TABLE CONFIG
	@Value("${to.table.name:totable}")
	protected String toDBTable;
	@Value("${to.table.insert.columns}")
	protected List<String> toTableInsertColumns;
	@Value("#{${to.table.values.columns}}")
	protected Map<String,String> toTableValuesColumns;

	/**
	 * GET DATA FROM SOURCE TABLE
	 * @param date
	 * @return List&lt;Map&lt;String, Object>> results
	 */
	protected List<Map<String, Object>> readDataFromSourceTable(String latestRecordValue){
		StringBuilder sql = new StringBuilder("SELECT "+ getColumnsString(fromTableColumns) +" FROM " + fromDBTable + " ");
		if(StringUtils.isNotBlank(latestRecordValue)) {
			Map.Entry<String, String> col = fromTableConditionColumn.entrySet().stream().findFirst().get();
			String colName = col.getKey();
			String colType = col.getValue();
			if ("DATE".equalsIgnoreCase(colType)) {
				sql.append(" WHERE FORMAT("+ colName +",'yyyy-MM-dd HH:mm:ss') > '"+ latestRecordValue +"' ");
			} else {
				sql.append(" WHERE "+ colName +" > "+ latestRecordValue +" ");
			}
		}

		log.info("==> Source data selection query : {}", sql.toString());
		return jdbcTemplateFrom.queryForList(sql.toString());
	}

	/**
	 * INSERT SOURCE DATA TO DESTINATION DB
	 * @param sourceData
	 * @return {@link int} Number of record inserted
	 */
	@Transactional
	protected int insertSourceDataToDestination(List<Map<String, Object>> sourceData) {
		if(sourceData == null || sourceData.isEmpty()) return 0;

		int totalInsertedRow = 0;

		for(Map<String, Object> map : sourceData) {
			StringBuilder sql = new StringBuilder("INSERT INTO " + toDBTable)
										.append(" ("+ getColumnsString(toTableInsertColumns) +") ")
										.append(" VALUES ")
										.append(" ("+ generateValuesFromMapForOracle(toTableValuesColumns, map) +") ");

			log.info("==> Isert query {}", sql.toString());
			int count = jdbcTemplateTo.update(sql.toString());
			totalInsertedRow += count;
		}

		return totalInsertedRow;
	}

	protected String generateValuesFromMapForOracle(Map<String, String> propertiesMap, Map<String, Object> sourceDataMap) {
		StringBuilder values = new StringBuilder();
		propertiesMap.entrySet().stream().forEach(e -> {
			String data = getModifiedValue(sourceDataMap.get(e.getKey()), e.getValue());
			if(e.getValue().equalsIgnoreCase("DATE")) {
				String datevalue = "TO_TIMESTAMP('"+ data +"', 'YYYY-MM-DD HH24:MI:SS.FF')";
				values.append(datevalue +",");
			} else {
				values.append("'"+ data +"',");
			}
		});
		int lastComma = values.toString().lastIndexOf(',');
		return values.toString().substring(0, lastComma);
	}

	protected String generateValuesFromMap(Map<String, String> propertiesMap, Map<String, Object> map) {
		StringBuilder values = new StringBuilder();
		propertiesMap.entrySet().stream().forEach(e -> {
			values.append("'"+ getModifiedValue(map.get(e.getKey()), e.getValue()) +"',");
		});
		int lastComma = values.toString().lastIndexOf(',');
		return values.toString().substring(0, lastComma);
	}

	protected String getColumnsString(List<String> columnsList) {
		StringBuilder columns = new StringBuilder();
		columnsList.stream().forEach(c -> {
			columns.append(c + ",");
		});
		int lastComma = columns.toString().lastIndexOf(',');
		return columns.toString().substring(0, lastComma);
	}

	protected String getColumnsString(Map<String,String> columnsList) {
		StringBuilder columns = new StringBuilder();
		columnsList.entrySet().stream().forEach(c -> {
			columns.append(c.getKey() + ",");
		});
		int lastComma = columns.toString().lastIndexOf(',');
		return columns.toString().substring(0, lastComma);
	}

	protected String getModifiedValue(Object data, String dataType) {
		if(data == null) return "";
		if("DATE".equalsIgnoreCase(dataType)) {
			return SDF.format((Date) data);
		} else if ("INTEGER".equalsIgnoreCase(dataType)) {
			return ((Integer) data).toString();
		} else if ("LONG".equalsIgnoreCase(dataType)) {
			return ((Long) data).toString();
		} else if ("BIGDECIMAL".equalsIgnoreCase(dataType)) {
			return  ((BigDecimal) data).toString();
		} else {
			return (String) data;
		}
	}

	/**
	 * GET FILE NAME WITH DIRECTORY
	 * @param fileName
	 * @param fileLocation
	 * @return {@link String}
	 */
	protected String getFileNameWithDirectory(String fileName, String fileLocation) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		fileName = fileName + "-" + sdf.format(new Date()) + ".csv";
		return fileLocation + fileName;
	}
}
