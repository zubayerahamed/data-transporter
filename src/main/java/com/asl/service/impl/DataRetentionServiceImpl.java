package com.asl.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.asl.service.DataRetentionService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 17, 2020
 */
@Slf4j
@Service
@Profile({"!file & !api"})
public class DataRetentionServiceImpl implements DataRetentionService {

	protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired @Qualifier("jdbcTemplatePrimary") private JdbcTemplate jdbcTemplatePrimary;

	@Value("${primary.table.name:recordstatus}")
	private String primaryDBTable;

	@Value("${primary.table.retention.day:1}")
	private String dataRetentionDay;

	@Override
	@Transactional
	public int doDataRetention() {

		int retentionDay = 1;
		try {
			retentionDay = Integer.valueOf(dataRetentionDay).intValue();
		} catch (Exception e) {
			log.error("ERROR IS : {}, {}", e.getMessage(), e);
		}

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, - retentionDay);
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);

		StringBuilder sql = new StringBuilder("DELETE FROM "+ primaryDBTable +" WHERE FINAL_DATE < '"+ SDF.format(cal.getTime()) +"' AND LIGHT_WEIGHT_TIME_STAMP IS NOT NULL AND LOAD_WEIGHT_TIME_STAMP IS NOT NULL;");
		log.info("===> Data retention query : {}", sql.toString());
		int count = jdbcTemplatePrimary.update(sql.toString());
		return count;
	}

}
