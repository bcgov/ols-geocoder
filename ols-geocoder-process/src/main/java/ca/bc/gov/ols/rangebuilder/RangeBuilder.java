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

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.enums.AddressScheme;
import ca.bc.gov.ols.enums.DividerType;
import ca.bc.gov.ols.enums.LaneRestriction;
import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.TravelDirection;
import ca.bc.gov.ols.geocoder.BlockFaceInterpolator;
import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStore;
import ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStoreFactory;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.geocoder.data.enumTypes.Side;
import ca.bc.gov.ols.geocoder.datasources.GeocoderDataSource;
import ca.bc.gov.ols.geocoder.datasources.GeocoderDataSourceFactory;
import ca.bc.gov.ols.rowreader.DateType;
import ca.bc.gov.ols.rowreader.JsonRowWriter;
import ca.bc.gov.ols.rowreader.RowReader;
import ca.bc.gov.ols.rowreader.RowWriter;
import ca.bc.gov.ols.rowreader.XsvRowWriter;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

public class RangeBuilder {
	private final static Logger logger = LoggerFactory.getLogger(RangeBuilder.class.getCanonicalName());
	
	public static final int NULL_ADDR = Integer.MIN_VALUE;

	private static final double DISTANCE_TOLERANCE = 1500;
	
	GeocoderDataSource dataSource;
	static GeometryFactory geometryFactory;
	
	private static String outputDataDir = "C:/apps/bgeo/data/";
	
	private boolean emptySid2Pid = false;
	private int sideSwapCount = 0;
	private int tooFarFromNearestSegCount = 0;
	private int tooFarFromNearestSegPseudoCount = 0;
	//private int noCandidatesCount = 0;
	//private int noCandidatesPseudoCount = 0;
	private int highwayAliasCount = 0;
	private int successfulStretchCount = 0;
	private int successfulDeadEndStretchCount = 0;
	private int excessiveStretchCount = 0;
	private int overlapsCount = 0;
	private int parityConflictCount = 0;
	private int paritySmoothableCount = 0;
	private int increasingSmoothedCount = 0;
	private int directionConflictCount = 0;
	private int missingAddrCount = 0;
	private int containsCount = 0;
	private int loopCount = 0;
	private int threeWayCount = 0;
	private int fourWayCount = 0;
	private int manyWayCount = 0;
	private int existingRangeAnchorPointCount = 0;
	private int pseudoSiteAnchorPointCount = 0;
	private int readSiteCount = 0;
	private int regularSiteCount = 0;
	private int subSiteCount = 0;
	private int regularCAPSiteCount = 0;
	private int segmentNCAPCount = 0;
	private int localityNCAPCount = 0;
	private int unallocatablePseudoSiteCount = 0;
	int unforceableParityCount = 0;
	int maxSiteId = 0;
	
	private Set<RbBlockFace> deletedFaces;
	private List<IRbSite> unallocatableSites;
	private RowWriter logWriter;
	
	public static void main(String[] args) {
		if(args.length != 1) {
			logger.error("Data directory parameter is required.");
			System.exit(-1);
		}
		String dir = args[0];
		File f = new File(dir);
		if(!f.isDirectory()) {
			logger.error("Invalid data dir: '" + dir +"'");
			System.exit(-1);
		}
		outputDataDir = dir;

		RangeBuilder rb = new RangeBuilder();
		rb.buildRanges();
	}

	public static GeometryFactory getGeometryFactory() {
		return geometryFactory;
	}

	public RangeBuilder() {
		Properties bootstrapConfig = GeocoderFactory.getBootstrapConfigFromEnvironment();
		GeocoderConfigurationStore configStore = GeocoderConfigurationStoreFactory.getConfigurationStore(bootstrapConfig);
		geometryFactory = new GeometryFactory(GeocoderConfig.BASE_PRECISION_MODEL, Integer.parseInt(configStore.getConfigParam("baseSrsCode").get()));

		GeocoderConfig config = new GeocoderConfig(configStore, geometryFactory);
		dataSource = GeocoderDataSourceFactory.getGeocoderDataSource(config, geometryFactory);
		configStore.close();
	}
	
