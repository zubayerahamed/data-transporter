package com.asl.schedular;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asl.service.DataRetentionService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 17, 2020
 */
@Slf4j
@Component
@Profile("!api")
public class DataRetentionSchedular {

	@Autowired private DataRetentionService service;

	// Will be running at every day 23:50
	@Scheduled(cron = "0 50 23 * * *")
	private void doDataRetention() {
		log.info("===> Data retention is running at : {}", new Date());
		int count = service.doDataRetention();
		log.info("Number of row deleted : {}", count);
	}
}
