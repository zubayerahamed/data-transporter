package com.asl.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
	@Value("${from.table.read.condition.column}")
	protected List<String> fromTableConditionColumns;
	@Value("${from.table.read.condition.column.type}")
	protected String formConditionColumnsType;
	@Value("${from.table.read.condition.select.case}")
	protected List<String> selectCase;
	@Value("${from.table.read.condition.select.case.name}")
	protected String selectCaseColName;
	@Value("${from.table.read.orderby.column}")
	protected String orderByCol;
	@Value("${from.table.read.orderby.type}")
	protected String orderByType;
	@Value("${from.table.read.starting.date}")
	protected String defaultStartingDate;

	// DESTINATION TABLE CONFIG
	@Value("${to.table.name:totable}")
	protected String toDBTable;
	@Value("${to.table.insert.columns}")
	protected List<String> toTableInsertColumns;
	@Value("#{${to.table.values.columns}}")
	protected Map<String,String> toTableValuesColumns;
	@Value("#{${to.table.update.columns.map}}")
	protected Map<String,String> updateDestinationColumnMap;
	@Value("#{${to.table.update.columns.where.map}}")
	protected Map<String, String> updateWhere;

	@Override
	public List<Map<String, Object>> readDataFromSourceTable(String latestRecordValue){
		StringBuilder sql = new StringBuilder("SELECT "+ getColumnsString(fromTableColumns));
		if(!selectCase.isEmpty()) {
			String case1 = selectCase.get(0);
			String case2 = selectCase.get(1);
			sql.append(", ");
			sql.append("CASE ");
			sql.append("	WHEN "+ case1 +" > "+ case2 +" "); 
			sql.append("	THEN " + case1 + " "); 
			sql.append("	ELSE COALESCE("+ case2 +", "+ case1 +") "); 
			sql.append("END AS "+ selectCaseColName +" ");
		}
		sql.append(" FROM " + fromDBTable);

		if("DATE".equalsIgnoreCase(formConditionColumnsType) && StringUtils.isBlank(latestRecordValue)) {
			latestRecordValue = defaultStartingDate;
		}

		if(StringUtils.isNotBlank(latestRecordValue)) {
			sql.append(" WHERE ");
			for(int i = 0; i < fromTableConditionColumns.size(); i++) {
				if(i == 0 && fromTableConditionColumns.size() > 1) sql.append(" ( ");
				if(i != 0 && fromTableConditionColumns.size() > 1) sql.append(" OR ");
				if("DATE".equalsIgnoreCase(formConditionColumnsType)) {
					sql.append(" FORMAT("+ fromTableConditionColumns.get(i) +",'yyyy-MM-dd HH:mm:ss') > '"+ latestRecordValue +"' ");
				} else {
					sql.append(" "+ fromTableConditionColumns.get(i) +" > "+ latestRecordValue +" ");
				}
				if(i == fromTableConditionColumns.size() - 1 && fromTableConditionColumns.size() > 1) sql.append(" ) ");
			}
		}
		sql.append(" ORDER BY "+ orderByCol +" "+ orderByType +" ");

		log.info("===> Source data selection query : {}", sql.toString());
		return jdbcTemplateFrom.queryForList(sql.toString());
	}




	@Override
	@Transactional
	public int updateSourceDataToDestination(List<Map<String, Object>> sourceData) {
		if(sourceData == null || sourceData.isEmpty()) return 0;

		int totalUpdated = 0;
		for(Map<String,Object> source : sourceData) {
			StringBuilder sql = new StringBuilder("UPDATE " + toDBTable + " ");
			sql.append(" SET ");
			StringBuilder setCondition = new StringBuilder();
			updateDestinationColumnMap.entrySet().stream().forEach(m -> {
				String key = m.getKey();
				String value = m.getValue();
				String[] dataWithType = value.split("\\|");
				String dataKey = dataWithType[0];
				String dataType = dataWithType[1];
				String modifiedValue = getModifiedValue(source.get(dataKey), dataType);
				if("DATE".equalsIgnoreCase(dataType)) {
					modifiedValue = "TO_TIMESTAMP('"+ modifiedValue +"', 'YYYY-MM-DD HH24:MI:SS.FF')";
					setCondition.append(" "+ key +"="+ modifiedValue +",");
				} else {
					setCondition.append(" "+ key +"='"+ modifiedValue +"',");
				}
				
			});
			// remove last comma
			int lastComma = setCondition.toString().lastIndexOf(',');
			sql.append(setCondition.toString().substring(0, lastComma));
			sql.append(" WHERE ");

			// Where condition
			String whereKey = null;
			String dataKey = null;
			String dataType = null;
			for(Map.Entry<String, String> u : updateWhere.entrySet()) {
				whereKey = u.getKey();
				String whereValue = u.getValue();
				String[] dataWithType = whereValue.split("\\|");
				dataKey = dataWithType[0];
				dataType = dataWithType[1];
			}
			sql.append(" "+ whereKey +"='"+ getModifiedValue(source.get(dataKey), dataType) +"' ");

			log.info("=============> Update destination data query {}", sql.toString());
			int count = jdbcTemplateTo.update(sql.toString());
			totalUpdated += count;
		}

		return totalUpdated;
	}



	@Transactional
	@Override
	public int insertSourceDataToDestination(List<Map<String, Object>> sourceData) {
		if(sourceData == null || sourceData.isEmpty()) return 0;

		int totalInsertedRow = 0;

		for(Map<String, Object> map : sourceData) {
			StringBuilder sql = new StringBuilder("INSERT INTO " + toDBTable)
										.append(" ("+ getColumnsString(toTableInsertColumns) +") ")
										.append(" VALUES ")
										.append(" ("+ generateValuesFromMapForOracle(toTableValuesColumns, map) +") ");

			log.info("===> Isert query {}", sql.toString());
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

	protected String getValuesString(List<String> valuesList) {
		StringBuilder columns = new StringBuilder();
		valuesList.stream().forEach(c -> {
			columns.append("'" + c + "',");
		});
		int lastComma = columns.toString().lastIndexOf(',');
		return columns.toString().substring(0, lastComma);
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

	protected String getPreviousFileNameWithDirectory(String fileName, String fileLocation) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		fileName = fileName + "-" + sdf.format(cal.getTime()) + ".csv";
		return fileLocation + fileName;
	}

	@Override
	@Transactional
	public int insertDummyDataToSourceTable() {

		String s = "SELECT TOP 1 TRANSACTION_ID FROM XXSSGIL_WB_INTEGRATION ORDER BY TRANSACTION_ID DESC";

		int id = 1;
		try {
			id += jdbcTemplateFrom.queryForObject(s, Integer.class);
		} catch (Exception e) {
			log.error("ERROR is : {}, {}", e.getMessage(), e);
		}

		String sql = "INSERT INTO XXSSGIL_WB_INTEGRATION \r\n" + 
				"    (TRANSACTION_ID,\r\n" + 
				"    TRIP_NUMBER,\r\n" + 
				"    LIGHT_WT_IN_KG,\r\n" + 
				"    LIGHT_WEIGHT_TIME_STAMP,\r\n" + 
				"    LOAD_WT_IN_KG,\r\n" + 
				"    LOAD_WEIGHT_TIME_STAMP,\r\n" + 
				"    LIGHT_WEIGHT_BY,\r\n" + 
				"    LOAD_WEIGHT_BY,\r\n" + 
				"    second_LIGHT_WEIGHT,\r\n" + 
				"    CREATE_BY,\r\n" + 
				"    CREATE_DATE,\r\n" + 
				"    UPDATE_BY,\r\n" + 
				"    UPDATE_DATE)\r\n" + 
				"VALUES \r\n" + 
				"    ('"+ id +"',\r\n" + 
				"    '10662960',\r\n" + 
				"    '14000',\r\n" + 
				"    '"+ SDF.format(new Date()) +"',\r\n" + 
				"    '8500',\r\n" + 
				"    '"+ SDF.format(new Date()) +"',\r\n" + 
				"    '1603',\r\n" + 
				"    '1603',\r\n" + 
				"    NULL,\r\n" + 
				"    '1603',\r\n" + 
				"    '"+ SDF.format(new Date()) +"',\r\n" + 
				"    '1603',\r\n" + 
				"    '"+ SDF.format(new Date()) +"')";
		
		return jdbcTemplateFrom.update(sql);
		
	}
}
