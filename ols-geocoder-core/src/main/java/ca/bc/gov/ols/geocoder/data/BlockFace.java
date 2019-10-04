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

import ca.bc.gov.ols.enums.AddressScheme;
import ca.bc.gov.ols.geocoder.data.enumTypes.Side;

/**
 * BlockFace encapsulates the address range, address scheme, locality, and width for one side of a
 * street segment. A reference to the actual street centerline geometry is also included; this
 * geometry object is expected to be shared by both BlockFaces.
 * 
 * The width value is representative of the width of this side of the street, from the centerline to
 * the curb.
 * 
 * Note that BlockFace is intended to be immutable. The primaryStreetName is mutable only to allow
 * for construction of the circular reference between StreetNames and BlockFaces. This should be
 * eventually be resolved using a Builder class similar to the Site and SiteBuilder classes. The
 * LineString is also technically mutable but must not ever be modified.
 * 
 * @author chodgson
 * 
 */
public class BlockFace {
	
	private final StreetSegment segment;
	
	private final Side side;
	private final int first;
	private final int last;
	private final AddressScheme scheme;
	private final float numLanes;
	private final Locality locality;
	private final String electoralArea;
	private final CivicAccessPoint[] accessPoints;
	
	public BlockFace(
			StreetSegment segment,
			Side side,
			int first,
			int last,
			String addressScheme,
			float numLanes,
			Locality locality,
			String electoralArea,
			CivicAccessPoint[] accessPoints) {
		this.segment = segment;
		this.side = side;
		this.first = first;
		this.last = last;
		this.scheme = AddressScheme.convert(addressScheme);
		this.numLanes = numLanes;
		this.locality = locality;
		this.electoralArea = electoralArea;
		this.accessPoints = accessPoints;
		if(segment != null) {
			segment.setBlockFace(side, this);
		}
	}
	
	public float getNumLanes() {
		return numLanes;
	}
	
	public StreetSegment getSegment() {
		return segment;
	}
	
	public Side getSide() {
		return side;
	}
	
	public int getFirst() {
		return first;
	}
	
	public int getLast() {
		return last;
	}
	
	public AddressScheme getAddressScheme() {
		return scheme;
	}
	
	public Locality getLocality() {
		return locality;
	}

	public String getElectoralArea() {
		return electoralArea;
	}

	public CivicAccessPoint[] getAccessPoints() {
		return accessPoints;
	}
	
	/**
	 * Returns the larger of the two range values.
	 * 
	 * @return the larger value of the two range values
	 */
	public int getMax() {
		if(first > last) {
			return first;
		}
		return last;
	}
	
	/**
	 * Returns the smaller of the two range values.
	 * 
	 * @return the smaller value of the two range values
	 */
	public int getMin() {
		if(first < last) {
			return first;
		}
		return last;
	}
	
	/**
	 * Determines if the give address number is contained in the block face range and scheme.
	 * 
	 * @param addr the address number to check
	 * @return true of the address is within the range and parity of this block face, false
	 *         otherwise
	 */
	public boolean contains(int addr) {
		// if the scheme doesn't include the address, return false
		if((scheme == AddressScheme.EVEN && addr % 2 != 0)
				|| (scheme == AddressScheme.ODD && addr % 2 == 0)) {
			return false;
		}
		// otherwise return true as long as we are in the range
		if(addr >= getMin() && addr <= getMax()) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "BlockFace: " + side + " " + first + "-" + last + " " + scheme;
	}
	
}
