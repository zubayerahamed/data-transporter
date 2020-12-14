/**
 * 
 */
package com.asl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Rollback;

import com.asl.service.DataTransportService;

/**
 * @author zubayer
 *
 */
@SpringBootTest
@Rollback(false)
@ComponentScan(basePackages = { "com.asl.*" })
public class ReadWriteDataTest {

	@Autowired private DataTransportService service;

	@Test
	@Transactional
	public void readSourceData() {
		// search latest record
		SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String latestRecordValue = service.getLatestRecordFromPrimaryDB() != null ? SDF.format(service.getLatestRecordFromPrimaryDB()) : null;
		System.out.println("===> Latest record value : " + latestRecordValue);

		// search source data
		List<Map<String, Object>> sourceData = service.readDataFromSourceTable(latestRecordValue);
		if(sourceData == null || sourceData.isEmpty()) System.out.println("====> No source data found");

		// search updatable
		List<String> transactionIds = new ArrayList<>();
		sourceData.stream().forEach(r -> {
			String trids = ((Integer) r.get("TRANSACTION_ID")).toString();
			transactionIds.add(trids);
			System.out.println("TRIDS : " + trids);
		});
		List<Map<String, Object>> updatableData = service.searcAndGethUpdatableData(transactionIds);
		List<String> updatableIds = new ArrayList<>();
		updatableData.stream().forEach(r -> {
			String trids = (String) r.get("TRANSACTION_ID");
			updatableIds.add(trids);
			System.out.println("Updatable TRIDS : " + trids);
		});

		// Seperate updatable & insertable data
		List<Map<String,Object>> newdata = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> olddata = new ArrayList<Map<String,Object>>();
		sourceData.stream().forEach(s -> {
			if(updatableIds.contains(((Integer) s.get("TRANSACTION_ID")).toString())) {
				olddata.add(s);
			} else {
				newdata.add(s);
			}
		});

		System.out.println("new data : " + newdata.size());
		System.out.println("old data : " + olddata.size());

		// Insert new source data to primary db
		int pdbNewInserted = service.insertSourceDataToPrimaryDB(newdata);
		System.out.println("Total new data inserted to PDB : " + pdbNewInserted);

		// Insert new source data to source db
		int sodNewInserted = service.insertSourceDataToDestination(newdata);
		System.out.println("Total new data inserted to SOD : " + sodNewInserted);

		// update old source data to primary db
		int pdbOldUpdated = service.updateSourceDataToPrimaryDB(olddata);
		System.out.println("Total old data updated to PDB : " + pdbOldUpdated);

		// update old source data to source db
		int sodOldUpdated = service.updateSourceDataToDestination(olddata);
		System.out.println("Total old data updated to SOD : " + sodOldUpdated);

	}
}
