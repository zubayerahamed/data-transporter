package com.asl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asl.service.DataRetentionService;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 17, 2020
 */
@RestController
@RequestMapping("/clear/data")
public class DataRetentionController {

	@Autowired private DataRetentionService service;

	@GetMapping
	public String doDataRetention() {
		int count = service.doDataRetention();
		return "Number of record deleted : " + count;
	}
}
