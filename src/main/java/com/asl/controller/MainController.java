package com.asl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asl.service.DataTransportService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 8, 2020
 */
@Slf4j
@RestController
@RequestMapping("/")
@Profile("!api")
public class MainController {

	@Autowired private DataTransportService service;

	@GetMapping
	public String sayHello() {
		log.info("===> Data transport service running from controller");
		service.doDataTransport();
		return "Application is running OK.";
	}
}
