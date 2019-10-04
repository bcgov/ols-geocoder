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
package ca.bc.gov.ols.geocoder.api.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IntersectionMatch extends GeocodeMatch {
	
	@XmlElement
	private StreetIntersectionAddress address;
	
	/**
	 * Required by JAXB
	 */
	public IntersectionMatch() {
	}
	
	public IntersectionMatch(StreetIntersectionAddress address, MatchPrecision precision,
			int precisionPoints) {
		super(precision, precisionPoints);
		this.address = address;
	}
	
	@Override
	public StreetIntersectionAddress getAddress() {
		return address;
	}
	
	@Override
	public String getAddressString() {
		return address.getAddressString();
	}
	
	@Override
	public Point getLocation() {
		return address.getLocation();
	}
	
	@Override
	public void setLocation(Point location) {
		address.setLocation(location);
	}
	
	@Override
	public String toString() {
		return address + " " + score + " " + precision;
	}
	
	@Override
	public void resolve(GeocoderDataStore ds) {
		// nothing to resolve for intersectionMatches
	}
	
}
