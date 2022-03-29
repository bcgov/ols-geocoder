package ca.bc.gov.ols.siteloaderprep;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.api.data.MatchFault;
import ca.bc.gov.ols.geocoder.api.data.MatchFault.MatchElement;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.data.enumTypes.PhysicalStatus;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.rowreader.CsvRowReader;
import ca.bc.gov.ols.rowreader.JsonRowReader;
import ca.bc.gov.ols.rowreader.RowReader;
import ca.bc.gov.ols.rowreader.RowWriter;
import ca.bc.gov.ols.rowreader.TsvRowReader;
import ca.bc.gov.ols.rowreader.XsvRowWriter;
import gnu.trove.map.hash.THashMap;


public class SiteLoaderPrep {
	private static final Logger logger = LoggerFactory.getLogger(SiteLoaderPrep.class.getCanonicalName());

	// TEST PARAMS
	// L:\Refractions\MLCSOG\Batch_Geocoder_2011-12\FME2Java\inputs L:\Refractions\MLCSOG\Batch_Geocoder_2011-12\FME2Java\outputs\java L:\Refractions\MLCSOG\Batch_Geocoder_2011-12\FME2Java\outputs\fme
	
	//private static String inputDir = "L:\\Refractions\\MLCSOG\\Batch_Geocoder_2011-12\\Staging\\";
	private static String inputDir; // = "L:\\Refractions\\MLCSOG\\Batch_Geocoder_2011-12\\FME2Java\\inputs\\";
	//private static String inputDir = "L:\\Refractions\\MLCSOG\\Batch_Geocoder_2011-12\\FME2Java\\inputs\\test\\";
	
	private static String outputDir; // = "L:\\Refractions\\MLCSOG\\Batch_Geocoder_2011-12\\FME2Java\\outputs\\java\\";

	private static String compareDir; // = "L:\\Refractions\\MLCSOG\\Batch_Geocoder_2011-12\\FME2Java\\outputs\\fme\\";
	//private static String compareDir = "L:\\Refractions\\MLCSOG\\Batch_Geocoder_2011-12\\FME2Java\\outputs\\fme\\test\\";

	/* TRACE CONDITION is a function that returns true when the given site should be traced */
	private static Function<InputSite, Boolean> TRACE_CONDITION = s -> false; // turns off tracing
	//private static Function<InputSite, Boolean> TRACE_CONDITION = s -> s.gId.equals(234363); // traces a specific gId value
	//private static final Set<Integer> VALUES = Set.of(529881); // for tracing a list of gId values
	//private static Function<InputSite, Boolean> TRACE_CONDITION = s -> VALUES.contains(s.gId);
	
	private static final int SRID = 3005;
	private static final LocalDate TODAY = LocalDate.now();
	
	private static final String DEFAULT_UNIT_DESIGNATOR = "UNIT";
	private static final int STREET_SCORE_CUTOFF = 78;
	private static final int LOCALITY_SCORE_CUTOFF = 68;
	private static final double ORPHAN_SUBSITE_GROUP_TOLERANCE = 1000;
	public static final int FIRST_GENERATED_PARENT_ID = 8000000;
	
	// input filenames
	private static final String ITN_ANCHOR_POINT_FILE = "site_ITN_anchor_points.tsv";
	private static final String STREET_LOAD_STREET_NAMES_FILE = "street_load_street_names.json";
	private static final String LOCALITIES_FILE = "street_load_localities.json";
	private static final String GEOCODE_HYBRID_NAMES_FILE = "geocode_Hybrid_names.csv";
	private static final String GEOCODE_HYBRID_FILE = "geocode_Hybrid.csv";
	private static final String RESULT_HYBRID_NAMES_FILE = "result_Hybrid_names.csv";
	private static final String VBC_PID_FILE = "VBC_PID_EXTRACT.csv";

	// output filenames
	private static final String SITE_HYBRID_FILE = "site_Hybrid.tsv";
	private static final String LOG_FILE = "site_loader_prep_log.csv";
	private static final String SID2PID_FILE = "sid2pid.csv";
	
