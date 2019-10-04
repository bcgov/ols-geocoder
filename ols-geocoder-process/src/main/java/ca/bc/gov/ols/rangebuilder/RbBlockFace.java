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

import ca.bc.gov.ols.enums.AddressScheme;
import ca.bc.gov.ols.geocoder.data.enumTypes.Side;

public class RbBlockFace {
	
	private final RbStreetSegment segment;
	private final Side side;
	private AddressScheme parity;
	private int from = RangeBuilder.NULL_ADDR;
	private int to = RangeBuilder.NULL_ADDR;
	
	public RbBlockFace(
			RbStreetSegment segment,
			Side side,
			AddressScheme parity,
			int from,
			int to) {
		this.segment = segment;
		this.side = side;
		this.parity = parity;
		this.from = from;
		this.to = to;
	}
	
	public RbStreetSegment getSegment() {
		return segment;
	}
	
	public Side getSide() {
		return side;
	}
	
	public int getFirst() {
		return from;
	}
	
	public int getLast() {
		return to;
	}
	
	public void setFirst(int value) {
		this.from = value;
	}
	
	public void setLast(int value) {
		this.to = value;
	}
	
	public AddressScheme getAddressScheme() {
		return this.parity;
	}
	
	/**
	 * Returns the larger of the two range values.
	 * 
	 * @return the larger value of the two range values
	 */
	public int getMax() {
		if(getFirst() > getLast()) {
			return getFirst();
		}
		return getLast();
	}
	
	/**
	 * Returns the smaller of the two range values.
	 * 
	 * @return the smaller value of the two range values
	 */
	public int getMin() {
		if(getFirst() < getLast()) {
			return getFirst();
		}
		return getLast();
	}
	
	public void setAddressScheme(AddressScheme scheme) {
		this.parity = scheme;
	}
	
	public void setMax(int i) {
		if(!getAddressScheme().includes(i)) {
			i--;
		}
		if(getFirst() > getLast()) {
			setFirst(i);
			// Integer old = segment.getAddr("from" + side.toChar());
			// segment.setAddr("from" + side.toChar(), first);
			// if(segment.has("from" + side.toChar() + "fixed")) {
			// Integer oldFixed = (Integer)segment.get("from" + side.toChar() + "fixed");
			// if(oldFixed == null) {
			// oldFixed = 0;
			// }
			// segment.set("from" + side.toChar() + "fixed", oldFixed + (first - old));
			// }
		} else {
			setLast(i);
			// Integer old = segment.getAddr("to" + side.toChar());
			// segment.setAddr("to" + side.toChar(), last);
			// if(segment.has("to" + side.toChar() + "fixed")) {
			// Integer oldFixed = (Integer)segment.get("to" + side.toChar() + "fixed");
			// if(oldFixed == null) {
			// oldFixed = 0;
			// }
			// segment.set("to" + side.toChar() + "fixed", oldFixed + (last - old));
			// }
		}
	}
	
	public void setMin(int i) {
		if(!getAddressScheme().includes(i)) {
			i++;
		}
		if(getFirst() < getLast()) {
			setFirst(i);
			// int old = segment.getAddr("from" + side.toChar());
			// segment.setAddr("from" + side.toChar(), first);
			// if(segment.has("from" + side.toChar() + "fixed")) {
			// Integer oldFixed = (Integer)segment.get("from" + side.toChar() + "fixed");
			// if(oldFixed == null) {
			// oldFixed = 0;
			// }
			// segment.set("from" + side.toChar() + "fixed", oldFixed + (old - first));
			// }
		} else {
			setLast(i);
			// Integer old = (Integer)segment.get("to" + side.toChar());
			// segment.setAddr("to" + side.toChar(), last);
			// if(segment.has("to" + side.toChar() + "fixed")) {
			// Integer oldFixed = (Integer)segment.get("to" + side.toChar() + "fixed");
			// if(oldFixed == null) {
			// oldFixed = 0;
			// }
			// segment.set("to" + side.toChar() + "fixed", oldFixed + (old - last));
			// }
		}
	}
	
	public void flip() {
		int oldFrom = from;
		from = to;
		to = oldFrom;
	}

	/**
	 * Determines if the given RbSite's address number is contained in the block face range and
	 * scheme.
	 * 
	 * @param site the site with the address number to check
	 * @return true of the address is within the range and parity of this block face, false
	 *         otherwise
	 */
	public boolean contains(IRbSite site) {
		int addr = site.getCivicNumber();
		// if the addr is the right parity and in the range
		if(isValid() && getAddressScheme().includes(addr) && addr >= getMin() && addr <= getMax()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Determines if the given BlockFace's range and scheme overlaps with this block face's range
	 * and scheme.
	 * 
	 * @param face the BlockFace to compare to
	 * @return true if the given BlockFace's address range overlaps with the range and parity of
	 *         this block face, false otherwise
	 */
	public boolean overlaps(RbBlockFace face) {
		// if either face isn't valid it can't overlap anything
		if(!isValid() || !face.isValid()) {
			return false;
		}
		// if the schemes are equal or either are continuous,
		// and the face is not entirely below or entirely above this one
		if((getAddressScheme().equals(face.getAddressScheme())
				|| getAddressScheme().equals(AddressScheme.CONTINUOUS)
				|| face.getAddressScheme().equals(AddressScheme.CONTINUOUS))
				&& face.getMin() <= getMax()
				&& face.getMax() >= getMin()) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Face(" + side + " " + getFirst() + "-"
				+ getLast() + " " + getAddressScheme() + ")";
	}

	public String toLongString() {
		StringBuilder sb = new StringBuilder("BlockFace: " + segment.getSegmentId() + " " + side + " " + getFirst() + "-"
				+ getLast() + " " + getAddressScheme() + " (");
		boolean first = true;
		for(IRbSite site : getSegment().getSites(side)) {
			if(!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(site.getCivicNumber() );
		}
		sb.append(")");
		return sb.toString();
	}

	public void empty() {
		setFirst(RangeBuilder.NULL_ADDR);
		setLast(RangeBuilder.NULL_ADDR);
		setAddressScheme(null);
		// segment.setAddr("from" + side.toChar(), 0);
		// segment.setAddr("to" + side.toChar(), 0);
		// segment.setAddr("from" + side.toChar() + "fixed", -999999);
		// segment.setAddr("to" + side.toChar() + "fixed", -999999);
		// segment.setParity(side.toChar(), null);
	}
	
	public boolean isValid() {
		if(getAddressScheme() == null
				|| getFirst() == RangeBuilder.NULL_ADDR
				|| getLast() == RangeBuilder.NULL_ADDR) {
			return false;
		}
		return true;
	}
}
