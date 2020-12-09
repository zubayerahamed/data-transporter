package com.asl.schedular;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asl.service.DataTransportService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 8, 2020
 */
@Slf4j
@Component
public class DBDataTransporterSchedular {

	@Autowired private DataTransportService service;

	@Scheduled(initialDelay = 2000L, fixedDelayString = "${data.transport.delay}")
	private void doDataTransport() {
		log.info("===> Schedular running at : {}", new Date());
		service.doDataTransport();
	}

}
