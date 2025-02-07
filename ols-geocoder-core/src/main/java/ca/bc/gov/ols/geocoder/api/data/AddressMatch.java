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
import javax.xml.bind.annotation.XmlTransient;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.data.AccessPoint;
import ca.bc.gov.ols.geocoder.data.ISite;
import ca.bc.gov.ols.geocoder.data.StreetSegment;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AddressMatch extends GeocodeMatch {
	
	@XmlElement(nillable = true)
	private SiteAddress address;
	
	@XmlTransient
	private StreetSegment segment;
	
	@XmlTransient
	private AccessPoint accessPoint;
	
	@XmlTransient
	private ISite site;
	
	public AddressMatch() {
	}
	
	public AddressMatch(SiteAddress address, MatchPrecision precision, int precisionPoints) {
		super(precision, precisionPoints);
		this.address = address;
	}
	
	/**
	 * Copy constructor, used for copying exactMatch instances before returning them. 
	 * @param toCopy
	 */
	public AddressMatch(AddressMatch toCopy) {
		super(toCopy);
		this.address = new SiteAddress(toCopy.address);
		this.segment = toCopy.segment;
		this.accessPoint = toCopy.accessPoint;
		this.site = toCopy.site;
	}
	
	@Override
	public AddressMatch copy() {
		return new AddressMatch(this);
	}
	
	@Override
	public String toString() {
		return address + " " + score + " " + precision;
	}
	
	@Override
	public SiteAddress getAddress() {
		return address;
	}
	
	@Override
	public String getAddressString() {
		return address.getAddressString();
	}
	
	public void setSegment(StreetSegment segment) {
		this.segment = segment;
	}
	
	public StreetSegment getSegment() {
		return segment;
	}
	
	public void setAccessPoint(AccessPoint accessPoint) {
		this.accessPoint = accessPoint;
	}
	
	public void setSite(ISite site) {
		this.site = site;
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
	public void resolve(GeocoderDataStore ds) {
		if(site != null) {
			ds.loadSiteDetailsById(address, site, accessPoint);
		}
	}
	
}
