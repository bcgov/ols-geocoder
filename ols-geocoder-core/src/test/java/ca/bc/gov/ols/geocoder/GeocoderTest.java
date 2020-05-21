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

import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.IntersectionMatch;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.test.TestCase;

public class GeocoderTest extends TestCase {
	final Logger logger = LoggerFactory.getLogger(GeocoderTest.class);
	
	@Override
	public void setUp() throws SQLException, ClassNotFoundException {
		if(gc == null) {
			gc = new GeocoderFactory().getGeocoder();
		}
	}
	
	public void testGeocodeIntersection() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"Douglas St and View St, Victoria, BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeIntersection(): " + results.getMatches().get(0) +
				" Faults: " + results.getBestMatch().getFaults());
		StreetIntersectionAddress bestAddress = ((IntersectionMatch)(results.getBestMatch()))
				.getAddress();
		assertEquals("Victoria", bestAddress.getLocalityName());
		assertEquals(0, results.getBestMatch().getFaults().size());
	}
	
	public void testGeocodeIntersectionSameNameBody() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"Atkins Rd and Atkins Ave, Langford, BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeIntersectionSameNameBody(): " + results.getMatches().get(0) +
				" Faults: " + results.getBestMatch().getFaults());
		StreetIntersectionAddress bestAddress = ((IntersectionMatch)(results.getBestMatch()))
				.getAddress();
		assertEquals("Langford", bestAddress.getLocalityName());
		assertEquals(0, results.getBestMatch().getFaults().size());
	}
	
	public void testBug28() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"310 4th ave ladysmith bc"));
		assertGreater(0, results.getMatches().size());
		logger.info("testBug28(): " + results.getBestMatch());
	}
	
	public void testBug31() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"13a 1207 Douglas st victoria, bc"));
		assertGreater(0, results.getMatches().size());
		logger.info("testBug31(): " + results.getBestMatch());
	}
	
	public void testBug43() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				" 1207 Douglas st victoria, bc"));
		assertGreater(0, results.getMatches().size());
		logger.info("testBug43(): " + results.getBestMatch());
	}
	
	public void testSlowAddress() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"100 1st st vancouver, bc"));
		assertGreater(0, results.getMatches().size());
		logger.info("testSlowAddress(): " + results.getBestMatch());
	}
	
	public void testParityIssue() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"210 WEDGE PL Nanaimo"));
		assertGreater(0, results.getMatches().size());
		assertGreater(80, results.getBestMatch().getScore());
		logger.info("testParityIssue(): " + results.getBestMatch());
	}
	
	public void testGeocodeNanaimoSite() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"1009 Beverly Dr Nanaimo BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeNanaimoSite(): " + results.getBestMatch());
		assertEquals(0, results.getBestMatch().getFaults().size());
		assertEquals(100, results.getBestMatch().getScore());
		assertEquals(MatchPrecision.CIVIC_NUMBER, results.getBestMatch().getPrecision());
	}
	
	public void testGeocodeLangleySite() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"6285 226 St, Langley, BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeLangleySite(): " + results.getBestMatch());
		assertEquals(0, results.getBestMatch().getFaults().size());
		assertEquals(100, results.getBestMatch().getScore());
		assertEquals(MatchPrecision.CIVIC_NUMBER, results.getBestMatch().getPrecision());
	}
	
	public void testGeocodeVancouverSite() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"7051 Main St, Vancouver, BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeVancouverSite(): " + results.getBestMatch());
		assertEquals(0, results.getBestMatch().getFaults().size());
		assertEquals(100, results.getBestMatch().getScore());
		assertEquals(MatchPrecision.CIVIC_NUMBER, results.getBestMatch().getPrecision());
	}
	
	public void testGeocodeBCASite() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"6570 Lyon Rd, Delta, BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeBCASite(): " + results.getBestMatch());
		assertEquals(0, results.getBestMatch().getFaults().size());
		assertEquals(100, results.getBestMatch().getScore());
		assertEquals(MatchPrecision.CIVIC_NUMBER, results.getBestMatch().getPrecision());
	}
	
	public void testStructuredGeocodeCivicAddress() {
		GeocodeQuery query = new GeocodeQuery();
		query.setLocalityName("Victoria");
		query.setCivicNumber("1207");
		query.setStreetName("Douglas");
		query.setStreetType("St");
		SearchResults results = gc.geocode(query);
		assertGreater(0, results.getMatches().size());
		logger.info("testStructuredGeocodeCivicAddress(): " + results.getBestMatch());
		SiteAddress bestAddress = ((AddressMatch)results.getBestMatch()).getAddress();
		assertEquals("Victoria", bestAddress.getLocalityName());
	}
	
	public void testGeocodeWithTypeMapping() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"58 King George Terrace Oak Bay BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeWithTypeMapping(): " + results.getBestMatch());
		logger.debug("testGeocodeWithTypeMapping(): "
				+ results.getBestMatch().getFaults().toString());
		assertEquals(0, results.getBestMatch().getFaults().size());
	}
	
	public void testGeocodeWithNameDirMapping() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"1530 N Dairy Road Saanich BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeWithNameDirMapping(): " + results.getBestMatch());
		logger.debug("testGeocodeWithNameDirMapping(): "
				+ results.getBestMatch().getFaults().toString());
		assertEquals(0, results.getBestMatch().getFaults().size());
	}
	
	public void testGeocodeWithDirMapping() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"340 Gorge Rd West Saanich BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeWithDirMapping(): " + results.getBestMatch());
		logger.debug("testGeocodeWithDirMapping(): "
				+ results.getBestMatch().getFaults().toString());
		assertEquals(0, results.getBestMatch().getFaults().size());
	}
	
	public void testGeocodeWithDirBeforeName() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"340 W Gorge Rd Saanich BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeWithDirBeforeName(): " + results.getBestMatch());
		logger.debug("testGeocodeWithDirBeforeName(): "
				+ results.getBestMatch().getFaults().toString());
		assertEquals(0, results.getBestMatch().getFaults().size());
	}
	
	public void testGeocodeWithNameMapping() {
		GeocodeQuery query = new GeocodeQuery();
		query.setLocalityName("Victoria");
		query.setCivicNumber("1813");
		query.setStreetName("Cres");
		query.setStreetType("Road");
		query.setStateProvTerr("BC");
		SearchResults results = gc.geocode(query);
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeWithNameMapping(): " + results.getBestMatch());
		logger.debug("testGeocodeWithNameMapping(): "
				+ results.getBestMatch().getFaults().toString());
		assertEquals(0, results.getBestMatch().getFaults().size());
	}
	
	public void testGeocodeCivicAddress() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"1207 Douglas St, Victoria, BC"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeCivicAddress(): " + results.getBestMatch());
		SiteAddress bestAddress = ((AddressMatch)results.getBestMatch()).getAddress();
		assertEquals("Victoria", bestAddress.getLocalityName());
	}
	
	public void testGeocodeNonExistentCivicNumber() {
		SearchResults results = gc.geocode(new GeocodeQuery(
				"12000 Douglas St, Victoria"));
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeNonExistentCivicNumber(): " + results.getBestMatch());
		assertEquals(MatchPrecision.STREET, results.getBestMatch().getPrecision());
	}
	
	public void testGeocodeLocality() {
		GeocodeQuery query = new GeocodeQuery();
		query.setLocalityName("Victoria");
		SearchResults results = gc.geocode(query);
		assertGreater(0, results.getMatches().size());
		logger.info("testGeocodeLocality(): " + results.getBestMatch());
		SiteAddress bestAddress = ((AddressMatch)results.getBestMatch()).getAddress();
		assertEquals("Victoria", bestAddress.getLocalityName());
		assertEquals(gc.getDatastore().getConfig().getMatchPrecisionPoints(
				MatchPrecision.LOCALITY),
				results.getBestMatch().getScore());
	}
	
}