	public void buildRanges() {
		openLogWriter();
		RowReader rr;
		
		// load street segments into a lookup table
		logger.info("Loading Street Segments");
		TIntObjectHashMap<RbStreetSegment> segmentIdMap = new TIntObjectHashMap<RbStreetSegment>();
		rr = dataSource.getStreetSegments();
		while(rr.next()) {
			int segmentId = rr.getInt("street_segment_id");
			int startIntersectionId = rr.getInt("start_intersection_id");
			int endIntersectionId = rr.getInt("end_intersection_id");
			int localityLeftId = rr.getInt("left_locality_id");
			int localityRightId = rr.getInt("right_locality_id");
			Integer eaLeftId = rr.getInteger("left_electoral_area_id");
			Integer eaRightId = rr.getInteger("right_electoral_area_id");
			int numLanesLeft = rr.getInt("num_lanes_left");
			int numLanesRight = rr.getInt("num_lanes_right");
			RoadClass roadClass = RoadClass.convert(rr.getString("road_class"));
			LaneRestriction laneRestriction = LaneRestriction.convert(
					rr.getString("lane_restriction"));
			TravelDirection travelDir = TravelDirection.convert(
					rr.getString("travel_direction"));
			DividerType dividerType = DividerType.convert(rr.getString("divider_type"));
			LineString centerLine = rr.getLineString();
			RbStreetSegment segment = new RbStreetSegment(segmentId, 
					startIntersectionId, endIntersectionId, centerLine,
					localityLeftId, localityRightId, eaLeftId, eaRightId, roadClass, laneRestriction, travelDir,
					dividerType, numLanesLeft, numLanesRight);
			if(true) {//dataSource.getConfig().getGenerateUsingExistingRanges() > 0) {
				if(rr.getInteger("first_address_left") != null) {
					segment.getSites(Side.LEFT).add(
							new PseudoSite(rr.getInt("first_address_left"), segment,
									segment.getCenterLine().getStartPoint(), 0));
					existingRangeAnchorPointCount++;
				}
				if(rr.getInteger("last_address_left") != null) {
					segment.getSites(Side.LEFT).add(
							new PseudoSite(rr.getInt("last_address_left"), segment,
									segment.getCenterLine().getEndPoint(),
									segment.getCenterLine().getLength()));
					existingRangeAnchorPointCount++;
				}
				if(rr.getInteger("first_address_right") != null) {
					segment.getSites(Side.RIGHT).add(
							new PseudoSite(rr.getInt("first_address_right"), segment,
									segment.getCenterLine().getStartPoint(), 0));
					existingRangeAnchorPointCount++;
				}
				if(rr.getInteger("last_address_right") != null) {
					segment.getSites(Side.RIGHT).add(
							new PseudoSite(rr.getInt("last_address_right"), segment,
									segment.getCenterLine().getEndPoint(),
									segment.getCenterLine().getLength()));
					existingRangeAnchorPointCount++;
				}
			}
			segmentIdMap.put(segmentId, segment);
		}
		rr.close();
		if(segmentIdMap.isEmpty()) {
			logger.error("No street segments found - cannot continue!");
			return;
		}
		
		// read the street names in
		logger.info("Loading Street Names");
		TIntObjectHashMap<String> streetTypesById = new TIntObjectHashMap<String>();
		rr = dataSource.getStreetNames();
		while(rr.next()) {
			int streetNameId = rr.getInt("street_name_id");
			//String body = rr.getString("name_body");
			String type = rr.getString("street_type");
			//String dir = rr.getString("street_direction");
			//String qual = rr.getString("street_qualifier");
			//Boolean typeIsPrefix = GeocoderUtil.charToBoolean(
			//		rr.getString("street_type_is_prefix_ind"));
			//Boolean dirIsPrefix = GeocoderUtil.charToBoolean(
			//		rr.getString("street_direction_is_prefix_ind"));
			streetTypesById.put(streetNameId, type);
		}
		rr.close();
		
		// use street name cross references to build lookup tables
		logger.info("Loading Street Name XRefs");
		TIntObjectHashMap<List<RbStreetSegment>> primaryStreetNameIdToSegmentListMap = new TIntObjectHashMap<List<RbStreetSegment>>();
		TIntObjectHashMap<List<RbStreetSegment>> aliasStreetNameIdToSegmentListMap = new TIntObjectHashMap<List<RbStreetSegment>>();
		rr = dataSource.getStreetNameOnSegments();
		while(rr.next()) {
			int nameId = rr.getInt("street_name_id");
			int segmentId = rr.getInt("street_segment_id");
			String isPrimary = rr.getString("is_primary_ind");
			RbStreetSegment seg = segmentIdMap.get(segmentId);
			// ignore xrefs to non-existent segments; should only be the case in test data
			if(seg == null) {
				logger.warn("CONSTRAINT VIOLATION: StreetNameOnSegment refers to non-existent segment: " + segmentId);
				continue;
			}
			if(isPrimary.equalsIgnoreCase("Y")) {
				// get the list of segs with this street name, if there is one
				List<RbStreetSegment> segsWithName = primaryStreetNameIdToSegmentListMap.get(nameId);
				if(segsWithName == null) {
					// create the list
					segsWithName = new ArrayList<RbStreetSegment>();
					primaryStreetNameIdToSegmentListMap.put(nameId, segsWithName);
				}
				// add this segment to the list
				segsWithName.add(seg);
				seg.setPrimaryStreetNameId(nameId);
			} else {
				// get the list of segs with this street name as an alias, if there is one
				List<RbStreetSegment> segsWithName = aliasStreetNameIdToSegmentListMap.get(nameId);
				if(segsWithName == null) {
					// create the list
					segsWithName = new ArrayList<RbStreetSegment>();
					aliasStreetNameIdToSegmentListMap.put(nameId, segsWithName);
				}
				// add this segment to the list
				segsWithName.add(seg);
			}
		}
		rr.close();
		
		// load sid2pid data into a lookup table
		logger.info("Loading sid2pid data.");
		Map<UUID,String> sid2pidMap = new HashMap<UUID, String>();
		rr = dataSource.getSid2Pids();
		while(rr.next()) {
			UUID siteUuid = UUID.fromString(rr.getString("SID"));
			String pids = rr.getString("PID");
			sid2pidMap.put(siteUuid, pids);
		}
		rr.close();
		if(sid2pidMap.isEmpty()) {
			emptySid2Pid = true;
		}
		// load all sites/accessPoints into a lookup table
		logger.info("Loading site and access point data, assigning sites to segments");
		List<IRbSite> extraSites = new ArrayList<IRbSite>();
		rr = dataSource.getCombinedSites();
		while(rr.next()) {
			readSiteCount++;
			if(readSiteCount % 100000 == 0) {
				logger.info("Site/AccessPoints Read: {}", readSiteCount);
			}
			int rangeType = rr.getInt("range_Type");

			IRbSite site;
			if(rangeType == 0) {
				site = new RbSite();
				regularSiteCount++;
			} else if(rangeType == -1) {
				// this accessPoint is for address range generation only
				// and is not to be used as a "real" site 
				site = new PseudoSite();
				pseudoSiteAnchorPointCount++;
			} else {
				logger.warn("Site ignored due to unexpected rangeType value of: " + rangeType);
				continue;
			}
			site.setSiteId(rr.getInt("SITE_ID"));
			site.setInputName(rr.getString("INPUT_NAME").intern());
			site.setSiteUuid(UUID.fromString(rr.getString("SITE_UUID")));
			site.setPids(sid2pidMap.get(site.getSiteUuid()));
			site.setParentSiteId(rr.getInt("PARENT_SITE_ID"));
			site.setSiteName(rr.getString("SITE_NAME"));
			site.setLocationDescriptor(LocationDescriptor.convert(rr.getString("LOCATION_DESCRIPTOR")));
			site.setUnitDesignator(rr.getString("UNIT_DESIGNATOR"));
			site.setUnitNumber(rr.getString("UNIT_NUMBER"));
			site.setUnitNumberSuffix(rr.getString("UNIT_NUMBER_SUFFIX"));
			site.setSitePositionalAccuracy(PositionalAccuracy.convert(rr.getString("SITE_POSITIONAL_ACCURACY")));
			site.setSiteStatus(rr.getString("SITE_STATUS"));
			site.setSiteChangeDate(rr.getDate("SITE_CHANGE_DATE"));
			site.setSiteRetireDate(rr.getDate("SITE_RETIRE_DATE"));
			site.setSiteX(rr.getDouble("SITE_ALBERS_X"));
			site.setSiteY(rr.getDouble("SITE_ALBERS_Y"));
			site.setApType(rr.getString("AP_TYPE"));
			site.setPrimary("Y".equals(rr.getString("IS_PRIMARY_IND")));
			site.setNarrativeLocation(rr.getString("NARRATIVE_LOCATION"));
			site.setAccessPositionalAccuracy(PositionalAccuracy.convert(rr.getString("ACCESS_POSITIONAL_ACCURACY")));
			site.setCivicNumber(rr.getInt("CIVIC_NUMBER"));
			site.setCivicNumberSuffix(rr.getString("CIVIC_NUMBER_SUFFIX"));
			site.setAccessPointStatus(rr.getString("ACCESS_POINT_STATUS"));
			site.setAccessRetireDate(rr.getDate("ACCESS_RETIRE_DATE"));
			site.setAccessX(rr.getDouble("ACCESS_ALBERS_X"));
			site.setAccessY(rr.getDouble("ACCESS_ALBERS_Y"));
			if(site.getAccessLocation() == null) {
				site.setOriginalAP(false);
			} else {
				site.setOriginalAP(true);
			}
			site.setFullAddress(rr.getString("FULL_ADDRESS"));
			site.setStreetSegmentId(rr.getInt("BLOCK_ID"));
			site.setLocalityId(rr.getInt("LOCALITY_ID"));
			site.setInterimStreetNameId(rr.getInt("INTERIM_STREET_NAME_ID"));
			
			if(site.getLocalityId() == 507 && site.getInterimStreetNameId() == 55771) {
				site.setInterimStreetNameId(30280);
			}

			// keep track of the max site_id
			if(site.getSiteId() != RowReader.NULL_INT_VALUE &&
					site.getSiteId() > maxSiteId) {
				maxSiteId = site.getSiteId();
			}

			if(site.getParentSiteId() != RowReader.NULL_INT_VALUE) {
				// this is a subsite, just pass it through
				extraSites.add(site);
				subSiteCount++;
				continue;
			}				
			
			if("NCAP".equalsIgnoreCase(site.getApType()) && site.getInterimStreetNameId() == RowReader.NULL_INT_VALUE) {
				// this is a locality-based ncap, they just pass through
				extraSites.add(site);
				localityNCAPCount++;
				continue;
			}
			
			List<RbStreetSegment> candidateSegs = primaryStreetNameIdToSegmentListMap.get(site.getInterimStreetNameId());
			List<RbStreetSegment> aliasCandidateSegs = aliasStreetNameIdToSegmentListMap.get(site.getInterimStreetNameId());

//			// null/empty candidateSegs - should only happen if the streetName is only used as an alias
//			if(candidateSegs == null || candidateSegs.isEmpty()) {
//				logger.debug("No candidate streetSegments for siteId: " + site.getSiteId() 
//						+ " based on interimStreetNameId: " + interimStreetNameId);
//				if(site instanceof PseudoSite) {
//					noCandidatesPseudoCount++;
//				} else {
//					noCandidatesCount++;
//				}
//				continue;
//			}
			
			// loop over the segments and find the closest one in the right locality
			RbStreetSegment closestSeg = null;
			double closestDist = Double.MAX_VALUE;
			// use the AccessPoint if there is one, otherwise fallback to the Site Point (the more common case)
			Point siteLocation = site.getAccessLocation();
			if(siteLocation == null) {
				siteLocation = site.getSiteLocation();
			}
			if(candidateSegs != null) {
				for(RbStreetSegment seg : candidateSegs) {
					double dist = computeMinDistance(siteLocation, seg.getCenterLine(), DISTANCE_TOLERANCE);
					if(dist < closestDist 
							&& (localitiesEquivalent(site.getLocalityId(), seg.getLocalityId(Side.LEFT))
								|| localitiesEquivalent(site.getLocalityId(), seg.getLocalityId(Side.RIGHT)))) {
						closestSeg = seg;
						closestDist = dist;
					}
				}
			}
			boolean notFound = closestSeg == null;
				
			boolean highwayAlias = false;
			if(aliasCandidateSegs != null) {
				for(RbStreetSegment seg : aliasCandidateSegs) {
					if(notFound || "hwy".equalsIgnoreCase(streetTypesById.get(seg.getPrimaryStreetNameId())) 
							|| "hwy".equalsIgnoreCase(streetTypesById.get(site.getInterimStreetNameId()))) {
						double dist = computeMinDistance(siteLocation, seg.getCenterLine(), DISTANCE_TOLERANCE);
						if(dist < closestDist 
								&& (localitiesEquivalent(site.getLocalityId(), seg.getLocalityId(Side.LEFT))
										|| localitiesEquivalent(site.getLocalityId(), seg.getLocalityId(Side.RIGHT)))) {
							closestSeg = seg;
							closestDist = dist;
							if(!notFound) {
								highwayAlias = true;
							}
						}
					}
				}				
			}
			
			// if the closestSeg is still too far away, we will drop this site
			// may need to reduce the minimum acceptable distance value
			if(closestDist > DISTANCE_TOLERANCE) {
				logger.debug("closest seg is too far from siteId: {}", site.getSiteId());
				if(closestSeg != null) {
					logSite(site, "closest seg (" + closestSeg.getSegmentId() + ") is too far from site(" + closestDist + " m)");
				} else {
					logSite(site, "closest seg is too far from site");
				}
				if(site instanceof PseudoSite) {
					tooFarFromNearestSegPseudoCount++;
				} else {
					tooFarFromNearestSegCount++;
				}
				continue;
			}
			if(highwayAlias) {
				highwayAliasCount++;
			}
			placeSite(site, closestSeg, null);
		}
		rr.close();
		
		// loop over all segments and determine which side should be odd and even
		// and also pick the mins and maxes
		logger.info("Forcing sites to consistent even/odd sides of segment");
		for(TIntObjectIterator<RbStreetSegment> it = segmentIdMap.iterator(); it.hasNext(); ) {
			it.advance();
			RbStreetSegment seg = it.value();
			// force sites to appropriate sides as per ResolveAddresses.java
			sideSwapCount += seg.forceParity(this);
			seg.setRangesFromSites();
		}
				
		// stretch ranges 
		stretchAddresses(primaryStreetNameIdToSegmentListMap);
		
		// output the results so far for investigation/testing/QA
		//dataSource.loadRanges(segmentIdMap, maxApId, "_rb");
		
		// clean up overlapping ranges by shifting sites to other segments as required
		Map<String, List<RbBlockFace>> streetFaces = destroyOverlaps(primaryStreetNameIdToSegmentListMap);

		// smooth ranges 
		smoothAddresses(primaryStreetNameIdToSegmentListMap);

		// Mirror one-sided segments as per MirrorSingleSide.java
		mirrorSingleSided(streetFaces);
		
		// log stats
		calcStats(segmentIdMap);
		if(emptySid2Pid) {
			logger.error("---------------------------------------------------------------");
			logger.error("WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING");
			logger.error("SID2PID file was empty or not present; data will have NO PIDS!");
			logger.error("---------------------------------------------------------------");
		}
		logWriter.close();
		
		logger.info("Writing output segments/sites");
		writeRanges(segmentIdMap, extraSites, outputDataDir, dataSource.getDates(), "");

		dataSource.close();
	}
	
