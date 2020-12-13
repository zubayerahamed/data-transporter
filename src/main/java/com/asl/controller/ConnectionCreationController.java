package com.asl.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 13, 2020
 */
@Controller
@RequestMapping("/connection")
public class ConnectionCreationController {

	@GetMapping
	public String loadConnnectionCreationPage(Model model) {
		return "connection";
	}
}
