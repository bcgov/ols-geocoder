package ca.bc.gov.ols.geocoder.api.data;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;

/** 
 * StringOnlyGeocodeMatch is a sort of "dummy" AddressMatch with only an addressString,
 * ONLY used for doing lookups in the exactMatchLookup
 */
public class StringOnlyGeocodeMatch extends GeocodeMatch {

	private String addressString;
	
	public StringOnlyGeocodeMatch(String addressString) {
		this.addressString = addressString;
	}

	/**
	 * Copy constructor, used for copying exactMatch instances before returning them.
	 * This should never be used in this dummy Match class but is required by the abstract parent.
	 * @param toCopy
	 */
	public StringOnlyGeocodeMatch(StringOnlyGeocodeMatch toCopy) {
		super(toCopy);
		this.addressString = toCopy.addressString;
	}
	
	@Override
	public StringOnlyGeocodeMatch copy() {
		return new StringOnlyGeocodeMatch(this);
	}
	
	@Override
	public Point getLocation() {
		return null;
	}

	@Override
	public void setLocation(Point location) {
	}

	@Override
	public String getAddressString() {
		return addressString;
	}

	@Override
	public GeocoderAddress getAddress() {
		return null;
	}

	@Override
	public void resolve(GeocoderDataStore ds) {
	}

}
