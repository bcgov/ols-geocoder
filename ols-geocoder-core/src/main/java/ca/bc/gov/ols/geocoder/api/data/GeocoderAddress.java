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

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Base class for all geocoder address results, containing the shared properties "
		+ "for both site addresses and intersection addresses.")
public abstract class GeocoderAddress implements ModifiableLocation {
	
	@XmlElement
	@XmlJavaTypeAdapter(GeometryAdapter.class)
	@Schema(description = "The geographic location of the address as a GeoJSON Point geometry.",
			example = "{\"type\":\"Point\",\"coordinates\":[-123.1216,49.2827]}")
	private Point location;

	@Schema(description = "The full name of the locality (neighbourhood, city, province) for the address.",
			example = "Victoria, BC")
	private String localityName;

	@Schema(description = "The type of locality.",
			example = "Municipality")
	private LocalityType localityType;

	@Schema(description = "The electoral area, if applicable.",
			example = "")
	private String electoralArea;

	@Schema(description = "The province or territory abbreviation.",
			example = "BC")
	private String stateProvTerr;
	
	@Schema(description = "The positional accuracy classification of the location point.")
	private PositionalAccuracy locationPositionalAccuracy;

	@Schema(description = "Describes what the location point actually refers to "
			+ "(e.g. intersectionPoint, rooftopPoint, accessPoint).")
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
		if(locality == null) {
			this.localityName = null;
			this.localityType = null;
			return;
		}
		addressString = null;
		this.localityName = locality.getFullyQualifiedName();
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
