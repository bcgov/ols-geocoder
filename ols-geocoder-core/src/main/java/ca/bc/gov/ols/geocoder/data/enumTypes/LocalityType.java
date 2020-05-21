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

import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;


public enum LocalityType {
	CITY(1, "City"),
	COMMUNITY(121, "Community"),
	DISTRICT_MUNICIPALITY(35, "District Municipality"),
	FIRST_NATION_VILLAGE(186, "First Nation Village"),
	INDIAN_RESERVE(512, "Indian Reserve"),
	LANDING(114, "Landing"),
	LOCALITY(108, "Locality"),
	MOUNTAIN_RESORT_MUNICIPALITY(39, "Mountain Resort Municipality"),
	RANCH(152, "Ranch"),
	RECREATIONAL_COMMUNITY(187, "Recreational Community"),
	REGIONAL_MUNICIPALITY(408, "Regional Municipality"),
	RESORT_MUNICIPALITY(16, "Resort Municipality"),
	SUBDIVISION(206, "Subdivision"),
	TOWN(2, "Town"),
	UNKNOWN(-1, "Unknown"),
	URBAN_COMMUNITY(200, "Urban Community"),
	VILLAGE(3, "Village");
	
	private int id;
	private String label;
	
	private LocalityType(int id, String label) {
		this.id = id;
		this.label = label;
	}
	
	/**
	 * Takes a string value and returns the corresponding LocalityType object.
	 * 
	 * @param localityType string representation of the LocalityType
	 * @return the LocalityType corresponding to the given string representation.
	 */
	public static LocalityType convert(String localityType) {
		for(LocalityType lt : values()) {
			if(lt.label.equalsIgnoreCase(localityType)) {
				return lt;
			}
		}
		LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX + LocalityType.class.getCanonicalName())
			.warn("CONSTRAINT VIOLATION: Invalid LocalityType value: '" + localityType + "'.");
		return UNKNOWN;
	}

	/**
	 * Takes an Integer value and returns the corresponding LocalityType object.
	 * 
	 * @param localityTypeId id of the LocalityType
	 * @return the LocalityType corresponding to the given id
	 */
	public static LocalityType convert(Integer localityTypeId) {
		if(localityTypeId != null) {
			for(LocalityType lt : values()) {
				if(lt.id == localityTypeId) {
					return lt;
				}
			}
		}
		LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX + LocalityType.class.getCanonicalName())
				.warn("CONSTRAINT VIOLATION: Invalid LocalityTypeId value: '" + localityTypeId + "'.");
		return UNKNOWN;
	}

	/**
	 * @return the string representation of this LocalityType object
	 */
	@Override
	public String toString() {
		return label;
	}
}
