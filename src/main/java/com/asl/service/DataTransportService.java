package com.asl.service;

import org.springframework.stereotype.Component;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 9, 2020
 */
@Component
public interface DataTransportService {

	/**
	 * Data transport start point
	 */
	public void doDataTransport();

	public int insertDummyDataToSourceTable();
}
