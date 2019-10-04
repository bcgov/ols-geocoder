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
package ca.bc.gov.ols.geocoder.data.indexing;

import java.util.Comparator;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.data.ILocation;

public class PointLocationComparator implements Comparator<ILocation> {
	
	Axis axis;
	
	public PointLocationComparator(Axis axis) {
		this.axis = axis;
	}
	
	@Override
	public int compare(ILocation p1, ILocation p2) {
		return compareAxis(p1.getLocation(), p2.getLocation(), axis);
	}
	
	public static int compareAxis(Point p1, Point p2, Axis axis) {
		double diff = diffAxis(p1, p2, axis);
		if(diff < 0) {
			return -1;
		} else if(diff > 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public static double diffAxis(Point p1, Point p2, Axis axis) {
		if(axis == Axis.X) {
			return p1.getX() - p2.getX();
		} else {
			return p1.getY() - p2.getY();
		}
	}
	
}
