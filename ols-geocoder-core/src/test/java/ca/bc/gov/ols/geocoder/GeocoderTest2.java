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

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.test.TestCase;

public class GeocoderTest2 extends TestCase {
	final Logger logger = LoggerFactory.getLogger(GeocoderTest2.class);
	
	@Override
	public void setUp() throws SQLException, ClassNotFoundException {
		if(gc == null) {
			gc = getTestGeocoder();
		}
	}
	
	public void testGeocodeCivicAddress() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"1 Douglas St, Victoria, British Columbia"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeCivicAddress(): " + results.getBestMatch());
		assertEquals(0, results.getBestMatch().getFaults().size());
		assertEquals(99, results.getBestMatch().getScore());
		assertEquals(MatchPrecision.BLOCK, results.getBestMatch().getPrecision());
	}
	
	public void testGeocodeNCAPWithStreet() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"Museum -- Douglas St, Victoria, British Columbia"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeNCAPWithStreet(): " + results.getBestMatch());
		assertEquals(0, results.getBestMatch().getFaults().size());
		assertEquals(100, results.getBestMatch().getScore());
		assertEquals(MatchPrecision.SITE, results.getBestMatch().getPrecision());
	}
	
	public void testGeocodeNCAPWithStreetByName() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"Museum -- "));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeNCAPWithStreetByName(): " + results.getBestMatch());
		assertEquals(MatchPrecision.SITE, results.getBestMatch().getPrecision());
	}
	
}
