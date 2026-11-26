package ca.bc.gov.ols.rest.controllers;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.controllers.SiteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.validation.BindingResult;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class SiteControllerTest {
	private static IGeocoder gc;

	@Spy
	SharedParameters queryParams;

	@Spy
	BindingResult bindingResult;

	@InjectMocks
	private SiteController ctrlr;

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
	public void testGetSubSitesByValidUuidWithChildren() throws Exception {
		OlsResponse resp = ctrlr.getSubSites(
				"00000000-0000-0000-0000-000000004002", queryParams, bindingResult);
		Object resp_o = resp.getResponseObj();
		SiteAddress[] addrs = (resp_o instanceof SiteAddress[]
				? (SiteAddress[])resp_o : null);
		assertNotNull(addrs);
		assertEquals(1, addrs.length);
		assertEquals("UNIT 419, parcelPoint -- Douglas St, Victoria, BC", addrs[0].getAddressString());
	}

	@Tag("Prod")
	@Test
	public void testGetSubSitesByValidUuidNoChildren() throws Exception {
		OlsResponse resp = ctrlr.getSubSites(
				"00000000-0000-0000-0000-000000004001", queryParams, bindingResult);
		Object resp_o = resp.getResponseObj();
		SiteAddress[] addrs = (resp_o instanceof SiteAddress[]
				? (SiteAddress[])resp_o : null);
		assertNotNull(addrs);
		assertEquals(0, addrs.length);
	}

	@Tag("Prod")
	@Test
	public void testGetSubSitesByInvalidUuidDoesNotCrash() throws Exception {
		OlsResponse resp = ctrlr.getSubSites(
				"00000000-0000-0000-0000-000000009999", queryParams, bindingResult);
		assertNotNull(resp);
	}

	@Tag("Prod")
	@Test
	public void testGetSubSitesByInvalidUuidReturnsEmpty() throws Exception {
		OlsResponse resp = ctrlr.getSubSites(
				"00000000-0000-0000-0000-000000009999", queryParams, bindingResult);
		Object resp_o = resp.getResponseObj();
		if(resp_o instanceof SiteAddress[]) {
			assertEquals(0, ((SiteAddress[])resp_o).length);
		} else {
			assertNull(resp_o);
		}
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