	private static final Map<MatchFault.MatchElement,List<String>> ALLOWED_CAP_FAULTS;
	static {
		Map<MatchFault.MatchElement,List<String>> allowedCapFaults = new EnumMap<MatchFault.MatchElement,List<String>>(MatchFault.MatchElement.class);
		allowedCapFaults.put(MatchFault.MatchElement.SITE_NAME, Collections.emptyList()); 
		allowedCapFaults.put(MatchFault.MatchElement.UNIT_NUMBER, Collections.emptyList()); 
		allowedCapFaults.put(MatchFault.MatchElement.UNIT_NUMBER_SUFFIX, Collections.emptyList());
		allowedCapFaults.put(MatchFault.MatchElement.UNIT_DESIGNATOR, Collections.emptyList());
		allowedCapFaults.put(MatchFault.MatchElement.CIVIC_NUMBER, Collections.emptyList());
		allowedCapFaults.put(MatchFault.MatchElement.CIVIC_NUMBER_SUFFIX, Collections.emptyList());
		allowedCapFaults.put(MatchFault.MatchElement.STREET_DIRECTION, List.of("notMatchedInHighway"));
		allowedCapFaults.put(MatchFault.MatchElement.POSTAL_ADDRESS_ELEMENT, Collections.emptyList());
		allowedCapFaults.put(MatchFault.MatchElement.PROVINCE, Collections.emptyList());
		allowedCapFaults.put(MatchFault.MatchElement.PROVINCE_GARBAGE, Collections.emptyList());
		ALLOWED_CAP_FAULTS = Collections.unmodifiableMap(allowedCapFaults);
	};

	private static final Map<MatchFault.MatchElement,List<String>> ALLOWED_NCAP_FAULTS;
	static {
		Map<MatchFault.MatchElement,List<String>> allowedNcapFaults = new EnumMap<MatchFault.MatchElement,List<String>>(MatchFault.MatchElement.class);
		allowedNcapFaults.put(MatchFault.MatchElement.SITE_NAME, Collections.emptyList()); 
		allowedNcapFaults.put(MatchFault.MatchElement.POSTAL_ADDRESS_ELEMENT, Collections.emptyList()); 
		allowedNcapFaults.put(MatchFault.MatchElement.PROVINCE, Collections.emptyList());		
		ALLOWED_NCAP_FAULTS = Collections.unmodifiableMap(allowedNcapFaults);
	};
	
	private static final Map<String,Integer> priorityMap;
	static {
		Map<String,Integer> pm = new HashMap<String,Integer>();
		pm.put("AddressBC", 1);
		pm.put("BCA", 2);
		pm.put("GSR", 3);
		pm.put("Chilliwack", 4);
		pm.put("Kamloops", 4);
		pm.put("Kelowna", 4);
		pm.put("LangleyTownship", 4);
		pm.put("Nanaimo", 4);
		pm.put("NorthCowichan", 4);
		pm.put("NorthVancouver", 4);
		pm.put("PrinceGeorge", 4);
		pm.put("RDOS", 4);
		pm.put("Saanich", 4);
		pm.put("Surrey", 4);
		pm.put("Vancouver", 4);
		pm.put("Victoria", 4);
		pm.put("WestKelowna", 4);
		pm.put("DataBC", 5);
		priorityMap = Collections.unmodifiableMap(pm);
	}

