package com.asl.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asl.model.Item;

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

	@Autowired @Qualifier("jdbcTemplateFrom") protected JdbcTemplate jdbcTemplateFrom;
	@Autowired @Qualifier("jdbcTemplateTo") protected JdbcTemplate jdbcTemplateTo;

	@GetMapping
	public String sayHello() {
		log.info("===> Data transport service running from controller");
		
		List<Map<String, Object>> result = jdbcTemplateFrom.queryForList("select * from item where id>02301229");
		for(Map<String, Object> map : result) {
			
			List<String> columns = new LinkedList<>();
			List<Object> values = new LinkedList<>();

			for(Map.Entry<String, Object> m : map.entrySet()) {
				columns.add(m.getKey());
				values.add(m.getValue());
			}

			
			
			String sql = "insert into item ("+ getColumnsString(columns) +") values ("+ getValuesString(columns, map) +")";
			System.out.println(sql);
		
			jdbcTemplateTo.update(sql);
		}
		
		
		return "Application is running OK.";
	}

	protected String getValuesString(List<String> columnsList, Map<String, Object> map) {
		StringBuilder columns = new StringBuilder();
		
		columnsList.stream().forEach(col -> {
			Object c = map.get(col);
			if("name".equalsIgnoreCase(col)) {
				String val = (String) c;
				val = val.replace("'", "''");
				columns.append("'" + val + "',");
			} else if(c instanceof String) {
				columns.append("'" + c + "',");
			} else if(c instanceof Date) {
				columns.append("'" + c + "',");
			} else if(c instanceof BigDecimal) {
				columns.append(c + ",");
			} else {
				columns.append(c + ",");
			}
		});

		int lastComma = columns.toString().lastIndexOf(',');
		return columns.toString().substring(0, lastComma);
	}

	protected String getColumnsString(List<String> columnsList) {
		StringBuilder columns = new StringBuilder();
		columnsList.stream().forEach(c -> {
			columns.append(c + ",");
		});
		int lastComma = columns.toString().lastIndexOf(',');
		return columns.toString().substring(0, lastComma);
	}
}
