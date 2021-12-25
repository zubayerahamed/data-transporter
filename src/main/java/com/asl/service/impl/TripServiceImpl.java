/**
 * 
 */
package com.asl.service.impl;

import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asl.model.Trip;
import com.asl.repository.TripRepository;
import com.asl.service.TripService;

/**
 * @author zubayer
 *
 */
@Service
public class TripServiceImpl implements TripService {

	@Autowired
	private TripRepository tripRepository;

	@Transactional
	@Override
	public Trip save(Trip trip) {
		if (trip == null) {
			return null;
		}
		return tripRepository.save(trip);
	}

	@Override
	public Trip findByTripNo(String tripNo) {
		if (StringUtils.isBlank(tripNo))
			return null;
		Optional<Trip> result = tripRepository.findById(tripNo);
		return result.isPresent() ? result.get() : null;
	}

}
