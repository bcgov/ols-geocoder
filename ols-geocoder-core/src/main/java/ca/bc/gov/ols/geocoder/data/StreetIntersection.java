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

import gnu.trove.set.hash.THashSet;

import java.util.Set;
import java.util.UUID;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.util.ArraySet;

public class StreetIntersection extends LocationBase {
	
	private final int id;
	private final UUID uuid;
	private final int degree;
	
	private final ArraySet<StreetName> primaryStreetNames = new ArraySet<StreetName>();
	private final ArraySet<StreetName> aliasStreetNames = new ArraySet<StreetName>();
	private final ArraySet<Locality> localities = new ArraySet<Locality>();
	
	public StreetIntersection(int id, UUID uuid, Point location, int degree) {
		super(location);
		this.id = id;
		this.uuid = uuid;
		this.degree = degree;
	}
	
	public Set<StreetName> getPrimaryStreetNames() {
		return primaryStreetNames;
	}
	
	public Set<StreetName> getAliasStreetNames() {
		return aliasStreetNames;
	}
	
	public void addLocality(Locality locality) {
		localities.add(locality);
	}
	
	public Set<Locality> getLocalities() {
		return localities;
	}
	
	public int getId() {
		return id;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public int getDegree() {
		return degree;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof StreetIntersection
				&& this.id == ((StreetIntersection)obj).id) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	public Set<StreetName> getStreetNames() {
		Set<StreetName> allNames = new THashSet<StreetName>(primaryStreetNames.size()
				+ aliasStreetNames.size());
		allNames.addAll(primaryStreetNames);
		allNames.addAll(aliasStreetNames);
		return allNames;
	}
	
	public void trimToSize() {
		primaryStreetNames.trimToSize();
		aliasStreetNames.trimToSize();
		localities.trimToSize();
	}
	
}
