package ca.bc.gov.ols.rest.controllers;

import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.controllers.GeocoderController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.validation.BindingResult;
import java.lang.reflect.Field;
import java.util.List;
import static ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision.STREET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class GeocoderControllerTest {
    private static IGeocoder gc;

    @Spy
    SharedParameters queryParams;

    @Spy
    BindingResult bindingResult;

    @InjectMocks
    private GeocoderController ctrlr;

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
    public void testGeocodeAddress() throws Exception {
        GeocodeQuery q = new GeocodeQuery("1207 Douglas st victoria BC");
        OlsResponse resp = ctrlr.geocoder(q, bindingResult);
        Object resp_o = resp.getResponseObj();
        SearchResults search_r = (resp_o instanceof SearchResults ? (SearchResults)resp_o : null);
        assertNotNull(search_r);
        List<GeocodeMatch> matches = search_r.getMatches();
        assertEquals(matches.size(), 1);
        GeocodeMatch match = matches.get(0);
        assertEquals(match.getLocation().getX(), 1);
        assertEquals(match.getLocation().getY(), 1);
        assertEquals(match.getPrecision(), STREET);
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