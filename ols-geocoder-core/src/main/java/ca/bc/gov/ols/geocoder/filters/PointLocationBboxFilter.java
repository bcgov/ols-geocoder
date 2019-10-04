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
package ca.bc.gov.ols.geocoder.filters;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import ca.bc.gov.ols.geocoder.data.ILocation;

public class PointLocationBboxFilter<T extends ILocation> implements Filter<T> {
	
	private final double minx, maxx, miny, maxy;
	
	public PointLocationBboxFilter(Polygon bbox) {
		Coordinate[] coords = bbox.getCoordinates();
		minx = Math.min(coords[0].x, coords[2].x);
		maxx = Math.max(coords[0].x, coords[2].x);
		miny = Math.min(coords[0].y, coords[2].y);
		maxy = Math.max(coords[0].y, coords[2].y);
	}

	public PointLocationBboxFilter(Point centre, double radius) {
		minx = centre.getX() - radius;
		maxx = centre.getX() + radius;
		miny = centre.getY() - radius;
		maxy = centre.getY() + radius;
	}

	@Override
	public boolean pass(ILocation item) {
		Point p = item.getLocation();
		if(p.getX() >= minx && p.getX() <= maxx
				&& p.getY() >= miny && p.getY() <= maxy) {
			return true;
		}
		return false;
	}
	
}
