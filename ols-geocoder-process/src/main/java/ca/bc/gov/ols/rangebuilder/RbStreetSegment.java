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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.enums.AddressScheme;
import ca.bc.gov.ols.enums.DividerType;
import ca.bc.gov.ols.enums.LaneRestriction;
import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.TravelDirection;
import ca.bc.gov.ols.geocoder.data.enumTypes.Side;
import ca.bc.gov.ols.util.ArraySet;

public class RbStreetSegment {
	private final static Logger logger = LoggerFactory.getLogger(RbStreetSegment.class.getCanonicalName());
	
	private final int segmentId;
	private final int startIntersectionId;
	private final int endIntersectionId;
	private int primaryStreetNameId;
	private final LineString centerLine;
	private final Map<Side, Integer> localities = new EnumMap<Side, Integer>(Side.class);
	private final Map<Side, Integer> electoralAreas = new EnumMap<Side, Integer>(Side.class);
	private final Map<Side, RbBlockFace> faces = new EnumMap<Side, RbBlockFace>(Side.class);
	private final Map<Side, List<IRbSite>> rbSites = new EnumMap<Side, List<IRbSite>>(Side.class);
	private final List<IRbSite> ncaps = new ArrayList<IRbSite>();
	private final Map<Side, Integer> numLanes = new EnumMap<Side, Integer>(Side.class);
	private final RoadClass roadClass;
	private final LaneRestriction laneRestriction;
	private final TravelDirection travelDirection;
	private final DividerType dividerType;
	private int sideSwapCount = 0;
		
	// a list of all the localityId:streetNameId combinations that this segment is in
	// includes aliases and both sides of the street
	// used to prevent creating overlapping ranges while mirroring
	private Set<String> streetFaceRefs = new ArraySet<String>();
	
	public RbStreetSegment(int segmentId, int startIntersectionId, int endIntersectionId, 
			LineString centerLine, int localityLeftId, int localityRightId, Integer eaLeftId, Integer eaRightId,
			RoadClass roadClass, LaneRestriction laneRestriction, TravelDirection travelDirection,
			DividerType dividerType, int numLanesLeft, int numLanesRight) {
		this.segmentId = segmentId;
		this.startIntersectionId = startIntersectionId;
		this.endIntersectionId = endIntersectionId;
		this.centerLine = centerLine;
		this.localities.put(Side.LEFT, localityLeftId);
		this.localities.put(Side.RIGHT, localityRightId);
		this.electoralAreas.put(Side.LEFT, eaLeftId);
		this.electoralAreas.put(Side.RIGHT, eaRightId);
		rbSites.put(Side.LEFT, new ArrayList<IRbSite>());
		rbSites.put(Side.RIGHT, new ArrayList<IRbSite>());
		numLanes.put(Side.LEFT, numLanesLeft);
		numLanes.put(Side.RIGHT, numLanesRight);
		
		setFace(new RbBlockFace(this, Side.LEFT, null, RangeBuilder.NULL_ADDR,
				RangeBuilder.NULL_ADDR));
		setFace(new RbBlockFace(this, Side.RIGHT, null, RangeBuilder.NULL_ADDR,
				RangeBuilder.NULL_ADDR));
		this.roadClass = roadClass;
		this.laneRestriction = laneRestriction;
		this.travelDirection = travelDirection;
		this.dividerType = dividerType;
	}
	
	public int getSegmentId() {
		return segmentId;
	}

	public int getStartIntersectionId() {
		return startIntersectionId;
	}

	public int getEndIntersectionId() {
		return endIntersectionId;
	}

	public int getPrimaryStreetNameId() {
		return primaryStreetNameId;
	}
	
	public void setPrimaryStreetNameId(int primaryStreetNameId) {
		this.primaryStreetNameId = primaryStreetNameId;
	}
	
	public LineString getCenterLine() {
		return centerLine;
	}
	
	public RoadClass getRoadClass() {
		return roadClass;
	}
	
	public LaneRestriction getLaneRestriction() {
		return laneRestriction;
	}
	
	public boolean isOneWay() {
		return !travelDirection.equals(TravelDirection.BIDIRECTIONAL);
	}
	
	public TravelDirection getTravelDirection() {
		return travelDirection;
	}
	
	public DividerType getDividerType() {
		return dividerType;
	}
	
	public RbBlockFace getFace(Side side) {
		return faces.get(side);
	}
	
	public void setFace(RbBlockFace newFace) {
		faces.put(newFace.getSide(), newFace);
	}
	
	public int getFromLeft() {
		return faces.get(Side.LEFT).getFirst();
	}
	
	public int getToLeft() {
		return faces.get(Side.LEFT).getLast();
	}
	
