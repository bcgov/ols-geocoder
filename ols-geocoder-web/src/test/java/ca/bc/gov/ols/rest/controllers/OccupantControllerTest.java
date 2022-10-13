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
package ca.bc.gov.ols.rest.controllers;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.controllers.OccupantController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.validation.BindingResult;
import java.lang.reflect.Field;
import java.util.List;

import static ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision.OCCUPANT;
import static org.junit.jupiter.api.Assertions.*;

public class OccupantControllerTest {
	private static IGeocoder gc;

	@Spy
	SharedParameters queryParams;

	@Spy
	BindingResult bindingResult;

	@InjectMocks
	private OccupantController ctrlr;

	@BeforeEach
	public void setup() throws Exception {
		MockitoAnnotations.openMocks(this);
		GeocoderFactory factory = new GeocoderFactory();
		factory.setUnitTestMode("TRUE");
		gc = factory.getGeocoder();
		setPrivateField(ctrlr, "geocoder", gc);
	}

	@Tag("Prod")
	@Test
	public void testGetOccupantById() throws Exception {
		OlsResponse resp = ctrlr.getOccupant("00000000-0000-0000-0000-000000006001", queryParams, bindingResult);
		Object resp_o = resp.getResponseObj();
		OccupantAddress address = (resp_o instanceof OccupantAddress ? (OccupantAddress)resp_o : null);
		assertNotNull(address);
		assertEquals(address.getOccupantId(), "00000000-0000-0000-0000-000000006001");
		assertEquals(address.getOccupantName(), "Refractions Research");
	}

	@Tag("Prod")
	@Test
	public void testGetOccupantByName() throws Exception {
		GeocodeQuery q = new GeocodeQuery("Refractions Research");
		OlsResponse resp = ctrlr.geocoder(q, bindingResult);
		Object resp_o = resp.getResponseObj();
		SearchResults search_r = (resp_o instanceof SearchResults ? (SearchResults)resp_o : null);
		assertNotNull(search_r);
		List<GeocodeMatch> matches = search_r.getMatches();
		assertEquals(matches.size(), 1);
		GeocodeMatch match = matches.get(0);
		assertEquals(match.getLocation().getX(), 1);
		assertEquals(match.getLocation().getY(), 1);
		assertEquals(match.getPrecision(), OCCUPANT);
		assertEquals(match.getPrecisionPoints(), 100);
	}

	@Tag("Prod")
	@Test
	public void testGetOccupantByPartialName() throws Exception {
		GeocodeQuery q = new GeocodeQuery("Refractions");
		OlsResponse resp = ctrlr.geocoder(q, bindingResult);
		Object resp_o = resp.getResponseObj();
		SearchResults search_r = (resp_o instanceof SearchResults ? (SearchResults)resp_o : null);
		assertNotNull(search_r);
		List<GeocodeMatch> matches = search_r.getMatches();
		assertEquals(matches.size(), 1);
		GeocodeMatch match = matches.get(0);
		assertEquals(match.getLocation().getX(), 1);
		assertEquals(match.getLocation().getY(), 1);
		assertEquals(match.getPrecision(), OCCUPANT);
		assertEquals(match.getPrecisionPoints(), 100);
	}

	@Tag("Dev")
	@Test
	public void testGetOccupantByNameAndAddress() throws Exception {
		GeocodeQuery q = new GeocodeQuery("refractions -- 1207 Douglas st victoria BC");
		OlsResponse resp = ctrlr.geocoder(q, bindingResult);
		Object resp_o = resp.getResponseObj();
		SearchResults search_r = (resp_o instanceof SearchResults ? (SearchResults)resp_o : null);
		assertNotNull(search_r);
		List<GeocodeMatch> matches = search_r.getMatches();
		assertEquals(matches.size(), 1);
		GeocodeMatch match = matches.get(0);
		assertEquals(match.getLocation().getX(), 1);
		assertEquals(match.getLocation().getY(), 1);
		assertEquals(match.getPrecision(), OCCUPANT);
		assertEquals(match.getPrecisionPoints(), 100);
	}

	@Tag("Dev")
	@Test
	public void testGetNearestOccupant() {
		fail("Not yet implemented");
	}

	@Tag("Dev")
	@Test
	public void testGetOccupantsNear() {
		fail("Not yet implemented");
	}

	@Tag("Dev")
	@Test
	public void testGetOccupantsWithin() {
		fail("Not yet implemented");
	}

	public static void setPrivateField(Object target, String fieldName, Object value){
		try {
			Field privateField = target.getClass().getDeclaredField(fieldName);
			privateField.setAccessible(true);
			privateField.set(target, value);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

}
