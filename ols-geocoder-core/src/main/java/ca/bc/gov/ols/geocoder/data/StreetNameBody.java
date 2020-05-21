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
package ca.bc.gov.ols.geocoder.data;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import ca.bc.gov.ols.util.ArraySet;

/**
 * StreetNameBody is what gets stored at leaves of the main NameLookupTrie.
 * 
 * It stores a list of all the streetName objects that have the same streetNameBody, and provides a
 * way to lookup intersections by intersecting streetNameBody. The intersection lookup uses a
 * NameLookupTrie if there is a large number of intersecting streetNameBodies, or a hashMap if there
 * are relatively few.
 * 
 * @author chodgson
 * 
 */
public class StreetNameBody {
	
	private ArraySet<StreetName> streetNames = new ArraySet<StreetName>(4);
	private Map<String, Set<StreetIntersection>> intersectionMap = null;
	
	public Set<StreetName> getStreetNames() {
		return streetNames;
	}
	
	public Set<StreetIntersection> getIntersections(String streetNameBody) {
		Set<StreetIntersection> intersections = intersectionMap.get(streetNameBody.toUpperCase());
		if(intersections != null) {
			return intersections;
		}
		return Collections.emptySet();
	}
	
	public void setIntersections(Map<String, Set<StreetIntersection>> intersectionMap) {
		this.intersectionMap = intersectionMap;
	}
	
	public void trimToSize() {
		streetNames.trimToSize();
	}
	
}