	public int getFromRight() {
		return faces.get(Side.RIGHT).getFirst();
	}
	
	public int getToRight() {
		return faces.get(Side.RIGHT).getLast();
	}
	
	public AddressScheme getParityLeft() {
		return faces.get(Side.LEFT).getAddressScheme();
	}
	
	public AddressScheme getParityRight() {
		return faces.get(Side.RIGHT).getAddressScheme();
	}
	
	public int getLocalityId(Side side) {
		return localities.get(side);
	}

	public Integer getElectoralAreaId(Side side) {
		return electoralAreas.get(side);
	}

	public float getNumLanes(Side side) {
		return numLanes.get(side);
	}
	
	public int getAddr(String addrName) {
		if("froml".equals(addrName)) {
			return getFromLeft();
		} else if("tol".equals(addrName)) {
			return getToLeft();
		} else if("fromr".equals(addrName)) {
			return getFromRight();
		} else if("tor".equals(addrName)) {
			return getToRight();
		} else {
			throw new IllegalArgumentException("'" + addrName
					+ "' is not a valid address range name.");
		}
	}
	
	public void setAddr(String addrName, int value) {
		if("froml".equals(addrName)) {
			faces.get(Side.LEFT).setFirst(value);
		} else if("tol".equals(addrName)) {
			faces.get(Side.LEFT).setLast(value);
		} else if("fromr".equals(addrName)) {
			faces.get(Side.RIGHT).setFirst(value);
		} else if("tor".equals(addrName)) {
			faces.get(Side.RIGHT).setLast(value);
		} else {
			throw new IllegalArgumentException("'" + addrName
					+ "' is not a valid address range name.");
		}
	}
	
	public AddressScheme getParity(String side) {
		if("l".equals(side)) {
			return faces.get(Side.LEFT).getAddressScheme();
		} else if("r".equals(side)) {
			return faces.get(Side.RIGHT).getAddressScheme();
		} else {
			throw new IllegalArgumentException("'" + side + "' is not a valid side name.");
		}
	}
	
	public void setParity(String side, AddressScheme parity) {
		if("l".equals(side)) {
			faces.get(Side.LEFT).setAddressScheme(parity);
		} else if("r".equals(side)) {
			faces.get(Side.RIGHT).setAddressScheme(parity);
		} else {
			throw new IllegalArgumentException("'" + side + "' is not a valid side name.");
		}
	}
	
	public List<IRbSite> getSites(Side side) {
		return rbSites.get(side);
	}

	public List<IRbSite> getNCAPs() {
		return ncaps;
	}
	
	public boolean isIncreasing(Side side) {
		RbBlockFace face = faces.get(side);
		return face.getLast() > face.getFirst();
	}
	
	public boolean isIndeterminate(Side side) {
		RbBlockFace face = faces.get(side);
		return face.getLast() == face.getFirst();
	}
	
