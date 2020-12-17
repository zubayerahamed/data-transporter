package com.asl;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Rollback;

import com.asl.service.DataRetentionService;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 13, 2020
 */
@SpringBootTest
@Rollback(false)
@ComponentScan(basePackages = { "com.asl.*" })
public class DataRetentionTest {

	@Autowired private DataRetentionService service;

	@Test
	@Transactional
	public void deleteAllPreviousData() {
		int count = service.doDataRetention();
		System.out.println("Number of row deleted : " + count);
	}
}
