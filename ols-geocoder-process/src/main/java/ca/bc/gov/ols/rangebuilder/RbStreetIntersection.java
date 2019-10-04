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
package ca.bc.gov.ols.rangebuilder;

import org.locationtech.jts.geom.Point;

public class RbStreetIntersection {
	
	private final int id;
	private final Point location;
	
	public RbStreetIntersection(int id, Point location) {
		this.id = id;
		this.location = location;
	}
	
	public int getId() {
		return id;
	}
	
	public Point getLocation() {
		return location;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RbStreetIntersection
				&& this.id == ((RbStreetIntersection)obj).id) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
}
