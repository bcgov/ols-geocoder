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
 * The possible sides of the street on which which a block face lies.
 */
public enum Side {
	LEFT, RIGHT;
	
	public static Side convert(String s) {
		char c = s.charAt(0);
		switch(c) {
		case 'L':
		case 'l':
			return LEFT;
		case 'R':
		case 'r':
			return RIGHT;
		}
		throw new IllegalArgumentException("Invalid Side) value: '" + c
				+ "' (must be one of 'L', 'l', 'R', 'r')");
	}
	
	public String toChar() {
		switch(this) {
		case LEFT:
			return "l";
		case RIGHT:
			return "r";
		}
		return "";
	}
	
	public Side opposite() {
		switch(this) {
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		}
		return null;
	}
}