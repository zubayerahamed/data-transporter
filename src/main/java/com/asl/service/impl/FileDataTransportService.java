package com.asl.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.asl.model.TrackingHistory;
import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 9, 2020
 */
@Slf4j
@Service
@Profile("file")
public class FileDataTransportService extends AbstractDataTransportService {

	private static final String LAST_REC_KEY = "LASTRECORD";

	@Value("${data.transport.record.status.filename}")
	private String fileName;
	@Value("${daat.transport.record.status.filelocation}")
	private String fileLocation;

	// FILE CONFIG
	@Value("${file.read.condition.column}")
	private String fetchConditionColumn;
	@Value("#{${file.write.columns}}")
	private Map<String,String> fileWriteColumns;

	/**
	 * Data transport start point
	 */
	@Override
	public void doDataTransport() {
		log.info("===> File profile is active");

		// GET LAST RECORD DATE FROM PPRIMARY DB
		String latestRecordValue = getLatestRecordValue();

		// READ DATA FROM SOURCE DB
		List<Map<String, Object>> sourceData = readDataFromSourceTable(latestRecordValue);
		if(sourceData.isEmpty()) {
			log.info("===> No new source data found to do transport");
			return;
		}

		// INSERT SOURCE DATA TO DESTINATION DB
		int rows = insertSourceDataToDestination(sourceData);
		if(rows < 1) {
			log.info("===> No record is inserted to Destination DB");
			return;
		}

		// INSERT LATEST SOURCE DATA TO PRIMARY TABLE FOR RECORD TRACKING
		boolean statRow = insertLatestSourceDataToFileAndSession(sourceData);
		if(!statRow) log.info("===> Latest record is not inserted into session/file");
	}

	/**
	 * GET LATEST RECORD VALUE
	 * @return {@link String}
	 */
	private String getLatestRecordValue() {
		TrackingHistory th = null;
		Object obj = sessionManager.getFromMap(LAST_REC_KEY);
		if(obj != null) {
			th = (TrackingHistory) obj;
		} else {
			// Check from file now 
			String fileNameWithDirectory = getFileNameWithDirectory(fileName, fileLocation);
			log.info("File name with directory: {}", fileNameWithDirectory);
			try (ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(fileNameWithDirectory));) {
				String line = reader.readLine();
				String[] data = new String[2];
				if(StringUtils.isNotBlank(line)) data = line.split(",");
				th = new TrackingHistory(data);
				System.out.println(th.toString());
			} catch (IOException e) {
				log.error("ERROR IS : {}, {}", e.getMessage(), e);
			}
		}

		if(th == null) return null;

		if("date".equalsIgnoreCase(fetchConditionColumn)) {
			return th.getDate() != null ? SDF.format((Date) th.getDate()) : null;
		} else {
			return th.getValue() != null ? (String) th.getValue() : null ;
		}
	}

	/**
	 * INSERT LATEST SOURCE DATA TO PRIMARY TABLE FOR RECORD TRACKING
	 * @param sourceData
	 * @return {@link boolean}
	 */
	@Transactional
	private boolean insertLatestSourceDataToFileAndSession(List<Map<String, Object>> sourceData) {
		if(sourceData == null || sourceData.isEmpty()) return false;

		Map<String, Object> map = sourceData.get(sourceData.size() - 1);
		String writableValue = null;
		String dataType = null;
		for(Map.Entry<String, String> m : fileWriteColumns.entrySet()) {
			dataType = m.getValue();
			writableValue = getModifiedValue(map.get(m.getKey()), m.getValue());
		}

		TrackingHistory th = new TrackingHistory();
		try {
			if("date".equalsIgnoreCase(dataType)) {
				th.setDate(SDF.parse(writableValue));
				th.setValue("NOVALUE");
			} else {
				th.setValue(writableValue);
				th.setDate(new Date());
			}
		} catch (Exception e) {
			log.error("ERROR is {}, {}", e.getMessage(), e); 
			return false;
		}

		// Set data to session manager
		if(sessionManager.getFromMap(LAST_REC_KEY) != null) {
			sessionManager.removeFromMap(LAST_REC_KEY);
			sessionManager.addToMap(LAST_REC_KEY, th);
		} else {
			sessionManager.addToMap(LAST_REC_KEY, th);
		}

		// Write data to file
		List<TrackingHistory> result = new ArrayList<TrackingHistory>();
		result.add(th);
		List<String[]> fileLineData = new ArrayList<String[]>();
		createFileLineDataFromResult(fileLineData, result);

		String fileNameWithDirectory = getFileNameWithDirectory(fileName, fileLocation);
		try (StringWriter writer = new StringWriter(); 
				CSVWriter csvWriter = new CSVWriter(writer, ',', '"', '/', "\n");
				FileWriter fw = new FileWriter(fileNameWithDirectory, true); 
				BufferedWriter bw = new BufferedWriter(fw)) {
			csvWriter.writeAll(fileLineData);
			csvWriter.close();
			bw.append(writer.getBuffer().toString());
		} catch (IOException e) {
			log.error("Error is: {}, {}", e.getMessage(), e);
			return false;
		}

		return true;
	}

	private void createFileLineDataFromResult(List<String[]> fileLineData, List<TrackingHistory> result) {
		result.stream().forEach(r -> {
			String[] dl = new String[2];
			dl[0] = r.getValue();
			dl[1] = SDF.format(r.getDate());
			fileLineData.add(dl);
		});
	}

}
