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

import java.util.Comparator;

import org.locationtech.jts.geom.Point;

public interface Location {
	
	public static final class DistanceComparator implements Comparator<Location> {
		private Point p;
		
		public DistanceComparator(Point p) {
			this.p = p;
		}
		
		/**
		 * Compares by the distance from the specified point
		 * 
		 * @param m1 the first match to compare
		 * @param m2 the second match to compare
		 * @return the value 0 if m1 and m2 are equally distant from the given point; a value less
		 *         than 0 if m1 is closer to the point than m2; and a value greater than 0 if m2 is
		 *         closer to the point than m1.
		 */
		@Override
		public int compare(Location m1, Location m2) {
			Point m1Loc = m1.getLocation();
			Point m2Loc = m2.getLocation();
			
			// if either are null we will call them equal; should not be any nulls
			if(m1Loc == null || m2Loc == null) {
				return 0;
			}
			return Double.compare(p.distance(m1Loc) - p.distance(m2Loc), 0);
		}
	}

	Point getLocation();
	
}