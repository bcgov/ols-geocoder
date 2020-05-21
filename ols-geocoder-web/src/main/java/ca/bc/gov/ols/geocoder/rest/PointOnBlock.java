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
package ca.bc.gov.ols.geocoder.rest;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.api.data.ModifiableLocation;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;

public class PointOnBlock implements ModifiableLocation {

	private int blockId;
	private Point point;
	private LocationDescriptor ld;
	
	public PointOnBlock(int blockId, Point point, LocationDescriptor ld) {
		this.blockId = blockId;
		this.point = point;
		this.ld = ld;
	}
	
	public int getBlockId() {
		return blockId;
	}
	
	@Override
	public Point getLocation() {
		return point;
	}
	
	public LocationDescriptor getLocationDescriptor() {
		return ld;
	}

	@Override
	public void setLocation(Point location) {
		this.point = location;
	}
}
