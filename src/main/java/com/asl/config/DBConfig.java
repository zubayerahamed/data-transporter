package com.asl.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 8, 2020
 */
@Configuration
public class DBConfig {

	@Autowired
	private Environment env;

	@Bean
	public DataSource fromDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getProperty("spring.datasource.from.driver-class-name"));
		dataSource.setUrl(env.getProperty("spring.datasource.from.url"));
		dataSource.setUsername(env.getProperty("spring.datasource.from.username"));
		dataSource.setPassword(env.getProperty("spring.datasource.from.password"));
		return dataSource;
	}

	@Bean
	public DataSource toDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getProperty("spring.datasource.to.driver-class-name"));
		dataSource.setUrl(env.getProperty("spring.datasource.to.url"));
		dataSource.setUsername(env.getProperty("spring.datasource.to.username"));
		dataSource.setPassword(env.getProperty("spring.datasource.to.password"));
		return dataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplateFrom(@Qualifier("fromDataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}

	@Bean
	public JdbcTemplate jdbcTemplateTo(@Qualifier("toDataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}
}
