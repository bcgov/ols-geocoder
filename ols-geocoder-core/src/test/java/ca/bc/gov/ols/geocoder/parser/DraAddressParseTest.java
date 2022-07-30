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
package ca.bc.gov.ols.geocoder.parser;

import java.sql.SQLException;

import ca.bc.gov.ols.geocoder.Geocoder;
import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.test.TestCase;
import ca.bc.gov.ols.util.StopWatch;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DraAddressParseTest extends TestCase {
	
	private boolean trace = false;
	AddressParser parser;
	
	@Override
	public void setUp() throws SQLException, ClassNotFoundException {
		if(gc == null) {
			gc = new GeocoderFactory().getGeocoder();
		}
		parser = ((Geocoder)gc).getParser();
	}

	@Test
	@Tag("Dev")
	public void testOneAddress() {
		run("420 GORGE RD E, VICTORIA, BC");
	}

	@Test
	@Tag("Dev")
	public void testSomeAddress() {
		run("5559 CLIPPER DR NANAIMO BC");
	}

	@Test
	@Tag("Dev")
	public void testUnitAddress() {
		run("bsmt 5559 CLIPPER DR NANAIMO BC");
	}

	@Test
	@Tag("Dev")
	public void testUvicAddress() {
		run("ROOM 103A, CLEARIHUE BUILDING, UNIVERSITY OF VICTORIA, 3800 FINNERTY RD, VICTORIA, BC");
	}

	@Test
	@Tag("Dev")
	public void testStarportAddress() {
		run("PAD 11, TERMINAL 3, BC SPACEPORT, 1 MILKY WAY, STAR CITY, BC");
	}

	@Test
	@Tag("Dev")
	public void testAddress() {
		
		run("FOO ST AND BAR ST VICTORIA BC");
		run("FOO ST AND FU ST AND BAR ST VICTORIA BC");
		run("1025 HAPPY VALLEY RD, METCHOSIN, BC");
		run("130A HILL ST, NELSON, BC");
		run("UNIT 1, 433 CEDAR RAPIDS BLVD, PEMBERTON, BC");
		run("PAD 2, 1200 NORTH PARK RD, SHAWNIGAN LAKE, BC");
		run("PORT ALICE HEALTH CENTRE, 1090 MARINE DRIVE, PORT ALICE, BC");
		run("ROYAL ATHLETIC PARK, 1014 CALEDONIA AVE, VICTORIA, BC");
		run("PAD 2, HAPPY MOBILE HOME PARK, 1200 NORTH PARK RD, SHAWNIGAN LAKE, BC");
		run("ROOM 230, WEST BLOCK, ROYAL JUBILEE HOSPITAL, 1952 BAY ST, VICTORIA, BC");
		run("Cabin C Heron, Happy Fishing Resort, Whopper, BC");
		run("WILLOW DRIVE, 70 MILE HOUSE, BC");
		run("HORSE LAKE ROAD, 100 MILE HOUSE, BC");
		run("PEACE RIVER REGIONAL DISTRICT, BC");
		run("100 MILE HOUSE, BC");
		run("PYPER LAKE, BC");
	}
	
	void run(String sentence) {
		run(sentence, true);
	}
	
	void run(String sentence, boolean expected) {
		parser.setTrace(trace);
		StopWatch sw = new StopWatch();
		sw.start();
		BasicParseDerivationHandler handler = new BasicParseDerivationHandler();
		parser.parse(sentence, false, handler);
		sw.stop();
		System.out.println("elapsed time: " + sw.getElapsedTime());
		boolean isValid = handler.getDerivations().size() > 0;
		if(isValid) {
			System.out.println(handler.toString());
		} else {
			System.out.println("No valid parsing found: " + sentence);
		}
		assertTrue(isValid == expected);
	}
	
}
