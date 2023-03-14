package ca.bc.gov.ols.streetprep;

import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.StateProvTerr;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocalityType;
import ca.bc.gov.ols.rowreader.CsvRowReader;
import ca.bc.gov.ols.rowreader.DateType;
import ca.bc.gov.ols.rowreader.JsonRowReader;
import ca.bc.gov.ols.rowreader.JsonRowWriter;
import ca.bc.gov.ols.rowreader.RowComparer;
import ca.bc.gov.ols.rowreader.RowReader;
import ca.bc.gov.ols.rowreader.RowWriter;
import ca.bc.gov.ols.rowreader.ShapefileRowReader;
import ca.bc.gov.ols.rowreader.TsvRowReader;
import ca.bc.gov.ols.siteloaderprep.RawStreetName;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;


public class StreetPrep {
	private static final Logger logger = LoggerFactory.getLogger(StreetPrep.class.getCanonicalName());

	private static String inputDir; // = "L:\\Refractions\\MLCSOG\\Batch_Geocoder_2011-12\\FME2Java\\B1 street prep\\inputs\\";
	private static String outputDir; // = "L:\\Refractions\\MLCSOG\\Batch_Geocoder_2011-12\\FME2Java\\B1 street prep\\outputs\\java\\";
	private static String compareDir; // = "L:\\Refractions\\MLCSOG\\Batch_Geocoder_2011-12\\FME2Java\\B1 street prep\\outputs\\fme\\";

	private static final int SRID = 3005;
	
	// input filenames
	private static final String TRANSPORT_LINE_DEMOGRAPHIC_FILE = "TRANSPORT_LINE_DEMOGRAPHIC.tsv";
	private static final String STRUCTURED_NAME_FILE = "STRUCTURED_NAME.tsv";
	private static final String NAME_PREFIX_CODE_FILE = "NAME_PREFIX_CODE.tsv";
	private static final String NAME_SUFFIX_CODE_FILE = "NAME_SUFFIX_CODE.tsv";
	private static final String NAME_DESCRIPTOR_CODE_FILE = "NAME_DESCRIPTOR_CODE.tsv";
	private static final String NAME_DIRECTION_CODE_FILE = "NAME_DIRECTION_CODE.tsv";
	private static final String LOCALITY_POLY_FILE = "LOCALITY_POLY.tsv";
	private static final String CUSTOM_CITY_POLY_FILE = "custom_city_polygons.shp";
	private static final String ABMS_ELECT_POLY_FILE = "ABMS_ELECT_polygon.shp";
	private static final String STATE_PROV_TERR_FILE = "state_prov_terrs.csv";
	private static final String BCGNIS_FILE = "bcgnis_input.csv";
	private static final String LOCALITY_LOCATIONS_FILE = "locality-locations.csv";
	
	// output filenames
	private static final String STREET_LOAD_STREET_NAMES_FILE = "street_load_street_names.json";
	private static final String STREET_LOAD_LOCALITIES_FILE = "street_load_localities.json";
	private static final String STREET_LOAD_STATE_PROV_TERR_FILE  = "street_load_state_prov_terrs.json";
	private static final String STREET_LOAD_ELECTORAL_AREAS_FILE  = "street_load_electoral_areas.json";
	private static final String STREET_LOAD_STREET_LOCALITY_CENTROIDS_FILE  = "street_load_street_locality_centroids.json";
	private static final String STREET_LOAD_STREET_NAME_ON_SEG_XREF_FILE  = "street_load_street_name_on_seg_xref.json";
	private static final String STREET_LOAD_STREET_INTERSECTIONS_FILE  = "street_load_street_intersections.json";
	private static final String STREET_LOAD_STREET_SEGMENTS_FILE  = "street_load_street_segments.json";
	private static final String STREET_LOAD_LOCALITY_MAPPINGS_FILE  = "street_load_locality_mappings.json";
	
	//private static final String STREET_LOAD_STREET_LOCALITY_CENTROIDS_DIFF_FILE  = "street_load_street_locality_centroids_diff.json";

	
	// we only care about a limited set of GNIS Features types
	private static final Set<Integer> ALLOWABLE_GNIS_FCODES = Set.of(1,2,3,16,35,39,108,114,121,152,186,187,200,206,408,512,543);
	
	// set of "locality name|FCODE" for localities to drop/ignore from BCGNIS 
	private final static Set<String> GNIS_TO_DROP = Set.of(
			"Abbotsford|121", 
			"Chilliwack|121", 
			"Esquimalt|512", 
			"Hornby Island|114", 
			"Masset|121", 
			"Mission|121", 
			"Pitt Meadows|121", 
			"Port Essington|512", 
			"Salmon Arm|121", 
			"Tumbler Ridge|121", 
			"Wells|121", 
			"Whistler|187"
	);
	
	// Set of ITN localities which shouldn't have Electoral Areas associated with them
	private static final Set<String> NO_EA_LOCALITY_NAMES = Set.of(
			 "Abbotsford",
			 "Agassiz",
			 "Anmore",
			 "Armstrong",
			 "Belcarra",
			 "Bowen Island",
			 "Burnaby",
			 "Castlegar",
			 "Central Saanich",
			 "Chemainus",
			 "Chilliwack",
			 "Colwood",
			 "Coquitlam",
			 "Crofton",
			 "Dawson Creek",
			 "Delta",
			 "Driftwood River",
			 "Harrison Hot Springs",
			 "Hazelton",
			 "Highlands",
			 "Langford",
			 "Langley",
			 "Lina Island",
			 "Mackenzie",
			 "Maple Ridge",
			 "Metchosin",
			 "Mission",
			 "New Hazelton",
			 "New Westminster",
			 "North Saanich",
			 "North Vancouver",
			 "Oak Bay",
			 "Pitt Meadows",
			 "Port Coquitlam",
			 "Port Edward",
			 "Port Moody",
			 "Prince Rupert",
			 "Richmond",
			 "Saanich",
			 "Sidney",
			 "Sooke",
			 "Spallumcheen",
			 "Taylor",
			 "Vancouver",
			 "Victoria",
			 "View Royal",
			 "West Vancouver",
			 "White Rock",
			 "Westholme"
	);
	
	// Map from LocalityName|LocalityId to StateProvTerr name
	// anything not listed here is assumed to be BC
	private static final Map<String, String> INITIAL_SPT_MAP;
	static {
		INITIAL_SPT_MAP = new HashMap<String, String>();
		INITIAL_SPT_MAP.put("Alaska|797", "AK"); 
		INITIAL_SPT_MAP.put("Banff|983", "AB");
		INITIAL_SPT_MAP.put("Carcross|974", "YT");
		INITIAL_SPT_MAP.put("Contact Creek|280", "YT");
		INITIAL_SPT_MAP.put("Crowsnest Pass|984", "AB");
		INITIAL_SPT_MAP.put("Fort Liard|978", "NWT");
		INITIAL_SPT_MAP.put("Grande Prairie|981", "AB");
		INITIAL_SPT_MAP.put("Haines|349", "AK");
		INITIAL_SPT_MAP.put("Haines Junction|970", "YT");
		INITIAL_SPT_MAP.put("Hyder|979", "AK");
		INITIAL_SPT_MAP.put("Idaho|990", "ID");
		INITIAL_SPT_MAP.put("Jakes Corner|976", "YT");
		INITIAL_SPT_MAP.put("Jasper|982", "AB");
		INITIAL_SPT_MAP.put("Montana|988", "MT");
		INITIAL_SPT_MAP.put("Skagway|971", "AK");
		INITIAL_SPT_MAP.put("Swift River|986", "YT");
		INITIAL_SPT_MAP.put("Teslin|987", "YT");
		INITIAL_SPT_MAP.put("Washington|989", "WA");
		INITIAL_SPT_MAP.put("Watson Lake|282", "YT");
		INITIAL_SPT_MAP.put("Whitehorse|973", "YT");
	}
	
