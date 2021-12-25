/**
 * 
 */
package com.asl.service;

import com.asl.model.Trip;

/**
 * @author zubayer
 *
 */
public interface TripService {

	public Trip save(Trip trip);

	public Trip findByTripNo(String tripNo);
}
