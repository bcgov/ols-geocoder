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
 * Interpolation enumerates the acceptable input values for the interpolation parameter. (ie.
 * adaptive, linear, none)
 * 
 * @author chodgson
 * 
 */
public enum Interpolation {
	ADAPTIVE("adaptive"),
	LINEAR("linear"),
	NONE("none");
	
	private String label;
	
	private Interpolation(String label) {
		this.label = label;
	}
	
	/**
	 * Converts from a string representation of the interpolation value to the Interpolation object.
	 * 
	 * @param interpolation the string representation of the Interpolation
	 * @return the Interpolation object corresponding to the given string representation
	 */
	public static Interpolation convert(String interpolation) {
		for(Interpolation interp : values()) {
			if(interp.label.equalsIgnoreCase(interpolation)) {
				return interp;
			}
		}
		if("".equals(interpolation) || null == interpolation) {
			return null;
		}
		throw new IllegalArgumentException("Invalid Interpolation value: '" + interpolation + "'.");
	}
	
	/**
	 * @return the string representation of this Interpolation object
	 */
	@Override
	public String toString() {
		return label;
	}
}