	// check for equality of locality ids, allowing for
	// City of Langley(1518) to be equal to Township of Langley(518) and
	// City of North Vancouver (1519) to be equal to District of North Vancouver(519) 
	private boolean localitiesEquivalent(int a, int b) {
		if(a == b
				|| (a == 518 && b == 1518)
				|| (a == 1518 && b == 518)
				|| (a == 519 && b == 1519)
				|| (a == 1519 && b == 519)) {
			return true;
		}
		return false;
	}
	
	private void placeSite(IRbSite site, RbStreetSegment streetSegment, Side side) {
		// determine which segment of the lineString is closest to the site point
		
		LineSegment closest = null; // the closest segment so far to the site point
		double closestSegDist = Double.MAX_VALUE; // the distance from the site point to the closest
										// segment so far
		double measure = 0; // how far along the line to the start of the current segment
		double closestMeasure = 0; // how far along the line to the start of the closest
									// segment so far
		CoordinateSequence coords = streetSegment.getCenterLine().getCoordinateSequence();
		Coordinate previousCoordinate = coords.getCoordinate(0);
		
		Point siteLocation = site.getSiteLocation();
		if(site.isOriginalAP()) {
			siteLocation = site.getAccessLocation();
		}
		Coordinate siteCoordinate = siteLocation.getCoordinate();
		for(int i = 1; i < coords.size(); i++) {
			Coordinate coordinate = coords.getCoordinate(i);
			LineSegment segment = new LineSegment(previousCoordinate, coordinate);
			double dist = segment.distance(siteCoordinate);
			if(dist < closestSegDist) {
				closest = segment;
				closestSegDist = dist;
				closestMeasure = measure;
			}
			previousCoordinate = coordinate;
			measure = measure + segment.getLength();
		}
		
		// if we don't already know which side it should be on
		if(side == null) {
			// determine which side of the segment the site point is on
			if(closest.orientationIndex(siteCoordinate) == 1) {
				side = Side.LEFT;
			} else {
				side = Side.RIGHT;
			}
		}
		
		Coordinate nearestCoord = closest.closestPoint(siteCoordinate);
		
		// calculate the measure value (distance along linestring)
		if(nearestCoord.equals(coords.getCoordinate(0))) {
			// we matched the start of the lineString
			closestMeasure = -closestSegDist;
		} else if(nearestCoord.equals(coords.getCoordinate(coords.size() - 1))) {
			// we matched the end of the lineString
			closestMeasure += closestSegDist;
		} else {
			// add the distance from the start of the lineSeg to the point
			closestMeasure += nearestCoord.distance(closest.p0);
		}
		site.setMeasure(closestMeasure);
		
		if(!site.isOriginalAP()) {
			// offset the accessPoint to the curb
			double curbOffset = getWidth(streetSegment.getFace(side), dataSource.getConfig());
			Point point = geometryFactory.createPoint(nearestCoord);
			Point offsetPoint = BlockFaceInterpolator.project(closest, point, curbOffset,
					side == Side.RIGHT, geometryFactory);
			site.setAccessX(offsetPoint.getX());
			site.setAccessY(offsetPoint.getY());
		}
		
		// add the site/accessPoint to a list for the side of the segment it is on
		if(site.getCivicNumber() == RowReader.NULL_INT_VALUE) {
			streetSegment.getNCAPs().add(site);
		} else {
			streetSegment.getSites(side).add(site);
		}
	}
	
	public void smoothAddresses(TIntObjectHashMap<List<RbStreetSegment>> streetNameIdToSegmentListMap) {
		logger.info("Smoothing address ranges");
		// use street name index to group ITN segments into block groups
		for(TIntObjectIterator<List<RbStreetSegment>> it = streetNameIdToSegmentListMap.iterator(); it.hasNext(); ) {
			it.advance();
			List<RbStreetSegment> segs = it.value();
			Map<Point, List<RbStreetSegment>> intersectionMap = new THashMap<Point, List<RbStreetSegment>>(
					segs.size() * 2);
			for(RbStreetSegment seg : segs) {
				LineString ls = seg.getCenterLine();
				addToMapList(intersectionMap, ls.getStartPoint(), seg);
				addToMapList(intersectionMap, ls.getEndPoint(), seg);
			}
			
			// smooth segments that are part of a continuous chain
			for(RbStreetSegment seg : segs) {
				LineString ls = seg.getCenterLine();
				List<RbStreetSegment> startPointSegs = intersectionMap.get(ls.getStartPoint());
				List<RbStreetSegment> endPointSegs = intersectionMap.get(ls.getEndPoint());
				RbStreetSegment startSeg = null;
				RbStreetSegment endSeg = null;
				// If there are exactly 2 segs meeting at the start point
				if(startPointSegs.size() == 2) {
					// find the "other" one
					for(RbStreetSegment otherSeg : startPointSegs) {
						if(!seg.equals(otherSeg)) {
							startSeg = otherSeg;
						}
					}
				}
				// If there are exactly 2 segs meeting at the end point
				if(endPointSegs.size() == 2) {
					// find the "other" one
					for(RbStreetSegment otherSeg : endPointSegs) {
						if(!seg.equals(otherSeg)) {
							endSeg = otherSeg;
						}
					}
				}
				// If we have a start and an end seg, we can compare them
				if(startSeg != null && endSeg != null) {
					Side startSegSide1 = Side.LEFT;
					Side endSegSide1 = Side.LEFT;
					if(startSeg.getCenterLine().getStartPoint().equals(ls.getStartPoint())) {
						startSegSide1 = Side.RIGHT;
					}
					if(endSeg.getCenterLine().getEndPoint().equals(ls.getEndPoint())) {
						endSegSide1 = Side.RIGHT;
					}
					
					if(startSeg.getParity(startSegSide1.toChar()) != null 
							&& startSeg.getParity(startSegSide1.toChar()) == endSeg.getParity(endSegSide1.toChar())
							&& startSeg.getParity(startSegSide1.toChar()) != seg.getParity("l")
							&& seg.getFromLeft() != NULL_ADDR) {
						paritySmoothableCount++;
						// TODO fix the parity
					}
					int startSegMed = startSeg.getMedianAddr(startSegSide1);
					int endSegMed = endSeg.getMedianAddr(endSegSide1);
					if(startSegMed != NULL_ADDR && endSegMed != NULL_ADDR 
							&& startSegMed < endSegMed
							&& !seg.isIncreasing(Side.LEFT)
							&& seg.getFromLeft() != NULL_ADDR) {
						seg.getFace(Side.LEFT).flip();
						increasingSmoothedCount++;
					}
					startSegMed = startSeg.getMedianAddr(startSegSide1.opposite());
					endSegMed = endSeg.getMedianAddr(endSegSide1.opposite());
					if(startSegMed != NULL_ADDR && endSegMed != NULL_ADDR 
							&& startSegMed < endSegMed
							&& !seg.isIncreasing(Side.RIGHT)
							&& seg.getFromLeft() != NULL_ADDR) {
						seg.getFace(Side.RIGHT).flip();
						increasingSmoothedCount++;
					}
				}				
			}
		}
	}
	
