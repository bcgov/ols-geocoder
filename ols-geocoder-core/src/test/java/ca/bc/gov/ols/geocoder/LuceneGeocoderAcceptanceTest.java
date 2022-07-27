package ca.bc.gov.ols.geocoder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.bc.gov.ols.junitFlags.DevTest;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.lucene.LuceneGeocoder;
import ca.bc.gov.ols.rowreader.XsvRowWriter;

@Category(DevTest.class)
@TestInstance(Lifecycle.PER_CLASS)
public class LuceneGeocoderAcceptanceTest {

	private LuceneGeocoder lgc;
	private XsvRowWriter logWriter;
	private List<String> logSchema;
	
	@BeforeAll
	void setup() {
		GeocoderFactory gcf = new GeocoderFactory();
		gcf.setCassandraContactPoint("hummingbird");
		lgc = new LuceneGeocoder(gcf.getGeocoder().getDatastore());
				
		logSchema = Arrays.asList(new String[] {
				"yourId", "testResult", "testMessage", "expectedMatchPrecision", "resultMatchPrecision", "addressString", "expectedFullAddress", "resultAddress", 
				"expectedFaults", "resultFaults", "status", "parcelPoint", "issue", "resultPoint"});
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss") ;
		File logFile = new File("log/" + dateFormat.format(date) + "_test_log.csv") ;
		logWriter = new XsvRowWriter(logFile, ',', logSchema, true);
	}
	
	@AfterAll
	void tearDown() {
		logWriter.close();
	}
	
	@ParameterizedTest
	@CsvFileSource(resources = "/atp_addresses.csv", numLinesToSkip = 1)
	void accept(String yourId, String addressString, String expectedMatchPrecisionStr, String expectedFullAddress, String expectedFaults, String status, String parcelPoint, String issue) {
		// create log record with test inputs
		Map<String, String> log = new HashMap<String, String>(logSchema.size(), 0.5f);
		log.put("yourId", yourId);
		log.put("addressString", addressString);
		log.put("expectedMatchPrecision", expectedMatchPrecisionStr);
		log.put("expectedFullAddress", expectedFullAddress);
		log.put("expectedFaults", expectedFaults);
		log.put("status", status);
		log.put("parcelPoint", parcelPoint);
		log.put("issue", issue);
		StringBuilder testMessage = new StringBuilder("");
		
		// check for invalid test params (skip test)
		boolean skip = false;
		
		if(addressString == null || addressString.isBlank()) {
			testMessage.append("blank addressString;");
			skip = true;
		}
		if(expectedMatchPrecisionStr == null || expectedMatchPrecisionStr.isBlank()) {
			testMessage.append("blank expectedMatchPrecision;");
			skip = true;
		}
		if(expectedFullAddress == null || expectedFullAddress.isBlank()) {
			testMessage.append("blank expectedFullAddress;");
			skip = true;
		}
//		if(!"R".equals(status)) {
//			testMessage.append("not a regression test;");
//			skip = true;
//		}
		MatchPrecision expectedMP = MatchPrecision.NONE;
		try {
			expectedMP = MatchPrecision.convert(expectedMatchPrecisionStr);
		} catch(IllegalArgumentException iae) {
			testMessage.append(iae.getMessage() + ";");
			skip = true;
		}
		if(!expectedMP.equals(MatchPrecision.CIVIC_NUMBER)) {
			testMessage.append("not a CIVIC_NUMBER test;");
			skip = true;
		}
		if(skip) {
			testMessage.append("test skipped;");
			String msg = testMessage.toString();
			log.put("testMessage", msg);
			log.put("testResult", "SKIP");
			logWriter.writeRow(log);
			assumeTrue(false, msg);
		}
		
		// execute test and compare results
		boolean fail = false;
		List<String> results = lgc.query(addressString);
		String result = results.get(0);
		log.put("resultAddress", result);
//		log.put("resultMatchPrecision", bestMatch.getPrecision().toString());
//		log.put("resultFaults", bestMatch.getFaults().toString());
//		log.put("resultPoint", bestMatch.getLocation().toText());
		
//		if(!expectedMP.equals(bestMatch.getPrecision())) {
//			testMessage.append("MatchPrecision did not match expected;");
//			fail = true;
//		}
		if(!expectedFullAddress.equals(result)) {
			testMessage.append("Full address string did not match expected;");
			fail = true;
		}
//		if(!compareFaults(expectedFaults, bestMatch.getFaults(), testMessage)) {
//			fail = true;
//		}
		
		String msg = testMessage.toString();
		log.put("testMessage", msg);
		if(fail) {
			log.put("testResult", "FAIL");
			logWriter.writeRow(log);
			String matchString = " Best Match: " + result; // + " - " + bestMatch.getPrecision() + " - " + bestMatch.getFaults().toString();
			assertTrue(false, msg + matchString);			
		}
		log.put("testResult", "SUCCESS");
		logWriter.writeRow(log);
	}

}
