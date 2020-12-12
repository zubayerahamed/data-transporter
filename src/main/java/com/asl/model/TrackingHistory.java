package com.asl.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 12, 2020
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class TrackingHistory {

	protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Long id;
	private String value;
	private Date date;

	public TrackingHistory(String value, Date date){
		this.value = value;
		this.date = date;
	}

	public TrackingHistory(String[] data) {
		this.value = data[0] != null ? data[0].replace("\"", "") : null;
		try {
			this.date = data[1] != null ? SDF.parse(data[1].replace("\"", "")) : null;
		} catch (ParseException e) {
			log.error("Error is : {}, {}", e.getMessage(), e);
		}
	}
}