	public void stretchAddresses(
			TIntObjectHashMap<List<RbStreetSegment>> streetNameIdToSegmentListMap) {
		logger.info("Stretching address ranges");
		
		// use street name index to group ITN segments into block groups
		for(TIntObjectIterator<List<RbStreetSegment>> it = streetNameIdToSegmentListMap.iterator(); it.hasNext(); ) {
			it.advance();
			int streetNameId = it.key();
			List<RbStreetSegment> segs = it.value();
			Map<Point, List<RbStreetSegment>> intersectionMap = new THashMap<Point, List<RbStreetSegment>>(
					segs.size() * 2);
			for(RbStreetSegment seg : segs) {
				LineString ls = seg.getCenterLine();
				addToMapList(intersectionMap, ls.getStartPoint(), seg);
				addToMapList(intersectionMap, ls.getEndPoint(), seg);
			}			
			
			// stretch address from/to values within block groups to cover complete ranges
			// and split at "nice" values (hundred blocks or similar)
			for(Entry<Point, List<RbStreetSegment>> intEntry : intersectionMap.entrySet()) {
				Point intPoint = intEntry.getKey();
				List<RbStreetSegment> intSegs = intEntry.getValue();
				if(intSegs.size() == 1) {
					// only one seg, this is a "dead end" case (might also be an offset
					// intersection)
					deadEndStretch(intSegs.get(0), intPoint);
					continue;
				}
				if(intSegs.size() == 2) {
					RbStreetSegment seg1 = intSegs.get(0);
					RbStreetSegment seg2 = intSegs.get(1);

					if(seg1 == seg2) {
						// this is just one seg intersecting itself in a loop, I don't think we can
						// do anything
						loopCount++;
						deadEndStretch(intSegs.get(0), intPoint);
						continue;
					}
					LineString seg1Line = seg1.getCenterLine();
					LineString seg2Line = seg2.getCenterLine();
					if(intPoint.equals(seg1Line.getStartPoint())) {
						if(intPoint.equals(seg2Line.getStartPoint())) {
							// segs are going in opposite direction, meeting at startPoint
							stretch(seg1, "l", "tol", "froml", seg2, "r", "fromr", "tor");
							stretch(seg1, "r", "tor", "fromr", seg2, "l", "froml", "tol");
						}
						if(intPoint.equals(seg2Line.getEndPoint())) {
							// segs are in same direction, seg1 < seg2
							stretch(seg1, "l", "tol", "froml", seg2, "l", "tol", "froml");
							stretch(seg1, "r", "tor", "fromr", seg2, "r", "tor", "fromr");
						}
					}
					if(intPoint.equals(seg1Line.getEndPoint())) {
						if(intPoint.equals(seg2Line.getStartPoint())) {
							// segs are going in same direction, seg1 > seg2
							stretch(seg1, "l", "froml", "tol", seg2, "l", "froml", "tol");
							stretch(seg1, "r", "fromr", "tor", seg2, "r", "fromr", "tor");
						}
						if(intPoint.equals(seg2Line.getEndPoint())) {
							// segs are in opposite direction, meeting at endpoint
							stretch(seg1, "l", "froml", "tol", seg2, "r", "tor", "fromr");
							stretch(seg1, "r", "fromr", "tor", seg2, "l", "tol", "froml");
						}
					}
					continue;
				}
				if(intSegs.size() == 3) {
					// might be a lollipop?
					threeWayCount++;
					for(RbStreetSegment seg : intSegs) {
						// treat each seg as a "dead end" case
						deadEndStretch(seg, intPoint);
					}
					continue;
				}
				if(intSegs.size() == 4) {
					// might be a figure eight?
					fourWayCount++;
					for(RbStreetSegment seg : intSegs) {
						// treat each seg as a "dead end" case
						deadEndStretch(seg, intPoint);
					}
					continue;
				}
				if(intSegs.size() > 4) {
					// WTF?
					manyWayCount++;
					for(RbStreetSegment seg : intSegs) {
						// treat each seg as a "dead end" case
						deadEndStretch(seg, intPoint);
					}
					logger.debug("more than 4 segments with same name id (" + streetNameId
							+ ") incident at intersection: " + intPoint);
					continue;
				}
				
			}
		}
	}
	
	private static boolean parityEqual(AddressScheme par1, AddressScheme par2) {
		if(par1 == null || par2 == null) {
			return false;
		}
		return par1.equals(par2);
	}
	
	private void deadEndStretch(RbStreetSegment seg, Point end) {
		LineString segLine = seg.getCenterLine();
		if(end.equals(segLine.getStartPoint())) {
			deadEndStretchSide(seg, "l", "tol", "froml");
			deadEndStretchSide(seg, "r", "tor", "fromr");
		}
		if(end.equals(segLine.getEndPoint())) {
			deadEndStretchSide(seg, "l", "froml", "tol");
			deadEndStretchSide(seg, "r", "fromr", "tor");
		}
	}
	
	private void deadEndStretchSide(RbStreetSegment seg, String side, String outerName,
			String innerName) {
		// the inner is the "dead end"
		int outer = seg.getAddr(outerName);
		int inner = seg.getAddr(innerName);
		if(outer == NULL_ADDR || inner == NULL_ADDR) {
			// can't do anything if there are missing addresses
			missingAddrCount++;
			return;
		}
		
		AddressScheme par = seg.getParity(side);
		// in the case where outer == inner, we stretch "to" upwards and "from" downwards
		if(outer < inner || (outer == inner && innerName.startsWith("to"))) {
			// round up to nearest hundred-1
			int newInner = (((inner / 100) + 1) * 100) - 1;
			if(newInner < 0) {
				newInner -= 99;
			}
			if(!par.includes(newInner)) {
				newInner--;
			}
			seg.setAddr(innerName, newInner);
			// seg.setAddr(innerName + "stretched", newInner - inner);
		} else if(inner != 0 && (outer > inner || (outer == inner && innerName.startsWith("from")))) {
			// leave 0's alone
			// round down to nearest hundred
			int newInner = (inner / 100) * 100;
			if(newInner == 0) {
				newInner++;
			}
			if(newInner < 0) {
				newInner -= 99;
			}
			if(!par.includes(newInner)) {
				newInner++;
			}
			seg.setAddr(innerName, newInner);
			// seg.setAddr(innerName + "stretched", inner - newInner);
		} // all cases should be covered above
		successfulDeadEndStretchCount++;
	}
	
