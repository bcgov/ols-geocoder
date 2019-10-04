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

import org.locationtech.jts.geom.Envelope;

public class AxisSorter implements Comparator<SpatiallyIndexable> {
	private Axis axis;
	public static final AxisSorter X = new AxisSorter(Axis.X);
	public static final AxisSorter Y = new AxisSorter(Axis.Y);
	
	public AxisSorter(Axis axis) {
		this.axis = axis;
	}
	
	/**
	 * sorts by the center of the envelope along the specified axis
	 * 
	 * @param b1 the first bound to compare
	 * @param b2 the second bound to compare
	 * @return the value 0 if b1 and b2 are have the same center along the specified axis;
	 *         a value less than 0 if b1's center is less than (left or below) b2's center;
	 *         or a value greater than 0 if b1's center greater than (right or above) b2's center.
	 */
	@Override
	public int compare(SpatiallyIndexable b1, SpatiallyIndexable b2) {
		if(b1 == null && b2 == null) {
			return 0;
		}
		if(b1 == null) {
			return 1;
		}
		if(b2 == null) {
			return -1;
		}
		Envelope e1 = b1.getEnvelope();
		Envelope e2 = b2.getEnvelope();
		if(axis.equals(Axis.X)) {
			return Double.compare(e1.getMinX() + e1.getMaxX(), e2.getMinX() + e2.getMaxX());
		} else {
			return Double.compare(e1.getMinY() + e1.getMaxY(), e2.getMinY() + e2.getMaxY());
		}
	}
	
}