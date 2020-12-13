package com.asl;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Rollback;

import com.asl.service.DataTransportService;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 13, 2020
 */
@SpringBootTest
@Rollback(false)
@ComponentScan(basePackages = { "com.asl.*" })
public class InsertTestData {

	@Autowired private DataTransportService service;

	@Test
	@Transactional
	public void insertTestData() {
		int count = service.insertDummyDataToSourceTable();
		System.out.println(count);
	}
}