	private void stretch(RbStreetSegment seg1, String seg1Side, String seg1OuterName,
			String seg1InnerName,
			RbStreetSegment seg2, String seg2Side, String seg2InnerName, String seg2OuterName) {
		
		int seg1outer = seg1.getAddr(seg1OuterName);
		int seg1inner = seg1.getAddr(seg1InnerName);
		int seg2inner = seg2.getAddr(seg2InnerName);
		int seg2outer = seg2.getAddr(seg2OuterName);
		
		if(seg1outer == NULL_ADDR || seg1inner == NULL_ADDR
				|| seg2inner == NULL_ADDR || seg2outer == NULL_ADDR) {
			// something is null, we can't do the "nice" stretching
			if(seg1outer != NULL_ADDR && seg1inner != NULL_ADDR) {
				deadEndStretchSide(seg1, seg1Side, seg1OuterName, seg1InnerName);
			} else if(seg2outer != NULL_ADDR && seg2inner != NULL_ADDR) {
				deadEndStretchSide(seg2, seg2Side, seg2OuterName, seg2InnerName);
			} else {
				// can't do anything if there are missing addresses from both segs
				missingAddrCount++;
			}
			return;
		}
		if(!parityEqual(seg1.getParity(seg1Side), seg2.getParity(seg2Side))) {
			logger.debug("Address range have different parity: " + seg1outer + " - "
					+ seg1inner
					+ " | " + seg2inner + " - " + seg2outer);
			parityConflictCount++;
			deadEndStretchSide(seg1, seg1Side, seg1OuterName, seg1InnerName);
			deadEndStretchSide(seg2, seg2Side, seg2OuterName, seg2InnerName);
			return;
		}
		AddressScheme par = seg1.getParity(seg1Side);
		if((seg1outer <= seg1inner && seg1inner < seg2inner && seg2inner <= seg2outer)
				|| (seg1outer >= seg1inner && seg1inner > seg2inner && seg2inner >= seg2outer)) {
			// maximum allowable stretch in address value is:
			// 500 + the average length in meters of the two segs
			double maxStretch = 500 + (seg1.getCenterLine().getLength()
					+ seg2.getCenterLine().getLength()) / 2;
			if(Math.abs(seg1inner - seg2inner) > maxStretch) {
				// too far
				excessiveStretchCount++;
				deadEndStretchSide(seg1, seg1Side, seg1OuterName, seg1InnerName);
				deadEndStretchSide(seg2, seg2Side, seg2OuterName, seg2InnerName);
				return;
			}
			int[] middles = findSplit(seg1inner, par, seg2inner, par);
			int split = Math.max(middles[0], middles[1]);
			int otherSplit = Math.min(middles[0], middles[1]);
			
			/*
			 * old way // average the middle values int split = (seg1inner + seg2inner)/2; // fix
			 * the parity if(("E".equals(par)) && (split % 2 != 0) || "O".equals(par) && (split % 2
			 * == 0)) { split += 1; } // calculate the other half of the split int otherSplit;
			 * if(par.equalsIgnoreCase("O") || par.equalsIgnoreCase("E")) { otherSplit = split - 2;
			 * } else { otherSplit = split - 1; }
			 */
			
			if(seg1outer < seg2outer) {
				seg1.setAddr(seg1InnerName, otherSplit);
				// seg1.setAddr(seg1InnerName + "stretched", otherSplit - seg1inner);
				seg2.setAddr(seg2InnerName, split);
				// seg2.setAddr(seg2InnerName + "stretched", seg2inner - split);
			}
			if(seg1outer > seg2outer) {
				seg1.setAddr(seg1InnerName, split);
				// seg1.setAddr(seg1InnerName + "stretched", seg1inner - split);
				seg2.setAddr(seg2InnerName, otherSplit);
				// seg2.setAddr(seg2InnerName + "stretched", otherSplit - seg2inner);
			}
			successfulStretchCount++;
		} else if((seg1outer <= seg1inner && seg1inner > seg2inner && seg2inner <= seg2outer)
				|| (seg1outer >= seg1inner && seg1inner < seg2inner && seg2inner >= seg2outer)) {
			// overlapping addresses
			overlapsCount++;
		} else if((seg1outer <= seg2inner && seg1inner >= seg2outer)
				|| (seg1outer >= seg2inner && seg1inner <= seg2outer)) {
			// one address contains the other
			logger.debug("Address range is contained by neighbor: " + seg1outer + " - "
					+ seg1inner
					+ " | " + seg2inner + " - " + seg2outer);
			containsCount++;
			deadEndStretchSide(seg1, seg1Side, seg1OuterName, seg1InnerName);
			deadEndStretchSide(seg2, seg2Side, seg2OuterName, seg2InnerName);
		} else {
			// addressing is not going in the same dir
			logger.debug("Address ranges have conflicting directionality: " + seg1outer
					+ " - " + seg1inner
					+ " | " + seg2inner + " - " + seg2outer);
			directionConflictCount++;
			deadEndStretchSide(seg1, seg1Side, seg1OuterName, seg1InnerName);
			deadEndStretchSide(seg2, seg2Side, seg2OuterName, seg2InnerName);
		}
	}
	
	private static <T, K> void addToMapList(Map<T, List<K>> map, T key, K streetSeg) {
		List<K> list = map.get(key);
		if(list == null) {
			list = new ArrayList<K>();
			map.put(key, list);
		}
		list.add(streetSeg);
	}
	
	private static int[] findSplit(int a, AddressScheme para, int b, AddressScheme parb) {
		int[] result = new int[2];
		int mid = (a + b) / 2;
		for(int i : new int[] {100, 50, 10, 1}) {
			for(int extra : new int[] {0, 1}) {
				int split = ((mid / i) + extra) * i;
				if(a <= split && split <= b) {
					if(a == split && split < b) {
						result[0] = split;
						result[1] = split + 1;
					} else {
						result[0] = split - 1;
						result[1] = split;
					}
					if(!para.includes(result[0])) {
						result[0]--;
					}
					if(!parb.includes(result[1])) {
						result[1]++;
					}
					return result;
				} else if(a >= split && split > b) {
					result[0] = split;
					result[1] = split - 1;
					if(!para.includes(result[0])) {
						result[0]++;
					}
					if(!parb.includes(result[1])) {
						result[1]--;
					}
					return result;
				}
			}
		}
		// should never get here unless a=b or parities are messed up
		return null;
	}
	
	public Map<String, List<RbBlockFace>> destroyOverlaps(
			TIntObjectHashMap<List<RbStreetSegment>> streetNameIdToSegmentListMap) {
		logger.info("Destroying overlaps");
		
		deletedFaces = new THashSet<RbBlockFace>();
		unallocatableSites = new ArrayList<IRbSite>();
		
		Map<String, List<RbBlockFace>> streetFaces = new THashMap<String, List<RbBlockFace>>();
		List<RbBlockFace> faces = new ArrayList<RbBlockFace>();
		
		for(TIntObjectIterator<List<RbStreetSegment>> it = streetNameIdToSegmentListMap.iterator(); it.hasNext(); ) {
			it.advance();
			int streetNameId = it.key();
			List<RbStreetSegment> segs = it.value();
			
			for(RbStreetSegment seg : segs) {
				for(Side side : Side.values()) {
					RbBlockFace face = seg.getFace(side);
					if(face.isValid()) {
						faces.add(face);
						int localityId = seg.getLocalityId(side);
						String streetFaceRef = localityId + ":" + streetNameId;
						addToMapList(streetFaces, streetFaceRef, face);
						seg.addStreetFaceRef(streetFaceRef);
					} // end if address range exists and is valid
				} // end for each side
			} // end for each segment
		} // end for each entry
		
		// loop over all the faces and check if they overlap other faces
		logger.info("Destroying BlockFace overlaps");
		while(!faces.isEmpty()) {
			List<RbBlockFace> retryFaces = new ArrayList<RbBlockFace>();
			for(RbBlockFace face : faces) {
				RbStreetSegment seg = face.getSegment();
				int nameId = seg.getPrimaryStreetNameId();
				int localityId = seg.getLocalityId(face.getSide());
				if(nameId != 0) {
					List<RbBlockFace> streetLocFaces = streetFaces.get(localityId + ":" + nameId);
					Iterator<RbBlockFace> faceIt = streetLocFaces.iterator();
					while(faceIt.hasNext()) {
						RbBlockFace otherFace = faceIt.next();
						if(deletedFaces.contains(otherFace)) {
							faceIt.remove();
							continue;
						}
						if(face != otherFace && face.overlaps(otherFace)) {
							logger.debug("Faces with overlapping addressing on segments {} and {}", 
									face.toLongString(), otherFace.toLongString());
							// here is where the destruction comes in
							shrink(face, otherFace);
							retryFaces.add(face);
							retryFaces.add(otherFace);
							logger.debug("overlaps resolved to {} and {}", 
									face.toLongString(), otherFace.toLongString());
						}
					}
				}
			} // end for each face
			logger.info("faces: " + faces.size() + "  retryFaces: " + retryFaces.size());
			faces = retryFaces;
		}
		return streetFaces;
	}
	
	private void shrink(RbBlockFace face1, RbBlockFace face2) {
		RbStreetSegment seg1 = face1.getSegment();
		RbStreetSegment seg2 = face2.getSegment();
		
		// determine the best place to divide the overlaps
		int pos1 = 0;
		int pos2 = 0;
		int best1 = 0;
		int best2 = 0;
		int bestTotal = 0;
		List<IRbSite> addrs1 = seg1.getSites(face1.getSide());
		List<IRbSite> addrs2 = seg2.getSites(face2.getSide());
		while(pos1 < addrs1.size() || pos2 < addrs2.size()) {
			int total = Math.max(pos1 + addrs2.size() - pos2, addrs1.size() - pos1 + pos2);
			if(total > bestTotal) {
				bestTotal = total;
				best1 = pos1;
				best2 = pos2;
			}
			if(pos2 >= addrs2.size()
					|| (pos1 < addrs1.size() && addrs1.get(pos1).getCivicNumber() < addrs2
							.get(pos2).getCivicNumber())) {
				pos1++;
			} else if(pos1 >= addrs1.size()
					|| (pos2 < addrs2.size() && addrs1.get(pos1).getCivicNumber() > addrs2
							.get(pos2).getCivicNumber())) {
				pos2++;
			} else {
				// we are not at the end of either list and both lists have the same addr value
				pos1++;
				pos2++;
			}
		}
		
		// determine which side to keep
		if(best1 == 0 && best2 == 0) {
			// we're throwing out one side's sites entirely
			// but there may still be some range left to recover
			if(addrs1.size() > addrs2.size()) {
				// face1 is the keeper
				recoverOrDelete(face1, face2);
			} else {
				// face2 is the keeper
				recoverOrDelete(face2, face1);
			}
		} else if(best1 + addrs2.size() - best2 > addrs1.size() - best1 + best2) {
			// keep low side of face1 and high side of face2
			int[] splits = findSplit(addrs1.get(best1 - 1).getCivicNumber(),
					face1.getAddressScheme(),
					addrs2.get(best2).getCivicNumber(), face2.getAddressScheme());
			face1.setMin(Math.min(face1.getMin(), face2.getMin()));
			face2.setMax(Math.max(face1.getMax(), face2.getMax()));
			face1.setMax(splits[0]);
			face2.setMin(splits[1]);
			//addrs1.subList(best1, addrs1.size()).clear();
			//addrs2.subList(0, best2).clear();
			/*
			 * old way face1.setMax(addrs2.get(best2).addr - 1);
			 * face2.setMin(addrs2.get(best2).addr); addrs1.subList(best1,addrs1.size()).clear();
			 * addrs2.subList(0,best2).clear();
			 */
		} else {
			// keep high side of face1 and low side of face2
			int[] splits = findSplit(addrs1.get(best1).getCivicNumber(), face1.getAddressScheme(),
					addrs2.get(best2 - 1).getCivicNumber(), face2.getAddressScheme());
			face1.setMax(Math.max(face1.getMax(), face2.getMax()));
			face2.setMin(Math.min(face1.getMin(), face2.getMin()));
			face1.setMin(splits[0]);
			face2.setMax(splits[1]);
			//addrs1.subList(0, best1).clear();
			//addrs2.subList(best2, addrs2.size()).clear();
			/*
			 * old way face1.setMin(addrs1.get(best1).addr); face2.setMax(addrs1.get(best1).addr -
			 * 1); addrs1.subList(0,best1).clear(); addrs2.subList(best2,addrs2.size()).clear();
			 */
		}
		// reallocate sites to the correct segment
		reallocateSites(face1, face2);
	}
	
