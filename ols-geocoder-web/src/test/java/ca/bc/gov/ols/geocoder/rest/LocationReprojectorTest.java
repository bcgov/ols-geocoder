package ca.bc.gov.ols.geocoder.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import ca.bc.gov.ols.geocoder.api.data.SiteAddress;

@Tag("Prod")
class LocationReprojectorTest {
	@Test
	void reprecisionLeavesMissingLocationUnset() {
		LocationReprojector reprojector = new LocationReprojector(4326, 4326);
		SiteAddress address = new SiteAddress();

		reprojector.reprecision(address);

		assertNull(address.getLocation());
	}

	@Test
	void reprecisionRoundsCoordinatesAndSetsTargetSrs() {
		LocationReprojector reprojector = new LocationReprojector(4326, 4326);
		SiteAddress address = new SiteAddress();
		address.setLocation(new GeometryFactory().createPoint(new Coordinate(1.123456789, 2.987654321)));

		reprojector.reprecision(address);

		assertEquals(1.1234568, address.getLocation().getX(), 0.00000001);
		assertEquals(2.9876543, address.getLocation().getY(), 0.00000001);
		assertEquals(4326, address.getLocation().getSRID());
	}
}
