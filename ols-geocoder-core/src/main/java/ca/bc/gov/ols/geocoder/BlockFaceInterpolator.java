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
package ca.bc.gov.ols.geocoder;

import ca.bc.gov.ols.enums.LaneRestriction;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.BlockFace;
import ca.bc.gov.ols.geocoder.data.CivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.ResultCivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.StreetSegment;
import ca.bc.gov.ols.geocoder.data.enumTypes.Interpolation;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.geocoder.data.enumTypes.Side;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.linearref.LocationIndexedLine;

/**
 * Handles interpolating an address along a BlockFace.
 * 
 * @author chodgson@refractions.net
 * 
 */
public class BlockFaceInterpolator {
	private final GeocoderConfig config;
	private final GeometryFactory geometryFactory;
	
	public BlockFaceInterpolator(GeocoderConfig config, GeometryFactory geometryFactory) {
		this.config = config;
		this.geometryFactory = geometryFactory;
	}
	
	public ResultCivicAccessPoint interpolate(BlockFace face, CivicAccessPoint input,
			GeocodeQuery query) {
		// for routingPoint always use -1 for setBack
		int setBack = LocationDescriptor.ROUTING_POINT.equals(query.getLocationDescriptor()) ? -1
				: query.getSetBack();
		return interpolate(face, input, query.getInterpolation(), setBack);
	}
	
	public ResultCivicAccessPoint interpolate(BlockFace face, CivicAccessPoint input,
			Interpolation interp, int setBack) {
		CivicAccessPoint result = null;
		Point resultPoint = null;
		PositionalAccuracy resultAccuracy = PositionalAccuracy.LOW;
		boolean isInterpolated = false;
		
		// fetch the access points if doing adaptive interpolation
		CivicAccessPoint[] aps = null;
		if(interp.equals(Interpolation.ADAPTIVE) || interp.equals(Interpolation.NONE)) {
			aps = face.getAccessPoints();
		}
		// do a binary search through the APs to find either the exact match or the best bounds
		// indexes of the upper and lower bounding APs in the list
		// bounds outside of the range of the list mean there is no lower- or higher-address AP
		int lowBound = -1;
		int highBound = 0;
		// if the list is null or empty the binary search bails out safely
		if(aps != null) {
			highBound = aps.length;
		}
		int index;
		while(true) {
			index = (int)Math.floor((lowBound + highBound) / 2d);
			if(index == lowBound) {
				// no exact match but we've found our bounds
				break;
			}
			int comparison = CivicAccessPoint.CIVIC_NUMBER_COMPARATOR.compare(aps[index], input);
			if(comparison == 0) {
				// found an exact match
				// no need to recalculate the setback if it is 0 (curb)
				if(setBack == 0) {
					setBack = -1;
				}
				result = aps[index];
				resultPoint = aps[index].getPoint();
				resultAccuracy = result.getPositionalAccuracy();
				break;
			} else if(comparison > 0) {
				// need to go lower
				highBound = index;
			} else if(comparison < 0) {
				// need to go higher
				lowBound = index;
			}
		}
		
		// at this point we either have a result AP, or valid bounds
		// if we don't have a result AP we need to use the bounds to interpolate an AP
		if(result == null) {
			// but don't bother to interpolate if we're not allowed to
			if(interp.equals(Interpolation.NONE)) {
				return null;
			}
			isInterpolated = true;
			LineString centerLine = face.getSegment().getCenterLine();
			LengthIndexedLine il = new LengthIndexedLine(centerLine);
			double length = centerLine.getLength();
			double width = getWidth(face);
			double lowDist = width;
			double highDist = length - width;
			// if the addresses are decreasing along the length of the line
			if(face.getFirst() > face.getLast()) {
				// reverse the distances
				lowDist = highDist ;
				highDist = width;
			}
			double minAddr = face.getMin();
			double maxAddr = face.getMax();
			if(aps != null) {
				// skip over any lowerAPs without point locations
				while(lowBound > -1 && aps[lowBound].getPoint() == null) {
					lowBound--;
				}
				if(lowBound > -1) {
					lowDist = il.project(aps[lowBound].getPoint().getCoordinate());
					minAddr = aps[lowBound].getCivicNumber();
				}
				// skip over any higher APs without point locations
				while(highBound < aps.length && aps[highBound].getPoint() == null) {
					highBound++;
				}
				if(highBound < aps.length) {
					highDist = il.project(aps[highBound].getPoint().getCoordinate());
					maxAddr = aps[highBound].getCivicNumber();
				}
			}
			double a = input.getCivicNumber() - minAddr;
			double b = maxAddr - minAddr;
			double c = highDist - lowDist;
			double distAlong = lowDist + (a / b * c);
			
			Coordinate coord = il.extractPoint(distAlong);
			
			result = input;
			resultPoint = geometryFactory.createPoint(coord);
		}
		
		if(setBack >= 0) {
			// now we need to offset the result AP as per the request
			resultPoint = applyOffset(resultPoint, face, setBack);
		}
		// clone the access point so we don't clobber the location of the access point
		return new ResultCivicAccessPoint(result, resultPoint, resultAccuracy, isInterpolated);
	}
	