	private void recoverOrDelete(RbBlockFace face1, RbBlockFace face2) {
		// face1 is the keeper
		// see what is left of face2, if any
		//List<RbSite> addrs2 = face2.getSegment().getSites(face2.getSide());
		
		int lowLeftOver = face1.getMin() - face2.getMin();
		int highLeftOver = face2.getMax() - face1.getMax();
		if(lowLeftOver > 0 && lowLeftOver > highLeftOver) {
			// there is some leftover on the low side, and it is more than there is on the high side
			face2.setMax(face1.getMin() - 1);
			//addrs2.clear();
		} else if(highLeftOver > 0) {
			// there is some leftover on the high side, and it is more than there is on the low side
			face2.setMin(face1.getMax() + 1);
			//addrs2.clear();
		} else {
			// there is no leftover, we'll just delete the face
			face2.empty();
			deletedFaces.add(face2);
		}
	}
	
	public void mirrorSingleSided(Map<String, List<RbBlockFace>> streetFaces) {
		logger.info("Mirroring single-sided segments");
		
		// loop over all the groups of faces with the same street name in the same locality
		for(Entry<String, List<RbBlockFace>> entry : streetFaces.entrySet()) {
			List<RbBlockFace> streetLocFaces = entry.getValue();
			
			// loop over each face in the group
			for(int faceIndex = 0; faceIndex < streetLocFaces.size(); faceIndex++) {
				RbBlockFace face = streetLocFaces.get(faceIndex);
				RbStreetSegment seg = face.getSegment();

				// if this face is not valid
				// or already has a valid opposite
				// or has continuous addressing
				// or has any type of divider
				// or has different localities on left and right
				if(!face.isValid()
						|| seg.getFace(face.getSide().opposite()).isValid()
						|| face.getAddressScheme() == AddressScheme.CONTINUOUS
						|| !DividerType.NONE.equals(face.getSegment().getDividerType())
						|| seg.getLocalityId(Side.LEFT) != seg.getLocalityId(Side.RIGHT)) {
					// we can't mirror this segment
					continue;
				}
				// create the proposed mirror face
				AddressScheme par = AddressScheme.ODD;
				if(face.getAddressScheme() == AddressScheme.ODD) {
					par = AddressScheme.EVEN;
				}
				Side side = face.getSide().opposite();
				
				int first, last;
				if(face.getFirst() < face.getLast()) {
					first = face.getFirst() - 1;
					if(first <= 0 || first % 100 == 99) {
						first += 2;
					}
					last = face.getLast() + 1;
					if(last % 100 == 0) {
						last -= 2;
					}
				} else if(face.getFirst() > face.getLast()) {
					first = face.getFirst() + 1;
					if(first % 100 == 0) {
						first -= 2;
					}
					last = face.getLast() - 1;
					if(last <= 0 || last % 100 == 99) {
						last += 2;
					}
				} else {
					// we won't bother mirroring single addr ranges
					continue;
				}
				RbBlockFace newFace = new RbBlockFace(face.getSegment(), side, par, first, last);
				
				// check it for overlaps with other segments and trim as necessary
				for(String streetFaceRef : seg.getStreetFaceRefs()) {
					for(RbBlockFace otherFace : streetFaces.get(streetFaceRef)) {
						if(newFace.overlaps(otherFace)) {
							// System.out.println("Face with overlapping addressing " + idMode +
							// ": "
							// + (Integer)face.getSegment().get(idMode)
							// + " " + streetname + " " + locality + " " + face
							// + " Overlaps with: " + otherFace);
							// here is where the destruction comes in
							oneSidedShrink(newFace, otherFace);
						}
					}
				}
				
				// if there is still some range left after cleaning up overlaps
				if(newFace.isValid()) {
					// it's a keeper
					// attach it to the segment
					seg.setFace(newFace);
					// add it to the list of faces for this street/locality group
					streetLocFaces.add(newFace);
				}
			} // end for each face
		} // end for each street/locality group
		
	}
	
	/*
	 * Shrink face1's range down to no longer overlap with face2
	 */
	private void oneSidedShrink(RbBlockFace face1, RbBlockFace face2) {
		if(face1.getMin() >= face2.getMin() && face1.getMax() <= face2.getMax()) {
			// face1 is contained in face2, we must not create face1
			face1.setFirst(NULL_ADDR);
			face1.setLast(NULL_ADDR);
		} else {
			// decide which end of face1 has more address space in it to keep
			if(face1.getMax() - face2.getMax() > face2.getMin() - face1.getMin()) {
				// keep the high end
				face1.setMin(face2.getMax() + 1);
			} else {
				// keep the low end
				face1.setMax(face2.getMin() - 1);
			}
		}
	}
	
	private void reallocateSites(RbBlockFace face1, RbBlockFace face2) {
		List<IRbSite> face1Sites = face1.getSegment().getSites(face1.getSide());
		List<IRbSite> face2Sites = face2.getSegment().getSites(face2.getSide());
		Iterator<IRbSite> face1SiteIt = face1Sites.iterator();
		while(face1SiteIt.hasNext()) {
			IRbSite site = face1SiteIt.next();
			if(!face1.contains(site)) {
				face1SiteIt.remove();
				if(face2.contains(site)) {
					// re-calc address point based on new face, and add it
					placeSite(site, face2.getSegment(), face2.getSide());
				} else {
					logger.debug("Unallocatable site: {}", site.getSiteId());
					logSite(site, "Unallocatable during overlap cleanup between resolved faces: " + face1 + " and " + face2);
					if(site instanceof PseudoSite) {
						unallocatablePseudoSiteCount++;
					} else {
						unallocatableSites.add(site);
					}
				}
			}
		}
		Iterator<IRbSite> face2SiteIt = face2Sites.iterator();
		while(face2SiteIt.hasNext()) {
			IRbSite site = face2SiteIt.next();
			if(!face2.contains(site)) {
				face2SiteIt.remove();
				if(face1.contains(site)) {
					// re-calc address point based on new face, and add it
					placeSite(site, face1.getSegment(), face1.getSide());
				} else {
					logger.debug("Unallocatable site: {}", site.getSiteId());
					logSite(site, "Unallocatable during overlap cleanup between resolved faces: " + face1 + " and " + face2);
					if(site instanceof PseudoSite) {
						unallocatablePseudoSiteCount++;
					} else {
						unallocatableSites.add(site);
					}
				}
			}
		}
		Collections.sort(face1Sites, IRbSite.ADDRESS_COMPARATOR);
		Collections.sort(face2Sites, IRbSite.ADDRESS_COMPARATOR);
	}
	
