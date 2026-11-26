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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import io.swagger.v3.oas.annotations.media.Schema;

import ca.bc.gov.ols.geocoder.data.Locality;
import ca.bc.gov.ols.geocoder.data.StreetIntersection;
import ca.bc.gov.ols.geocoder.data.StreetName;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Schema(description = "Represents a street intersection address, including the intersection's unique ID, "
		+ "name (built from the intersecting street names), degree (number of streets meeting at the intersection), "
		+ "and the representative point location.")
public class StreetIntersectionAddress extends GeocoderAddress {

	@Schema(description = "The unique identifier (UUID) of the intersection.",
			example = "74628473-1460-4d5f-8699-34964e8d3ba6")
	private String id;

	@Schema(description = "The name of the intersection, built from the intersecting street names "
			+ "(e.g. 'Douglas St and Johnson St'). If only one street name is known, it is repeated.",
			example = "Douglas St and Johnson St")
	private String name;

	@Schema(description = "The degree of the intersection, i.e. the number of streets meeting at this point. "
			+ "A dead-end has degree 1, a T-intersection has degree 2, a four-way intersection has degree 4.",
			example = "4")
	private int degree;
	
	public StreetIntersectionAddress() {
	}
	
	public StreetIntersectionAddress(StreetIntersection intersection) {
		setLocation(intersection.getLocation());
		id = intersection.getUuid().toString();
		name = buildName(intersection.getPrimaryStreetNames());
		degree = intersection.getDegree();
		determineLocality(intersection.getLocalities());
		setLocationPositionalAccuracy(PositionalAccuracy.HIGH);
		setLocationDescriptor(LocationDescriptor.INTERSECTION_POINT);
	}
	
	/**
	 * Pick the alphabetically first locality name if there is more than one.
	 * 
	 * @param localities the set of localities to pick from
	 */
	private void determineLocality(Set<Locality> localities) {
		Locality primaryLocality = null;
		for(Locality loc : localities) {
			if(primaryLocality == null || loc.getName().compareTo(primaryLocality.getName()) < 0) {
				primaryLocality = loc;
			}
		}
		setLocality(primaryLocality);
	}
	
	public int getSrsCode() {
		if(getLocation() == null) {
			return 0;
		}
		return getLocation().getSRID();
	}
	
	private String buildName(Set<StreetName> names) {
		ArrayList<String> nameList = new ArrayList<String>(names.size());
		for(StreetName sn : names) {
			nameList.add(sn.toString());
		}
		Collections.sort(nameList);
		// if there is only one street name, we repeat it
		if(nameList.size() == 1) {
			nameList.add(nameList.get(0));
		}
		StringBuilder sb = new StringBuilder();
		boolean firstStreet = true;
		for(String name : nameList) {
			if(firstStreet) {
				firstStreet = false;
			} else {
				sb.append(" and ");
			}
			sb.append(name);
		}
		return sb.toString();
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		addressString = null;
		this.name = name;
	}
	
	public int getDegree() {
		return degree;
	}
	
	public String getAddressString() {
		if(addressString == null) {
			addressString = name + ", " + getLocalityName() + ", " + getStateProvTerr();
		}
		return addressString;
	}
	
	@Override
	public String toString() {
		return getAddressString();
	}
	
}