	private GeometryFactory geometryFactory;
	private RowWriter rejectWriter;
	Map<String, Integer> localityMap;
	
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
		SiteLoaderPrep prep = new SiteLoaderPrep();
		prep.run();
	}
	
	public SiteLoaderPrep() {
		geometryFactory = new GeometryFactory(GeocoderConfig.BASE_PRECISION_MODEL, SRID);
	}
	
	public void run() {
		try {
			rejectWriter = openRejectWriter();
			localityMap = readLocalities();
			Map<String, RawStreetName> streetNameMap = readStreetNames();
			Map<String, GeocodeResult> addressStringMap = readGeocodeNames(streetNameMap);
			List<InputSite> pseudoSites = new ArrayList<InputSite>();
			Map<String, List<InputSite>> siteMap = readGeocodeHybrid(addressStringMap, pseudoSites);
			List<InputSite> additionalBcaSids = new ArrayList<InputSite>();
			List<InputSite> dedupedSites = deduplicateSites(siteMap, additionalBcaSids);
			siteMap = null;
			assignParents(dedupedSites);
			List<OutputSite> anchorPoints = readSitesHybrid(inputDir + ITN_ANCHOR_POINT_FILE);
			writeOutput(dedupedSites, pseudoSites, anchorPoints);
			writeSid2Pids(dedupedSites, additionalBcaSids);
			dedupedSites = null;
		} finally {
			rejectWriter.close();
		}
		if(compareDir != null) {
			List<OutputSite> outSites = readSitesHybrid(outputDir + SITE_HYBRID_FILE);
			List<OutputSite> compareSites = readSitesHybrid(compareDir + SITE_HYBRID_FILE);
			compareOutputs(outSites, compareSites);
		}
	}

	private RowWriter openRejectWriter() {
		File logFile = new File(outputDir + LOG_FILE);
		List<String> siteSchema = Arrays.asList("ID","GID","SITE_ID","REASON");
		return new XsvRowWriter(logFile, ',', siteSchema, true);
	}
	
	private Map<String, Integer> readLocalities() {
		Map<String, Integer> localityMap = new THashMap<String, Integer>();
		try(RowReader rr = new JsonRowReader(inputDir + LOCALITIES_FILE, geometryFactory)) {
			while(rr.next()) {
				int id = rr.getInt("locality_id");
				String name = rr.getString("locality_name");
				// loc.type = LocalityType.convert(rr.getInteger("locality_type_id"));
				// loc.ea = rr.getInt("electoral_area_id");
				int stateProvTerrId = rr.getInt("state_prov_terr_id");
				// Point point = rr.getPoint();
				// only load localities in BC and only from ITN (not BCGNIS)
				if(stateProvTerrId == 1 ) { //&& loc.id < 10000) {
					localityMap.put(simplifyLocalityName(name), id);
				}
			}
		}
		return localityMap;
	}
	
	private Map<String, List<String>> readPids() {
		Map<String, List<String>> jurolMap = new THashMap<String, List<String>>();
		try(RowReader rr = new CsvRowReader(inputDir + VBC_PID_FILE, geometryFactory)) {
			while(rr.next()) {
				String jur = rr.getString("JURISDICTION");
				String roll = rr.getString("ROLL_NUM");
				String pid = rr.getString("PID");
				List<String> pids = jurolMap.get(jur+roll);
				if(pids == null) {
					pids = new ArrayList<String>();
					jurolMap.put(jur+roll, pids);
				}
				pids.add(pid);
			}
		}
		for(List<String> pids : jurolMap.values()) {
			Collections.sort(pids);
		}
		return jurolMap;
	}

	private Map<String, RawStreetName> readStreetNames() {
		Map<String, RawStreetName> nameMap = new THashMap<String, RawStreetName>();
		try(RowReader rr = new JsonRowReader(inputDir + STREET_LOAD_STREET_NAMES_FILE, geometryFactory)) {
			while(rr.next()) {
				RawStreetName name = new RawStreetName();
				name.id = rr.getInt("street_name_id");
				name.body = rr.getString("name_body");
				name.type = rr.getString("street_type");
				name.dir = rr.getString("street_direction");
				name.qual = rr.getString("street_qualifier");
				name.typeIsPrefix = "Y".equals(rr.getString("street_type_is_prefix_ind"));
				name.dirIsPrefix = "Y".equals(rr.getString("street_direction_is_prefix_ind"));
				nameMap.put(name.toString(), name);
			}
		}
		return nameMap;
	}
	
	private Map<String, GeocodeResult> readGeocodeNames(Map<String, RawStreetName> streetNameMap) {
		ArrayList<GeocodeResult> inputNames = new ArrayList<GeocodeResult>(10000);
		// read in the geocoder input names
		try(RowReader rr = new CsvRowReader(inputDir + GEOCODE_HYBRID_NAMES_FILE, geometryFactory)) {
			while(rr.next()) {
				GeocodeResult gr = new GeocodeResult(); 
				gr.addressString = rr.getString("addressString");
				gr.yourId = rr.getString("yourId");
				inputNames.add(gr);
			}
		}
		//inputNames.trimToSize();
		
		Map<String, GeocodeResult> addressStringMap = new THashMap<String, GeocodeResult>(inputNames.size());
		// now match the geocoder results back to the inputs based on sequence number
		try(RowReader rr = new CsvRowReader(inputDir + RESULT_HYBRID_NAMES_FILE, geometryFactory)) {
			while(rr.next()) {
				int sequenceNumber = rr.getInt("sequenceNumber");
				if(sequenceNumber > inputNames.size()) {
					logger.error("Invalid geocode name result record with sequence number {}; does the result_Hybrid_names.csv match the geocode_Hybrid_names.csv?", sequenceNumber);
					continue;
				}
				GeocodeResult gr = inputNames.get(sequenceNumber - 1);
				gr.fullAddress = rr.getString("fullAddress");
				// Some addresses will have a front gate part in the results - we don't want that. eg. GSR_UNIT cases
				// FME: FrontGateTester
				if(gr.fullAddress.contains(" -- ")) {
					//FME: AddressWithUnit
					gr.fullAddress = gr.fullAddress.substring(gr.fullAddress.indexOf(" -- ") + 4);
				}
				gr.score = rr.getInt("score");
				gr.matchPrecision = MatchPrecision.convert(rr.getString("matchPrecision"));
				gr.precisionPoints = rr.getInt("precisionPoints");
				String localityName = rr.getString("localityName");
				gr.localityId = getLocalityId(localityName);
				// build an array of MatchFault objects from the faults string
				String faults = rr.getString("faults");
				String[] faultArray = faults.substring(1, faults.length()-1).split(", ");
				gr.faults = new ArrayList<MatchFault>(faultArray.length);
				for(String faultStr : faultArray) {
					if(faultStr.length() > 0) {
						gr.faults.add(new MatchFault(faultStr));
					}
				}
				gr.executionTime = rr.getDouble("executionTime");			
				
				// build a street name string out of the result street name components
				RawStreetName n = new RawStreetName();
				n.body = rr.getString("streetName");
				n.type = rr.getString("streetType");
				n.typeIsPrefix = rr.getBoolean("isStreetTypePrefix");
				n.dir = rr.getString("streetDirection");
				n.dirIsPrefix = rr.getBoolean("isStreetDirectionPrefix");
				n.qual = rr.getString("streetQualifier");
				
				String fullStreetName = n.toString();
				// look the street name up in our map of all street names
				if(fullStreetName != null) {
					gr.name = streetNameMap.get(fullStreetName);
					if(gr.name == null) {
						// We get this error due to mismatched vintages; shouldn't happen with consistent data 
						logger.error("Street name not matched to base data: {}; does the vintage of the geocoder base data match what this script is using?", fullStreetName);
					}
				}
				addressStringMap.put(gr.addressString, gr);
			}
		}
		return addressStringMap;
	}

	private Map<String,List<InputSite>> readGeocodeHybrid(Map<String, GeocodeResult> addressStringMap, List<InputSite> pseudoSites) {
		Map<String,List<InputSite>> siteMap = new THashMap<String,List<InputSite>>(2500000);
		try(RowReader rr = new CsvRowReader(inputDir + GEOCODE_HYBRID_FILE, geometryFactory)) {
			while(rr.next()) {
				InputSite site = new InputSite(); 
				site.id = rr.getInt("ID");
				site.addressString = rr.getString("addressString");
				site.yourId = rr.getString("yourId");
				site.siteName = rr.getString("siteName");
				site.unitDesignator = rr.getString("unitDesignator");
				site.unitNumber = rr.getString("unitNumber");
				site.unitNumberSuffix = rr.getString("unitNumberSuffix");
				// FME UnitTester
				if(site.unitNumber != null && !site.unitNumber.isEmpty()
						&& (site.unitDesignator == null || site.unitDesignator.isEmpty())) {
					site.unitDesignator = DEFAULT_UNIT_DESIGNATOR;
				}
				site.civicNumber = rr.getInteger("civicNumber");
				site.civicNumberSuffix = rr.getString("civicNumberSuffix");
				site.localityName = rr.getString("LocalityName");
				site.localityId = getLocalityId(site.localityName);
				site.provinceCode = rr.getString("provinceCode");
				site.gId = rr.getInt("GID");
				site.inputName = rr.getString("INPUT_NAME");
				site.siteId = rr.getInt("SITE_ID");
				site.locationDescriptor = LocationDescriptor.convert(rr.getString("LOCATION_DESCRIPTOR"));
				site.positionalAccuracy = PositionalAccuracy.convert(rr.getString("SITE_POSITIONAL_ACCURACY"));
				site.status = PhysicalStatus.convert(rr.getString("SITE_STATUS"));
				site.retireDate = rr.getDate("SITE_RETIRE_DATE");
				site.albersX = rr.getInteger("SITE_ALBERS_X");
				site.albersY = rr.getInteger("SITE_ALBERS_Y");
				site.apType = rr.getString("AP_TYPE");
				site.isPrimary = Optional.ofNullable(rr.getBoolean("IS_PRIMARY_IND")).orElse(true);
				site.narrativeLocation = rr.getString("NARRATIVE_LOCATION");
				site.accessPositionalAccuracy = PositionalAccuracy.convert(rr.getString("ACCESS_POSITIONAL_ACCURACY"));
				site.accessPointStatus = PhysicalStatus.convert(rr.getString("ACCESS_POINT_STATUS"));
				site.accessRetireDate = rr.getDate("ACCESS_RETIRE_DATE");
				site.accessAlbersX = rr.getInteger("ACCESS_ALBERS_X");
				site.accessAlbersY = rr.getInteger("ACCESS_ALBERS_Y");
				site.isPseudoSite = Optional.ofNullable(rr.getBoolean("PSEUDO_SITE")).orElse(false);
				site.superFullSiteDescriptor = rr.getString("superFullSiteDescriptor");

				trace(site, "was read in");
				
				if(site.localityId == null) {
					logReject(site, "LOCALITY MATCH FAILURE");
					continue;
				}
				
				// FME: SitePointTester
				if(site.albersX == null || site.albersY == null) {
					logReject(site, "NO SITE POINT");
					continue;
				}

				// FME: CivicNumTester, CivicNumTester_2
				if(site.civicNumber == null && !"NCAP".equals(site.apType) && !"TBD".equals(site.apType)) {
					logReject(site, "NO CIVIC NUMBER");
					continue;
				}
				
				//FME: OutsideRegionLeftValueMapper, BClocalityTester
				switch(site.localityName) {
					case "Banff":
					case "Carcross":
					case "Fort Liard":
					case "Grand Prairie":
					case "Haines Junction":
					case "Hyder":
					case "Idaho":
					case "Jakes Corner":
					case "Jasper":
					case "Montana":
					case "Washington":
					case "Watson Lake":
					case "Whitehorse":
						logReject(site, "OUTSIDE BC");
						continue;
				}
				site.result = addressStringMap.get(site.addressString);
				if(site.result == null) {
					logger.error("geocode_hybrid record did not find a matching geocode name result for addressString: {}", site.addressString);
					continue;
				}
				
				// FME: PrecisionTest
				if(site.result.matchPrecision == MatchPrecision.STREET) {
					// FME: AP_TYPE:CAPorGSR
					if("CAP".equals(site.apType) || "GSR_UNIT".equals(site.apType)) {
						// FME: ScoreCutoffTest		
						int faultAllowance = calculateFaultAllowance(site.result.faults, ALLOWED_CAP_FAULTS);
						if(site.result.score < (STREET_SCORE_CUTOFF - faultAllowance)) {
							logReject(site, "LOW SCORE");
							// stop further processing
							continue;
						}
					// FME: AP_TYPE:TBD
					} else if("TBD".equals(site.apType)) {
						// FME: civic_tester
						if(site.civicNumber == null) {
							// FME: ScoreCutoffTest_3
							int faultAllowance = calculateFaultAllowance(site.result.faults, ALLOWED_NCAP_FAULTS);
							if(site.result.score < (STREET_SCORE_CUTOFF - faultAllowance)) {
								logReject(site, "LOW SCORE");
								// stop further processing
								continue;
							} else {
								// FME: AP_TYPEsetter_2
								site.apType = "NCAP";
							}
						} else {
							// FME: AP_TYPEsetter_3
							site.apType = "CAP";
						}
					}
				// FME: PrecisionTestLocality
				// FME: PrecisionTestNCAP
				} else if(site.result.matchPrecision == MatchPrecision.LOCALITY
						&& ("NCAP".equals(site.apType) || "TBD".equals(site.apType))) {
					// FME: ScoreCutoffTest_2
					int faultAllowance = calculateFaultAllowance(site.result.faults, ALLOWED_NCAP_FAULTS);
					if(site.result.score < (LOCALITY_SCORE_CUTOFF - faultAllowance)) {
						logReject(site, "LOW SCORE");
						// stop further processing
						continue;
					} else {
						// FME: AP_TYPEsetter
						site.apType = "NCAP";
					}
				} else {
					logReject(site, "POOR MATCH");
					// stop further processing
					continue;
				}
				// separate pseudo-sites into their own list
				// FME: PseudoTest
				if(site.isPseudoSite) {
					site.uuid = UUID.randomUUID();
					pseudoSites.add(site);
				} else {
					// group the addresses based on the the hashString which is essentially the full address string
					String hashString = site.uniqueKey();
					List<InputSite> siteList = siteMap.get(hashString);
					if(siteList == null) {
						siteList = new ArrayList<InputSite>(2);
						siteMap.put(hashString, siteList);
					}
					site.uuid = UUID.randomUUID();
					siteList.add(site);
				}
			}
		}
		return siteMap;
	}
	
	private List<InputSite> deduplicateSites(Map<String, List<InputSite>> siteMap, List<InputSite> additionalBcaSids) {
		List<InputSite> deduped = new ArrayList<InputSite>(siteMap.size());
		for(Map.Entry<String,List<InputSite>> entry: siteMap.entrySet()) {
			String key = entry.getKey();
			List<InputSite> siteList = entry.getValue();
			siteList.sort((s1, s2) -> {
				Integer s1p = priorityMap.get(s1.inputName);
				if(s1p == null) return 1;
				Integer s2p = priorityMap.get(s2.inputName);
				if(s2p == null) return -1;
				return s1p.compareTo(s2p);
			});
			InputSite site = siteList.get(0);
			for(int i = 1; i < siteList.size(); i++) {
				InputSite rejectedSite = siteList.get(i);
				logReject(rejectedSite, "REPEAT " + rejectedSite.apType + " ADDRESS DROPPED");
				if("BCA".equals(rejectedSite.inputName)) {
					rejectedSite.uuid = site.uuid;
					additionalBcaSids.add(rejectedSite);
				}
			}
			// FME: GSRtester
			if(site.inputName.equals("GSR") 
					&& !"NCAP".equals(site.apType)) {
				// FME: AttributeRemover_GSR
				site.narrativeLocation = null;
				site.siteName = null;
			}
			trace(site, "was kept after deduplication");
			
			deduped.add(site);
		}
		return deduped;
	}
	
	private void assignParents(List<InputSite> dedupedSites) {
		int nextParentId = FIRST_GENERATED_PARENT_ID;
		Map<String,List<InputSite>> subsiteMap = new THashMap<String,List<InputSite>>();
		List<InputSite> parents = new ArrayList<InputSite>();
		for(InputSite site : dedupedSites) {
			trace(site, "entering parent/child determination");
			// FME: SubsiteTester
			// if the site is a child
			if((site.unitNumber != null && !site.unitNumber.isEmpty())
					|| site.superFullSiteDescriptor != null && !site.superFullSiteDescriptor.isEmpty()) {
				List<InputSite> children = subsiteMap.get(site.subsiteKey());
				if(children == null) {
					children = new ArrayList<InputSite>();
					subsiteMap.put(site.subsiteKey(), children);
				}
				children.add(site);
			} else {
				// possible parent
				parents.add(site);
			}
		}
		dedupedSites.clear();
		// FME: FeatureMerger_3
		for(InputSite parent : parents) {
			dedupedSites.add(parent);
			trace(parent, "is a parent, adding children");
			List<InputSite> children = subsiteMap.remove(parent.subsiteKey());
			if(children != null) {
				for(InputSite child : children) {
					trace(child, "is a child, being added to parent gId: " + parent.gId);
					child.parentId = parent.siteId;
					child.forceSubsite();
					dedupedSites.add(child);
				}
			}
		}
		// FME: OrphanAggregator
		// anything left in the hashmap is an orphan
		// loop over the groups of orphans
		for(List<InputSite> orphans : subsiteMap.values()) {
			// determine the bounds of the orphan group 
			Envelope bounds = new Envelope();
			for(Iterator<InputSite> i = orphans.iterator(); i.hasNext(); ) {
				InputSite orphan = i.next();
				// FME: GSRunitTester_2
				if(orphan.apType.equals("GSR_UNIT")) {
					logReject(orphan, "GSR UNIT ORPHAN");
					i.remove();
				} else {
					// FME: BoundsExtractor
					bounds.expandToInclude(orphan.albersX, orphan.albersY);
				}
			}
			if(orphans.isEmpty()) continue;
			
			// FME: BoundsDimensions
			// FME: ProximityTester
			// if the bounds are too large, we toss out the whole orphan group
			// TODO: perhaps find the centroid and only throw out orphans that are too far, if the std dev is low enough
			if(bounds.getWidth() > ORPHAN_SUBSITE_GROUP_TOLERANCE 
					|| bounds.getHeight() > ORPHAN_SUBSITE_GROUP_TOLERANCE) {
				for(InputSite orphan: orphans) {
					logReject(orphan, "SPREAD OUT SUBSITE GROUP (BBOX: " + bounds.getWidth() + " x " + bounds.getHeight());
				}
			} else {
				// FME: Counter
				int parentId = nextParentId++;
				
				// FME: SiteCreator
				// create a parent for the orphans
				InputSite parent = new InputSite();
				// pick an arbitrary orphan to copy attributes from
				InputSite chosenOne = orphans.get(0);
				parent.siteId = parentId;
				parent.gId = parentId;
				parent.result = chosenOne.result;
				parent.locationDescriptor = chosenOne.locationDescriptor;
				parent.status = PhysicalStatus.ACTIVE;
				// TODO: perhaps use the calculated centroid instead of an arbitrary child location
				parent.albersX = chosenOne.albersX;
				parent.albersY = chosenOne.albersY;
				parent.apType = chosenOne.apType;
				parent.isPrimary = chosenOne.isPrimary;
				parent.accessPositionalAccuracy = chosenOne.accessPositionalAccuracy;
				parent.civicNumber = chosenOne.civicNumber;
				parent.civicNumberSuffix = chosenOne.civicNumberSuffix;
				parent.accessPointStatus = chosenOne.accessPointStatus;
				parent.positionalAccuracy = PositionalAccuracy.MEDIUM;
				parent.localityId = chosenOne.localityId;
				// TODO maybe inputName should be "GENERATED"?
				parent.inputName = chosenOne.inputName;
				parent.uuid = UUID.randomUUID();
				dedupedSites.add(parent);
				for(InputSite orphan : orphans) {
					trace(orphan, "is an orphan, being added to newly created parent gId: " + parent.gId);
					orphan.parentId = parentId;
					orphan.forceSubsite();
					dedupedSites.add(orphan);
				}
			}
				
		}
		
	}

	private List<OutputSite> readSitesHybrid(String filePath) {
		List<OutputSite> outSites = new ArrayList<OutputSite>(4000000);
		try(RowReader rr = new TsvRowReader(filePath, geometryFactory)) {
			while(rr.next()) {
				if(rr.getInteger("GID") == null) continue;
				OutputSite site = new OutputSite(rr); 
				outSites.add(site);
			}
		}
		return outSites;
	}

	private void compareOutputs(List<OutputSite> outSites, List<OutputSite> compareSites) {
		outSites.sort(Comparator.comparingInt(s -> s.gId));
		compareSites.sort(Comparator.comparingInt(s -> s.gId));
		int idxA = 0;
		int idxB = 0;
		int matches = 0;
		int diffs = 0;
		int extras = 0;
		int missing = 0;
		while(idxA < outSites.size() && idxB < compareSites.size()) {
			OutputSite a = outSites.get(idxA);
			OutputSite b = compareSites.get(idxB);
			if(a.gId.equals(b.gId)) {
				// compare the two and output differences if any
				String diff = a.compare(b);
				if(diff.isEmpty()) {
					matches++;
				} else {
					diffs++;
					if(diffs < 10) {
						logger.info("{} diff: {}", a.gId, diff);
					}
				}
				idxA++;
				idxB++;
			} else if(a.gId < b.gId) {
				// record "a" is not in the output we are comparing to
				extras++;
				if(extras < 10) {
					logger.info("+{}", a.gId);
				}
				idxA++;
			} else {
				// record "b" is in the compared output but not ours
				missing++;
				if(missing < 10) {
					logger.info("-{}", b.gId);
				}
				idxB++;
			}
		}
		// output any extra unmatched output
		for(; idxA < outSites.size(); idxA++) {
			// record "a" is not in the output we are comparing to
			OutputSite a = outSites.get(idxA);
			logger.info("+{}", a.gId);
			extras++;
		}
		// output any extra unmatched compared output
		for(; idxB < compareSites.size(); idxB++) {
			// record "b" is in the compared output but not ours
			OutputSite b = compareSites.get(idxB);
			logger.info("-{}", b.gId);
			missing++;
		}
		logger.info("matches: {}, diffs: {}, extras: {}, missing: {}", matches, diffs, extras, missing);
	}
	
	private void trace(InputSite site, String message) {
		if(TRACE_CONDITION.apply(site)) {
			logger.info("TRACING gId: {} {}", site.gId, message);
		}
	}
	
	private void logReject(InputSite site, String reason) {
		trace(site, "rejected, reason: " + reason);
		Map<String,Object> row = new HashMap<String,Object>(30);
		row.put("ID", site.id);
		row.put("GID", site.gId);
		row.put("SITE_ID", site.siteId);
		row.put("REASON", reason);
		rejectWriter.writeRow(row);
	}
	
	private RowWriter openSiteOutputWriter() {
		File outFile = new File(outputDir + SITE_HYBRID_FILE);
		List<String> siteSchema = Arrays.asList(
				"GID",
				"INPUT_NAME",
				"SITE_ID",
				"SITE_UUID",
				"PARENT_SITE_ID",
				"SITE_NAME",
				"LOCATION_DESCRIPTOR",
				"UNIT_DESIGNATOR",
				"UNIT_NUMBER",
				"UNIT_NUMBER_SUFFIX",
				"SITE_POSITIONAL_ACCURACY",
				"SITE_STATUS",
				"SITE_RETIRE_DATE",
				"SITE_ALBERS_X",
				"SITE_ALBERS_Y",
				"AP_TYPE",
				"IS_PRIMARY_IND",
				"NARRATIVE_LOCATION",
				"ACCESS_POSITIONAL_ACCURACY",
				"CIVIC_NUMBER",
				"CIVIC_NUMBER_SUFFIX",
				"ACCESS_POINT_STATUS",
				"ACCESS_RETIRE_DATE",
				"ACCESS_ALBERS_X",
				"ACCESS_ALBERS_Y",
				"FULL_ADDRESS",
				"LOCALITY_ID",
				"INTERIM_STREET_NAME_ID",
				"RANGE_TYPE",
				"SITE_CHANGE_DATE"
			);
		return new XsvRowWriter(outFile, '\t', siteSchema, true);
	}
	
	private void writeOutput(List<InputSite> sitesToOutput, List<InputSite> pseudoSites, List<OutputSite> anchorPoints) {
		try(RowWriter siteOutputWriter = openSiteOutputWriter()) {
			for(InputSite site : sitesToOutput) {
				writeSiteOutput(siteOutputWriter, site);
			}
			for(InputSite site : pseudoSites) {
				writeSiteOutput(siteOutputWriter, site);
			}
			for(OutputSite site : anchorPoints) {
				site.write(siteOutputWriter);
			}
		} 
	}
	
	private void writeSiteOutput(RowWriter siteOutputWriter, InputSite site) {
		Map<String,Object> row = new HashMap<String,Object>(30);
		row.put("GID", site.gId);
		row.put("INPUT_NAME", site.inputName);
		row.put("SITE_ID", site.siteId);
		row.put("SITE_UUID", site.uuid);
		row.put("PARENT_SITE_ID", site.parentId); 
		row.put("SITE_NAME", site.siteName); 
		row.put("LOCATION_DESCRIPTOR", site.locationDescriptor == null ? "" : site.locationDescriptor.toString()); 
		row.put("UNIT_DESIGNATOR", site.unitDesignator); 
		row.put("UNIT_NUMBER", site.unitNumber); 
		row.put("UNIT_NUMBER_SUFFIX", site.unitNumberSuffix); 
		row.put("SITE_POSITIONAL_ACCURACY", site.positionalAccuracy); 
		row.put("SITE_STATUS", site.isPseudoSite ? 'X' : site.status == null ? "" : site.status.toDbValue()); 
		row.put("SITE_RETIRE_DATE", site.retireDate); 
		row.put("SITE_ALBERS_X", site.albersX); 
		row.put("SITE_ALBERS_Y", site.albersY);
		// FME: GSRunitTester
		if("GSR_UNIT".equals(site.apType)) {
			// FME: UnitTester
			if(site.unitNumber != null && !site.unitNumber.isBlank()) {
				// FME: APtypeResetter
				site.apType = "";
			} else {
				// FME: APtypeResetter_2
				site.apType = "CAP";
			}
		}
		row.put("AP_TYPE", site.apType); 
		row.put("IS_PRIMARY_IND", site.isPrimary); 
		row.put("NARRATIVE_LOCATION", site.narrativeLocation); 
		row.put("ACCESS_POSITIONAL_ACCURACY", site.accessPositionalAccuracy == null ? "" : site.accessPositionalAccuracy.toString()); 
		row.put("CIVIC_NUMBER", site.civicNumber); 
		row.put("CIVIC_NUMBER_SUFFIX", site.civicNumberSuffix); 
		row.put("ACCESS_POINT_STATUS", site.accessPointStatus == null ? "" : site.accessPointStatus.toDbValue()); 
		row.put("ACCESS_RETIRE_DATE", site.accessRetireDate); 
		row.put("ACCESS_ALBERS_X", site.accessAlbersX); 
		row.put("ACCESS_ALBERS_Y", site.accessAlbersY); 
		row.put("FULL_ADDRESS", site.fullAddress());
		row.put("LOCALITY_ID", site.localityId); 
		row.put("INTERIM_STREET_NAME_ID", site.result.name == null ? "" : site.result.name.id); 
		row.put("RANGE_TYPE", site.isPseudoSite ? -1 : 0); 
		row.put("SITE_CHANGE_DATE", TODAY); 
		siteOutputWriter.writeRow(row);
	}
	
	private void writeSid2Pids(List<InputSite> outputSites, List<InputSite> additionalBcaSids) {
		Map<String, List<String>> jurolMap = readPids();
		try(RowWriter sid2pidWriter = openSid2PidWriter()) {
			for(InputSite site : outputSites) {
				writeSid2Pid(sid2pidWriter, site, jurolMap.get(site.yourId));
			}
			for(InputSite site : additionalBcaSids) {
				writeSid2Pid(sid2pidWriter, site, jurolMap.get(site.yourId));
			}
		}
	}
	
	private void writeSid2Pid(RowWriter sid2pidWriter, InputSite site, List<String> pids) {
		if("BCA".equals(site.inputName) && (site.yourId != null && !site.yourId.isEmpty())) {
			if(pids != null) {
				Map<String,Object> row = new HashMap<String,Object>();
				row.put("SID", site.uuid);
				row.put("PID", String.join("|", pids));
				sid2pidWriter.writeRow(row);
			} else {
				logReject(site,"NO XREF");
			}
		}
	}
	
	private RowWriter openSid2PidWriter() {
		File sid2pidFile = new File(outputDir + SID2PID_FILE);
		List<String> siteSchema = Arrays.asList("SID","PID");
		return new XsvRowWriter(sid2pidFile, ',', siteSchema, true);
	}
	
	private int calculateFaultAllowance(List<MatchFault> faults, Map<MatchElement, List<String>> allowedFaultMap) {
		int faultAllowance = 0;
		for(MatchFault fault : faults) {
			if(faultIsAllowed(fault, allowedFaultMap)) {
				faultAllowance += fault.getPenalty();
			}
		}
		return faultAllowance;
	}

	private boolean faultIsAllowed(MatchFault fault, Map<MatchElement, List<String>> allowedFaultMap) {
		List<String> allowedFaults = allowedFaultMap.get(fault.getElement());
		if(allowedFaults != null && (allowedFaults.isEmpty() || allowedFaults.contains(fault.getFault()))) {
			return true;
		}
		return false;
	}

	private Integer getLocalityId(String localityName) {
		if(localityName != null) {
			Integer id = localityMap.get(simplifyLocalityName(localityName));
			if(id != null) {
				return id;
			} else {
				logger.error("Unknown Locality Name: '{}' could not find in 'street_load_localities.json', possibly out of province", localityName );
			}
		}
		return null;
	}
	
	private String simplifyLocalityName(String localityName) {
		return localityName.replaceAll("ç", "c").replaceAll("\\W", "").toUpperCase();
	}
	
	private static String addTrailingSeparator(String path) {
		if(path.charAt(path.length()-1) == File.separatorChar){
		    return path;
		}
		return path += File.separator;
	}
}
