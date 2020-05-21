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
 * PositionalAccuracy enumerates the possible accuracy values for a Site or other MatchResult. (ie.
 * low, medium, coarse, high)
 * 
 * @author chodgson
 * 
 */
public enum PositionalAccuracy {
	LOW, MEDIUM, COARSE, HIGH;
	
	/**
	 * Converts from a string representation to the corresponding PositionalAccuracy object
	 * 
	 * @param accuracy the string representation of the PositionalAccuracy level
	 * @return the PositionalAccuracy object corresponding to the given string representation
	 */
	public static PositionalAccuracy convert(String accuracy) {
		if("LOW".equalsIgnoreCase(accuracy)) {
			return LOW;
		}
		if("MEDIUM".equalsIgnoreCase(accuracy)) {
			return MEDIUM;
		}
		if("COARSE".equalsIgnoreCase(accuracy)) {
			return COARSE;
		}
		if("HIGH".equalsIgnoreCase(accuracy)) {
			return HIGH;
		}
		if(accuracy == null || accuracy.isEmpty()) {
			return null;
		}
		throw new IllegalArgumentException("Invalid PositionalAccuracy value: '" + accuracy + "'.");
	}
	
	/**
	 * @return the human-readable string representation of this PositionalAccuracy, to be used for
	 *         display purposes.
	 */
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
	
	/**
	 * @return the short-form (all caps) string representation of this PositionalAccuracy, as used
	 *         in the database.
	 */
	public String toDbValue() {
		return super.toString();
	}
}