	private void calcStats(TIntObjectHashMap<RbStreetSegment> segmentIdMap) {
		int totalAddressCoverage = 0;
		int addressedBlockFaceCount = 0;
		int unAddressedBlockFaceCount = 0;
		int faceWithIllegalRangeCount = 0;
		
		for(TIntObjectIterator<RbStreetSegment> it = segmentIdMap.iterator(); it.hasNext(); ) {
			it.advance();
			RbStreetSegment seg = it.value();
			for(Side side : Side.values()) {
				RbBlockFace face = seg.getFace(side);
				if(face.isValid()) {
					if(!face.getAddressScheme().includes(face.getFirst())
							|| !face.getAddressScheme().includes(face.getLast())) {
						logger.warn("Face has out-of-parity values: " + face.toString());
						faceWithIllegalRangeCount++;
					}
					totalAddressCoverage += (face.getMax() - face.getMin()) / 2 + 1;
					addressedBlockFaceCount++;
				} else {
					face.empty();
					unAddressedBlockFaceCount++;
				}
				for(IRbSite site : seg.getSites(side)) {
					if(site instanceof RbSite) {
						regularCAPSiteCount++;
					}
				}
			}
			for(IRbSite site : seg.getNCAPs()) {
				if(site instanceof RbSite) {
					segmentNCAPCount++;
				}
			}
		}
		logger.info("");
		logger.info("Initial Range Resolution");
		logger.info("-------------------------------------");
		logger.info("Total Side-swaps: " + sideSwapCount);
		logger.info("Unforceable Parities: " + unforceableParityCount);
		logger.info("");
		logger.info("Overlapping Range Resolution");
		logger.info("------------------------------------------------");
		logger.info("Deleted Faces: " + deletedFaces.size());
		logger.info("Unallocatable Sites: " + unallocatableSites.size());
		logger.info("");
		logger.info("Range Stretching");
		logger.info("------------------------------------------------");
		logger.info("Parity Smoothable Count: " + paritySmoothableCount);
		logger.info("Increasing Smoothed Count: " + increasingSmoothedCount);
		logger.info("Successful 2-way Stretches: " + successfulStretchCount);
		logger.info("Successful dead-end Stretches: " + successfulDeadEndStretchCount);
		logger.info("Missing Address count: " + missingAddrCount);
		logger.info("Excessive stretch count: " + excessiveStretchCount);
		logger.info("Overlaps count: " + overlapsCount);
		logger.info("Conflicting parity count: " + parityConflictCount);
		logger.info("Conflicting Directionality count: " + directionConflictCount);
		logger.info("Contains count: " + containsCount);
		logger.info("Loop count: " + loopCount);
		logger.info("3-way count: " + threeWayCount);
		logger.info("4-way count: " + fourWayCount);
		logger.info("5+-way count: " + manyWayCount);
		
		logger.info("");
		logger.info("Address Range Summary Stats");
		logger.info("-----------------------------------------------------------------");
		logger.info("totalAddressCoverage: " + totalAddressCoverage);
		logger.info("addressedBlockFaceCount: " + addressedBlockFaceCount);
		logger.info("unAddressedBlockFaceCount: " + unAddressedBlockFaceCount);
		logger.info("faceWithIllegalRangeCount: " + faceWithIllegalRangeCount);

		logger.info("");
		logger.info("Site Inputs");
		logger.info(String.format("Anchor points generated from input ranges:    %9d", existingRangeAnchorPointCount));
		logger.info("-----------------------------------------------------------------");
		logger.info(String.format("Sites Read from input file:                   %9d", readSiteCount));
		logger.info("-----------------------------------------------------------------");
		logger.info(String.format("Anchor points from pseudo-Sites:              %9d", pseudoSiteAnchorPointCount));
		logger.info(String.format("Regular sites:                                %9d", regularSiteCount));
		logger.info(String.format("Highway Aliases (included in above):          %9d", highwayAliasCount));
		logger.info("-----------------------------------------------------------------");
		int totalInputs = pseudoSiteAnchorPointCount + regularSiteCount;
		if(readSiteCount == totalInputs) {
			logger.info("Inputs balance correctly.");
		} else {
			logger.error("Inputs do not balance! difference: " + (readSiteCount - totalInputs));
		}

		logger.info("");
		logger.info("Pseudo-Site/Anchor Points");
		logger.info("-----------------------------------------------------------------");
		//logger.info(String.format("No segments with matching primary name:       %9d", noCandidatesPseudoCount));
		logger.info(String.format("No segments within distance tolerance(%.0fm): %9d", DISTANCE_TOLERANCE, tooFarFromNearestSegPseudoCount));
		logger.info(String.format("Displaced by overlapping range resolution:    %9d", unallocatablePseudoSiteCount));

		logger.info("");
		logger.info("Site Results");
		logger.info("-----------------------------------------------------------------");
		logger.info(String.format("Outputtable Sites from input:                 %9d", regularSiteCount));
		logger.info("-----------------------------------------------------------------");
		//logger.info(String.format("No segments with matching primary name:       %9d", noCandidatesCount));
		logger.info(String.format("No segments within distance tolerance(%.0fm): %9d", DISTANCE_TOLERANCE, tooFarFromNearestSegCount));
		logger.info(String.format("Displaced by overlapping range resolution:    %9d", unallocatableSites.size()));
		logger.info(String.format("Segment-based NCAPs:                          %9d", segmentNCAPCount));
		logger.info(String.format("Locality-based NCAPs:                         %9d", localityNCAPCount));
		logger.info(String.format("Sub-Sites:                                    %9d", subSiteCount));
		logger.info(String.format("Regular Sites/CAPs:                           %9d", regularCAPSiteCount));
		logger.info("-----------------------------------------------------------------");
		int totalOutputs = tooFarFromNearestSegCount + unallocatableSites.size() + segmentNCAPCount 
				+ localityNCAPCount + subSiteCount + regularCAPSiteCount; 
		if(regularSiteCount == totalOutputs) {
			logger.info("Results balance correctly.");
		} else {
			logger.error("Results do not balance! difference: " + (regularSiteCount - totalOutputs));
		}
	}
	
	private static float getWidth(RbBlockFace face, GeocoderConfig config) {
		RbStreetSegment seg = face.getSegment();
		float laneWidth = config.getRoadLaneWidth(seg.getRoadClass());
		float baseWidth = config.getRoadBaseWidth(seg.getRoadClass());
		float dividerWidth = config.getRoadDividerWidth(seg.getDividerType());
		float narrowMultiplier = 1;
		if(LaneRestriction.NARROW.equals(seg.getLaneRestriction())) {
			narrowMultiplier = config.getRoadNarrowMultiplier();
		}
		if(face.getSegment().isOneWay()) {
			return baseWidth + (seg.getHalfTotalLanes() * laneWidth * narrowMultiplier);
		} else {
			return baseWidth + (seg.getNumLanes(face.getSide()) * laneWidth * narrowMultiplier)
					+ (dividerWidth / 2);
		}
	}
	
