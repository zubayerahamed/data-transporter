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

	@Autowired
	@Qualifier("jdbcTemplatePrimary")
	private JdbcTemplate jdbcTemplatePrimary;

	@Autowired
	@Qualifier("jdbcTemplateFrom")
	private JdbcTemplate jdbcTemplateFrom;

	@Autowired
	@Qualifier("jdbcTemplateTo")
	private JdbcTemplate jdbcTemplateTo;

	@Value("${primary.table.name:recordstatus}")
	private String primaryDBTable;

	@Value("${from.table.name:fromtable}")
	private String fromDBTable;

	@Value("${to.table.name:totable}")
	private String toDBTable;

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
		if(log.isDebugEnabled()) {
			sourceData.stream().forEach(r -> {
				log.debug(r.get("id") + " - " + r.get("branch") + " - " + r.get("date"));
			});
		}

		// INSERT SOURCE DATA TO DESTINATION DATA
		boolean status = insertSourceDataToDestination(sourceData);
		System.out.println(status);
	}

	@Transactional
	private boolean insertSourceDataToDestination(List<Map<String, Object>> sourceData) {
		if(sourceData == null || sourceData.isEmpty()) return false;

		for(Map<String, Object> map : sourceData) {

			Integer id = (Integer) map.get("id");
			String value = (String) map.get("branch");
			Date date = (Date) map.get("date");

			StringBuilder sql = new StringBuilder("INSERT INTO " + toDBTable + " ( ");
			sql.append(" id, value, date ) VALUES ('"+ id +"','"+ value +"','"+ SDF.format(date) +"') ");

			log.debug("==> Isert query {}", sql.toString());
			int count = jdbcTemplateTo.update(sql.toString());
		}

		// INSERT LAST DATA TO RECORD STATUS
		Map<String, Object> map = sourceData.get(sourceData.size() - 1);
		String value = (String) map.get("branch");
		Date date = (Date) map.get("date");
		StringBuilder sql = new StringBuilder("INSERT INTO " + primaryDBTable + " (name, date) VALUES ('"+ value +"','"+ SDF.format(date) +"') ");
		int count = jdbcTemplatePrimary.update(sql.toString());

		return true;
	}

	private Date getLastInsertedRecordDate() {
		StringBuilder sql = new StringBuilder("SELECT * FROM " + primaryDBTable + " ORDER BY date DESC LIMIT 1");

		List<Map<String, Object>> result = jdbcTemplatePrimary.queryForList(sql.toString());
		if(log.isDebugEnabled()) {
			result.stream().forEach(r -> {
				log.debug(r.get("id") + " - " + r.get("name") + " - " + r.get("date"));
			});
		}

		Date lastRecordDate = null;
		if(!result.isEmpty()) {
			Map<String, Object> rowMap = result.stream().findFirst().orElse(null);
			lastRecordDate = (Date) rowMap.get("date");
		}
		System.out.println(lastRecordDate);
		return lastRecordDate;
	}

	private List<Map<String, Object>> readDataFromSourceTable(Date date){
		StringBuilder sql = new StringBuilder("SELECT * FROM " + fromDBTable);
		if(date != null) {
			sql.append(" WHERE date > '"+ SDF.format(date) +"' ");
		}
		log.debug("==> From selection : {}", sql.toString());
		return jdbcTemplateFrom.queryForList(sql.toString());
	}
}