	private static final int SIDE_POINT_OFFSET = 2; // meters, to offset a point from a segment for left/right PIP
	private static final double CUSTOM_LOCALITY_LOCATION_TOLERANCE = 15000; // meters 
	private static final int GNIS_FUDGE_FACTOR = 25; // meters, to allow a GNIS point to be outside of a same-name locality polygon and still match it

	// meters tolerance for Douglas-Peucker simplifier, 0.0 disables simplification
	private static final double GEOM_SIMPLIFY_TOLERANCE = 0.0;
	// for tracking the effectiveness of the simplification, if applied
	private int originalPoints = 0; 
	private int reducedPoints = 0; 

	private static final int FIRST_GNIS_LOCALITY_ID = 10000;
	private static final int FIRST_EXIT_NAME_ID = 250000;
	
	private GeometryFactory geometryFactory;
	private Map<DateType,LocalDate> dates; 
	
	private Map<String,MathTransform> transformToAlbers = new HashMap<String,MathTransform>();
	
	public static void main(String[] args) {
		if(args.length < 1) {
			logger.error("Data directory parameter is required.");
			System.exit(-1);
		}
		String dir = args[0];
		File f = new File(dir);
		if(!f.isDirectory()) {
			logger.error("Invalid data dir: '" + dir +"'");
			System.exit(-1);
		}
		inputDir = addTrailingSeparator(dir);
		if(args.length >= 2) {
			dir = args[1];
			f = new File(dir);
			if(!f.isDirectory()) {
				logger.error("Invalid output dir: '" + dir +"'");
				System.exit(-1);
			}
			outputDir = addTrailingSeparator(dir);;
		} else {
			outputDir = inputDir;
		}
		
		if(args.length >= 3) {
			dir = args[2];
			f = new File(dir);
			if(!f.isDirectory()) {
				logger.error("Invalid compare dir: '" + dir +"'");
				System.exit(-1);
			}
			compareDir = addTrailingSeparator(dir);
		}
		if(args.length > 3) {
			logger.error("Too many parameters; expected 1-3, inputDir outputDir compareDir");
			System.exit(-1);
		}
		StreetPrep prep = new StreetPrep();
		prep.run();
	}
	
	public StreetPrep() {
		geometryFactory = new GeometryFactory(GeocoderConfig.BASE_PRECISION_MODEL, SRID);
		setupTransforms();
	}
	
