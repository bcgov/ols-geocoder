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

import java.util.EnumSet;

public enum MatchPrecision {
	OCCUPANT, UNIT, SITE, CIVIC_NUMBER, INTERSECTION, BLOCK, STREET, LOCALITY, PROVINCE, NONE;
	
	public static MatchPrecision convert(String mp) {
		try {
			return MatchPrecision.valueOf(mp.trim().toUpperCase());
		} catch(IllegalArgumentException iae) {
			throw new IllegalArgumentException("Invalid MatchPrecision value: '" + mp + "'.");
		}
	}
	
	public static EnumSet<MatchPrecision> parseList(String in) {
		if(in == null || in.isEmpty()) {
			return null;
		}
		EnumSet<MatchPrecision> result = EnumSet.noneOf(MatchPrecision.class);
		String[] parts = in.split(",");
		for(String part : parts) {
			result.add(MatchPrecision.convert(part));
		}
		return result;
	}
}
