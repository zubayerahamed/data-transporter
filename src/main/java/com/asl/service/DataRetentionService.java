package com.asl.service;

import org.springframework.stereotype.Component;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 17, 2020
 */
@Component
public interface DataRetentionService {

	/**
	 * Delete all previous data from tracking history based on date
	 * @return int , Number of row deleted
	 */
	public int doDataRetention();
}
