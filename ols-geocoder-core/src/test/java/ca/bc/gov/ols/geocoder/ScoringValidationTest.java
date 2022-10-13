/**
 * Copyright © 2008-2019, Province of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.bc.gov.ols.geocoder;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.api.data.MatchFault.MatchElement;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.test.TestCase;

/**
 * Validates the scoring parameters against a set of rules built up over time as examples of less
 * than ideal scoring results are identified and dealt with. Uses the default values set in
 * GeocoderConfig to avoid connecting to the database. Note that an actual running instance will
 * override the default values with the values from the database; after tweaking the default values,
 * you will want to update the values in the database.
 * 
 * @author chodgson
 * 
 */
public class ScoringValidationTest extends TestCase {
	final Logger logger = LoggerFactory.getLogger(ScoringValidationTest.class);

	@Test
	@Tag("Dev")
	public void testScoringConsistency() {
		GeocoderConfig config = new GeocoderConfig();
		
		// Example: "1 Clipper Ave Nanaimo" should match to STREET: Clipper Dr before LOCALITY:
		// Nanaimo
		// Rule: matchPrecision.STREET - STREET_TYPE.notMatched - CIVIC_NUMBER.notInAnyBlock >
		// matchPrecisison.Locality - STREET_NAME.notMatched
		assertGreater(config.getMatchPrecisionPoints(MatchPrecision.STREET)
				- config.getMatchFault("", MatchElement.STREET_NAME, "spelledWrong").getPenalty()
				- config.getMatchFault("",  MatchElement.CIVIC_NUMBER, "notInAnyBlock").getPenalty(),
				config.getMatchPrecisionPoints(MatchPrecision.LOCALITY)
						- config.getMatchFault("", MatchElement.STREET_NAME, "notMatched").getPenalty());
		
		// TODO: Put more rules here as they are identified
	}

	@Test
	@Tag("Dev")
	public void testStuff() throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection("jdbc:postgresql://192.168.50.7:5432/bgeo",
				"bgeo", "bgeo");
		Statement stmt = conn.createStatement();
		String sql = "SELECT input_string, canonical_form FROM bgeo.bgeo_affix_mappings";
		ResultSet rs = stmt.executeQuery(sql);
		Map<String, Integer> abbrs = new THashMap<String, Integer>();
		while(rs.next()) {
			// String inputString = rs.getString("input_string").toUpperCase();
			// if(abbrs.get(inputString) == null) {
			// abbrs.put(inputString, 1);
			// } else {
			// abbrs.put(inputString, abbrs.get(inputString) + 1);
			// }
			String canonicalForm = rs.getString("canonical_form").toUpperCase();
			if(abbrs.get(canonicalForm) == null) {
				abbrs.put(canonicalForm, 1);
			} else {
				abbrs.put(canonicalForm, abbrs.get(canonicalForm) + 1);
			}
		}
		stmt.close();
		
		stmt = conn.createStatement();
		sql = "SELECT name_body FROM bgeo.bgeo_street_names";
		rs = stmt.executeQuery(sql);
		Set<String> matchedAbbrs = new THashSet<String>();
		while(rs.next()) {
			String nameBody = rs.getString("name_body");
			String[] words = nameBody.split("\\s+");
			for(String word : words) {
				Integer count;
				if((count = abbrs.get(word.toUpperCase())) != null) {
					System.out.println(nameBody + ": " + word);
					if(count > 1) {
						matchedAbbrs.add(word);
					}
				}
			}
		}
		stmt.close();
		
		System.out.println(matchedAbbrs.toString());
		
	}

	@Test
	@Tag("Dev")
	public void testMapPerformance() {
		final int SIZE = 1000000;
		System.out.println("map size:" + SIZE);
		long startTime = System.nanoTime();
		Map<Integer, String> map = new THashMap<Integer, String>(
				(int)Math.round(SIZE / 0.75) + 1);
		// HashMap<Integer, String> map = new HashMap<Integer, String>();
		// TreeMap<Integer, String> map = new TreeMap<Integer, String>();
		for(int i = 0; i < SIZE; i++) {
			map.put(i, "foo");
		}
		long creationTime = System.nanoTime() - startTime;
		startTime = System.nanoTime();
		for(int i = 0; i < SIZE; i++) {
			map.get(i);
		}
		long searchTime = System.nanoTime() - startTime;
		System.out.println("Map creation: " + (creationTime / 1000000)
				+ " search: " + (searchTime / 1000000));
		System.out
				.println("Memory in use after loading(Megs): "
						+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000));
		
	}
}