	public void writeRanges(TIntObjectHashMap<RbStreetSegment> segmentIdMap, List<IRbSite> extraSites, 
			String baseFilePathString, Map<DateType, LocalDate> dates, String tableSuffix) {
		File streetsFile = new File(baseFilePathString + "street_load_street_segments_geocoder.json");
		RowWriter streetWriter = new JsonRowWriter(streetsFile, "bgeo_street_segments", dates);
		File sitesFile = new File(baseFilePathString + "site_Hybrid_geocoder.tsv");
		List<String> siteSchema = Arrays.asList("SITE_ID","SITE_UUID","PARENT_SITE_ID",
				"SITE_NAME","LOCATION_DESCRIPTOR","UNIT_DESIGNATOR","UNIT_NUMBER","UNIT_NUMBER_SUFFIX",
				"SITE_POSITIONAL_ACCURACY","SITE_STATUS","SITE_CHANGE_DATE","SITE_RETIRE_DATE",
				"SITE_ALBERS_X","SITE_ALBERS_Y","AP_TYPE","IS_PRIMARY_IND","NARRATIVE_LOCATION",
				"ACCESS_POSITIONAL_ACCURACY","CIVIC_NUMBER","CIVIC_NUMBER_SUFFIX","ACCESS_POINT_STATUS",
				"ACCESS_RETIRE_DATE","ACCESS_ALBERS_X","ACCESS_ALBERS_Y","FULL_ADDRESS","STREET_SEGMENT_ID",
				"LOCALITY_ID","PIDS","INPUT_NAME");
		XsvRowWriter siteWriter = new XsvRowWriter(sitesFile, '\t', siteSchema, false);
		int segCount = 0;
		int siteCount = 0;
		BitSet writtenSites = new BitSet(maxSiteId);
		for(TIntObjectIterator<RbStreetSegment> it = segmentIdMap.iterator(); it.hasNext(); ) {
			it.advance();
			RbStreetSegment seg = it.value();
			Map<String,Object> row = new THashMap<String,Object>();
			row.put("STREET_SEGMENT_ID", seg.getSegmentId());
			row.put("START_INTERSECTION_ID", seg.getStartIntersectionId());
			row.put("END_INTERSECTION_ID", seg.getEndIntersectionId());
			row.put("LEFT_LOCALITY_ID", seg.getLocalityId(Side.LEFT));
			row.put("RIGHT_LOCALITY_ID", seg.getLocalityId(Side.RIGHT));
			row.put("FIRST_ADDRESS_LEFT", seg.getFromLeft() == RangeBuilder.NULL_ADDR ? null : seg.getFromLeft());
			row.put("LAST_ADDRESS_LEFT", seg.getToLeft() == RangeBuilder.NULL_ADDR ? null : seg.getToLeft());
			row.put("FIRST_ADDRESS_RIGHT", seg.getFromRight() == RangeBuilder.NULL_ADDR ? null : seg.getFromRight());
			row.put("LAST_ADDRESS_RIGHT", seg.getToRight() == RangeBuilder.NULL_ADDR ? null : seg.getToRight());
			row.put("ADDRESS_PARITY_LEFT", AddressScheme.parityToString(seg.getParityLeft()));
			row.put("ADDRESS_PARITY_RIGHT", AddressScheme.parityToString(seg.getParityRight()));
			row.put("ROAD_CLASS", seg.getRoadClass().toString());
			row.put("NUM_LANES_LEFT", Math.round(seg.getNumLanes(Side.LEFT)));
			row.put("NUM_LANES_RIGHT", Math.round(seg.getNumLanes(Side.RIGHT)));
			row.put("TRAVEL_DIRECTION", seg.getTravelDirection().toString());
			row.put("DIVIDER_TYPE", seg.getDividerType().toString());
			row.put("LANE_RESTRICTION", seg.getLaneRestriction().toString());
			//row.put("START_TRAFFIC_IMPACTOR", seg.);
			//row.put("END_TRAFFIC_IMPACTOR", seg.);
			//row.put("SPEED_LIMIT", seg.);
			//row.put("VIRTUAL_IND", seg.);
			//row.put("SURFACE_TYPE", seg.);
			row.put("LEFT_ELECTORAL_AREA_ID", seg.getElectoralAreaId(Side.LEFT));
			row.put("RIGHT_ELECTORAL_AREA_ID", seg.getElectoralAreaId(Side.RIGHT));
			row.put("geom", seg.getCenterLine());
			streetWriter.writeRow(row);
			segCount++;
			for(Side side : Side.values()) {
				for(IRbSite site : seg.getSites(side)) {
					// only output RbSites, not PseudoSites/AnchorPoints
					if(site instanceof RbSite) {
						Map<String, Object> siteRow = makeSiteRow(site, seg.getLocalityId(side), seg.getSegmentId());
						siteWriter.writeRow(siteRow);
						writtenSites.set(site.getSiteId());
						siteCount++;
					}
				}
			}
			for(IRbSite site : seg.getNCAPs()) {
				// only output RbSites, not PseudoSites/AnchorPoints
				if(site instanceof RbSite) {
					Map<String, Object> siteRow = makeSiteRow(site, site.getLocalityId(), seg.getSegmentId());
					siteWriter.writeRow(siteRow);
					siteCount++;
				}
			}

		}
		for(IRbSite site : extraSites) {
			// only output RbSites, not PseudoSites/AnchorPoints
			// and is either not a child site or the parent has been written
			if(site instanceof RbSite &&
					(site.getParentSiteId() == RowReader.NULL_INT_VALUE 
							|| writtenSites.get(site.getParentSiteId()))) {
				Map<String, Object> siteRow = makeSiteRow(site, site.getLocalityId(), RowReader.NULL_INT_VALUE);
				siteWriter.writeRow(siteRow);
				siteCount++;
			}
		}

		streetWriter.close();
		siteWriter.close();
		logger.info("Segments written: " + segCount);
		logger.info("Sites written: " + siteCount);
	}

	private Map<String, Object> makeSiteRow(IRbSite site, int localityId, int segmentId) {
		Map<String,Object> siteRow = new THashMap<String,Object>(30);
		siteRow.put("SITE_ID", site.getSiteId());
		siteRow.put("SITE_UUID", site.getSiteUuid());
		siteRow.put("PARENT_SITE_ID", convertNull(site.getParentSiteId()));
		siteRow.put("SITE_NAME", site.getSiteName());
		siteRow.put("LOCATION_DESCRIPTOR", site.getLocationDescriptor());
		siteRow.put("UNIT_DESIGNATOR", site.getUnitDesignator());
		siteRow.put("UNIT_NUMBER", site.getUnitNumber());
		siteRow.put("UNIT_NUMBER_SUFFIX", site.getUnitNumberSuffix());
		siteRow.put("SITE_POSITIONAL_ACCURACY", site.getSitePositionalAccuracy());
		siteRow.put("SITE_STATUS", site.getSiteStatus());
		siteRow.put("SITE_CHANGE_DATE", site.getSiteChangeDate());
		siteRow.put("SITE_RETIRE_DATE", site.getSiteRetireDate());
		siteRow.put("SITE_ALBERS_X", Math.round(site.getSiteLocation().getX()));
		siteRow.put("SITE_ALBERS_Y", Math.round(site.getSiteLocation().getY()));
		siteRow.put("AP_TYPE", site.getApType());
		siteRow.put("IS_PRIMARY_IND", site.isPrimary() ? "Y" : "N");
		siteRow.put("NARRATIVE_LOCATION", site.getNarrativeLocation());
		siteRow.put("ACCESS_POSITIONAL_ACCURACY", site.getAccessPositionalAccuracy());
		siteRow.put("CIVIC_NUMBER", convertNull(site.getCivicNumber()));
		siteRow.put("CIVIC_NUMBER_SUFFIX", site.getCivicNumberSuffix());
		siteRow.put("ACCESS_POINT_STATUS", site.getAccessPointStatus());
		siteRow.put("ACCESS_RETIRE_DATE", site.getAccessRetireDate());
		Point accessLocation = site.getAccessLocation();
		if(accessLocation != null) {
			siteRow.put("ACCESS_ALBERS_X", Math.round(accessLocation.getX()));
			siteRow.put("ACCESS_ALBERS_Y", Math.round(accessLocation.getY()));
		} else {
			siteRow.put("ACCESS_ALBERS_X", Math.round(site.getSiteLocation().getX()));
			siteRow.put("ACCESS_ALBERS_Y", Math.round(site.getSiteLocation().getY()));
		}
		siteRow.put("FULL_ADDRESS", site.getFullAddress());
		siteRow.put("STREET_SEGMENT_ID", convertNull(segmentId));
		siteRow.put("LOCALITY_ID", convertNull(localityId));
		siteRow.put("PIDS", site.getPids());
		siteRow.put("INPUT_NAME", site.getInputName());
		return siteRow;
	}
	
	private void openLogWriter() {
		File logFile = new File(outputDataDir + "rangebuilder_log.csv");
		List<String> siteSchema = Arrays.asList("SITE_ID","SITE_UUID","PARENT_SITE_ID",
				"SITE_NAME","LOCATION_DESCRIPTOR","UNIT_DESIGNATOR","UNIT_NUMBER","UNIT_NUMBER_SUFFIX",
				"SITE_POSITIONAL_ACCURACY","SITE_STATUS","SITE_CHANGE_DATE","SITE_RETIRE_DATE",
				"SITE_ALBERS_X","SITE_ALBERS_Y","AP_TYPE","IS_PRIMARY_IND","NARRATIVE_LOCATION",
				"ACCESS_POSITIONAL_ACCURACY","CIVIC_NUMBER","CIVIC_NUMBER_SUFFIX","ACCESS_POINT_STATUS",
				"ACCESS_RETIRE_DATE","ACCESS_ALBERS_X","ACCESS_ALBERS_Y","FULL_ADDRESS","STREET_SEGMENT_ID",
				"LOCALITY_ID","INTERIM_STREET_NAME_ID","INPUT_NAME","MESSAGE");
		logWriter = new XsvRowWriter(logFile, ',', siteSchema, true);
	}
	
	private void logSite(IRbSite site, String message) {
		Map<String, Object> row = makeSiteRow(site, site.getLocalityId(), RowReader.NULL_INT_VALUE);
		row.put("INTERIM_STREET_NAME_ID", site.getInterimStreetNameId());
		row.put("MESSAGE", message);
		logWriter.writeRow(row);
	}
	
	static Integer convertNull(int i) {
		if(i == RowReader.NULL_INT_VALUE) {
			return null;
		} 
		return i;
	}

	private double computeMinDistance(Point pt, LineString line, double terminateDistance) {
		double minDistance = line.getEnvelopeInternal().distance(pt.getEnvelopeInternal());
		if(minDistance > terminateDistance) {
			return Double.MAX_VALUE;
		}
		minDistance = Double.MAX_VALUE;
		Coordinate[] coord0 = line.getCoordinates();
		Coordinate coord = pt.getCoordinate();
		// brute force approach!
		for (int i = 0; i < coord0.length - 1; i++) {
			//double dist = CGAlgorithms.distancePointLine(coord, coord0[i], coord0[i + 1] );
			double dist = Distance.pointToSegment(coord, coord0[i], coord0[i + 1] );
			if (dist < minDistance) {
				minDistance = dist;
			}
		}
		return minDistance;
	  }
}
