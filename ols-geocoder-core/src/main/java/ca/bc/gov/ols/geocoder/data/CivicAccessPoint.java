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

import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

/**
 * A CivicAccessPoint is an access point located at a civic address number on a street segment. Note
 * that the CivicAccessPoint object is intended to be immutable. A copy constructor is provided to
 * create a copy of the access point with a new location.
 * 
 * @author chodgson
 * 
 */
public class CivicAccessPoint extends AccessPoint {
	
	// used to sort sites based on their civicNumbers and suffixes
	public static final Comparator<CivicAccessPoint> CIVIC_NUMBER_COMPARATOR = new Comparator<CivicAccessPoint>() {
		@Override
		public int compare(CivicAccessPoint ap1, CivicAccessPoint ap2) {
			if(ap1.civicNumber < ap2.civicNumber) {
				return -1;
			}
			if(ap1.civicNumber > ap2.civicNumber) {
				return 1;
			}
			if((ap1.civicNumberSuffix == null || ap1.civicNumberSuffix.isEmpty())
					&& (ap2.civicNumberSuffix == null || ap2.civicNumberSuffix.isEmpty())) {
				return 0;
			}
			if(ap1.civicNumberSuffix == null || ap1.civicNumberSuffix.isEmpty()) {
				return -1;
			}
			if(ap2.civicNumberSuffix == null || ap2.civicNumberSuffix.isEmpty()) {
				return 1;
			}
			return ap1.civicNumberSuffix.compareTo(ap2.civicNumberSuffix);
		}
	};
	
	private final int civicNumber;
	private final String civicNumberSuffix;
	private BlockFace blockFace;
	
	public CivicAccessPoint(ISite site, int civicNumber, String civicNumberSuffix,
			Point point, PositionalAccuracy positionalAccuracy,
			String narrativeLocation) {
		super(site, point, positionalAccuracy,
				narrativeLocation);
		this.civicNumber = civicNumber;
		this.civicNumberSuffix = civicNumberSuffix;
	}
	
	public CivicAccessPoint(int civicNumber, String civicNumberSuffix) {
		super(null, null, null, null);
		this.civicNumber = civicNumber;
		this.civicNumberSuffix = civicNumberSuffix;
	}
	
	/**
	 * To create a copy of this CivicAccessPoint with a different point location. The core data is
	 * immutable; so to use a CivicAccessPoint for a dynamically created (interpolated) location,
	 * this is needed.
	 * 
	 * @param cap the CivicAccessPoint to make a copy of
	 * @param point the new location
	 * @param positionalAccuracy the new accuracy
	 */
	protected CivicAccessPoint(CivicAccessPoint cap, Point point, PositionalAccuracy positionalAccuracy) {
		super(cap, point, positionalAccuracy);
		this.civicNumber = cap.civicNumber;
		this.civicNumberSuffix = cap.civicNumberSuffix;
	}
	
	public int getCivicNumber() {
		return civicNumber;
	}
	
	public String getCivicNumberSuffix() {
		return civicNumberSuffix;
	}
	
	public BlockFace getBlockFace() {
		return blockFace;
	}
	
	public void setBlockFace(BlockFace face) {
		this.blockFace = face;
	}
	
}
