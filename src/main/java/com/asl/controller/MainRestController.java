package com.asl.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asl.model.Trip;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 23, 2021
 */
@RestController
@RequestMapping("/")
public class MainRestController {

	@GetMapping
	public String checkApplicationStatus() {
		return "Application is running ok.";
	}

	@PostMapping("/trip")
	public Trip save(Trip trip) {
		
		System.out.println(trip.toString());
		
		return trip;
	}

	@GetMapping("/trip/sample")
	public Trip sample() {
		return new Trip();
	}
}
