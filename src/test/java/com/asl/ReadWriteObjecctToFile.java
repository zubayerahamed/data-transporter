package com.asl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.opencsv.CSVWriter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 12, 2020
 */
@Slf4j
public class ReadWriteObjecctToFile {

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
	private static final String FILE_NAME = "data-transporter";
	private static final String FILE_LOCATION = "D:/ASL/";

	@Test
	public void writeObjectToFile() {

		String fileName = FILE_NAME + "-" + SDF.format(new Date()) + ".csv";
		String fileNameWithDirectory = FILE_LOCATION + fileName;

		System.out.println(fileName);
		List<PrimaryTable> result = new ArrayList<PrimaryTable>();
		result.add(new PrimaryTable("Dhaka", new Date()));
		result.add(new PrimaryTable("Borishal", new Date()));

		List<String[]> fileLineData = new ArrayList<String[]>();
		createFileLineDataFromResult(fileLineData, result);

		StringWriter writer = new StringWriter();
		CSVWriter csvWriter = new CSVWriter(writer, ',', '"', '/', "\n");
		csvWriter.writeAll(fileLineData);
		try {
			csvWriter.close();
		} catch (IOException e) {
			log.error("Error on writer close: {}", e);
		}

		try (FileWriter fw = new FileWriter(fileNameWithDirectory, true); BufferedWriter bw = new BufferedWriter(fw)) {
			bw.append(writer.getBuffer().toString());
		} catch (IOException e) {
			log.error("ERROR is : {}, {}", e.getMessage(), e);
		}

	}

	@Test
	public void readDataFromFile() {
		String fileName = FILE_NAME + "-" + SDF.format(new Date()) + ".csv";
		String fileNameWithDirectory = FILE_LOCATION + fileName;

		try (ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(fileNameWithDirectory));) {
			String line = reader.readLine();
			String[] data = new String[2];
			if(StringUtils.isNotBlank(line)) data = line.split(",");
			PrimaryTable pt = new PrimaryTable(data);
			System.out.println(pt.toString());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}

	private void createFileLineDataFromResult(List<String[]> fileLineData, List<PrimaryTable> result) {
		result.stream().forEach(r -> {
			String[] dl = new String[2];

			dl[0] = r.getValue();
			dl[1] = SDF.format(r.getDate());

			fileLineData.add(dl);
		});

	}
}

@Slf4j
@Data
class PrimaryTable {
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
	private Long id;
	private String value;
	private Date date;

	PrimaryTable(String value, Date date) {
		this.value = value;
		this.date = date;
	}

	PrimaryTable(String[] data){
		this.value = data[0].replace("\"", "");
		try {
			this.date = SDF.parse(data[1].replace("\"", ""));
		} catch (ParseException e) {
			log.error(e.getMessage());
		} 
	}
}