	public void run() {
		dates = new HashMap<DateType,LocalDate>();
		dates.put(DateType.PROCESSING_DATE, LocalDate.now());
		
		// Localities
		Map<String, StateProvTerr> sptMap = readStateProvTerr();
		Map<String,Point> locTweakMap = readLocalityLocations();
		TIntObjectMap<RawLocality> localityMap = readLocalities(locTweakMap, sptMap);
		List<CustomCity> ccList = readCustomCityPolys(localityMap);
		TIntSet noEaLocalities = buildNoEaLocalities(localityMap);
		List<RawLocality> gnisLocalities = readBCGNIS(locTweakMap, ccList);
		PointInPolygonCoverageIndex<RawLocality> localityPolyIndex = buildLocalityPolyIndex(localityMap);
		applyGNISLocalities(localityMap, localityPolyIndex, gnisLocalities);
		TIntObjectMap<RawElectoralArea> eaMap = readElectoralAreas();
		PointInPolygonCoverageIndex<RawElectoralArea> eaPolyIndex = buildElectoralAreaPolyIndex(eaMap);
		applyLocalityElectoralAreas(localityMap, eaPolyIndex);
		
		// LocalityMappings
		Map<String, RawLocalityMapping> localityMappingMap = buildLocalityMappings(localityMap, localityPolyIndex);
		
		// StreetNames
		Map<String,String> streetTypeMap = readStreetTypes();
		Map<String, String> streetQualMap = readStreetDescriptors();
		Map<String, String> streetDirMap = readStreetDirections();
		TIntObjectMap<RawStreetName> streetNameIdMap = readStreetNames(streetTypeMap, streetQualMap, streetDirMap);
		
		// Streets Segments
		TIntObjectMap<RawStreetSeg> segMap = readTransportLines();
		correctSegmentLocalities(segMap, localityMap);
		applySegmentElectoralAreas(segMap, eaPolyIndex, noEaLocalities);
		addHwyAndExitNames(streetNameIdMap, segMap);
		
		// write outputs
		writeStateProvTerrs(sptMap);
		writeElectoralAreas(eaMap);
		writeLocalities(localityMap);
		writeLocalityMappings(localityMappingMap);
		writeStreetNames(streetNameIdMap);
		writeStreetSegments(segMap);
		writeStreetNameOnSegs(segMap, streetNameIdMap);
		writeStreetIntersections(buildStreetIntersections(segMap));
		writeStreetLocalityCentroids(buildStreetLocalityCentroids(segMap));
		
		// compare with previous outputs, eg. from FME
		if(compareDir != null) {
			logger.info("Comparing {}", STREET_LOAD_STATE_PROV_TERR_FILE);
			try(RowReader rr1 = new JsonRowReader(outputDir + STREET_LOAD_STATE_PROV_TERR_FILE, geometryFactory);
					RowReader rr2 = new JsonRowReader(compareDir + STREET_LOAD_STATE_PROV_TERR_FILE, geometryFactory)) {
				RowComparer comp = new RowComparer(rr1, rr2);
				comp.compare(Arrays.asList("STATE_PROV_TERR_ID", "STATE_PROV_TERR_NAME", "COUNTRY_CODE", "geom"));
			}
			logger.info("Comparing {}", STREET_LOAD_ELECTORAL_AREAS_FILE);
			try(RowReader rr1 = new JsonRowReader(outputDir + STREET_LOAD_ELECTORAL_AREAS_FILE, geometryFactory);
					RowReader rr2 = new JsonRowReader(compareDir + STREET_LOAD_ELECTORAL_AREAS_FILE, geometryFactory)) {
				RowComparer comp = new RowComparer(rr1, rr2);
				comp.compare(Arrays.asList("ELECTORAL_AREA_ID", "ELECTORAL_AREA_NAME"));
			}
			logger.info("Comparing {}", STREET_LOAD_LOCALITIES_FILE);
			try(RowReader rr1 = new JsonRowReader(outputDir + STREET_LOAD_LOCALITIES_FILE, geometryFactory);
					RowReader rr2 = new JsonRowReader(compareDir + STREET_LOAD_LOCALITIES_FILE, geometryFactory)) {
				RowComparer comp = new RowComparer(rr1, rr2);
				comp.setOutputLimit(0);
				comp.compare(Arrays.asList("LOCALITY_ID", "LOCALITY_NAME", "LOCALITY_TYPE_ID", "STATE_PROV_TERR_ID", "ELECTORAL_AREA_ID"),
						Arrays.asList("LOCALITY_NAME", "LOCALITY_TYPE_ID", "STATE_PROV_TERR_ID", "ELECTORAL_AREA_ID"), 
						o -> ((Number)o.get("LOCALITY_ID")).intValue() < FIRST_GNIS_LOCALITY_ID ? o.get("LOCALITY_ID").toString() : (String)o.get("LOCALITY_NAME"));
			}
			logger.info("Comparing {} (note: 100% extra/missing expected due to using differently punctuated/cased name strings)", STREET_LOAD_LOCALITY_MAPPINGS_FILE);
			try(RowReader rr1 = new JsonRowReader(outputDir + STREET_LOAD_LOCALITY_MAPPINGS_FILE, geometryFactory);
					RowReader rr2 = new JsonRowReader(compareDir + STREET_LOAD_LOCALITY_MAPPINGS_FILE, geometryFactory)) {
				RowComparer comp = new RowComparer(rr1, rr2);
				comp.setOutputLimit(0);
				comp.compare(Arrays.asList("LOCALITY_ID", "INPUT_STRING", "CONFIDENCE", "USER_DEFINED_IND", "ACTIVE_IND"),
						Arrays.asList("LOCALITY_ID", "INPUT_STRING", "CONFIDENCE", "USER_DEFINED_IND", "ACTIVE_IND"), 
						o -> (String)o.get("INPUT_STRING") + "|" + o.get("LOCALITY_ID").toString());
			}
			logger.info("Comparing {}", STREET_LOAD_STREET_NAMES_FILE);
			try(RowReader rr1 = new JsonRowReader(outputDir + STREET_LOAD_STREET_NAMES_FILE, geometryFactory);
					RowReader rr2 = new JsonRowReader(compareDir + STREET_LOAD_STREET_NAMES_FILE, geometryFactory)) {
				RowComparer comp = new RowComparer(rr1, rr2);
				comp.compare(Arrays.asList("STREET_NAME_ID", "NAME_BODY", "STREET_TYPE", "STREET_TYPE_IS_PREFIX_IND", "STREET_DIRECTION", "STREET_DIRECTION_IS_PREFIX_IND", "STREET_QUALIFIER"),
						Arrays.asList("NAME_BODY", "STREET_TYPE", "STREET_TYPE_IS_PREFIX_IND", "STREET_DIRECTION", "STREET_DIRECTION_IS_PREFIX_IND", "STREET_QUALIFIER"),
						o -> ((Number)o.get("STREET_NAME_ID")).intValue() < FIRST_EXIT_NAME_ID ? o.get("STREET_NAME_ID").toString() : (String)o.get("NAME_BODY"));
			}
			logger.info("Comparing {}", STREET_LOAD_STREET_NAME_ON_SEG_XREF_FILE);
			try(RowReader rr1 = new JsonRowReader(outputDir + STREET_LOAD_STREET_NAME_ON_SEG_XREF_FILE, geometryFactory);
					RowReader rr2 = new JsonRowReader(compareDir + STREET_LOAD_STREET_NAME_ON_SEG_XREF_FILE, geometryFactory)) {
				RowComparer comp = new RowComparer(rr1, rr2);
				comp.setOutputLimit(0);
				comp.setFilter(o -> ((Number)o.get("STREET_NAME_ID")).intValue() < FIRST_EXIT_NAME_ID);
				comp.compare(Arrays.asList("STREET_NAME_ID", "STREET_SEGMENT_ID", "IS_PRIMARY_IND"),
						Arrays.asList("IS_PRIMARY_IND"),
						o -> o.get("STREET_NAME_ID").toString() + "|" + o.get("STREET_SEGMENT_ID").toString());
			}
			logger.info("Comparing {}", STREET_LOAD_STREET_SEGMENTS_FILE);
			try(RowReader rr1 = new JsonRowReader(outputDir + STREET_LOAD_STREET_SEGMENTS_FILE, geometryFactory);
					RowReader rr2 = new JsonRowReader(compareDir + STREET_LOAD_STREET_SEGMENTS_FILE, geometryFactory)) {
				RowComparer comp = new RowComparer(rr1, rr2);
				comp.setOutputLimit(20);
				comp.compare(Arrays.asList(
						"STREET_SEGMENT_ID", 
						"START_INTERSECTION_ID", "END_INTERSECTION_ID", 
						"LEFT_LOCALITY_ID", "RIGHT_LOCALITY_ID", 
						//"FIRST_ADDRESS_LEFT", "LAST_ADDRESS_LEFT", "FIRST_ADDRESS_RIGHT", "LAST_ADDRESS_RIGHT", 
						//"ADDRESS_PARITY_LEFT", "ADDRESS_PARITY_RIGHT", 
						"ROAD_CLASS", "NUM_LANES_LEFT", "NUM_LANES_RIGHT", "LANE_RESTRICTION_CODE", "TRAVEL_DIRECTION", 
						"DIVIDER_TYPE", "ACCESS_RESTRICTION_CODE",
						"FROM_TRAFFIC_IMPACTOR_CODE", "TO_TRAFFIC_IMPACTOR_CODE", 
						"SPEED_LIMIT", "VIRTUAL_IND", 
						//"SURFACE_TYPE", 
						"RIGHT_ELECTORAL_AREA_ID", "LEFT_ELECTORAL_AREA_ID", 
						"TRUCK_ROUTE_IND", 
						"FROM_LEFT_TURN_RESTRICTION", "FROM_CENTRE_TURN_RESTRICTION", "FROM_RIGHT_TURN_RESTRICTION", 
						"TO_LEFT_TURN_RESTRICTION", "TO_CENTRE_TURN_RESTRICTION", "TO_RIGHT_TURN_RESTRICTION", 
						"FROM_VEHICLE_MAX_HEIGHT", "TO_VEHICLE_MAX_HEIGHT", 
						"FROM_VEHICLE_MAX_WIDTH", "TO_VEHICLE_MAX_WIDTH", 
						"FROM_VEHICLE_MAX_WEIGHT", "TO_VEHICLE_MAX_WEIGHT"
				));
			}
			logger.info("Comparing {}", STREET_LOAD_STREET_INTERSECTIONS_FILE);
			try(RowReader rr1 = new JsonRowReader(outputDir + STREET_LOAD_STREET_INTERSECTIONS_FILE, geometryFactory);
					RowReader rr2 = new JsonRowReader(compareDir + STREET_LOAD_STREET_INTERSECTIONS_FILE, geometryFactory)) {
				RowComparer comp = new RowComparer(rr1, rr2);
				comp.compare(Arrays.asList("STREET_INTERSECTION_ID", "DEGREE"));
			}
			logger.info("Comparing {}", STREET_LOAD_STREET_LOCALITY_CENTROIDS_FILE);
			try(RowReader rr1 = new JsonRowReader(outputDir + STREET_LOAD_STREET_LOCALITY_CENTROIDS_FILE, geometryFactory);
					RowReader rr2 = new JsonRowReader(compareDir + STREET_LOAD_STREET_LOCALITY_CENTROIDS_FILE, geometryFactory)) {
					//RowWriter geomDiffWriter = new JsonRowWriter(new File(outputDir + STREET_LOAD_STREET_LOCALITY_CENTROIDS_DIFF_FILE), null)) {
				RowComparer comp = new RowComparer(rr1, rr2);
				comp.setFilter(o -> ((Number)o.get("STREET_NAME_ID")).intValue() < FIRST_EXIT_NAME_ID);
				comp.setOutputAverageDistance();
				//comp.setOutputGeomDiff(geomDiffWriter);
				comp.compare(Arrays.asList("STREET_NAME_ID", "LOCALITY_ID", "geom"), 
						Collections.emptyList(),
						o -> o.get("STREET_NAME_ID").toString() + "|" + o.get("LOCALITY_ID").toString());
			}
		}
	}

	private void setupTransforms() {
		try {
			CoordinateReferenceSystem nad27 = CRS.decode("EPSG:4267");
			CoordinateReferenceSystem nad83 = CRS.decode("EPSG:4269");
			CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");
			CoordinateReferenceSystem bcalbers = CRS.decode("EPSG:3005");
			transformToAlbers.put("NAD27", CRS.findMathTransform(nad27, bcalbers));
			transformToAlbers.put("NAD83", CRS.findMathTransform(nad83, bcalbers));
			transformToAlbers.put("WGS84", CRS.findMathTransform(wgs84, bcalbers));
		} catch(FactoryException e) {
			throw new RuntimeException("Unexpected error in coordinate reprojection.");
		}
	}
	
	private Point doTransform(Point p, MathTransform transform) {
		try {
			Point newP = (Point)JTS.transform(p, transform);
			newP.setSRID(3005);
			return newP;
		} catch (TransformException te) {
			throw new RuntimeException("Unexpected error in coordinate reprojection.");
		}
	}
	
