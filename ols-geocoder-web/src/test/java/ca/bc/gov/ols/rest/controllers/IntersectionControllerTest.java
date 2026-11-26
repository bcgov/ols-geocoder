package ca.bc.gov.ols.rest.controllers;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.controllers.IntersectionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.validation.BindingResult;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class IntersectionControllerTest {
	private static IGeocoder gc;

	@Spy
	SharedParameters queryParams;

	@Spy
	BindingResult bindingResult;

	@InjectMocks
	private IntersectionController ctrlr;

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
	public void testGetIntersectionByValidUuid() throws Exception {
		OlsResponse resp = ctrlr.getIntersection(
				"00000000-0000-0000-0000-000000001001", queryParams, bindingResult);
		Object resp_o = resp.getResponseObj();
		StreetIntersectionAddress addr = (resp_o instanceof StreetIntersectionAddress
				? (StreetIntersectionAddress)resp_o : null);
		assertNotNull(addr);
		assertEquals("00000000-0000-0000-0000-000000001001", addr.getID());
	}

	@Tag("Prod")
	@Test
	public void testGetIntersectionByInvalidUuidDoesNotCrash() throws Exception {
		OlsResponse resp = ctrlr.getIntersection(
				"00000000-0000-0000-0000-000000009999", queryParams, bindingResult);
		assertNotNull(resp);
	}

	@Tag("Prod")
	@Test
	public void testGetIntersectionByInvalidUuidReturnsEmpty() throws Exception {
		OlsResponse resp = ctrlr.getIntersection(
				"00000000-0000-0000-0000-000000009999", queryParams, bindingResult);
		Object resp_o = resp.getResponseObj();
		if(resp_o instanceof StreetIntersectionAddress[]) {
			assertEquals(0, ((StreetIntersectionAddress[])resp_o).length);
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
