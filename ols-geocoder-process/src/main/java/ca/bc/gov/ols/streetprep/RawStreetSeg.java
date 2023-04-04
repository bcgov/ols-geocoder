package ca.bc.gov.ols.streetprep;

import java.util.Map;

import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.enums.AccessRestriction;
import ca.bc.gov.ols.enums.AddressScheme;
import ca.bc.gov.ols.enums.DividerType;
import ca.bc.gov.ols.enums.LaneRestriction;
import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.SurfaceType;
import ca.bc.gov.ols.enums.TrafficImpactor;
import ca.bc.gov.ols.enums.TravelDirection;
import ca.bc.gov.ols.rowreader.RowReader;
import ca.bc.gov.ols.rowreader.RowWriter;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;

public class RawStreetSeg {

	int streetSegmentId; // "TRANSPORT_LINE_ID" / "STREET_SEGMENT_ID"
	int startIntersectionId; // "FROM_TRANSPORT_NODE_POINT_ID" / "START_INTERSECTION_ID"
	int endIntersectionId; // "TO_TRANSPORT_NODE_POINT_ID" / "END_INTERSECTION_ID"
	int leftLocalityId; // "LEFT_LOCALITY_ID" / "LEFT_LOCALITY_ID"
	int rightLocalityId; // "RIGHT_LOCALITY_ID" / "RIGHT_LOCALITY_ID"
	Integer firstAddressLeft; // "FROM_LEFT_HOUSE_NUMBER" / "FIRST_ADDRESS_LEFT" 
	Integer lastAddressLeft; // "TO_LEFT_HOUSE_NUMBER" / "LAST_ADDRESS_LEFT" 
	Integer firstAddressRight; // "FROM_RIGHT_HOUSE_NUMBER" / "FIRST_ADDRESS_RIGHT"
	Integer lastAddressRight; // "TO_RIGHT_HOUSE_NUMBER" / "LAST_ADDRESS_RIGHT"
	AddressScheme addressParityLeft; // "LEFT_HOUSE_NUM_SCHEME_CODE" / "ADDRESS_PARITY_LEFT"
	AddressScheme addressParityRight; // "RIGHT_HOUSE_NUM_SCHEME_CODE" / "ADDRESS_PARITY_RIGHT"
	RoadClass roadClass; // "TRANSPORT_LINE_TYPE_CODE" / "ROAD_CLASS"
	int numLanesLeft; // "LEFT_NUMBER_OF_LANES" / "NUM_LANES_LEFT"
	int numLanesRight; // "RIGHT_NUMBER_OF_LANES" / "NUM_LANES_RIGHT"
	LaneRestriction laneRestriction; // "LANE_RESTRICTION" / "LANE_RESTRICTION_CODE"
	TravelDirection travelDirection; // "TRAVEL_DIRECTION_CODE" / "TRAVEL_DIRECTION"
	DividerType dividerType; // "TRANSPORT_LINE_DIVIDED_CODE" / "DIVIDER_TYPE"
	AccessRestriction accessRestriction; // "ACCESS_RESTRICTION_IND" / "ACCESS_RESTRICTION_CODE"
	TrafficImpactor startTrafficImpactor; // "START_TRAFFIC_IMPACTOR" / "FROM_TRAFFIC_IMPACTOR_CODE"
	TrafficImpactor endTrafficImpactor; // "END_TRAFFIC_IMPACTOR" / "TO_TRAFFIC_IMPACTOR_CODE"
	int speedLimit; // "SPEED_LIMIT" / "SPEED_LIMIT"
	boolean isVirtual; // "VIRTUAL_IND" / "VIRTUAL_IND"
	SurfaceType surfaceType; // "TRANSPORT_LINE_SURFACE_CODE" / "SURFACE_TYPE"
	Integer rightElectoralAreaId; // "RIGHT_ELECTORAL_AREA_ID"
	Integer leftElectoralAreaId; // "LEFT_ELECTORAL_AREA_ID"
	boolean isTruckRoute; // "TRUCK_ROUTE_IND" / "TRUCK_ROUTE_IND"
	// turn restrictions
	String fromLeft; // "FROM_LEFT_TURN_TIME_CODE" / "FROM_LEFT_TURN_RESTRICTION" 	
	String fromCentre; // "FROM_CENTRE_TURN_TIME_CODE" / "FROM_CENTRE_TURN_RESTRICTION"
	String fromRight; // "FROM_RIGHT_TURN_TIME_CODE" / "FROM_RIGHT_TURN_RESTRICTION"
	String toLeft; // "TO_LEFT_TURN_TIME_CODE" / "TO_LEFT_TURN_RESTRICTION"
	String toCentre; // "TO_CENTRE_TURN_TIME_CODE" / "TO_CENTRE_TURN_RESTRICTION"
	String toRight; // "TO_RIGHT_TURN_TIME_CODE" / "TO_RIGHT_TURN_RESTRICTION"	
	// width/weight/height restrictions
	double fromHeight; // "FROM_VEHICLE_MAX_HEIGHT_METRE" / "FROM_VEHICLE_MAX_HEIGHT"	
	double toHeight; // "TO_VEHICLE_MAX_HEIGHT_METRE" / "TO_VEHICLE_MAX_HEIGHT" 	
	double fromWidth; // "FROM_VEHICLE_MAX_WIDTH_METRE" / "FROM_VEHICLE_MAX_WIDTH"
	double toWidth; // "TO_VEHICLE_MAX_WIDTH_METRE" / "TO_VEHICLE_MAX_WIDTH" 
	Integer fromWeight; // "FROM_VEHICLE_MAX_WEIGHT_KG" / "FROM_VEHICLE_MAX_WEIGHT"
	Integer toWeight; // "TO_VEHICLE_MAX_WEIGHT_KG" / "TO_VEHICLE_MAX_WEIGHT"
	String highwayExitNum; // "HIGHWAY_EXIT_NUMBER"
	String highwayRoute1; // "HIGHWAY_ROUTE_1"	
	String highwayRoute2; // "HIGHWAY_ROUTE_2"
	String highwayRoute3; // "HIGHWAY_ROUTE_3"
	TIntArrayList nameIds; // "STRUCTURED_NAME_#_ID"	
	LineString geom;
	
