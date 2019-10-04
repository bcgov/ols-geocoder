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

import org.locationtech.jts.geom.Point;

public class StateProvTerr {
	private final int id;
	private final String name;
	private final String countryCode;
	private final Point location;
	
	public StateProvTerr(int id, String name, String countryCode, Point location) {
		this.id = id;
		this.name = name;
		this.countryCode = countryCode;
		this.location = location;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCountryCode() {
		return countryCode;
	}
	
	public Point getLocation() {
		return location;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
