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
 * Enumerates the possible Status values for a Site (ie. active, retired, proposed)
 * 
 * @author chodgson
 * 
 */
public enum PhysicalStatus {
	ACTIVE, RETIRED, PROPOSED;
	
	/**
	 * Converts from a string representation to the corresponding PhysicalStatus object
	 * 
	 * @param status the string representation, either a database value or a full string
	 * @return the corresponding PhysicalStatus object
	 */
	public static PhysicalStatus convert(String status) {
		if(status != null) {
			if("A".equals(status)) {
				return ACTIVE;
			}
			if("R".equals(status)) {
				return RETIRED;
			}
			if("P".equals(status)) {
				return PROPOSED;
			}
			if("active".equals(status.toLowerCase())) {
				return ACTIVE;
			}
			if("retired".equals(status.toLowerCase())) {
				return RETIRED;
			}
			if("proposed".equals(status.toLowerCase())) {
				return PROPOSED;
			}
		}
		throw new IllegalArgumentException("Invalid PhysicalStatus value: '" + status + "'.");
	}
	
	/**
	 * @return the human-readable string representation of the PhysicalStatus, to be used for
	 *         display purposes.
	 */
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
	
	/**
	 * @return the short-form (single character) string representation of the PhysicalStatus, as
	 *         used in the database.
	 */
	public String toDbValue() {
		return super.toString().substring(0, 1);
	}
}