	public int forceParity(RangeBuilder rb) {
		int startSites = rbSites.get(Side.LEFT).size() + rbSites.get(Side.RIGHT).size();
		int leftEvenCount = 0;
		int leftOddCount = 0;
		int rightEvenCount = 0;
		int rightOddCount = 0;
		List<IRbSite> evenSites = new ArrayList<IRbSite>();
		List<IRbSite> oddSites = new ArrayList<IRbSite>();
		
		for(IRbSite rbSite : rbSites.get(Side.LEFT)) {
			if(rbSite.getCivicNumber() % 2 == 0) {
				leftEvenCount++;
				evenSites.add(rbSite);
			} else {
				leftOddCount++;
				oddSites.add(rbSite);
			}
		}
		for(IRbSite rbSite : rbSites.get(Side.RIGHT)) {
			if(rbSite.getCivicNumber() % 2 == 0) {
				rightEvenCount++;
				evenSites.add(rbSite);
			} else {
				rightOddCount++;
				oddSites.add(rbSite);
			}
		}
		
		// we assign the even and odd sides based on which assignment will require moving fewer
		// addresses
		if(leftEvenCount + rightOddCount > leftOddCount + rightEvenCount) {
			rbSites.put(Side.LEFT, evenSites);
			rbSites.put(Side.RIGHT, oddSites);
			faces.get(Side.LEFT).setAddressScheme(AddressScheme.EVEN);
			faces.get(Side.RIGHT).setAddressScheme(AddressScheme.ODD);
			sideSwapCount = leftOddCount + rightEvenCount;
		} else if(leftEvenCount + rightOddCount < leftOddCount + rightEvenCount) {
			rbSites.put(Side.LEFT, oddSites);
			rbSites.put(Side.RIGHT, evenSites);
			faces.get(Side.LEFT).setAddressScheme(AddressScheme.ODD);
			faces.get(Side.RIGHT).setAddressScheme(AddressScheme.EVEN);
			sideSwapCount = leftEvenCount + rightOddCount;
		} else {
			if(leftEvenCount > 0 && leftOddCount == 0) {
				faces.get(Side.LEFT).setAddressScheme(AddressScheme.EVEN);
			} else if(leftOddCount > 0 && leftEvenCount == 0) {
				faces.get(Side.LEFT).setAddressScheme(AddressScheme.ODD);
			} else if(leftEvenCount + leftOddCount > 0) {
				logger.debug("unable to force parity on left side of segment " + segmentId);
				rb.unforceableParityCount++;
				faces.get(Side.LEFT).setAddressScheme(AddressScheme.CONTINUOUS);
			}
			if(rightEvenCount > 0 && rightOddCount == 0) {
				faces.get(Side.RIGHT).setAddressScheme(AddressScheme.EVEN);
			} else if(rightOddCount > 0 && rightEvenCount == 0) {
				faces.get(Side.RIGHT).setAddressScheme(AddressScheme.ODD);
			} else if(rightEvenCount + rightOddCount > 0) {
				logger.debug("unable to force parity on right side of segment " + segmentId);
				rb.unforceableParityCount++;
				faces.get(Side.RIGHT).setAddressScheme(AddressScheme.CONTINUOUS);
			}
		}
		int endSites = rbSites.get(Side.LEFT).size() + rbSites.get(Side.RIGHT).size();
		if(startSites != endSites) {
			logger.error("startSites(" + startSites + ") != endSites(" + endSites + ")");
		}
		if(sideSwapCount > 0) {
			logger.debug("Sideswaps: {} on segment {}", sideSwapCount, segmentId);
		}
		return sideSwapCount;
	}
	
	public void setRangesFromSites() {
		for(Side side : Side.values()) {
			List<IRbSite> sites = rbSites.get(side);
			if(sites.isEmpty()) {
				continue;
			}
			// sort and find min and max civic numbers
			Collections.sort(sites, IRbSite.ADDRESS_COMPARATOR);
			IRbSite minAddr = sites.get(0);
			IRbSite maxAddr = sites.get(sites.size()-1);
			
			// resolve froms and tos based on relative position of mins and maxes
			// if the measures of the min and max civic numbers are the same (eg. a block with only building)
			if(minAddr.getMeasure() == maxAddr.getMeasure()) {
				// then use the relative civic numbers of the sites with max and min measures
				IRbSite shortAddr = Collections.min(sites, IRbSite.MEASURE_COMPARATOR);
				IRbSite longAddr = Collections.max(sites, IRbSite.MEASURE_COMPARATOR);
				if(shortAddr.getCivicNumber() < longAddr.getCivicNumber()) {
					faces.get(side).setFirst(minAddr.getCivicNumber());
					faces.get(side).setLast(maxAddr.getCivicNumber());
				} else {
					faces.get(side).setFirst(maxAddr.getCivicNumber());
					faces.get(side).setLast(minAddr.getCivicNumber());					
				}
			} else if(minAddr.getMeasure() < maxAddr.getMeasure()) {
				faces.get(side).setFirst(minAddr.getCivicNumber());
				faces.get(side).setLast(maxAddr.getCivicNumber());
			} else {
				faces.get(side).setFirst(maxAddr.getCivicNumber());
				faces.get(side).setLast(minAddr.getCivicNumber());
			}
			// check that the faces are either valid or empty
			RbBlockFace face = faces.get(side);
			if(!face.isValid()
					&& (face.getFirst() != RangeBuilder.NULL_ADDR || face.getLast() != RangeBuilder.NULL_ADDR)) {
				logger.error("Some but not all values set on blockface: " + face.toString());
			}
		}
	}
	
	public int getMedianAddr(Side side) {
		List<IRbSite> sites = rbSites.get(side);
		if(sites.isEmpty()) {
			return RangeBuilder.NULL_ADDR;
		}
		return sites.get(sites.size()/2).getCivicNumber();
	}

	
	public Set<String> getStreetFaceRefs() {
		return streetFaceRefs;
	}
	
	public boolean addStreetFaceRef(String ref) {
		return streetFaceRefs.add(ref);
	}

	public float getHalfTotalLanes() {
		float totalLanes = numLanes.get(Side.LEFT) + numLanes.get(Side.RIGHT);
		return (float) (totalLanes / 2.0);
	}

	public String toString() {
		return "Seg(" + faces.get(Side.RIGHT) + "," + faces.get(Side.LEFT)+ ")";
	}

}