	public RawStreetSeg(RowReader rr) {
		//"TRANSPORT_LINE_ID"	"CREATE_INTEGRATION_SESSION_ID"	"MODIFY_INTEGRATION_SESSION_ID"	"CREATE_PARTNER_ORG_ID"	"MODIFY_PARTNER_ORG_ID"	"CUSTODIAN_PARTNER_ORG_ID"	
		// "DATA_CAPTURE_METHOD_CODE"	"Z_VALUE_DERIVED_IND"	"CAPTURE_DATE"	"DEACTIVATION_DATE"	
		//"TRANSPORT_LINE_TYPE_CODE"	"TRANSPORT_LINE_SURFACE_CODE"	"TRANSPORT_LINE_DIVIDED_CODE"	"TRAVEL_DIRECTION_CODE"	
		// "TRANSPORT_LINE_STRUCTURE_CODE"	"SPEED_LIMIT"	"LEFT_NUMBER_OF_LANES"	"RIGHT_NUMBER_OF_LANES"	"TOTAL_NUMBER_OF_LANES"	"UNDER_CONSTRUCTION_IND"	
		// "VIRTUAL_IND"	"DISASTER_ROUTE_IND"	"TRUCK_ROUTE_IND"	"LEFT_LOCALITY_ID"	"RIGHT_LOCALITY_ID"	"LEFT_REGIONAL_DISTRICT_ID"	"RIGHT_REGIONAL_DISTRICT_ID"	
		// "STRUCTURED_NAME_1_ID"	"STRUCTURED_NAME_2_ID"	"STRUCTURED_NAME_3_ID"	"STRUCTURED_NAME_4_ID"	"STRUCTURED_NAME_5_ID"	"STRUCTURED_NAME_6_ID"	"STRUCTURED_NAME_7_ID"	
		// "HIGHWAY_ROUTE_1"	"HIGHWAY_ROUTE_2"	"HIGHWAY_ROUTE_3"	"HIGHWAY_EXIT_NUMBER"	"INDUSTRY_NAME_1"	"INDUSTRY_NAME_2"	"INDUSTRY_NAME_3"	
		// "SINGLE_HOUSE_NUMBER"	"LEFT_HOUSE_NUM_SCHEME_CODE"	"FROM_LEFT_HOUSE_NUMBER"	"TO_LEFT_HOUSE_NUMBER"	
		// "RIGHT_HOUSE_NUM_SCHEME_CODE"	"FROM_RIGHT_HOUSE_NUMBER"	"TO_RIGHT_HOUSE_NUMBER"	
		// "LANE_RESTRICTION_CODE"	"ACCESS_RESTRICTION_CODE"	"FROM_TRAFFIC_IMPACTOR_CODE"	"TO_TRAFFIC_IMPACTOR_CODE"	
		// "FROM_LEFT_TURN_TIME_CODE"	"FROM_CENTRE_TURN_TIME_CODE"	"FROM_RIGHT_TURN_TIME_CODE"	"TO_LEFT_TURN_TIME_CODE"	"TO_CENTRE_TURN_TIME_CODE"	"TO_RIGHT_TURN_TIME_CODE"	
		// "FROM_VEHICLE_MAX_WEIGHT_KG"	"TO_VEHICLE_MAX_WEIGHT_KG"	"FROM_VEHICLE_MAX_WIDTH_METRE"	"TO_VEHICLE_MAX_WIDTH_METRE"	
		// "FROM_VEHICLE_MAX_HEIGHT_METRE"	"TO_VEHICLE_MAX_HEIGHT_METRE"	
		// "MINISTRY_OF_TRANSPORT_ID"	"MINISTRY_OF_TRANSPORT_NAME"	"INTEGRATION_NOTES"	"EXCLUDED_RULES"	
		// "GEOMETRY"	"DEMOGRAPHIC_IND"	"EXTENDED_DATA"	"MINISTRY_OF_TRANSPORT_DATA"	"FROM_NAVIGATION_RULES"	"ALONG_NAVIGATION_RULES"	"TO_NAVIGATION_RULES"

		streetSegmentId = rr.getInt("TRANSPORT_LINE_ID");
		startIntersectionId = rr.getInt("FROM_TRANSPORT_NODE_POINT_ID");
		endIntersectionId = rr.getInt("TO_TRANSPORT_NODE_POINT_ID");
		leftLocalityId = rr.getInt("LEFT_LOCALITY_ID");
		rightLocalityId = rr.getInt("RIGHT_LOCALITY_ID");
		firstAddressLeft = rr.getInteger("FROM_LEFT_HOUSE_NUMBER");
		lastAddressLeft = rr.getInteger("TO_LEFT_HOUSE_NUMBER");
		firstAddressRight = rr.getInteger("FROM_RIGHT_HOUSE_NUMBER");
		lastAddressRight = rr.getInteger("TO_RIGHT_HOUSE_NUMBER");
		addressParityLeft = AddressScheme.convert(rr.getString("LEFT_HOUSE_NUM_SCHEME_CODE"));
		addressParityRight = AddressScheme.convert(rr.getString("RIGHT_HOUSE_NUM_SCHEME_CODE"));
		roadClass = RoadClass.convert(rr.getString("TRANSPORT_LINE_TYPE_CODE"));
		numLanesLeft = rr.getInt("LEFT_NUMBER_OF_LANES");
		numLanesRight = rr.getInt("RIGHT_NUMBER_OF_LANES");
		// TODO: do we need to check this value? rr.getInt("TOTAL_NUMBER_OF_LANES");
		laneRestriction = LaneRestriction.convert(rr.getString("LANE_RESTRICTION_CODE"));
		travelDirection = TravelDirection.convert(rr.getString("TRAVEL_DIRECTION_CODE"));
		dividerType = DividerType.convert(rr.getString("TRANSPORT_LINE_DIVIDED_CODE"));
		accessRestriction = AccessRestriction.convert(rr.getString("ACCESS_RESTRICTION_CODE"));
		startTrafficImpactor = TrafficImpactor.convert(rr.getString("FROM_TRAFFIC_IMPACTOR_CODE")); 
		endTrafficImpactor = TrafficImpactor.convert(rr.getString("TO_TRAFFIC_IMPACTOR_CODE")); 
		speedLimit = rr.getInt("SPEED_LIMIT");
		isVirtual = rr.getBoolean("VIRTUAL_IND");
		surfaceType= SurfaceType.convert(rr.getString("TRANSPORT_LINE_SURFACE_CODE"));
		isTruckRoute = rr.getBoolean("TRUCK_ROUTE_IND");
		// turn restrictions
		fromLeft = rr.getString("FROM_LEFT_TURN_TIME_CODE");
		fromCentre = rr.getString("FROM_CENTRE_TURN_TIME_CODE");
		fromRight = rr.getString("FROM_RIGHT_TURN_TIME_CODE");
		toLeft = rr.getString("TO_LEFT_TURN_TIME_CODE");
		toCentre = rr.getString("TO_CENTRE_TURN_TIME_CODE");
		toRight = rr.getString("TO_RIGHT_TURN_TIME_CODE");
		// width/weight/height restrictions
		fromHeight = rr.getDouble("FROM_VEHICLE_MAX_HEIGHT_METRE");
		toHeight = rr.getDouble("TO_VEHICLE_MAX_HEIGHT_METRE");
		fromWidth = rr.getDouble("FROM_VEHICLE_MAX_WIDTH_METRE");
		toWidth = rr.getDouble("TO_VEHICLE_MAX_WIDTH_METRE");
		fromWeight = rr.getInteger("FROM_VEHICLE_MAX_WEIGHT_KG");
		toWeight = rr.getInteger("TO_VEHICLE_MAX_WEIGHT_KG");
		highwayExitNum = rr.getString("HIGHWAY_EXIT_NUMBER");
		highwayRoute1 = rr.getString("HIGHWAY_ROUTE_1");
		highwayRoute2 = rr.getString("HIGHWAY_ROUTE_2");
		highwayRoute3 = rr.getString("HIGHWAY_ROUTE_3");
		geom = rr.getLineString("GEOMETRY");
		
		nameIds = new TIntArrayList(7);
		for(int i = 1; i <= 7; i++) {
			Integer nameId = rr.getInteger("STRUCTURED_NAME_" + i + "_ID");
			if(nameId != null && !nameIds.contains(nameId)) {
				nameIds.add(nameId);
			}
		}
		nameIds.trimToSize();
	}