	private Map<String,StateProvTerr> readStateProvTerr() {
		Map<String,StateProvTerr> sptMap = new HashMap<String, StateProvTerr>();
		try(RowReader rr = new CsvRowReader(inputDir + STATE_PROV_TERR_FILE, geometryFactory)) {
			while(rr.next()) {
				int id = rr.getInt("STATE_PROV_TERR_ID");
				String name = rr.getString("STATE_PROV_TERR_NAME");
				String countryCode = rr.getString("COUNTRY_CODE");
				Point loc = rr.getPoint();
				StateProvTerr spt = new StateProvTerr(id, name, countryCode, loc);
				sptMap.put(name, spt);
			}
		}
		return sptMap;
	}

	private Map<String,Point> readLocalityLocations() {
		Map<String,Point> locTweakMap = new HashMap<String,Point>();
		
		try(RowReader rr = new CsvRowReader(inputDir + LOCALITY_LOCATIONS_FILE, geometryFactory)) {
			// localityName,sourceType,province,latitude,longitude,comment
			while(rr.next()) {
				String name = rr.getString("localityName");
				if(name == null || name.isEmpty()) {
					logger.warn("Invalid line in {}, no localityName value provided", LOCALITY_LOCATIONS_FILE);
					continue;
				}
				String source = rr.getString("sourceType");
				if(!"ITN".equals(source) && !"BCGNIS".equals(source)) {
					logger.warn("Invalid line in {}, invalid sourceType value provided: '{}'", LOCALITY_LOCATIONS_FILE, source);
					continue;
				}
				double lat = rr.getDouble("latitude");
				double lon = rr.getDouble("longitude");
				Point loc = geometryFactory.createPoint(new Coordinate(lat,lon));
				loc = doTransform(loc, transformToAlbers.get("NAD83"));
				Point existing = locTweakMap.putIfAbsent(source + "|" + name, loc);
				if(existing != null) {
					logger.error("Duplicate localityName: '{}' in file: {}", name, LOCALITY_LOCATIONS_FILE);
				}
			}
		}
		return locTweakMap;
	}

	private List<RawLocality> readBCGNIS(Map<String,Point> locTweakMap, List<CustomCity> ccList) {
		List<RawLocality> gnisLocalities = new ArrayList<RawLocality>();
		Map<String,List<RawLocality>> tweakListMap = new HashMap<String,List<RawLocality>>();
		try(RowReader rr = new CsvRowReader(inputDir + BCGNIS_FILE, null, Charset.forName("ISO-8859-1"))) {
			while(rr.next()) {
				// Official Name,Feature Type,Feature Type Code,Mapsheet,Latitude,Longitude,Datum
				int fcode = rr.getInt("Feature Type Code");
				if(!ALLOWABLE_GNIS_FCODES.contains(fcode)) continue;
				String name = rr.getString("Official Name");
				
				// drop the less preferred fcode of some duplicated names
				if(GNIS_TO_DROP.contains(name + "|" + fcode)) continue;
				
				// fix this one special case
				if(name.equals("Mount Washington")) {
					name = "Mt Washington";
				}
				
				RawLocality loc = new RawLocality();
				loc.type = LocalityType.convert(fcode);
				loc.source = "BCGNIS";
				
				String latDMS = rr.getString("Latitude");
				double lat = convertDMS(latDMS);
				String lonDMS = rr.getString("Longitude");
				double lon = convertDMS(lonDMS);
				Point p = geometryFactory.createPoint(new Coordinate(lat,-lon));
				String datum = rr.getString("Datum");
				MathTransform transform = transformToAlbers.get(datum);
				if(transform == null) {
					logger.error("Invalid Datum for BCGNIS record, ignoring: '{}'", datum);
					continue;
				}
				loc.point = doTransform(p, transform);
				
				// check if it is one of the custom cities (ie. city/district of North Van, Langley)
				for(CustomCity cc : ccList) {
					if(name.equals(cc.locBase) && fcode == cc.outTypeId) {
						name = cc.outType + " of " + name;
					}
				}
				
				// check if it is one of the custom locality locations
				if(locTweakMap.get("BCGNIS|" + name) != null) {
					List<RawLocality> list = tweakListMap.get("BCGNIS|" + name);
					if(list == null) {
						list = new ArrayList<RawLocality>();
						tweakListMap.put("BCGNIS|" + name, list);
					}
					// save to review later
					list.add(loc);
				}
				loc.name = name;
				gnisLocalities.add(loc);
			}
		}
		
		// go through the tweak list and find the closest gnis record with a matching name
		for(Entry<String, List<RawLocality>> entry : tweakListMap.entrySet()) {
			double bestDist = Double.POSITIVE_INFINITY;
			RawLocality bestLocality = null;
			Point tweakPoint = locTweakMap.remove(entry.getKey());
			for(RawLocality loc : entry.getValue()) {
				double dist = loc.point.distance(tweakPoint);
				if(dist < bestDist) {
					bestLocality = loc;
					bestDist = dist;
				}
			}
			if(bestDist <= CUSTOM_LOCALITY_LOCATION_TOLERANCE) {
				bestLocality.point = tweakPoint;
			} else {
				logger.warn("Ignoring custom locality location in file: {} too far from matching GNIS record: {}m", LOCALITY_LOCATIONS_FILE, bestDist);
			}
		}
		// anything left in the locTweakMap was not used
		for(String name : locTweakMap.keySet()) {
			logger.warn("Unused record in file: {} with name {}", LOCALITY_LOCATIONS_FILE, name);
		}

		return gnisLocalities;
	}
	
