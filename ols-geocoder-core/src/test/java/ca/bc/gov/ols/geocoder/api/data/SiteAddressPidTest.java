package ca.bc.gov.ols.geocoder.api.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

@Tag("Prod")
class SiteAddressPidTest {
	@Test
	void exposesPidAndErrorAndCopiesThem() {
		SiteAddress original = new SiteAddress();
		original.setLocation(new GeometryFactory().createPoint(new Coordinate(1, 2)));
		original.setPid("123456789");
		original.setError("No civic address found");

		SiteAddress copy = new SiteAddress(original);

		assertEquals("123456789", copy.getPid());
		assertEquals("No civic address found", copy.getError());
	}
}