	public void write(RowWriter rw) {
		Map<String, Object> row = new THashMap<String, Object>();
		row.put("STREET_SEGMENT_ID", streetSegmentId);
		row.put("START_INTERSECTION_ID", startIntersectionId);
		row.put("END_INTERSECTION_ID", endIntersectionId);
		row.put("LEFT_LOCALITY_ID", leftLocalityId);
		row.put("RIGHT_LOCALITY_ID", rightLocalityId);
		row.put("FIRST_ADDRESS_LEFT", firstAddressLeft);
		row.put("LAST_ADDRESS_LEFT", lastAddressLeft);
		row.put("FIRST_ADDRESS_RIGHT", firstAddressRight);
		row.put("LAST_ADDRESS_RIGHT", lastAddressRight);
		row.put("ADDRESS_PARITY_LEFT", addressParityLeft);
		row.put("ADDRESS_PARITY_RIGHT", addressParityRight);
		row.put("ROAD_CLASS", roadClass);
		row.put("NUM_LANES_LEFT", numLanesLeft);
		row.put("NUM_LANES_RIGHT", numLanesRight);
		row.put("LANE_RESTRICTION", laneRestriction);
		row.put("TRAVEL_DIRECTION", travelDirection);
		row.put("DIVIDER_TYPE", dividerType);
		row.put("ACCESS_RESTRICTION_IND", accessRestriction);
		row.put("START_TRAFFIC_IMPACTOR", startTrafficImpactor);
		row.put("END_TRAFFIC_IMPACTOR", endTrafficImpactor);
		row.put("SPEED_LIMIT", speedLimit);
		row.put("VIRTUAL_IND", isVirtual ? "Y" : "N");
		row.put("SURFACE_TYPE", surfaceType);
		row.put("RIGHT_ELECTORAL_AREA_ID", rightElectoralAreaId); 
		row.put("LEFT_ELECTORAL_AREA_ID", leftElectoralAreaId); 
		row.put("TRUCK_ROUTE_IND", isTruckRoute ? "Y" : "N");
		// turn restrictions
		row.put("FROM_LEFT_TURN_RESTRICTION", fromLeft);	
		row.put("FROM_CENTRE_TURN_RESTRICTION", fromCentre);
		row.put("FROM_RIGHT_TURN_RESTRICTION", fromRight);
		row.put("TO_LEFT_TURN_RESTRICTION", toLeft);	
		row.put("TO_CENTRE_TURN_RESTRICTION", toCentre);	
		row.put("TO_RIGHT_TURN_RESTRICTION", toRight);	
		// width/weight/height restrictions
		if(!Double.isNaN(fromHeight)) row.put("FROM_VEHICLE_MAX_HEIGHT", fromHeight);	
		if(!Double.isNaN(toHeight)) row.put("TO_VEHICLE_MAX_HEIGHT", toHeight);	
		if(!Double.isNaN(fromWidth)) row.put("FROM_VEHICLE_MAX_WIDTH", fromWidth);	
		if(!Double.isNaN(toWidth)) row.put("TO_VEHICLE_MAX_WIDTH", toWidth);	
		row.put("FROM_VEHICLE_MAX_WEIGHT", fromWeight);	
		row.put("TO_VEHICLE_MAX_WEIGHT", toWeight);
		// route numbers
		row.put("HIGHWAY_ROUTE_1", highwayRoute1);
		row.put("HIGHWAY_ROUTE_2", highwayRoute2);
		row.put("HIGHWAY_ROUTE_3", highwayRoute3);
		row.put("geom", geom);
		rw.writeRow(row);
	}
}
