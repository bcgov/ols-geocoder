package ca.bc.gov.ols.rest.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;

import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.PidsResponse;
import ca.bc.gov.ols.geocoder.rest.controllers.ParcelController;

@Tag("Prod")
class ParcelControllerTest {
	private ParcelController controller;
	private IGeocoder geocoder;
	private SharedParameters params;
	private BeanPropertyBindingResult bindingResult;

	@BeforeEach
	void setUp() throws Exception {
		GeocoderFactory factory = new GeocoderFactory();
		factory.setUnitTestMode("TRUE");
		geocoder = factory.getGeocoder();
		controller = new ParcelController();
		SiteControllerTest.setPrivateField(controller, "geocoder", geocoder);
		params = new SharedParameters();
		bindingResult = new BeanPropertyBindingResult(params, "params");
	}

	@Test
	void returnsPidsForKnownSite() {
		OlsResponse response = controller.getPids(
				"00000000-0000-0000-0000-000000004002", params, bindingResult);

		PidsResponse pids = assertInstanceOf(PidsResponse.class, response.getResponseObj());
		assertEquals("028726014", pids.getPids());
	}

	@Test
	void returnsEmptyPidsForUnknownSite() {
		OlsResponse response = controller.getPids(
				"00000000-0000-0000-0000-000000009999", params, bindingResult);

		PidsResponse pids = assertInstanceOf(PidsResponse.class, response.getResponseObj());
		assertNull(pids.getSiteUuid());
		assertNull(pids.getPids());
	}

	@Test
	void returnsPidAddressResultsAndMetadata() {
		OlsResponse response = controller.getAddresses(
				"bad,028726014,123456789", params, bindingResult);

		SiteAddress[] addresses = assertInstanceOf(SiteAddress[].class, response.getResponseObj());
		assertEquals(3, addresses.length);
		assertEquals("bad", addresses[0].getPid());
		assertEquals("Invalid PID: bad. A PID must contain exactly 9 digits.", addresses[0].getError());
		assertEquals("028726014", addresses[1].getPid());
		assertNull(addresses[1].getError());
		assertEquals("123456789", addresses[2].getPid());
		assertEquals("No civic address was found for PID: 123456789", addresses[2].getError());
		assertEquals("utf-8", response.getExtraInfo("encoding"));
		assertNotNull(response.getExtraInfo("searchTimestamp"));
		assertEquals(ca.bc.gov.ols.geocoder.config.GeocoderConfig.VERSION,
				response.getExtraInfo("version"));
	}

	@Test
	void datastoreLooksUpSitesByPid() {
		assertNotNull(geocoder.getDatastore().getRawSiteByPid("028726014"));
		assertNull(geocoder.getDatastore().getRawSiteByPid("999999999"));

		SiteAddress address = geocoder.getDatastore().getSiteByPid("028726014",
				ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor.ANY, 0);
		assertNotNull(address);
		assertEquals("028726014", address.getPid());
		assertNull(geocoder.getDatastore().getSiteByPid("999999999",
				ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor.ANY, 0));
	}

	@Test
	void pidEndpointsHandleDummyGeocoderWithoutDatastore() throws Exception {
		GeocoderFactory factory = new GeocoderFactory();
		factory.setDummyMode("TRUE");
		IGeocoder dummyGeocoder = factory.getGeocoder();
		SiteControllerTest.setPrivateField(controller, "geocoder", dummyGeocoder);

		OlsResponse pidsResponse = controller.getPids(
				"00000000-0000-0000-0000-000000004002", params, bindingResult);
		PidsResponse pids = assertInstanceOf(PidsResponse.class, pidsResponse.getResponseObj());
		assertNull(pids.getSiteUuid());

		OlsResponse addressesResponse = controller.getAddresses("123456789", params, bindingResult);
		SiteAddress[] addresses = assertInstanceOf(SiteAddress[].class, addressesResponse.getResponseObj());
		assertEquals("No civic address was found for PID: 123456789", addresses[0].getError());
		assertEquals("", addressesResponse.getExtraInfo("disclaimer"));
	}
}
