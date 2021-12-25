package com.asl.controller;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asl.model.ResponseHelper;
import com.asl.model.Trip;
import com.asl.service.TripService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 23, 2021
 */
@Slf4j
@RestController
@RequestMapping("/")
@Api(value = "Trip Rest Endpoint", description = "Trip Operations")
public class TripController {

	@Autowired private TripService tripService;
	@Autowired private ResponseHelper responseHelper;

	@GetMapping
	public String checkApplicationStatus() {
		return "Application is running ok.";
	}

	@ApiOperation(value = "Add Or Update Trip Info")
	@PostMapping("/api/v1/rest/trip")
	public Map<String, Object> save(@RequestBody Trip trip) {

		log.info("Current Trip : {}", trip.toString());

		Trip exist = tripService.findByTripNo(trip.getTripNo());
		if(exist != null) {
			BeanUtils.copyProperties(trip, exist, "lightWeithInKg", "lightWeightTime", "lightWeightBy");

			exist = tripService.save(exist);

			if(exist == null) {
				responseHelper.setErrorStatusAndMessage("Can't update trip");
				responseHelper.addDataToResponse("content", null);
				return responseHelper.getResponse();
			}

			responseHelper.setSuccessStatusAndMessage("Trip updated successfully");
			responseHelper.addDataToResponse("content", exist);
			return responseHelper.getResponse();
		}

		trip = tripService.save(trip);
		if(trip == null) {
			responseHelper.setErrorStatusAndMessage("Can't save trip");
			responseHelper.addDataToResponse("content", null);
			return responseHelper.getResponse();
		}

		responseHelper.setSuccessStatusAndMessage("Trip saved successfully");
		responseHelper.addDataToResponse("content", trip);
		return responseHelper.getResponse();

	}

	@ApiOperation(value = "Find Trip Info")
	@GetMapping("/api/v1/rest/trip/{id}")
	public Map<String, Object> sample(@PathVariable String id) {
		Trip trip = tripService.findByTripNo(id);

		if(trip == null) {
			responseHelper.setErrorStatusAndMessage("Data not found");
			responseHelper.addDataToResponse("content", null);
			return responseHelper.getResponse();
		}

		responseHelper.setSuccessStatusAndMessage("Data found");
		responseHelper.addDataToResponse("content", trip);
		return responseHelper.getResponse();
	}

}
