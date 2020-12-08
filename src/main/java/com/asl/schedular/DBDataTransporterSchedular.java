package com.asl.schedular;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 8, 2020
 */
@Slf4j
@Component
public class DBDataTransporterSchedular {

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

	@Scheduled(initialDelay = 2000L, fixedDelayString = "${data.transport.delay}")
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
			sql.append(" WHERE date BETWEEN '"+ date +"' AND '"+ new Date() +"' ");
		}
		return jdbcTemplateFrom.queryForList(sql.toString());
	}

}
