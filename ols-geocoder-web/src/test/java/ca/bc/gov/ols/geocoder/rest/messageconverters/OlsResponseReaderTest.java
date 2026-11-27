package ca.bc.gov.ols.geocoder.rest.messageconverters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;

@Tag("Prod")
class OlsResponseReaderTest {
	@Test
	void writesParcelFeaturesAndErrorPropertiesWithMetadata() throws Exception {
		SiteAddress errorAddress = new SiteAddress();
		errorAddress.setPid("bad");
		errorAddress.setError("Invalid PID");
		SiteAddress foundAddress = new SiteAddress();
		foundAddress.setPid("123456789");
		foundAddress.setStreetName("Main");

		OlsResponse response = new OlsResponse(new SiteAddress[] {errorAddress, foundAddress});
		response.setExtraInfo("encoding", "utf-8");
		response.setExtraInfo("searchTimestamp", "2026-11-27T12:00:00");
		response.setExtraInfo("executionTime", "1.5");
		response.setExtraInfo("version", "test-version");
		response.setExtraInfo("baseDataDate", "2026-11-26");
		response.setExtraInfo("disclaimer", "disclaimer text");
		response.setExtraInfo("privacyStatement", "privacy text");
		response.setExtraInfo("copyrightNotice", "copyright text");
		response.setOutputSRS(4326);
		StringWriter output = new StringWriter();
		OlsResponseReader reader = new OlsResponseReader(response,
				new JsonOlsResponseWriter(output, null), null);

		reader.convert();

		String json = output.toString();
		assertTrue(json.contains("\"type\":\"FeatureCollection\""));
		assertTrue(json.contains("\"encoding\":\"utf-8\""));
		assertTrue(json.contains("\"srsCode\":4326") || json.contains("\"crs\""));
		assertTrue(json.contains("\"pid\":\"bad\""));
		assertTrue(json.contains("\"error\":\"Invalid PID\""));
		assertTrue(json.contains("\"pid\":\"123456789\""));
		assertTrue(json.contains("\"features\""));
	}

	@Test
	void writesEmptyParcelMetadataWhenResponseHasNoMetadata() throws Exception {
		OlsResponse response = new OlsResponse(new SiteAddress[0]);
		StringWriter output = new StringWriter();
		OlsResponseReader reader = new OlsResponseReader(response,
				new JsonOlsResponseWriter(output, null), null);

		reader.convert();

		String json = output.toString();
		assertTrue(json.contains("\"type\":\"FeatureCollection\""));
		assertTrue(json.contains("\"features\":[]"));
		assertFalse(json.contains("\"encoding\""));
	}

	@Test
	void metadataWriterDefaultsToFeatureCollectionWithoutMetadata() throws Exception {
		StringWriter output = new StringWriter();
		JsonOlsResponseWriter writer = new JsonOlsResponseWriter(output, null);
		writer.featureCollectionHeader(Map.of("encoding", "utf-8"));
		writer.featureCollectionFooter();

		assertTrue(output.toString().contains("\"encoding\":\"utf-8\""));
	}

	@Test
	void htmlWriterUsesDefaultMetadataHeaderFallback() throws Exception {
		StringWriter output = new StringWriter();
		HtmlOlsResponseWriter writer = new HtmlOlsResponseWriter(output, null);
		writer.featureCollectionHeader(Map.of("encoding", "utf-8"));
		writer.featureCollectionFooter();

		assertTrue(output.toString().contains("<div class=\"results\"><table>"));
		assertTrue(output.toString().contains("</table></div>"));
	}

	@Test
	void serializesSearchResultMetadataUsingSharedKeys() throws Exception {
		GeocoderFactory factory = new GeocoderFactory();
		factory.setUnitTestMode("TRUE");
		IGeocoder geocoder = factory.getGeocoder();
		SearchResults results = new SearchResults(new GeocodeQuery(), Collections.emptyList(), null);
		OlsResponse response = new OlsResponse(results);
		StringWriter output = new StringWriter();
		OlsResponseReader reader = new OlsResponseReader(response,
				new JsonOlsResponseWriter(output, geocoder.getDatastore().getConfig()),
				geocoder.getDatastore().getConfig());

		reader.convert();

		String json = output.toString();
		assertTrue(json.contains("\"type\":\"FeatureCollection\""));
		assertTrue(json.contains("\"encoding\":\"ascii\""));
		assertTrue(json.contains("\"searchTimestamp\""));
		assertTrue(json.contains("\"executionTime\""));
		assertTrue(json.contains("\"version\""));
		assertTrue(json.contains("\"features\":[]"));
	}
}