	private double convertDMS(String dms) {
		String[] parts = dms.split(" ");
		return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]) / 60 + Double.parseDouble(parts[2]) / 3600;
	}
	
	private PointInPolygonCoverageIndex<RawLocality> buildLocalityPolyIndex(TIntObjectMap<RawLocality> localityMap) {
		PointInPolygonCoverageIndex<RawLocality> localityPolyIndex = new PointInPolygonCoverageIndex<RawLocality>(l -> l.geom);
		TIntObjectIterator<RawLocality> iterator = localityMap.iterator();
		while(iterator.hasNext()) {
			iterator.advance();
			RawLocality loc = iterator.value();
			localityPolyIndex.insert(loc);
		}
		localityPolyIndex.build();
		return localityPolyIndex;
	}
	
	private PointInPolygonCoverageIndex<RawElectoralArea> buildElectoralAreaPolyIndex(TIntObjectMap<RawElectoralArea> eaMap) {
		PointInPolygonCoverageIndex<RawElectoralArea> index = new PointInPolygonCoverageIndex<RawElectoralArea>(ea -> ea.geom);
		TIntObjectIterator<RawElectoralArea> eaIterator = eaMap.iterator();
		while(eaIterator.hasNext()) {
			eaIterator.advance();
			RawElectoralArea ea = eaIterator.value();
			index.insert(ea);
		}
		index.build();
		return index;
	}
	
	private void applyGNISLocalities(TIntObjectMap<RawLocality> localityMap, PointInPolygonCoverageIndex<RawLocality> localityPolyIndex, List<RawLocality> gnisLocalities) {
		HashMap<String,List<RawLocality>> gnisNameMap = new HashMap<String,List<RawLocality>>();
		int nextGnisLocalityId = FIRST_GNIS_LOCALITY_ID;
		for(RawLocality gnisLoc : gnisLocalities) {
			Envelope queryEnv = gnisLoc.point.getEnvelopeInternal();
			queryEnv.expandBy(GNIS_FUDGE_FACTOR);
			List<RawLocality> locList = localityPolyIndex.query(queryEnv);
			boolean found = false;
			// first look through all localities within the fudge factor for a name match
			for(RawLocality loc : locList) {
				// compare names after removing diacritics, etc, case-insensitive
				if(loc.geom.distance(gnisLoc.point) < GNIS_FUDGE_FACTOR
						&&NameSimplifier.simplify(loc.name).equals(NameSimplifier.simplify(gnisLoc.name))) {
					// if names are the same, copy the attributes from the gnis loc to the ITN loc
					loc.name = gnisLoc.name;
					loc.type = gnisLoc.type;
					loc.point = gnisLoc.point;
					found = true;
					break;
				}
			}
			if(!found) {
				// no name match found, then just borrow the ids from the containing locality, and add it to main localities
				for(RawLocality loc : locList) {
					if(loc.geom.contains(gnisLoc.point)) {
						// if the matched locality is in BC
						if(loc.stateProvTerrId == 1) {
							// add the GNIS loc to the main localities with a new id starting at 10000
							gnisLoc.id = nextGnisLocalityId++;
							gnisLoc.containingLocalityId = loc.id;
							gnisLoc.stateProvTerrId = loc.stateProvTerrId;
							localityMap.put(gnisLoc.id, gnisLoc);
							String fqName = gnisLoc.name + "|" + loc.name;
							List<RawLocality> sameNameList = gnisNameMap.get(fqName);
							if(sameNameList == null) {
								sameNameList = new ArrayList<RawLocality>(2);
								gnisNameMap.put(fqName, sameNameList);
							}
							sameNameList.add(gnisLoc);
						}
						found = true;
					}
				}
			}
			if(!found) {
				logger.warn("GNIS locality not matched to an ITN locality: {}", gnisLoc.name);
			}
		}
		// look for any localities that share the same name and need disambiguation
		for(List<RawLocality> list : gnisNameMap.values()) {
			if(list.size() > 1) {
				for(RawLocality loc : list) {
					loc.disambiguator = loc.type.toString();
					// "locality" is too ambiguous for a disambiguator
					if(loc.type == LocalityType.LOCALITY) loc.disambiguator = "sparse locality";
					// we don't lower case Indian Reserve for some reason
					if(loc.type != LocalityType.INDIAN_RESERVE) loc.disambiguator = loc.disambiguator.toLowerCase();
				}
			}
		}
		
	}
	
	private void applyLocalityElectoralAreas(TIntObjectMap<RawLocality> localityMap, PointInPolygonCoverageIndex<RawElectoralArea> index) {
		TIntObjectIterator<RawLocality> locIterator = localityMap.iterator();
		while(locIterator.hasNext()) {
			locIterator.advance();
			RawLocality loc = locIterator.value();
			if(loc.point == null) {
				loc.point = loc.geom.getCentroid();
			}
			if(loc.geom != null && !loc.geom.intersects(loc.point)) {
				loc.point = loc.geom.getInteriorPoint();
			}
			RawElectoralArea ea = index.queryOne(loc.point);
			if(ea != null) {
				loc.electoralAreaId = ea.id;
			}
		}
	}
	
	private Map<String, RawLocalityMapping> buildLocalityMappings(TIntObjectMap<RawLocality> localityMap, PointInPolygonCoverageIndex<RawLocality> localityPolyIndex) {
		Map<String, RawLocalityMapping> lmMap = new HashMap<String, RawLocalityMapping>();
		TIntObjectIterator<RawLocality> locIterator = localityMap.iterator();
		while(locIterator.hasNext()) {
			locIterator.advance();
			RawLocality loc = locIterator.value();
			if("ITN".equals(loc.source)) {
				List<RawLocality> locList = localityPolyIndex.query(loc.geom.getEnvelopeInternal());
				for(RawLocality neighborLoc : locList) {
					if(neighborLoc.id != loc.id && neighborLoc.geom.intersects(loc.geom)) {
						RawLocalityMapping lm = new RawLocalityMapping();
						lm.inputString = neighborLoc.name;
						lm.localityId = loc.id;
						lm.confidence = 80;
						lmMap.put(lm.inputString + "|" + lm.localityId, lm);
					}
				}
			}
			if("BCGNIS".equals(loc.source) && loc.containingLocalityId != null) {
				RawLocalityMapping lm = new RawLocalityMapping();
				lm.inputString = loc.name;
				lm.localityId = loc.containingLocalityId;
				lm.confidence = 80;
				lmMap.put(lm.inputString + "|" + lm.localityId, lm);
			}
		}
		return lmMap;
	}

	private TIntObjectMap<RawLocality> readLocalities(Map<String,Point> locTweakMap, Map<String, StateProvTerr> sptMap) {
		TIntObjectMap<RawLocality> localityMap = new TIntObjectHashMap<RawLocality>();
		RawLocality ubc = null, van = null;
		try(RowReader rr = new TsvRowReader(inputDir + LOCALITY_POLY_FILE, geometryFactory)) {
			while(rr.next()) {
				RawLocality loc = new RawLocality();
				loc.id = rr.getInt("LOCALITY_ID");
				loc.name = rr.getString("NAME");
				String spt = INITIAL_SPT_MAP.get(loc.name + "|" + loc.id);
				if(spt == null) {
					spt = "BC";
				}
				loc.type = LocalityType.UNKNOWN;
				loc.stateProvTerrId = sptMap.get(spt).getId();
				loc.geom = rr.getGeometry("GEOMETRY");
				loc.source = "ITN";
				loc.point = locTweakMap.remove("ITN|" + loc.name);
				if(loc.name.equals("UBC")) {
					ubc = loc;
				} else {
					localityMap.put(loc.id, loc);
					if(loc.name.equals("Vancouver")) {
						van = loc;
					}
				}
			}
		}
		if(van == null || ubc == null) {
			logger.error("Could not combine UBC and Vancouver polygons, one or more missing from localities file");
		} else {
			van.geom = van.geom.union(ubc.geom);	
		}
		
		return localityMap;
	}

	private List<CustomCity> readCustomCityPolys(TIntObjectMap<RawLocality> localityMap) {
		List<CustomCity> ccList = new ArrayList<CustomCity>();
		try(RowReader rr = new ShapefileRowReader(inputDir + CUSTOM_CITY_POLY_FILE)) {
			while(rr.next()) {
				CustomCity cc = new CustomCity(rr);
				ccList.add(cc);
				RawLocality outLoc = localityMap.get(cc.outLocId);
				outLoc.geom = outLoc.geom.difference(cc.geom);
				if(cc.outType != null && !cc.outType.isEmpty()) {
					outLoc.name = cc.outType + " of " + outLoc.name;
				}
				outLoc.type = LocalityType.convert(cc.outTypeId);
				
				RawLocality inLoc = new RawLocality();
				inLoc.id = cc.inLocId;
				inLoc.name = cc.locBase;
				if(cc.inType != null && !cc.inType.isEmpty()) {
					inLoc.name = cc.inType + " of " + inLoc.name;
				}
				inLoc.type = LocalityType.convert(cc.inTypeId);
				inLoc.source = "ITN";
				inLoc.stateProvTerrId = 1; // BC
				inLoc.geom = cc.geom;
				localityMap.put(inLoc.id, inLoc);
			}
		}
		return ccList;
	}

	private TIntObjectMap<RawElectoralArea> readElectoralAreas() {
		TIntObjectMap<RawElectoralArea> eaMap = new TIntObjectHashMap<RawElectoralArea>();
		try(RowReader rr = new ShapefileRowReader(inputDir + ABMS_ELECT_POLY_FILE)) {
			while(rr.next()) {
				RawElectoralArea ea = new RawElectoralArea();
				ea.id = rr.getInt("AA_ID");
				ea.name = rr.getString("AA_NAME");
				ea.geom = rr.getGeometry();
				eaMap.put(ea.id, ea);
			}
		}
		return eaMap;
	}

	private Map<String, String> readStreetTypes() {
		Map<String, String> streetTypeMap = new HashMap<String, String>();
		try(RowReader rr = new TsvRowReader(inputDir + NAME_PREFIX_CODE_FILE, geometryFactory);) {
			while(rr.next()) {
				String code = rr.getString("NAME_PREFIX_CODE");
				String desc = rr.getString("DESCRIPTION");
				String existing = streetTypeMap.put(code, desc);
				if(existing != null && !existing.equals(desc)) {
					logger.error("Duplicate street Type Code with different description: {} : {}, {}", code, existing, desc);
				}
			}
		}
		try(RowReader rr = new TsvRowReader(inputDir + NAME_SUFFIX_CODE_FILE, geometryFactory);) {
			while(rr.next()) {
				String code = rr.getString("NAME_SUFFIX_CODE");
				String desc = rr.getString("DESCRIPTION");
				String existing = streetTypeMap.put(code, desc);
				if(existing != null && !existing.equals(desc)) {
					logger.error("Duplicate street Type Code with different description: {} : {}, {}", code, existing, desc);
				}
			}
		}
		// Handle special duplicate cases of CTR/CNTR and SQ/SQR
		streetTypeMap.put("SQR", "Sq");
		streetTypeMap.put("CNTR", "Ctr");
		
		return streetTypeMap;
	}

	private Map<String, String> readStreetDescriptors() {
		Map<String, String> streetQualMap = new HashMap<String, String>();
		try(RowReader rr = new TsvRowReader(inputDir + NAME_DESCRIPTOR_CODE_FILE, geometryFactory);) {
			while(rr.next()) {
				String code = rr.getString("NAME_DESCRIPTOR_CODE");
				String desc = rr.getString("DESCRIPTION");
				String existing = streetQualMap.putIfAbsent(code, desc);
				if(existing != null && !existing.equals(desc)) {
					logger.error("Duplicate street Descriptor Code with different description: {} : {}, {}", code, existing, desc);
				}
			}
		}
		return streetQualMap;
	}
	
	private Map<String, String> readStreetDirections() {
		Map<String, String> streetDirMap = new HashMap<String, String>();
		try(RowReader rr = new TsvRowReader(inputDir + NAME_DIRECTION_CODE_FILE, geometryFactory);) {
			while(rr.next()) {
				String code = rr.getString("NAME_DIRECTION_CODE");
				String desc = rr.getString("DESCRIPTION");
				String existing = streetDirMap.putIfAbsent(code, desc);
				if(existing != null && !existing.equals(desc)) {
					logger.error("Duplicate street Direction Code with different description: {} : {}, {}", code, existing, desc);
				}
			}
		}
		return streetDirMap;
	}

	private TIntObjectMap<RawStreetName> readStreetNames(Map<String, String> streetTypeMap, Map<String, String> streetQualMap, Map<String, String> streetDirMap) {
		TIntObjectMap<RawStreetName> nameMap = new TIntObjectHashMap<RawStreetName>();
		try(RowReader rr = new TsvRowReader(inputDir + STRUCTURED_NAME_FILE, geometryFactory)) {
			while(rr.next()) {
				RawStreetName name = new RawStreetName();
				//"STRUCTURED_NAME_ID"	"FULL_NAME"	"PREFIX_NAME_DIRECTION_CODE"	"NAME_PREFIX_CODE"	"NAME_BODY"	"NAME_SUFFIX_CODE"	"SUFFIX_NAME_DIRECTION_CODE"	"NAME_DESCRIPTOR_CODE"	"CREATE_INTEGRATION_SESSION_ID"	"MODIFY_INTEGRATION_SESSION_ID"
				name.id = rr.getInt("STRUCTURED_NAME_ID");
				name.body = rr.getString("NAME_BODY");
				
				name.qual = rr.getString("NAME_DESCRIPTOR_CODE");
				if(name.qual != null) {
					String newQual = streetQualMap.get(name.qual);
					if(newQual == null) {
						logger.error("Invalid street descriptor: '{}'", name.qual);
					} else {
						name.qual = newQual;
					}
				}

				String preType = rr.getString("NAME_PREFIX_CODE");
				if(preType != null) {
					String newType = streetTypeMap.get(preType);
					if(newType == null) {
						logger.error("Invalid street type prefix: '{}'", preType);
					} else {
						preType = newType;
					}
				}
				String postType = rr.getString("NAME_SUFFIX_CODE");
				if(postType != null) {
					String newType = streetTypeMap.get(postType);
					if(newType == null) {
						logger.error("Invalid street type suffix: '{}'", postType);
					} else {
						postType = newType;
					}
				}
				
				String preDir = rr.getString("PREFIX_NAME_DIRECTION_CODE");
				String postDir = rr.getString("SUFFIX_NAME_DIRECTION_CODE");

				if(postType != null && !postType.isEmpty()) {
					// we have a post type
					name.type = postType;
					name.typeIsPrefix = false;
					if(preType != null && !preType.isEmpty()) {
						// we also have a preType, it needs to be prepended to the body
						name.body = preType + " " + name.body;
					}
				} else if(preType != null && !preType.isEmpty()) {
					// we have only a preType
					name.type = preType;
					name.typeIsPrefix = true;
				}
				
				if(postDir != null && !postDir.isEmpty() ) {
					// we have a post directional
					name.dir = postDir;
					name.dirIsPrefix = false;
					if(preDir != null && !preDir.isEmpty()) {
						// we also have a preDir, it needs to be prepended to the body
						// but first we need to get the full version of it
						String newDir = streetDirMap.get(preDir);
						if(newDir == null) {
							logger.error("Invalid street dir prefx: '{}'", preDir);
						}
						name.body = newDir + " " + name.body;
					}
				} else if(preDir != null && !preDir.isEmpty()) {
					// we have only a preDir
					name.dir = preDir;
					name.dirIsPrefix = true;
				}
				
				nameMap.put(name.id, name);
			}
		}
		return nameMap;
	}	
	
	private TIntObjectMap<RawStreetSeg> readTransportLines() {
		TIntObjectMap<RawStreetSeg> segMap = new TIntObjectHashMap<RawStreetSeg>();
		try(RowReader rr = new TsvRowReader(inputDir + TRANSPORT_LINE_DEMOGRAPHIC_FILE, geometryFactory)) {
			while(rr.next()) {
				if(rr.getBoolean("DEMOGRAPHIC_IND")) {
					RawStreetSeg seg = new RawStreetSeg(rr);
					segMap.put(seg.streetSegmentId, seg);
				}
			}
		}
		return segMap;
	}
	
	private void correctSegmentLocalities(TIntObjectMap<RawStreetSeg> segMap, TIntObjectMap<RawLocality> localityMap) {
		TIntObjectIterator<RawStreetSeg> iterator = segMap.iterator();
		while(iterator.hasNext()) {
			iterator.advance();
			RawStreetSeg seg = iterator.value();
			// fix UBC cases
			if(seg.leftLocalityId == 924 /* UBC */) {
				seg.leftLocalityId = 502; // Vancouver
			}
			if(seg.rightLocalityId == 924 /* UBC */) {
				seg.rightLocalityId = 502; // Vancouver
			}
			
			// fix North Vancouver/district of cases
			Geometry northVan = localityMap.get(1519).geom; // North Vancouver (central city)
			if(seg.leftLocalityId == 519) {
				if(northVan.contains(getOffsetPoint(seg.geom, true))) {
					seg.leftLocalityId = 1519;
				}
			}
			if(seg.rightLocalityId == 519) {
				if(northVan.contains(getOffsetPoint(seg.geom, false))) {
					seg.rightLocalityId = 1519;
				}
			}

			// fix Langley/township of cases
			Geometry langley = localityMap.get(1518).geom; // North Vancouver (central city)
			if(seg.leftLocalityId == 518) {
				if(langley.contains(getOffsetPoint(seg.geom, true))) {
					seg.leftLocalityId = 1518;
				}
			}
			if(seg.rightLocalityId == 518) {
				if(langley.contains(getOffsetPoint(seg.geom, false))) {
					seg.rightLocalityId = 1518;
				}
			}
		}
	}

	
	private TIntSet buildNoEaLocalities(TIntObjectMap<RawLocality> localityMap) {
		TIntSet noEaLocalities = new TIntHashSet();
		TIntObjectIterator<RawLocality> iterator = localityMap.iterator();
		while(iterator.hasNext()) {
			iterator.advance();
			RawLocality loc = iterator.value();
			if(NO_EA_LOCALITY_NAMES.contains(loc.name)) {
				noEaLocalities.add(loc.id);
			}
		}
		return noEaLocalities;
	}
	
	private void applySegmentElectoralAreas(TIntObjectMap<RawStreetSeg> segMap, PointInPolygonCoverageIndex<RawElectoralArea> eaPolyIndex, TIntSet noEaLocalities) {
		TIntObjectIterator<RawStreetSeg> iterator = segMap.iterator();
		while(iterator.hasNext()) {
			iterator.advance();
			RawStreetSeg seg = iterator.value();
			// left side
			if(!noEaLocalities.contains(seg.leftLocalityId)) {
				Point point = getOffsetPoint(seg.geom, true);
				RawElectoralArea ea = eaPolyIndex.queryOne(point);
				if(ea != null) {
					seg.leftElectoralAreaId = ea.id;
				}
			}
			// right side
			if(!noEaLocalities.contains(seg.rightLocalityId)) {
				Point point = getOffsetPoint(seg.geom, false);
				RawElectoralArea ea = eaPolyIndex.queryOne(point);
				if(ea != null) {
					seg.rightElectoralAreaId = ea.id;
				}
			}
		}
	}

	private void addHwyAndExitNames(TIntObjectMap<RawStreetName> streetNameIdMap, TIntObjectMap<RawStreetSeg> segMap) {
		Map<String,RawStreetName> nameMap = new HashMap<String,RawStreetName>();
		// build a name-based lookup for streetNames
		TIntObjectIterator<RawStreetName> nameIterator = streetNameIdMap.iterator();
		while(nameIterator.hasNext()) {
			nameIterator.advance();
			RawStreetName streetName = nameIterator.value();
			nameMap.put(streetName.toString(), streetName);
		}
		// look through segs for exits, create as a new name
		int nextNameId = FIRST_EXIT_NAME_ID;
		TIntObjectIterator<RawStreetSeg> segIterator = segMap.iterator();
		while(segIterator.hasNext()) {
			segIterator.advance();
			RawStreetSeg seg = segIterator.value();
			List<String> namesToAdd = new ArrayList<String>(4);
			if(seg.highwayExitNum != null && !seg.highwayExitNum.isEmpty()) {
				namesToAdd.add("Exit " + seg.highwayExitNum);
			}
			if(seg.highwayRoute1 != null && !seg.highwayRoute1.isEmpty()) {
				namesToAdd.add("Hwy " + seg.highwayRoute1);
			}
			if(seg.highwayRoute2 != null && !seg.highwayRoute2.isEmpty()) {
				namesToAdd.add("Hwy " + seg.highwayRoute2);
			}
			if(seg.highwayRoute3 != null && !seg.highwayRoute3.isEmpty()) {
				namesToAdd.add("Hwy " + seg.highwayRoute3);
			}
			for(String nameToAdd : namesToAdd) {
				RawStreetName name = nameMap.get(nameToAdd);
				if(name == null) {
					name = new RawStreetName();
					name.id = nextNameId++;
					name.body = nameToAdd;
					streetNameIdMap.put(name.id, name);
					nameMap.put(name.toString(), name);
				}
				if(!seg.nameIds.contains(name.id)) {
					seg.nameIds.add(name.id);
				}
			}
		}
	}

	private Point getOffsetPoint(LineString line, boolean toLeft) {
		LengthIndexedLine lil = new LengthIndexedLine(line);
		return geometryFactory.createPoint(lil.extractPoint(line.getLength()/2, toLeft ? SIDE_POINT_OFFSET : -SIDE_POINT_OFFSET));
	}
	
	private void writeStateProvTerrs(Map<String, StateProvTerr> sptMap) {
		try(RowWriter rw = new JsonRowWriter(new File(outputDir + STREET_LOAD_STATE_PROV_TERR_FILE), "bgeo_state_prov_terrs", dates)) {
			for(StateProvTerr spt : sptMap.values()) {
				Map<String, Object> row = new THashMap<String, Object>();
				row.put("STATE_PROV_TERR_ID", spt.getId());
				row.put("STATE_PROV_TERR_NAME", spt.getName());
				row.put("COUNTRY_CODE", spt.getCountryCode());
				row.put("geom", spt.getLocation());
				rw.writeRow(row);
			}
		}
	}
	
	private void writeElectoralAreas(TIntObjectMap<RawElectoralArea> eaMap) {
		try(RowWriter rw = new JsonRowWriter(new File(outputDir + STREET_LOAD_ELECTORAL_AREAS_FILE), "bgeo_electoral_areas", dates)) {
			TIntObjectIterator<RawElectoralArea> iterator = eaMap.iterator();
			while(iterator.hasNext()) {
				iterator.advance();
				RawElectoralArea ea = iterator.value();
				Map<String, Object> row = new THashMap<String, Object>();
				row.put("ELECTORAL_AREA_ID", ea.id);
				row.put("ELECTORAL_AREA_NAME", ea.name);
				rw.writeRow(row);
			}
		}
	}

	private void writeLocalities(TIntObjectMap<RawLocality> localityMap) {
		try(RowWriter rw = new JsonRowWriter(new File(outputDir + STREET_LOAD_LOCALITIES_FILE), "bgeo_localities", dates)) {
			TIntObjectIterator<RawLocality> iterator = localityMap.iterator();
			while(iterator.hasNext()) {
				iterator.advance();
				RawLocality loc = iterator.value();
				Map<String, Object> row = new THashMap<String, Object>();
				row.put("LOCALITY_ID", loc.id);
				row.put("LOCALITY_NAME", loc.name);
				row.put("LOCALITY_QUALIFIER", loc.getQualifier(localityMap));
				if(loc.type != null) {
					row.put("LOCALITY_TYPE_ID", loc.type.getId());
				}
				row.put("STATE_PROV_TERR_ID", loc.stateProvTerrId);
				row.put("ELECTORAL_AREA_ID", loc.electoralAreaId);
				if(loc.point != null) {
					row.put("geom", loc.point);
				} else {
					row.put("geom", loc.geom.getCentroid());
				}
				rw.writeRow(row);
			}
		}
	}
	
	private void writeLocalityMappings(Map<String, RawLocalityMapping> localityMappingMap) {
		try(RowWriter rw = new JsonRowWriter(new File(outputDir + STREET_LOAD_LOCALITY_MAPPINGS_FILE), "bgeo_locality_mappings", dates)) {
			int nextId = 1;
			for(RawLocalityMapping lm : localityMappingMap.values()) {
				Map<String, Object> row = new THashMap<String, Object>();
				row.put("LOCALITY_MAPPING_ID", nextId++);
				row.put("INPUT_STRING", lm.inputString);
				row.put("LOCALITY_ID", lm.localityId);
				row.put("CONFIDENCE", lm.confidence);
				row.put("USER_DEFINED_IND", "N");
				row.put("ACTIVE_IND", "Y");
				rw.writeRow(row);
			}
		}
	}

	private void writeStreetNames(TIntObjectMap<RawStreetName> nameMap) {
		try(RowWriter rw = new JsonRowWriter(new File(outputDir + STREET_LOAD_STREET_NAMES_FILE), "bgeo_street_names", dates)) {
			TIntObjectIterator<RawStreetName> iterator = nameMap.iterator();
			while(iterator.hasNext()) {
				iterator.advance();
				RawStreetName name = iterator.value();
				Map<String, Object> row = new THashMap<String, Object>();
				row.put("STREET_NAME_ID", name.id);
				row.put("NAME_BODY", name.body);
				if(name.type != null) {
					row.put("STREET_TYPE", name.type);
					row.put("STREET_TYPE_IS_PREFIX_IND", name.typeIsPrefix ? "Y" : "N");
				}
				if(name.dir != null) {
					row.put("STREET_DIRECTION", name.dir);
					row.put("STREET_DIRECTION_IS_PREFIX_IND", name.dirIsPrefix ? "Y" : "N");
				}
				if(name.qual != null) {
					row.put("STREET_QUALIFIER", name.qual);
				}
				rw.writeRow(row);
			}
		}
	}
	
	private void writeStreetSegments(TIntObjectMap<RawStreetSeg> segMap) {
		try(RowWriter rw = new JsonRowWriter(new File(outputDir + STREET_LOAD_STREET_SEGMENTS_FILE), "bgeo_street_segments", dates)) {
			TIntObjectIterator<RawStreetSeg> iterator = segMap.iterator();
			while(iterator.hasNext()) {
				iterator.advance();
				RawStreetSeg seg = iterator.value();
				if(GEOM_SIMPLIFY_TOLERANCE > 0) {
					Geometry newGeom = DouglasPeuckerSimplifier.simplify(seg.geom, GEOM_SIMPLIFY_TOLERANCE);
					originalPoints += seg.geom.getNumPoints();
					reducedPoints += newGeom.getNumPoints();
					seg.geom = (LineString)newGeom;
				}
				seg.write(rw);
			}
		}
		if(GEOM_SIMPLIFY_TOLERANCE > 0) {
			logger.info("Geometry simplification tolerance of {} reduced point output by {} from {} to {}", GEOM_SIMPLIFY_TOLERANCE, 
					originalPoints - reducedPoints, originalPoints, reducedPoints);
		}
	}
	
	private void writeStreetNameOnSegs(TIntObjectMap<RawStreetSeg> segMap, TIntObjectMap<RawStreetName> streetNameIdMap) {
		try(RowWriter rw = new JsonRowWriter(new File(outputDir + STREET_LOAD_STREET_NAME_ON_SEG_XREF_FILE), "bgeo_street_name_on_seg_xref", dates)) {
			TIntObjectIterator<RawStreetSeg> segIterator = segMap.iterator();
			while(segIterator.hasNext()) {
				segIterator.advance();
				RawStreetSeg seg = segIterator.value();
				for(int nameIdx = 0; nameIdx < seg.nameIds.size(); nameIdx++) {
					Map<String, Object> row = new THashMap<String, Object>();
					row.put("STREET_NAME_ID", seg.nameIds.get(nameIdx));
					row.put("STREET_SEGMENT_ID", seg.streetSegmentId);
					row.put("IS_PRIMARY_IND", nameIdx == 0 ? "Y" : "N");
					rw.writeRow(row);
				}
			}
		}
	}

	private TIntObjectMap<RawStreetIntersection> buildStreetIntersections(TIntObjectMap<RawStreetSeg> segMap) {
		TIntObjectMap<RawStreetIntersection> intMap = new TIntObjectHashMap<RawStreetIntersection>();
		TIntObjectIterator<RawStreetSeg> segIterator = segMap.iterator();
		while(segIterator.hasNext()) {
			segIterator.advance();
			RawStreetSeg seg = segIterator.value();
			// start node intersection
			RawStreetIntersection i = intMap.get(seg.startIntersectionId);
			if(i == null) {
				i = new RawStreetIntersection();
				i.id = seg.startIntersectionId;
				i.degree = 1;
				i.point = seg.geom.getStartPoint();
				intMap.put(i.id, i);
			} else {
				i.degree++;
			}
			// end node intersection
			i = intMap.get(seg.endIntersectionId);
			if(i == null) {
				i = new RawStreetIntersection();
				i.id = seg.endIntersectionId;
				i.degree = 1;
				i.point = seg.geom.getEndPoint();
				intMap.put(i.id, i);
			} else {
				i.degree++;
			}
		}
		return intMap;
	}
	
	private void writeStreetIntersections(TIntObjectMap<RawStreetIntersection> intMap) {
		try(RowWriter rw = new JsonRowWriter(new File(outputDir + STREET_LOAD_STREET_INTERSECTIONS_FILE), "bgeo_street_intersections", dates)) {
			TIntObjectIterator<RawStreetIntersection> intIterator = intMap.iterator();
			while(intIterator.hasNext()) {
				intIterator.advance();
				RawStreetIntersection i = intIterator.value();
				Map<String, Object> row = new THashMap<String, Object>();
				row.put("STREET_INTERSECTION_ID", i.id);
				row.put("INTERSECTION_UUID", UUID.randomUUID());
				row.put("DEGREE", i.degree);
				row.put("geom", i.point);
				rw.writeRow(row);
			}
		}
		
	}

	private Map<String,Point> buildStreetLocalityCentroids(TIntObjectMap<RawStreetSeg> segMap) {
		// start by building a map from nameId|localityId to a list of segments
		Map<String,List<LineString>> primarySegListMap = new HashMap<String,List<LineString>>();
		Map<String,List<LineString>> aliasSegListMap = new HashMap<String,List<LineString>>();
		TIntObjectIterator<RawStreetSeg> segIterator = segMap.iterator();
		while(segIterator.hasNext()) {
			segIterator.advance();
			RawStreetSeg seg = segIterator.value();
			List<Integer> localityIds = new ArrayList<Integer>(2);
			localityIds.add(seg.leftLocalityId);
			if(seg.leftLocalityId != seg.rightLocalityId) {
				localityIds.add(seg.rightLocalityId);
			}
			for(int localityId : localityIds) {
				// start with just primary names
				String key = seg.nameIds.get(0) + "|" + localityId;
				List<LineString> geomList = primarySegListMap.get(key);
				if(geomList == null) {
					geomList = new ArrayList<LineString>();
					primarySegListMap.put(key, geomList);
				}
				geomList.add(seg.geom);
				// then do any aliases
				for(int nameIdx = 1; nameIdx < seg.nameIds.size(); nameIdx++) {
					key = seg.nameIds.get(nameIdx) + "|" + localityId;
					geomList = aliasSegListMap.get(key);
					if(geomList == null) {
						geomList = new ArrayList<LineString>();
						aliasSegListMap.put(key, geomList);
					}
					geomList.add(seg.geom);
				}
			}
		}
		// convert each list of segments to a single point
		Map<String,Point> centroidMap = new HashMap<String,Point>();
		// start with the primary names
		for(Entry<String, List<LineString>> entry : primarySegListMap.entrySet()) {
			MultiLineString mls = geometryFactory.createMultiLineString(entry.getValue().toArray(new LineString[0]));
			DistanceOp distOp = new DistanceOp(mls, mls.getCentroid());
			Coordinate[] coords = distOp.nearestPoints();
			centroidMap.put(entry.getKey(), geometryFactory.createPoint(coords[0]));
		}
		// then add alias names
		for(Entry<String, List<LineString>> entry : aliasSegListMap.entrySet()) {
			// don't overwrite primary name entries
			if(centroidMap.get(entry.getKey()) == null) {
				MultiLineString mls = geometryFactory.createMultiLineString(entry.getValue().toArray(new LineString[0]));
				DistanceOp distOp = new DistanceOp(mls, mls.getCentroid());
				Coordinate[] coords = distOp.nearestPoints();
				centroidMap.put(entry.getKey(), geometryFactory.createPoint(coords[0]));
			}
		}
		
		return centroidMap;
	}
	
	private void writeStreetLocalityCentroids(Map<String,Point> centroidMap) {
		try(RowWriter rw = new JsonRowWriter(new File(outputDir + STREET_LOAD_STREET_LOCALITY_CENTROIDS_FILE), "bgeo_street_locality_centroids", dates)) {
			for(Entry<String, Point> entry : centroidMap.entrySet()) {
				Map<String, Object> row = new THashMap<String, Object>();
				String[] ids = entry.getKey().split("\\|");
				row.put("STREET_NAME_ID", Integer.parseInt(ids[0]));
				row.put("LOCALITY_ID", Integer.parseInt(ids[1]));
				row.put("geom", entry.getValue());
				rw.writeRow(row);
			}
		}
		
	}
	
		private static String addTrailingSeparator(String path) {
		if(path.charAt(path.length()-1) == File.separatorChar){
		    return path;
		}
		return path += File.separator;
	}
}
