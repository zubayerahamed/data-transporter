package com.asl.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 8, 2020
 */
@Slf4j
@RestController
@RequestMapping("/")
public class MainController {

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired @Qualifier("jdbcTemplatePrimary") private JdbcTemplate jdbcTemplatePrimary;
	@Autowired @Qualifier("jdbcTemplateFrom") private JdbcTemplate jdbcTemplateFrom;
	@Autowired @Qualifier("jdbcTemplateTo") private JdbcTemplate jdbcTemplateTo;

	@Value("${primary.table.name:recordstatus}")
	private String primaryDBTable;
	@Value("${from.table.name:fromtable}")
	private String fromDBTable;
	@Value("${to.table.name:totable}")
	private String toDBTable;

	@Value("${primary.table.columns}")
	private List<String> primaryTableColumns;
	@Value("#{${from.table.columns}}")
	private Map<String,String> fromTableColumns;
	@Value("${to.table.columns}")
	private List<String> toTableColumns;
	@Value("#{${primary.table.insert.columns}}")
	private Map<String,String> primaryTableInsertColumns;

	@GetMapping
	public String sayHello() {
		doDataTransport();

		return "Application is running OK.";
	}

	private void doDataTransport() {
		log.info("===> Schedular running at : {}", new Date());

		Date lastRecordDate = getLastInsertedRecordDate();

		// READ DATA FROM SOURCE DB
		List<Map<String, Object>> sourceData = readDataFromSourceTable(lastRecordDate);
		if(sourceData.isEmpty()) {
			log.debug("===> No new source data found....");
			return;
		}

		// INSERT SOURCE DATA TO DESTINATION DB
		boolean status = insertSourceDataToDestination(sourceData);
		System.out.println(status);
	}

	private String generateValuesFromMap(Map<String, String> propertiesMap, Map<String, Object> map) {
		StringBuilder values = new StringBuilder();
		propertiesMap.entrySet().stream().forEach(e -> {
			values.append("'"+ getModifiedValue(map.get(e.getKey()), e.getValue()) +"',");
		});
		int lastComma = values.toString().lastIndexOf(',');
		return values.toString().substring(0, lastComma);
	}

	private String getModifiedValue(Object data, String dataType) {
		if(data == null) return "";
		if("DATE".equalsIgnoreCase(dataType)) {
			return SDF.format((Date) data);
		} else if ("INTEGER".equalsIgnoreCase(dataType)) {
			return ((Integer) data).toString();
		} else {
			return (String) data;
		}
	}

	@Transactional
	private boolean insertSourceDataToDestination(List<Map<String, Object>> sourceData) {
		if(sourceData == null || sourceData.isEmpty()) return false;

		for(Map<String, Object> map : sourceData) {
			StringBuilder sql = new StringBuilder("INSERT INTO " + toDBTable)
										.append(" ("+ getColumnsString(toTableColumns) +") ")
										.append(" VALUES ")
										.append(" ("+ generateValuesFromMap(fromTableColumns, map) +") ");
			System.out.println(sql.toString());
			log.debug("==> Isert query {}", sql.toString());
			//int count = jdbcTemplateTo.update(sql.toString());
		}

		// INSERT LAST DATA TO RECORD STATUS
		Map<String, Object> map = sourceData.get(sourceData.size() - 1);
		String value = (String) map.get("branch");
		Date date = (Date) map.get("date");
		StringBuilder sql = new StringBuilder("INSERT INTO " + primaryDBTable + " (value, date) VALUES ("+ generateValuesFromMap(primaryTableInsertColumns, map) +") ");
		System.out.println(sql.toString());
		
		int count = jdbcTemplatePrimary.update(sql.toString());

		return true;
	}

	private Date getLastInsertedRecordDate() {
		StringBuilder sql = new StringBuilder("SELECT * FROM " + primaryDBTable + " ORDER BY date DESC LIMIT 1");

		List<Map<String, Object>> result = jdbcTemplatePrimary.queryForList(sql.toString());

		Date lastRecordDate = null;
		if(!result.isEmpty()) {
			Map<String, Object> rowMap = result.stream().findFirst().orElse(null);
			if(rowMap.get("date") != null) lastRecordDate = (Date) rowMap.get("date");
		}

		log.debug("Last record date : {}", lastRecordDate);
		return lastRecordDate;
	}

	private List<Map<String, Object>> readDataFromSourceTable(Date date){
		StringBuilder sql = new StringBuilder("SELECT "+ getColumnsString(fromTableColumns) +" FROM " + fromDBTable);
		if(date != null) sql.append(" WHERE date > '"+ SDF.format(date) +"' ");
		log.debug("==> Source data selection query : {}", sql.toString());
		return jdbcTemplateFrom.queryForList(sql.toString());
	}

	private String getColumnsString(List<String> columnsList) {
		StringBuilder columns = new StringBuilder();
		columnsList.stream().forEach(c -> {
			columns.append(c + ",");
		});
		int lastComma = columns.toString().lastIndexOf(',');
		return columns.toString().substring(0, lastComma);
	}

	private String getColumnsString(Map<String,String> columnsList) {
		StringBuilder columns = new StringBuilder();
		columnsList.entrySet().stream().forEach(c -> {
			columns.append(c.getKey() + ",");
		});
		int lastComma = columns.toString().lastIndexOf(',');
		return columns.toString().substring(0, lastComma);
	}
}
