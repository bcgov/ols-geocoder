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
package ca.bc.gov.ols.geocoder.data.enumTypes;

/**
 * LocationDescriptor describes what the location point is actually referring to (eg. localityPoint,
 * intersectionPoint, accessPoint, routingPoint, parcelPoint).
 * 
 * @author chodgson
 * 
 */
public enum LocationDescriptor {
	ANY("any"),
	STREET_POINT("streetPoint"),
	LOCALITY_POINT("localityPoint"),
	PROVINCE_POINT("provincePoint"),
	INTERSECTION_POINT("intersectionPoint"),
	ACCESS_POINT("accessPoint"),
	ROUTING_POINT("routingPoint"),
	BLOCK_FACE_POINT("blockFacePoint"),
	ROOFTOP_POINT("rooftopPoint"),
	FRONT_DOOR_POINT("frontDoorPoint"),
	PARCEL_POINT("parcelPoint");
	
	private String label;
	
	private LocationDescriptor(String label) {
		this.label = label;
	}
	
	/**
	 * Takes a string value and returns the corresponding LocationDescriptor object.
	 * 
	 * @param locationDescriptor string representation of the LocationDescriptor
	 * @return the LocationDescriptor corresponding to the given string representation.
	 */
	public static LocationDescriptor convert(String locationDescriptor) {
		for(LocationDescriptor ld : values()) {
			if(ld.label.equalsIgnoreCase(locationDescriptor)) {
				return ld;
			}
		}
		if(locationDescriptor == null || locationDescriptor.isEmpty()) {
			return null;
		}
		throw new IllegalArgumentException("Invalid LocationDescriptor value: '"
				+ locationDescriptor + "'.");
	}
	
	/**
	 * @return the string representation of this LocationDescriptor object
	 */
	@Override
	public String toString() {
		return label;
	}
}