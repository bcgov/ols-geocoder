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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.enums.DividerType;
import ca.bc.gov.ols.enums.LaneRestriction;
import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.TravelDirection;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.Side;
import ca.bc.gov.ols.geocoder.data.indexing.SpatiallyIndexable;

public class StreetSegment implements SpatiallyIndexable {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ StreetSegment.class.getCanonicalName());
	
	private final int segmentId;
	private final LineString centerLine;
	private final RoadClass roadClass;
	private final LaneRestriction laneRestriction;
	private final TravelDirection travelDirection;
	private final DividerType dividerType;
	private BlockFace leftFace;
	private BlockFace rightFace;
	
	// we are being a little tricky here - at the time of construction,
	// we only know the street name IDs, as the StreetName objects haven't
	// been made yet - but at run time we want the actual StreetName object
	// so in order to save a little space, we re-use these fields for both purposes
	private Object primaryStreetName;
	private ArrayList<Object> aliasNames;
	
	public StreetSegment(int segmentId, LineString centerLine, RoadClass roadClass,
			LaneRestriction laneRestriction, TravelDirection travelDirection,
			DividerType dividerType) {
		this.segmentId = segmentId;
		this.centerLine = centerLine;
		this.roadClass = roadClass;
		this.laneRestriction = laneRestriction;
		this.travelDirection = travelDirection;
		this.dividerType = dividerType;
		this.aliasNames = new ArrayList<Object>();
	}
	
	public int getSegmentId() {
		return segmentId;
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
		return travelDirection.isOneWay();
	}
	
	public TravelDirection getTravelDirection() {
		return travelDirection;
	}
	
	public DividerType getDividerType() {
		return dividerType;
	}
	
	public BlockFace getBlockFace(Side side) {
		if(Side.LEFT == side) {
			return leftFace;
		}
		if(Side.RIGHT == side) {
			return rightFace;
		}
		return null;
	}
	
	public void setBlockFace(Side side, BlockFace face) {
		if(getBlockFace(side) != null) {
			logger.error("Attempt to modify immutable StreetSegment.face." + side
					+ " on segment Id: " + segmentId);
		} else {
			if(Side.LEFT == side) {
				leftFace = face;
			} else if(Side.RIGHT == side) {
				rightFace = face;
			}
		}
	}
	
	public StreetName getPrimaryStreetName() {
		if(primaryStreetName instanceof StreetName) {
			return (StreetName)primaryStreetName;
		}
		return null;
	}
	
	public void setPrimaryStreetNameId(Integer primaryNameId) {
		this.primaryStreetName = primaryNameId;
	}

	public List<Object> getAliasNames() {
		return Collections.unmodifiableList(aliasNames);
	}
	
	public void addAliasNameId(Integer aliasNameId) {
		aliasNames.add(aliasNameId);
	}

	public void resolveName(int streetNameId, StreetName streetName) {
		// check if the name is either a primary or alias
		// and replace the Integer with the StreetName
		if(primaryStreetName instanceof Integer
				&& streetNameId == (Integer)primaryStreetName) {
			this.primaryStreetName = streetName;
		} else { 
			for(int i = 0; i < aliasNames.size(); i++) {
				if(aliasNames.get(i) instanceof Integer
						&& streetNameId == (Integer)(aliasNames.get(i))) {
					aliasNames.set(i, streetName);
				}
			}
		}
		aliasNames.trimToSize();
	}

	@Override
	public Envelope getEnvelope() {
		return centerLine.getEnvelopeInternal();
	}

	@Override
	public double distance(Point p) {
		return centerLine.distance(p);
	}

//	public void trimToSize() {
//		for(Object aliasName : aliasNames) {
//			if(aliasName instanceof Integer) {
//				logger.warn("AliasNameId: " + aliasName + " was not resolved on segmentId: " + segmentId);
//			}
//		}
//		aliasNames.trimToSize();
//	}

}