	public Point applyOffset(Point point, BlockFace face, int setBack) {
		// now we need to offset the result AP as per the request
		if(face.getSide() == Side.LEFT || face.getSide() == Side.RIGHT) {
			double distance = 0;
			if(setBack >= 0) {
				distance = getWidth(face) + setBack;
			} else if(setBack == Integer.MIN_VALUE) {
				distance = config.getBlockFaceOffset();
			}
			if(distance > 0) {
				LocationIndexedLine locil = new LocationIndexedLine(face.getSegment().getCenterLine());
				LineSegment localSeg = locil.indexOf(point.getCoordinate()).getSegment(
						face.getSegment().getCenterLine());
				Point offsetPoint = project(localSeg, point, distance,
						face.getSide() == Side.RIGHT, geometryFactory);
				return offsetPoint;
			}
		}
		return point;
	}
	
	/**
	 * Calculates the width from the centerline to the curb as per jTrac-426
	 * 
	 * @param face the BlockFace of which to calculate the width
	 * @return the calculated width from the centerline to the curb
	 */
	private float getWidth(BlockFace face) {
		StreetSegment seg = face.getSegment();
		float laneWidth = config.getRoadLaneWidth(seg.getRoadClass());
		float baseWidth = config.getRoadBaseWidth(seg.getRoadClass());
		float dividerWidth = config.getRoadDividerWidth(seg.getDividerType());
		float narrowMultiplier = 1;
		if(LaneRestriction.NARROW.equals(seg.getLaneRestriction())) {
			narrowMultiplier = config.getRoadNarrowMultiplier();
		}
		if(face.getSegment().isOneWay()) {
			return baseWidth + (face.getNumLanes() * laneWidth * narrowMultiplier);
		} else {
			return baseWidth + (face.getNumLanes() * laneWidth * narrowMultiplier)
					+ (dividerWidth / 2);
		}
	}
	
	public static Point project(LineSegment seg, Point p, double distance, Boolean projectRight,
			GeometryFactory gf) {
		double x1 = seg.p0.x;
		double y1 = seg.p0.y;
		double x2 = seg.p1.x;
		double y2 = seg.p1.y;
		
		double vx = x2 - x1;
		double vy = y2 - y1;
		
		// compute unit vector
		double sqr = vx * vx + vy * vy;
		double len = Math.sqrt(sqr);
		double nvx = vx / len;
		double nvy = vy / len;
		
		// compute right hand vector
		double rnvx = (projectRight) ? nvy : -1 * nvy;
		double rnvy = (projectRight) ? -1 * nvx : nvx;
		
		// set vector length
		double mrnvx = rnvx * distance;
		double mrnvy = rnvy * distance;
		
		// translate to p
		double px = mrnvx + p.getX();
		double py = mrnvy + p.getY();
		
		return gf.createPoint(new Coordinate(px, py));
	}
	
	/**
	 * Method will return extrapolated access point for given point with provided setBack
	 * 
	 * ---------------------------------- road centreline
	 * 
	 * ----(E)--------------------------- curb line | | | (P)
	 * 
	 * where E is extrapolated accessPoint and P is parcelPoint, frontDoorPoint, or rooftopPoint
	 * (picture displays setBack = 0)
	 * 
	 * @param point a point representing the site, such as a parcel centroid
	 * @param face the block face onto which to extrapolate
	 * @param setBack the setback distance from the calculated curb
	 * @return the extrapolated access point with the setBack as provided
	 */
	public Point extrapolate(Point point, BlockFace face, int setBack) {
		if(point == null) {
			return null;
		}
		// Get the line representing street centre
		LineString centerLine = face.getSegment().getCenterLine();
		LengthIndexedLine il = new LengthIndexedLine(centerLine);
		double intersectionDist = 0;
		// Length along segment where it will then intersect the site point
		intersectionDist = il.project(point.getCoordinate());
		Coordinate coord = il.extractPoint(intersectionDist);
		Point ap = geometryFactory.createPoint(coord);
		return applyOffset(ap, face, setBack);
	}
	
	// same as above but figure out which face based on the location of the point relative to the line
	public Point extrapolate(Point point, StreetSegment streetSegment, int setBack) {
		if(point == null) {
			return null;
		}
		// determine which segment of the lineString is closest to the site point
		LineSegment closest = null; // the closest segment so far to the site point
		double closestSegDist = Double.MAX_VALUE; // the distance from the site point to the closest
										// segment so far
		CoordinateSequence coords = streetSegment.getCenterLine().getCoordinateSequence();
		Coordinate previousCoordinate = coords.getCoordinate(0);
		
		Coordinate pointCoordinate = point.getCoordinate();
		for(int i = 1; i < coords.size(); i++) {
			Coordinate coordinate = coords.getCoordinate(i);
			LineSegment segment = new LineSegment(previousCoordinate, coordinate);
			double dist = segment.distance(pointCoordinate);
			if(dist < closestSegDist) {
				closest = segment;
				closestSegDist = dist;
			}
			previousCoordinate = coordinate;
		}
		Side side;
		// determine which side of the segment the site point is on
		if(closest.orientationIndex(pointCoordinate) == 1) {
			side = Side.LEFT;
		} else {
			side = Side.RIGHT;
		}
		
		Coordinate nearestCoord = closest.closestPoint(pointCoordinate);
		Point resultPoint = geometryFactory.createPoint(nearestCoord);
		if(setBack >= 0) {
			// now we need to offset the result AP as per the request
			resultPoint = applyOffset(resultPoint, streetSegment.getBlockFace(side), setBack);
		}
		return resultPoint;
	}
}
