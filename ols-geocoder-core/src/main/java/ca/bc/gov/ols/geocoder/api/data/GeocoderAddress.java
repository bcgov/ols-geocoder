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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.data.Locality;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocalityType;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.geocoder.util.GeometryAdapter;

/**
 * Common logic for all address objects
 * 
 * @author elitvin
 */
@XmlSeeAlso({SiteAddress.class, StreetIntersectionAddress.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class GeocoderAddress implements ModifiableLocation {
	
	@XmlElement
	@XmlJavaTypeAdapter(GeometryAdapter.class)
	private Point location;
	
	private String localityName;
	private LocalityType localityType;
	private String electoralArea;
	private String stateProvTerr;
	
	private PositionalAccuracy locationPositionalAccuracy;
	private LocationDescriptor locationDescriptor;
	
	// used for caching addressStrings
	@XmlTransient
	protected String addressString;
	
	public GeocoderAddress() {
	
	}
		
	// copy constructor does a shallow copy except for the location point
	public GeocoderAddress(GeocoderAddress base) {
		location = (Point)(base.location.copy());
		localityName = base.localityName;
		localityType = base.localityType;
		electoralArea = base.electoralArea;
		stateProvTerr = base.stateProvTerr;
		locationPositionalAccuracy = base.locationPositionalAccuracy;
		locationDescriptor = base.locationDescriptor;		
		addressString = base.addressString;
	}
	
	public void setLocality(Locality locality) {
		addressString = null;
		this.localityName = locality.getName();
		this.localityType = locality.getType();
		electoralArea = locality.getElectoralArea();
		this.stateProvTerr = locality.getStateProvTerr().getName();
	}
	
	@Override
	public Point getLocation() {
		return location;
	}
	
	@Override
	public void setLocation(Point location) {
		this.location = location;
	}
	
	public String getLocalityName() {
		return localityName;
	}
	
	public void setLocalityName(String localityName) {
		addressString = null;
		this.localityName = localityName;
	}
	
	public LocalityType getLocalityType() {
		return localityType;
	}
	
	public void setLocalityType(LocalityType localityType) {
		this.localityType = localityType;
	}

	public String getElectoralArea() {
		return electoralArea;
	}

	public void setElectoralArea(String electoralArea) {
		this.electoralArea = electoralArea;
	}

	public String getStateProvTerr() {
		return stateProvTerr;
	}
	
	public void setStateProvTerr(String stateProvTerr) {
		addressString = null;
		this.stateProvTerr = stateProvTerr;
	}
	
	public PositionalAccuracy getLocationPositionalAccuracy() {
		return locationPositionalAccuracy;
	}
	
	public void setLocationPositionalAccuracy(
			PositionalAccuracy locationPositionalAccuracy) {
		this.locationPositionalAccuracy = locationPositionalAccuracy;
	}
	
	public LocationDescriptor getLocationDescriptor() {
		return locationDescriptor;
	}
	
	public void setLocationDescriptor(LocationDescriptor locationDescriptor) {
		this.locationDescriptor = locationDescriptor;
	}
	
}
