/**
 * 
 */
package com.asl;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Rollback;

import com.asl.service.DataTransportService;

/**
 * @author zubayer
 *
 */
@SpringBootTest
@Rollback(false)
@ComponentScan(basePackages = { "com.asl.*" })
public class ReadSourceDataTest {

	@Autowired private DataTransportService service;

	@Test
	@Transactional
	public void readSourceData() {
		String latestRecordValue = "2020-12-12 18:30:00";
		List<Map<String, Object>> result = service.readDataFromSourceTable(latestRecordValue);
		System.out.println(result.size());
	}
}
