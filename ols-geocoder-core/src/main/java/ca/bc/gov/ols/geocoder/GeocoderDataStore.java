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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.enums.DividerType;
import ca.bc.gov.ols.enums.LaneRestriction;
import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.TravelDirection;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.GeometryReprojector;
import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.config.AbbreviationMapping;
import ca.bc.gov.ols.geocoder.config.FeatureNotSupportedException;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStore;
import ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStoreFactory;
import ca.bc.gov.ols.geocoder.config.LocalityMapping;
import ca.bc.gov.ols.geocoder.config.UnitDesignator;
import ca.bc.gov.ols.geocoder.data.AccessPoint;
import ca.bc.gov.ols.geocoder.data.BlockFace;
import ca.bc.gov.ols.geocoder.data.BusinessCategory;
import ca.bc.gov.ols.geocoder.data.CivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.ILocation;
import ca.bc.gov.ols.geocoder.data.IOccupant;
import ca.bc.gov.ols.geocoder.data.ISite;
import ca.bc.gov.ols.geocoder.data.Locality;
import ca.bc.gov.ols.geocoder.data.LocalityMapTarget;
import ca.bc.gov.ols.geocoder.data.NonCivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.OccupantFactory;
import ca.bc.gov.ols.geocoder.data.SiteFactory;
import ca.bc.gov.ols.geocoder.data.StateProvTerr;
import ca.bc.gov.ols.geocoder.data.StreetIntersection;
import ca.bc.gov.ols.geocoder.data.StreetName;
import ca.bc.gov.ols.geocoder.data.StreetNameBody;
import ca.bc.gov.ols.geocoder.data.StreetSegment;
import ca.bc.gov.ols.geocoder.data.enumTypes.GeocoderFeature;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocalityType;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.data.enumTypes.PhysicalStatus;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.geocoder.data.enumTypes.Side;
import ca.bc.gov.ols.geocoder.data.indexing.BlockFaceIntervalTree;
import ca.bc.gov.ols.geocoder.data.indexing.ExactMatchLookup;
import ca.bc.gov.ols.geocoder.data.indexing.InvertedIndex;
import ca.bc.gov.ols.geocoder.data.indexing.InvertedIndex.InvertedIndexBuilder;
import ca.bc.gov.ols.geocoder.data.indexing.KDTree;
import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.NameLookupTrie;
import ca.bc.gov.ols.geocoder.data.indexing.PartialTagIndex;
import ca.bc.gov.ols.geocoder.data.indexing.TagIndex;
import ca.bc.gov.ols.geocoder.data.indexing.TrieWordMap;
import ca.bc.gov.ols.geocoder.data.indexing.Word;
import ca.bc.gov.ols.geocoder.data.indexing.WordClass;
import ca.bc.gov.ols.geocoder.data.indexing.WordMap;
import ca.bc.gov.ols.geocoder.data.indexing.WordMapBuilder;
import ca.bc.gov.ols.geocoder.datasources.GeocoderDataSource;
import ca.bc.gov.ols.geocoder.datasources.GeocoderDataSourceFactory;
import ca.bc.gov.ols.geocoder.dra.DraLexicalRules;
import ca.bc.gov.ols.geocoder.filters.ExcludeUnitsAccessPointFilter;
import ca.bc.gov.ols.geocoder.filters.ExcludeUnitsSiteFilter;
import ca.bc.gov.ols.geocoder.filters.Filter;
import ca.bc.gov.ols.geocoder.filters.Filters;
import ca.bc.gov.ols.geocoder.filters.MultiFilter;
import ca.bc.gov.ols.geocoder.filters.OnlyCivicAccessPointFilter;
import ca.bc.gov.ols.geocoder.filters.OnlyCivicSiteFilter;
import ca.bc.gov.ols.geocoder.filters.PointLocationBboxFilter;
import ca.bc.gov.ols.geocoder.filters.StreetIntersectionDegreeFilter;
import ca.bc.gov.ols.geocoder.status.BasicStatus;
import ca.bc.gov.ols.geocoder.status.SystemStatus;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;
import ca.bc.gov.ols.rowreader.DateType;
import ca.bc.gov.ols.rowreader.RowReader;
import ca.bc.gov.ols.util.ArraySet;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;

/**
 * The GeocoderDatastore holds the general logic for building the data structures to support
 * geocoding. The database-specific queries are left to the database-specific GeocoderDataSource
 * classes.
 * 
 * This class load a large portion of the data in the supporting database into memory in several
 * data structures. Various temporary data structures are created while any one table is being
 * queried, and they are used later after all the necessary tables are queried to build a bigger
 * structure, and then in some cases set null to free up the memory.
 * 
 * @author chodgson
 */
public class GeocoderDataStore {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX +
			GeocoderDataStore.class.getCanonicalName());

	/* used to pre-allocate the exactMatchLookup array */
	private static final int NUM_EXPECTED_ADDRESSES = 10000;

	private GeocoderDataSource dataSource;
	private SystemStatus status;
	
	/* This is the global geometry factory, all geometries are created using it */
	private static GeometryFactory geometryFactory;
	
	BlockFaceInterpolator interpolator;
	
	private EnumSet<GeocoderFeature> featureSet = EnumSet.allOf(GeocoderFeature.class);

	/*
	 * Map from Intersection UUID to Intersection object, used for geocode lookups
	 */
	private Map<UUID,StreetIntersection> intersectionsByUuid;

	/* This wraps a trie structure used to spell-correct, map and classify all incoming words */
	private WordMap wordMap;
	
	/* use to normalize all date references (like interning strings) */
	private Map<LocalDate,LocalDate> dateMap;
	
	/*
	 * This is a trie structure which essentially maps from StreetNameBody strings to StreetNameBody
	 * objects. It is built by the data structure building process, and used for geocoding streets
	 * and intersections
	 */
	private NameLookupTrie<StreetNameBody> streetNameTrie;
	
	/*
	 * Maps from a SiteName (fullSiteDescriptor) to a list of sites with that name
	 */
	private InvertedIndex<ISite> siteNameIndex;

	/*
	 * Maps from an OccupantName and SiteName (fullSiteDescriptor) to a list of occupants with that name
	 */
	private InvertedIndex<IOccupant> occupantNameIndex;

	/* used to lookup occupants by Id */
	private Map<UUID,IOccupant> occupantsByUuid;
	
	/* used to lookup occupants by keywords */
	private TagIndex<IOccupant> occupantKeywordIndex;
	
	private Map<String, BusinessCategory> businessCategories;
	
	private ExactMatchLookup exactMatchLookup;
	
	/* Maps from words within a tag to the complete tag string */
	private PartialTagIndex partialTagIndex;
	
	private Set<String> unitDesignators;
	
	private Map<String, String> abbrMappings;
	
	/*
	 * This is built by the data structure building process, and used for looking up localities
	 * while allowing for spelling errors
	 */
	private NameLookupTrie<LocalityMapTarget> localityNameTrie;
	
	/* Used for looking up localities by Id */
	private Map<Integer, Locality> localityIdMap;
	
	/* Used for looking up State/Prov/Terr by name */
	private Map<String, StateProvTerr> stateProvTerrByName;
	
	private TIntObjectHashMap<StateProvTerr> stateProvTerrById;
	
	/* Used for looking up sites by UUID */
	private Map<UUID, ISite> sitesByUuid;
	
	/* Used for looking up segments by segmentId */
	TIntObjectHashMap<StreetSegment> streetSegmentIdMap;
	
	/* Used for reverse Geocoding AccessPoints */
	private KDTree<AccessPoint> apKDTree;

	/* Used for reverse Geocoding Sites */
	private KDTree<ISite> siteKDTree;

	/* Used for reverse Geocoding Occupants */
	private KDTree<IOccupant> occupantKDTree;

	/* Used for reverse Geocoding Intersections */
	private KDTree<StreetIntersection> intersectionKDTree;

	/* Used for reverse Geocoding StreetSegments */
	//private RTree<StreetSegment> streetSegmentRTree;

	GeometryReprojector reprojector;

	private Map<DateType, ZonedDateTime> dates;
	
	public GeocoderDataStore(Properties bootstrapConfig, GeometryFactory gf, GeometryReprojector reprojector) {
		logger.info("GeocoderDataStore() constructor called");
		geometryFactory = gf;
		this.reprojector = reprojector;
		status = new SystemStatus();
		status.startTimestamp = ZonedDateTime.now().toString();
		EnumSet<GeocoderFeature> features = GeocoderFeature.fromStringList(bootstrapConfig.getProperty("features"));
		if(features != null) {
			featureSet = features;
		}
		GeocoderConfigurationStore configStore = GeocoderConfigurationStoreFactory.getConfigurationStore(bootstrapConfig);
		
		if(geometryFactory == null) {
			geometryFactory = new GeometryFactory(GeocoderConfig.BASE_PRECISION_MODEL, Integer.parseInt(configStore.getConfigParam("baseSrsCode").get()));
		}

		GeocoderConfig config = new GeocoderConfig(configStore, geometryFactory);
		dataSource = GeocoderDataSourceFactory.getGeocoderDataSource(config, geometryFactory);
		interpolator = new BlockFaceInterpolator(dataSource.getConfig(), geometryFactory);
		loadData();
	}
	
	public LocalityMapTarget getBestLocalityMapping(String localityName) {
		if(localityName == null) {
			return null;
		}
		Collection<MisspellingOf<LocalityMapTarget>> matches = localityNameTrie.queryExactWordPrefix(localityName);
		MisspellingOf<LocalityMapTarget> best = null;
		for(MisspellingOf<LocalityMapTarget> lm : matches) {
			if(best == null || lm.get().getConfidence() > best.get().getConfidence()
					|| lm.getError() < best.getError()) {
				best = lm;
			}
		}
		if(best == null) {
			return null;
		}
		return best.get();
	}
	
	public List<MisspellingOf<LocalityMapTarget>> getLocalities(String localityName) {
		return localityNameTrie.queryExactWordPrefix(localityName);
	}
	
	public StateProvTerr getStateProvTerr(String stateProvTerr) {
		return stateProvTerrByName.get(stateProvTerr);
	}
	
	public List<MisspellingOf<StreetNameBody>> getStreetNameBodies(String name) {
		return streetNameTrie.queryExactWordPrefix(name);
	}
	
	public List<MisspellingOf<StreetName>> getStreetNames(String name, String type, String dir, String qual) {
		List<MisspellingOf<StreetName>> names = new ArrayList<MisspellingOf<StreetName>>();
		if(name.equalsIgnoreCase("st")) return names; // "St" is too vague to match anything
		List<MisspellingOf<StreetNameBody>> bodies = streetNameTrie.queryExactWordPrefix(name);
		for(MisspellingOf<StreetNameBody> body : bodies) {
			for(StreetName streetName : body.get().getStreetNames()) {
				if(body.getError() == 0
						&& GeocoderUtil.equalsIgnoreCaseNullSafe(streetName.getType(), type)
						&& GeocoderUtil.equalsIgnoreCaseNullSafe(streetName.getDir(), dir)
						&& GeocoderUtil.equalsIgnoreCaseNullSafe(streetName.getQual(), qual)) {
					names.add(0, new MisspellingOf<StreetName>(streetName, body.getError(), name));
				} else {
					names.add(new MisspellingOf<StreetName>(streetName, body.getError(), name));
				}
			}
		}
		return names;
	}
	
	public Set<ISite> getSitesByName(String[] siteNameWords) {
		if(siteNameWords.length == 0) {
			return Collections.<ISite> emptySet();
		}
		for(int i = 0; i < siteNameWords.length; i++) {
			if(unitDesignators.contains(siteNameWords[i])) {
				siteNameWords[i] = dataSource.getConfig().getDefaultUnitDesignator();
			}
		}
		return siteNameIndex.query(siteNameWords);
	}

	public Set<IOccupant> getOccupantsByName(String[] occupantNameWords) {
		if(occupantNameWords.length == 0) {
			return Collections.<IOccupant> emptySet();
		}
		return occupantNameIndex.query(occupantNameWords);
	}

	private void loadData() {
		logger.info("Starting loading GeocoderDataStore data structure...");
		long startTime = System.currentTimeMillis();
		logger.debug("Max memory:{}  In use already:{}",
				(Runtime.getRuntime().maxMemory() / 1000000),
				((Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory()) / 1000000));
		dateMap = new THashMap<LocalDate,LocalDate>();
		WordMapBuilder wordMapBuilder = new WordMapBuilder();
		exactMatchLookup = new ExactMatchLookup(NUM_EXPECTED_ADDRESSES);
		abbrMappings = buildAbbreviationMappings(wordMapBuilder);
		TIntObjectHashMap<ISite> siteIdMap = new TIntObjectHashMap<ISite>();
		stateProvTerrByName = buildStateProvTerrMap(wordMapBuilder);
		TIntObjectHashMap<String> electoralAreaById = buildElectoralAreaIdMap();
		Map<String, Set<LocalityMapTarget>> localityMappings = buildLocalityNameTrie(electoralAreaById, wordMapBuilder);
		localityNameTrie = new NameLookupTrie<LocalityMapTarget>(localityMappings);
		TIntObjectHashMap<StreetIntersection> intersectionIdMap = buildIntersectionIdMap();
		TIntObjectHashMap<List<AccessPoint>> accessPointMap = buildAccessPointAndSiteMaps(wordMapBuilder, siteIdMap);
		streetNameTrie = buildStreetNameTrie(wordMapBuilder, accessPointMap, intersectionIdMap, electoralAreaById);

		// add all the sites to the exactMatchLookup
		siteIdMap.forEachValue(new TObjectProcedure<ISite>() {
			@Override
			public boolean execute(ISite site) {
				AddressMatch match = new AddressMatch(new SiteAddress(site, null), MatchPrecision.SITE, 
						getConfig().getMatchPrecisionPoints(MatchPrecision.SITE));
				exactMatchLookup.add(match);
				return true;
			}
		});
		
		// clean up and build the exactMatchLookup
		logger.info("Sorting Exact Match Lookup...");
		exactMatchLookup.build();
		
		// should be done reading from config now
		dataSource.getConfig().close();
		
		// loop over all intersections and minimize their arraySets
		for(StreetIntersection i : intersectionIdMap.valueCollection()) {
			i.trimToSize();
		}
		
		// these are all only for reverse geocoding, we can save space by not creating them (eg. for CPF)
		if(featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			//streetSegmentRTree = new RTree<StreetSegment>(streetSegmentIdMap.valueCollection());
			List<AccessPoint> apList = new ArrayList<AccessPoint>(accessPointMap.size());
			for(List<AccessPoint> aps : accessPointMap.valueCollection()) {
				apList.addAll(aps);
			}
			apKDTree = new KDTree<AccessPoint>(apList);
			siteKDTree = new KDTree<ISite>(new ArrayList<ISite>(siteIdMap.valueCollection()));
			intersectionKDTree = new KDTree<StreetIntersection>(
					new ArrayList<StreetIntersection>(intersectionIdMap.valueCollection()));
			intersectionsByUuid = new THashMap<UUID,StreetIntersection>(intersectionIdMap.capacity());
			for(StreetIntersection i : intersectionIdMap.valueCollection()) {
				intersectionsByUuid.put(i.getUuid(), i);
			}
		}
		// we're done with this map let's free it up
		intersectionIdMap = null;

		businessCategories = buildBusinessCategories();
		occupantKeywordIndex = new TagIndex<IOccupant>();
		// this also builds the name index, which we need for forward geocoding
		occupantsByUuid = buildOccupantMap(wordMapBuilder, businessCategories, siteIdMap);
		// but we only actually need the Uuid index for reverse geocoding 
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			occupantsByUuid = null;
		} else {
			occupantKDTree = new KDTree<IOccupant>(new ArrayList<IOccupant>(occupantsByUuid.values()));
		}
		
		Map<String, Set<Word>> initialWordMap = wordMapBuilder.getWordMap();
		Map<String,String> compoundWordMap = buildCompoundWordMap(initialWordMap.keySet());
		
		// add trie entries to map from compound words to the split version and vice-versa
		// first for streetNames
		for(String name : streetNameTrie.findAllStrings()) {
			List<String> compoundWordPhrases = createCompoundWordPhrases(name, compoundWordMap);
			Set<StreetNameBody> result = streetNameTrie.queryExact(name);
			for(String compoundWordPhrase : compoundWordPhrases) {
				streetNameTrie.add(compoundWordPhrase, result);
				// we also need to add these (possibly previously unknown) compound words to our wordmap
				wordMapBuilder.addPhrase(compoundWordPhrase, WordClass.STREET_NAME_BODY);
			}
		}
		
		// then for localities
		for(String name : localityNameTrie.findAllStrings()) {
			List<String> compoundWordPhrases = createCompoundWordPhrases(name, compoundWordMap);
			Set<LocalityMapTarget> result = localityNameTrie.queryExact(name);
			for(String compoundWordPhrase : compoundWordPhrases) {
				localityNameTrie.add(compoundWordPhrase, result);
				// we also need to add these (possibly previously unknown) compound words to our wordmap
				wordMapBuilder.addPhrase(compoundWordPhrase, WordClass.LOCALITY_NAME);
			}
		}
		
		wordMap = new TrieWordMap(wordMapBuilder.getWordMap());
		wordMapBuilder = null;
		dateMap = null;
		siteIdMap = null;
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("Finished loading GeocoderDataStore data structure ({} secs).",
				elapsedTime / 1000);
		logger.debug(
				"Memory in use after loading(Megs): {}",
				((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000));
		dates = dataSource.getDates();
		status.setDates(dates);
		if(dates == null) {
			logger.error("Data file dates are inconsistent; Errors may result! Check that all files were processed and copied correctly.");
		} else {
			logger.info("Data file dates are consistent.");
		}
	}
	
	private Map<String,String> buildCompoundWordMap(Set<String> wordSet) {
		Map<String,String> compoundWordMap = new THashMap<String,String>();
		for(String word : wordSet) {
			if(word.length() >= 6) {
				for(int split = 3; split < word.length()-3; split++) {
					String a = word.substring(0, split);
					String b = word.substring(split);
					if(wordSet.contains(a) && wordSet.contains(b)) {
						// we found a compound word
						compoundWordMap.put(word, a + " " + b );
						logger.debug("CompoundWordMap: {} + {} = {}", a, b, word);
					}
				}
			}
		}
		return compoundWordMap;
	}
	
	private List<String> createCompoundWordPhrases(String name, Map<String,String> compoundWordMap) {
		List<String> newPhrases = new ArrayList<String>();
		String[] nameWords = GeocoderUtil.wordSplit(name);
		// loop over the words that make up the name
		for(int i = 0; i < nameWords.length; i++) {
			// check if the word is a compound word
			String splitWords = compoundWordMap.get(nameWords[i]);
			if(splitWords != null) {
				// build a new name phrase with the two words split
				StringBuilder sb = new StringBuilder(name.length());
				for(int j = 0; j < nameWords.length; j++) {
					if( j == i ) {
						sb.append(splitWords);
					} else {
						sb.append(nameWords[j]);
					}
					// no space at the end
					if(j != nameWords.length) {
						sb.append(" ");
					}
				}
				logger.debug("Split Compound Word Phrase:" + sb.toString());
				newPhrases.add(sb.toString());				
			}
			// if there is another word after this one
			if(i + 1 < nameWords.length) {
				// if both words are alphabetic (no numbers), not "NEAR", and at least 3 character long
				if(nameWords[i].length() > 2 && !nameWords[i].equals("NEAR") && GeocoderUtil.isAlpha(nameWords[i]) 
						&& nameWords[i+1].length() > 2 && !nameWords[i+1].equals("NEAR") && GeocoderUtil.isAlpha(nameWords[i+1])) {
					logger.debug("Created Compound Word: " + nameWords[i] + " + " + nameWords[i+1] + " = " + nameWords[i] + nameWords[i+1] );
					// build a new name phrase that combines the two words
					StringBuilder sb = new StringBuilder(name.length());
					for(int j = 0; j < nameWords.length; j++) {
						sb.append(nameWords[j]);
						// no space after the first combined word or at the end
						if(j != i && j != nameWords.length) {
							sb.append(" ");
						}
					}
					logger.debug("Combined Compound Word Phrase:" + sb.toString());
					newPhrases.add(sb.toString());
				}
			}
		}
		return newPhrases;
	}	

	private TIntObjectHashMap<StreetIntersection> buildIntersectionIdMap() {
		// build a map from the intersection Id to the Intersection object
		TIntObjectHashMap<StreetIntersection> intMap = new TIntObjectHashMap<StreetIntersection>();
		RowReader rr = dataSource.getIntersections();
		int count = 0;
		while(rr.next()) {
			count++;
			int id = rr.getInt("street_intersection_id");
			UUID uuid = UUID.fromString(rr.getString("intersection_uuid"));
			int degree = rr.getInt("degree");
			Point point = rr.getPoint();
			StreetIntersection intersection = new StreetIntersection(id, uuid, point, degree);
			intMap.put(id, intersection);
		}
		logger.debug("Intersection count: {}", count);
		status.counts.put("intersections", count);
		rr.close();
		return intMap;
	}
	
	private Map<String, StateProvTerr> buildStateProvTerrMap(
			WordMapBuilder wordMapBuilder) {
		Map<String, StateProvTerr> sptMap = new THashMap<String, StateProvTerr>();
		stateProvTerrById = new TIntObjectHashMap<StateProvTerr>();
		RowReader rr = dataSource.getStateProvTerrs();
		int count = 0;
		while(rr.next()) {
			count++;
			int id = rr.getInt("state_prov_terr_id");
			String unMappedName = rr.getString("state_prov_terr_name");
			String countryCode = rr.getString("country_code");
			Point point = rr.getPoint();
			String mappedName = mapWord(unMappedName).toUpperCase();
			wordMapBuilder.addPhrase(mappedName, WordClass.STATE_PROV_TERR);
			StateProvTerr spt = new StateProvTerr(id, unMappedName, countryCode, point);
			sptMap.put(mappedName, spt);
			stateProvTerrById.put(id, spt);
			
			SiteAddress address = new SiteAddress(spt);
			AddressMatch match = new AddressMatch(address, MatchPrecision.PROVINCE,
					getConfig().getMatchPrecisionPoints(MatchPrecision.PROVINCE));
			exactMatchLookup.add(match);
		}
		logger.debug("State/Prov/Terr count: {}", count);
		status.counts.put("state_prov_terrs", count);
		rr.close();
		
		return sptMap;
	}
	
	private Map<String, Set<LocalityMapTarget>> buildLocalityNameTrie(
			TIntObjectHashMap<String> electoralAreaById, WordMapBuilder wordMapBuilder) {
		// Build a map from locality names to a set of the LocalityMappings the name maps to
		Map<String, Set<LocalityMapTarget>> localityMappings = new THashMap<String, Set<LocalityMapTarget>>();
		localityIdMap = new THashMap<Integer, Locality>();
		RowReader rr = dataSource.getLocalities();
		int count = 0;
		while(rr.next()) {
			count++;
			int id = rr.getInt("locality_id");
			String unMappedName = rr.getString("locality_name");
			String unMappedQual = rr.getString("locality_qualifier");
			LocalityType localityType = LocalityType.convert(rr.getInteger("locality_type_id"));
			int ea = rr.getInt("electoral_area_id");
			int stateProvTerrId = rr.getInt("state_prov_terr_id");
			Point point = rr.getPoint();
			Locality locality = new Locality(id, unMappedName, unMappedQual, localityType,
					electoralAreaById.get(ea), stateProvTerrById.get(stateProvTerrId), point);
			localityIdMap.put(id, locality);
			
			SiteAddress address = new SiteAddress(locality);
			AddressMatch match = new AddressMatch(address, MatchPrecision.LOCALITY,
					getConfig().getMatchPrecisionPoints(MatchPrecision.LOCALITY));
			exactMatchLookup.add(match);
			
			String mappedName = mapWords(unMappedName).toUpperCase();
			wordMapBuilder.addPhrase(mappedName, WordClass.LOCALITY_NAME);
			String mappedQualifiedName = mappedName;
			if(unMappedQual != null) {
				String mappedQual = mapWords(unMappedQual).toUpperCase();
				wordMapBuilder.addPhrase(mappedQual, WordClass.LOCALITY_NAME);
				mappedQualifiedName = mappedName + " " + mappedQual;
			}
			Set<LocalityMapTarget> names = localityMappings.get(mappedQualifiedName);
			if(names == null) {
				names = new ArraySet<LocalityMapTarget>();
				localityMappings.put(mappedQualifiedName, names);
			}
			names.add(new LocalityMapTarget(100, locality));
		}
		logger.debug("Locality count: {}", count);
		status.counts.put("localities", count);
		rr.close();
		
		Stream<LocalityMapping> locMaps = dataSource.getLocalityMappings();
		count = 0;
		for(Iterator<LocalityMapping> it = locMaps.iterator(); it.hasNext(); ) {
			LocalityMapping locMap = it.next();
			count++;
			String mappedName = mapWords(locMap.getInputString()).toUpperCase();
			wordMapBuilder.addPhrase(mappedName, WordClass.LOCALITY_NAME);
			Locality locality = localityIdMap.get(locMap.getLocalityId());
			if(locality == null) {
				logger.warn("CONSTRAINT VIOLATION: Locality Mapping refers to non-existent locality: " + locMap.getLocalityId());
				continue;
			}
						
			LocalityMapTarget newMapping = new LocalityMapTarget(locMap.getConfidence(), locality);
			Set<LocalityMapTarget> names = localityMappings.get(mappedName);
			if(names == null) {
				names = new ArraySet<LocalityMapTarget>();
				localityMappings.put(mappedName, names);
				names.add(newMapping);
			} else {
				// if there are already mappings for this name, check for a duplicate locality
				boolean duplicate = false;
				for(LocalityMapTarget mapping : names) {
					if(mapping.getLocality().equals(locality)) {
						// we will silently ignore these duplicates as they aren't really a big deal.
						//logger.info("Locality Mapping \"{}\" ({})is a duplicate, ignored.",
						//		unMappedName, mappedName);
						duplicate = true;
						break;
					}
				}
				if(!duplicate) {
					names.add(newMapping);
				}
			}
		};
		logger.debug("Locality Mapping count: {}", count);
		status.counts.put("locality_mappings", count);
		
		// minimize the arrays used for the locality mappings sets
		for(Set<LocalityMapTarget> set : localityMappings.values()) {
			((ArraySet<LocalityMapTarget>)set).trimToSize();
		}
		
		// build a trie using the map
		return localityMappings;
	}
	
	private TIntObjectHashMap<String> buildElectoralAreaIdMap() {
		TIntObjectHashMap<String> eaMap = new TIntObjectHashMap<String>();
		RowReader rr = dataSource.getElectoralAreas();
		int count = 0;
		while(rr.next()) {
			count++;
			int id = rr.getInt("electoral_area_id");
			String name = rr.getString("electoral_area_name");
			eaMap.put(id, name);
		}
		logger.debug("Electoral Area count: {}", count);
		status.counts.put("electorial_areas", count);
		rr.close();
		return eaMap;
	}
	
	private TIntObjectHashMap<List<AccessPoint>> buildAccessPointAndSiteMaps(WordMapBuilder wordMapBuilder,
			TIntObjectHashMap<ISite> siteIdMap) {
		sitesByUuid = new THashMap<UUID, ISite>();
		TIntObjectHashMap<List<AccessPoint>> apMap = new TIntObjectHashMap<List<AccessPoint>>();
		
		RowReader rr = dataSource.getCombinedSitesPost();
		int siteCount = 0;
		int apCount = 0;
		while(rr.next()) {
			siteCount++;
			Integer siteId = rr.getInt("site_id");
			String pids = rr.getString("pids");
			String unMappedSiteName = rr.getString("site_name");
			if(unMappedSiteName != null && !unMappedSiteName.isEmpty()) {
				String mappedSiteName = mapWords(unMappedSiteName);
				wordMapBuilder.addPhrase(mappedSiteName, WordClass.NAME);
			}
			String unitNumber = rr.getString("unit_number");
			String unitDesignator = rr.getString("unit_designator");
			if((unitDesignator == null || unitDesignator.isEmpty())
					&& unitNumber != null && !unitNumber.isEmpty()) {
				unitDesignator = getConfig().getDefaultUnitDesignator();
			}
			LocalDate siteRetireDate = rr.getDate("site_retire_date");
			if(siteRetireDate == null) {
				siteRetireDate = GeocoderConfig.NOT_RETIRED_DATE;
			}
			
			ISite site = SiteFactory.createSite(siteId, 
					UUID.fromString(rr.getString("site_uuid")),
					pids,
					rr.getInteger("parent_site_id"), 
					unMappedSiteName, 
					unitNumber, 
					rr.getString("unit_number_suffix"), 
					unitDesignator,
					rr.getPoint("site_albers_"),
					PositionalAccuracy.convert(rr.getString("site_positional_accuracy")),
					LocationDescriptor.convert(rr.getString("location_descriptor")),
					PhysicalStatus.convert(rr.getString("site_status")), 
					siteRetireDate, 
					normalizeDate(rr.getDate("site_change_date")));
			
			siteIdMap.put(siteId, site);
			
			//AP stuff
			String apType = rr.getString("ap_type");
			AccessPoint ap = null;
			if(apType != null) {
				apCount++;
				if(apCount % 100000 == 0) {
					logger.debug("AP count: " + apCount + " Memory in use (Megs): {}",
							((Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
									.freeMemory()) / 1000000));
				}
				int localityId = rr.getInt("locality_id");
				Integer segmentId = rr.getInteger("street_segment_id");
				int civicNumber = rr.getInt("civic_number");
				String civicNumberSuffix = rr.getString("civic_number_suffix");
				if(civicNumberSuffix != null) {
					civicNumberSuffix = civicNumberSuffix.toUpperCase();
				}
				boolean isPrimary = Boolean.TRUE.equals(rr.getBoolean("is_primary_ind"));
				Point point = rr.getPoint("access_albers_");
				// if no accessPoint, fallback on SitePoint
				if(point == null) {
					point = rr.getPoint("site_albers_");
				}
				PositionalAccuracy positionalAccuracy = PositionalAccuracy.convert(
						rr.getString("access_positional_accuracy"));
				String narrativeLocation = rr.getString("narrative_location"); 
				
				if(apType.equals("CAP")) {
					if(civicNumber == RowReader.NULL_INT_VALUE) {
						logger.warn("CONSTRAINT VIOLATION: CAP has null civic number; SiteID: " + siteId);
					} else {
						ap = new CivicAccessPoint(site, civicNumber,
								civicNumberSuffix, point, positionalAccuracy,
								narrativeLocation);
					}
				} else {
					ap = new NonCivicAccessPoint(site,
							localityIdMap.get(localityId), null, point, positionalAccuracy,
							narrativeLocation);
				}
				if(segmentId != null) {
					List<AccessPoint> aps = apMap.get(segmentId);
					if(aps == null) {
						aps = new ArrayList<AccessPoint>();
						apMap.put(segmentId, aps);
					}
					aps.add(ap);
				}
				if(isPrimary) {
					site.setPrimaryAccessPoint(ap);
				}
			}
			
		}
		logger.debug("Site Count: " + siteCount);
		status.counts.put("sites", siteCount);
		logger.debug("Access Point Count: " + apCount);
		status.counts.put("access_points", apCount);
		rr.close();
		
		// loop over all the Sites in the siteIdMap and resolve their parents
		for(ISite site : siteIdMap.valueCollection()) {
			site.resolveParent(siteIdMap);
		}
		
		// loop over all the Sites and minimize their children arrays.
		for(ISite site : siteIdMap.valueCollection()) {
			site.trimToSize();
		}
		
		// build the siteNameIndex (have to do this after the parent sites are resolved)
		// and also the siteByUuid map
		InvertedIndexBuilder<ISite> builder = new InvertedIndexBuilder<ISite>();
		for(ISite site : siteIdMap.valueCollection()) {
			if(featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
				sitesByUuid.put(site.getUuid(), site);
			}
			String unMappedSiteName = site.getFullSiteName(); // was Descriptor
			if(unMappedSiteName != null && !unMappedSiteName.isEmpty()) {
				builder.addItem(mapWordsArray(unMappedSiteName, true), site);
			}
		}
		siteNameIndex = builder.build();
		return apMap;
	}
	
//	private IntHashMap<List<AccessPoint>>[] buildAccessPointMap(
//			IntHashMap<Site> siteIdMap) {
//		@SuppressWarnings("unchecked")
//		IntHashMap<List<AccessPoint>>[] apMap = new IntHashMap[dataSource.getConfig()
//				.getNumRanges()];
//		for(int i = 0; i < dataSource.getConfig().getNumRanges(); i++) {
//			apMap[i] = new IntHashMap<List<AccessPoint>>();
//		}
//		RowReader rr = dataSource.getAccessPoints();
//		int count = 0;
//		while(rr.next()) {
//			count++;
//			if(count % 100000 == 0) {
//				logger.debug("AP count: " + count + " Memory in use (Megs): {}",
//						((Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
//								.freeMemory()) / 1000000));
//			}
//			int id = rr.getInt("access_point_id");
//			String apType = rr.getString("ap_type");
//			int siteId = rr.getInt("site_id");
//			int localityId = rr.getInt("locality_id");
//			Integer segmentId = null;
//			if(rr.getObject("street_segment_id") != null) {
//				segmentId = rr.getInt("street_segment_id");
//			}
//			int civicNumber = rr.getInt("civic_number");
//			String civicNumberSuffix = rr.getString("civic_number_suffix");
//			String isPrimary = rr.getString("is_primary_ind");
//			Point point = rr.getPoint();
//			PositionalAccuracy positionalAccuracy = PositionalAccuracy.convert(
//					rr.getString("positional_accuracy"));
//			AccessPoint ap = null;
//			if(apType.equals("CAP")) {
//				ap = new CivicAccessPoint(id, siteIdMap.get(siteId), civicNumber,
//						civicNumberSuffix, point, positionalAccuracy);
//			} else {
//				ap = new NonCivicAccessPoint(id, siteIdMap.get(siteId),
//						localityIdMap.get(localityId), point, positionalAccuracy);
//			}
//			if(segmentId != null) {
//				List<AccessPoint> aps = apMap.get(segmentId);
//				if(aps == null) {
//					aps = new ArrayList<AccessPoint>();
//					apMap.put(segmentId, aps);
//				}
//				aps.add(ap);
//			}
//			if(isPrimary.equals("Y")) {
//				siteIdMap.get(siteId).setPrimaryAccessPoint(ap);
//			}
//			
//		}
//		logger.debug("Access Point Count: {}", count);
//		rr.close();
//		return apMap;
//	}
	
	private LocalDate normalizeDate(LocalDate date) {
		if(date == null) {
			return null;
		}
		LocalDate normDate = dateMap.get(date);
		if(normDate == null) {
			dateMap.put(date,date);
		} 
		return normDate;
	}

	// build a map from streetName Ids to a list of StreetSegments with that StreetName
	private TIntObjectHashMap<List<StreetSegment>> buildStreetSegmentNameMap(
			TIntObjectHashMap<List<AccessPoint>> accessPointMap,
			TIntObjectHashMap<List<StreetIntersection>> primaryNameIdToIntersectionsMap,
			TIntObjectHashMap<List<StreetIntersection>> nameIdToIntersectionsMap,
			TIntObjectHashMap<StreetIntersection> intersectionIdMap,
			TIntObjectHashMap<String> electoralAreaIdMap) {
		// and also a map from segment Ids to a Set of StreetIntersections
		TIntObjectHashMap<StreetIntersection[]> segmentIdToIntersectionMap = new TIntObjectHashMap<StreetIntersection[]>();
		// first build a map from segmentId to a list of BlockFaces
		streetSegmentIdMap = new TIntObjectHashMap<StreetSegment>();
		RowReader rr = dataSource.getStreetSegmentsPost();
		int count = 0;
		int faceCount = 0;
		logger.debug("Starting loading street segments");
		while(rr.next()) {
			if(count % 10000 == 0) {
				logger.debug("street segment count: " + count + " Memory in use (Megs): {}",
						((Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
								.freeMemory()) / 1000000));
			}
			count++;
			int segmentId = rr.getInt("street_segment_id");
			int firstAddrLeft = rr.getInt("first_address_left");
			int firstAddrRight = rr.getInt("first_address_right");
			int lastAddrLeft = rr.getInt("last_address_left");
			int lastAddrRight = rr.getInt("last_address_right");
			String parityLeft = rr.getString("address_parity_left");
			String parityRight = rr.getString("address_parity_right");
			int localityLeft = rr.getInt("left_locality_id");
			int localityRight = rr.getInt("right_locality_id");
			int eaLeft = rr.getInt("left_electoral_area_id");
			int eaRight = rr.getInt("right_electoral_area_id");
			float numLanesLeft = rr.getInt("num_lanes_left");
			float numLanesRight = rr.getInt("num_lanes_right");
			RoadClass roadClass = RoadClass.convert(rr.getString("road_class"));
			LaneRestriction laneRestriction = LaneRestriction.convert(
					rr.getString("lane_restriction"));
			TravelDirection travelDir = TravelDirection.convert(
					rr.getString("travel_direction"));
			DividerType dividerType = DividerType.convert(rr.getString("divider_type"));
			int startIntersectionId = rr.getInt("start_intersection_id");
			int endIntersectionId = rr.getInt("end_intersection_id");
			LineString centerLine;
			centerLine = rr.getLineString();
			StreetSegment segment = new StreetSegment(segmentId, centerLine, roadClass,
					laneRestriction, travelDir, dividerType);
			if(segment.isOneWay()) {
				float totalLanes = numLanesLeft + numLanesRight;
				numLanesLeft = numLanesRight = (float)(totalLanes / 2.0);
			}
			
			// create the BlockFaces, ignoring those with "None" and "Single" address parities
			// and also ignore any with NULL_INT_VALUE for either address endpoint
			BlockFace leftFace = null;
			BlockFace rightFace = null;
			if(parityLeft != null && !parityLeft.equals("N")
					&& !parityLeft.equals("S")
					&& firstAddrLeft != RowReader.NULL_INT_VALUE
					&& lastAddrLeft != RowReader.NULL_INT_VALUE) {
				faceCount++;
				CivicAccessPoint[] aps = getCivicAccessPointsForFace(segmentId,
						firstAddrLeft, lastAddrLeft, parityLeft.charAt(0),
						accessPointMap);
				leftFace = new BlockFace(segment, Side.LEFT,
						firstAddrLeft, lastAddrLeft, parityLeft,
						numLanesLeft, localityIdMap.get(localityLeft), electoralAreaIdMap.get(eaLeft), aps);
				if(aps != null) {
					for(CivicAccessPoint ap : aps) {
						ap.setBlockFace(leftFace);
					}
				}
			}
			if(parityRight != null && !parityRight.equals("N")
					&& !parityRight.equals("S")
					&& firstAddrRight != RowReader.NULL_INT_VALUE
					&& lastAddrRight != RowReader.NULL_INT_VALUE) {
				faceCount++;
				CivicAccessPoint[] aps = getCivicAccessPointsForFace(segmentId,
						firstAddrRight, lastAddrRight, parityRight.charAt(0),
						accessPointMap);
				rightFace = new BlockFace(segment, Side.RIGHT,
						firstAddrRight, lastAddrRight, parityRight,
						numLanesRight, localityIdMap.get(localityRight), electoralAreaIdMap.get(eaRight), aps);
				if(aps != null) {
					for(CivicAccessPoint ap : aps) {
						ap.setBlockFace(rightFace);
					}
				}
			}
			streetSegmentIdMap.put(segmentId, segment);
			
			// set the street segment for NCAPs with a segment ID
			List<AccessPoint> aps = accessPointMap.get(segmentId);
			if(aps != null) {
				for(AccessPoint ap : aps) {
					if(ap instanceof NonCivicAccessPoint) {
						((NonCivicAccessPoint)ap).setStreetSegment(segment);
					} else if(ap instanceof CivicAccessPoint) {
						CivicAccessPoint cap = (CivicAccessPoint) ap;
						if((rightFace == null || !rightFace.contains(cap.getCivicNumber())) 
								&& (leftFace == null || !leftFace.contains(cap.getCivicNumber()))) {
							logger.warn("CAP out of segment ranges: siteId:" + cap.getSite().getId() + " address: " + cap.getCivicNumber() 
									+ " not with segmentId: " + segmentId + " (" + (rightFace == null ? "null" : rightFace.getMin() + "-" + rightFace.getMax()) 
									+ "/" + (leftFace == null ? "null" : leftFace.getMin() + "-" + leftFace.getMax()) + ")");
						}
					}
				}
			}
			
			// add intersections to the array for the id
			// (don't duplicate them if they are the same)
			StreetIntersection[] intersections;
			if(startIntersectionId == endIntersectionId) {
				intersections = new StreetIntersection[] {intersectionIdMap
						.get(startIntersectionId)};
			} else {
				intersections = new StreetIntersection[] {
						intersectionIdMap.get(startIntersectionId),
						intersectionIdMap.get(endIntersectionId)
				};
			}
			segmentIdToIntersectionMap.put(segmentId, intersections);
			// add localities to the intersections
			
			for(StreetIntersection intersection : intersections) {
				intersection.addLocality(localityIdMap.get(localityRight));
				intersection.addLocality(localityIdMap.get(localityLeft));
			}
			
		}
		rr.close();
		
		logger.debug("Street Segment count: {}", count);
		status.counts.put("street_segments", count);
		logger.debug("Block Face count: {}", faceCount);
		status.counts.put("block_faces", faceCount);
		
		TIntObjectHashMap<List<StreetSegment>> nameIdToSegmentsMap = new TIntObjectHashMap<List<StreetSegment>>();
		rr = dataSource.getStreetNameOnSegments();
		count = 0;
		while(rr.next()) {
			count++;
			int name_id = rr.getInt("street_name_id");
			int segment_id = rr.getInt("street_segment_id");
			boolean is_primary = Boolean.TRUE.equals(rr.getBoolean("is_primary_ind"));
			StreetSegment segment = streetSegmentIdMap.get(segment_id);
			if(segment != null) {
				// get the list of segs for this street name so far, if there is one
				List<StreetSegment> segs = nameIdToSegmentsMap.get(name_id);
				if(segs == null) {
					segs = new ArrayList<StreetSegment>();
					nameIdToSegmentsMap.put(name_id, segs);
				}
				// add this segment to the list for this name
				segs.add(segment);

				// set the primary name id for the segment
				if(is_primary) {
					segment.setPrimaryStreetNameId(name_id);
				} else {
					segment.addAliasNameId(name_id);
				}
			}
			// add all of the intersections from the segment to the nameIdToIntersectionMap
			StreetIntersection[] intersections = segmentIdToIntersectionMap.get(segment_id);
			if(intersections == null) {
				logger.warn("CONSTRAINT VIOLATION: Street Name on Segment refers to non-existent SegmentId: " + segment_id);
			} else {
				for(StreetIntersection intersection : intersections) {
					if(is_primary) {
						List<StreetIntersection> nameIntersections = primaryNameIdToIntersectionsMap
								.get(name_id);
						if(nameIntersections == null) {
							nameIntersections = new ArrayList<StreetIntersection>();
							primaryNameIdToIntersectionsMap.put(name_id, nameIntersections);
						}
						nameIntersections.add(intersection);
					} else {
						List<StreetIntersection> nameIntersections = nameIdToIntersectionsMap
								.get(name_id);
						if(nameIntersections == null) {
							nameIntersections = new ArrayList<StreetIntersection>();
							nameIdToIntersectionsMap.put(name_id, nameIntersections);
						}
						nameIntersections.add(intersection);
					}
				}
			}
		}
		rr.close();
		
		logger.debug("Street Name on Segment count: {}", count);
		status.counts.put("street_name_on_segments", count);
		return nameIdToSegmentsMap;
	}
	
	private CivicAccessPoint[] getCivicAccessPointsForFace(int segmentId, int firstAddr,
			int lastAddr,
			char parity, TIntObjectHashMap<List<AccessPoint>> accessPointMap) {
		List<AccessPoint> segAps = accessPointMap.get(segmentId);
		if(segAps == null) {
			return null;
		}
		ArrayList<CivicAccessPoint> aps = new ArrayList<CivicAccessPoint>(segAps.size());
		for(AccessPoint ap : segAps) {
			if(!(ap instanceof CivicAccessPoint)) {
				continue;
			}
			CivicAccessPoint cap = (CivicAccessPoint)ap;
			if(cap.getCivicNumber() <= Math.max(firstAddr, lastAddr)
					&& cap.getCivicNumber() >= Math.min(firstAddr, lastAddr)
					&& (('E' == parity && cap.getCivicNumber() % 2 == 0)
							|| ('O' == parity && cap.getCivicNumber() % 2 != 0)
							|| ('E' != parity && 'O' != parity))) {
				aps.add(cap);
			} 
		}
		Collections.sort(aps, CivicAccessPoint.CIVIC_NUMBER_COMPARATOR);
		return aps.toArray(new CivicAccessPoint[aps.size()]);
	}
	
	private NameLookupTrie<StreetNameBody> buildStreetNameTrie(
			WordMapBuilder wordMapBuilder,
			TIntObjectHashMap<List<AccessPoint>> accessPointMap,
			TIntObjectHashMap<StreetIntersection> intersectionIdMap,
			TIntObjectHashMap<String> electoralAreaIdMap) {
		TIntObjectHashMap<List<StreetIntersection>> primaryNameIdToIntersectionsMap = new TIntObjectHashMap<List<StreetIntersection>>();
		TIntObjectHashMap<List<StreetIntersection>> nameIdToIntersectionsMap = new TIntObjectHashMap<List<StreetIntersection>>();
		TIntObjectHashMap<List<StreetSegment>> nameIdToSegmentsMap = buildStreetSegmentNameMap(accessPointMap,
				primaryNameIdToIntersectionsMap, nameIdToIntersectionsMap, intersectionIdMap, electoralAreaIdMap);
		TIntObjectHashMap<StreetName> streetNameIdMap = new TIntObjectHashMap<StreetName>();
		Map<StreetName, Integer> streetNameToIdMap = new THashMap<StreetName, Integer>();
		// Build a map from name body Strings to a list of the StreetNames with that body
		// The list is stored in the StreetNameBody object
		Map<String, StreetNameBody> nameBodyMap = new THashMap<String, StreetNameBody>();
		RowReader rr = dataSource.getStreetNames();
		int count = 0;
		// TODO read the street names twice - first time to build a dictionary of words to split into, second to build the trie but with words split where possible
		while(rr.next()) {
			count++;
			int streetNameId = rr.getInt("street_name_id");
			String unMappedBody = rr.getString("name_body");
			String type = rr.getString("street_type");
			String dir = rr.getString("street_direction");
			String qual = rr.getString("street_qualifier");
			boolean typeIsPrefix = Boolean.TRUE.equals(rr.getBoolean("street_type_is_prefix_ind"));
			boolean dirIsPrefix = Boolean.TRUE.equals(rr.getBoolean("street_direction_is_prefix_ind"));
			//todo should mapwords do the splitting?
			String mappedBody = mapWords(unMappedBody).toUpperCase();
			wordMapBuilder.addPhrase(mappedBody, WordClass.STREET_NAME_BODY);
			BlockFaceIntervalTree blockTree = null;
			List<StreetSegment> segs = nameIdToSegmentsMap.get(streetNameId);
			if(segs != null) {
				List<BlockFace> faces = new ArrayList<BlockFace>(segs.size()*2);
				for(StreetSegment seg : segs) {
					if(seg.getBlockFace(Side.LEFT) != null) {
						faces.add(seg.getBlockFace(Side.LEFT));
					}
					if(seg.getBlockFace(Side.RIGHT) != null) {
						faces.add(seg.getBlockFace(Side.RIGHT));
					}
				}
				if(!faces.isEmpty()) {
					blockTree = new BlockFaceIntervalTree(faces);
				}
			}
			StreetName streetName = new StreetName(unMappedBody, type, dir, qual, typeIsPrefix,
					dirIsPrefix, blockTree);
			// replace the temporary numeric id with the actual StreetName object reference
			if(segs != null) {
				for(StreetSegment seg : segs) {
					seg.resolveName(streetNameId, streetName);
				}
			}
			// add this name to the list for its body
			StreetNameBody nameBody = nameBodyMap.get(mappedBody);
			if(nameBody == null) {
				nameBody = new StreetNameBody();
				nameBodyMap.put(mappedBody, nameBody);
			}
			Set<StreetName> names = nameBody.getStreetNames();
			names.add(streetName);
			streetNameIdMap.put(streetNameId, streetName);
			streetNameToIdMap.put(streetName, streetNameId);
		}
		rr.close();
		
		logger.debug("Street Name count: {}", count);
		status.counts.put("street_names", count);
		// loop over the PrimaryNameId -> IntersectionList map
		for(TIntObjectIterator<List<StreetIntersection>> it = primaryNameIdToIntersectionsMap.iterator(); it.hasNext(); ){
			it.advance();
			for(StreetIntersection intersection : it.value()) {
				// add the streetName object to the Intersection's StreetName set
				intersection.getPrimaryStreetNames().add(streetNameIdMap.get(it.key()));
			}
		}
		// loop over the nameId -> IntersectionList map
		for(TIntObjectIterator<List<StreetIntersection>> it = nameIdToIntersectionsMap.iterator(); it.hasNext(); ) {
			it.advance();
			for(StreetIntersection intersection : it.value()) {
				// add the streetName object to the Intersection's StreetName set
				intersection.getAliasStreetNames().add(streetNameIdMap.get(it.key()));
			}
		}
		
		// remove generic street names from intersections, eg. "turning lane" "exit ramp"
		for(StreetIntersection intersection : intersectionIdMap.valueCollection()) {
			Set<StreetName> names = intersection.getPrimaryStreetNames();
			List<StreetName> orderedNames = new ArrayList<StreetName>(names);
			orderedNames.sort(new Comparator<StreetName>() {
				@Override
				public int compare(StreetName sn1, StreetName sn2) {
                	return sn1.toString().compareTo(sn2.toString());
				}
			});
			//int removed = 0;
			//int removeable = 0;
			for(int nameIdx = orderedNames.size()-1; nameIdx >= 0; nameIdx--) {
				StreetName streetName = orderedNames.get(nameIdx);
				// in the ITN all generic names start with a lowercase letter, while real names do not
				// but we want to leave at least 2 names so that the intersection makes sense
				if(Character.isLowerCase(streetName.getBody().charAt(0))) {
					//removeable++;
					if(names.size() > 2) {
						names.remove(streetName);
						//removed++;
					}
				}
			}
//			if(removed > 0 && removed < removeable) {
//				logger.info("Intersection with many generic names: {}", orderedNames);
//			}
		}
		
		// loop over street_locality_centroid table and add centroids to the streetname
		rr = dataSource.getStreetLocalityCentroids();
		count = 0;
		while(rr.next()) {
			count++;
			int locality_id = rr.getInt("locality_id");
			int street_name_id = rr.getInt("street_name_id");
			double[] coords = new double[2];
			coords[0] = rr.getPoint().getX();
			coords[1] = rr.getPoint().getY();
			StreetName sn = streetNameIdMap.get(street_name_id);
			if(sn == null) {
				logger.info("Centroid provided for non-existent name id: {}", street_name_id);
				continue;
			}
			sn.addLocalityCentroid(localityIdMap.get(locality_id), coords);
		}
		rr.close();
		
		logger.debug("Street Locality Centroid count: {}", count);
		status.counts.put("street_locality_centroids", count);
		
		// loop over all streetNames and compact them
		for(StreetName sn : streetNameIdMap.valueCollection()) {
			sn.trimToSize();
		}
		// done with this so release the memory
		streetNameIdMap = null;
		
		// loop over the name body string -> NameBody object map
		// int nameBodyCount = 0;
		// int smallCount = 0;
		for(Entry<String, StreetNameBody> entry : nameBodyMap.entrySet()) {
			// nameBodyCount++;
			StreetNameBody nameBody = entry.getValue();
			// build a Map from NameBody string (of streets that intersect this one) to Intersection
			Map<String, Set<StreetIntersection>> intersectionMap = new THashMap<String, Set<StreetIntersection>>();
			// Loop over the StreetNames for this nameBody
			// int nameWithBodyCount = 0;
			for(StreetName nameWithBody : nameBody.getStreetNames()) {
				// nameWithBodyCount++;
				int nameId = streetNameToIdMap.get(nameWithBody);
				List<StreetIntersection> intersections = new ArrayList<StreetIntersection>();
				List<StreetIntersection> aliasIntersections = nameIdToIntersectionsMap.get(nameId);
				if(aliasIntersections != null) {
					intersections.addAll(aliasIntersections);
				}
				List<StreetIntersection> primaryIntersections = primaryNameIdToIntersectionsMap
						.get(nameId);
				if(primaryIntersections != null) {
					intersections.addAll(primaryIntersections);
				}
				if(intersections.size() == 0) {
					continue;
				}
				// loop over the intersections for this streetName
				// int intersectionCount = 0;
				for(StreetIntersection intersection : intersections) {
					// intersectionCount++;
					// loop over names for this intersection
					// int nameAtIntersectionCount = 0;
					for(StreetName nameAtIntersection : intersection.getStreetNames()) {
						// nameAtIntersectionCount++;
						String mappedName = mapWords(nameAtIntersection.getBody()).toUpperCase();
						Set<StreetIntersection> intersectionsForName = intersectionMap
								.get(mappedName);
						if(intersectionsForName == null) {
							intersectionsForName = new ArraySet<StreetIntersection>();
							intersectionMap.put(mappedName, intersectionsForName);
						}
						intersectionsForName.add(intersection);
						// System.out.println(entry.getKey() + ":" + nameBodyCount + "  " +
						// nameWithBody + ":" + nameWithBodyCount + "  intersectionCount=" +
						// intersectionCount + "  " + nameAtIntersection + ":" +
						// nameAtIntersectionCount);
					}
				}
			}
			//if(intersectionMap.size() <= 10) {
				// smallCount++;
			//}
			// minimize the arrays used for the intersection sets
			for(Set<StreetIntersection> set : intersectionMap.values()) {
				((ArraySet<StreetIntersection>)set).trimToSize();
			}
			
			// System.out.println(entry.getKey() + ":" + intersectionMap.size() + " smallCount: " +
			// smallCount + "/" + nameBodyCount);
			nameBody.setIntersections(intersectionMap);
			
			// minimize the array used for the streetName set
			nameBody.trimToSize();
		}
		// done with these so free the memory
		streetNameToIdMap = null;
		primaryNameIdToIntersectionsMap = null;
		nameIdToIntersectionsMap = null;
		
		// return nameBodyMap;
		// build a trie using the map
		return new NameLookupTrie<StreetNameBody>(nameBodyMap, true);
	}
	
	private Map<String, BusinessCategory> buildBusinessCategories() {
		Map<String, BusinessCategory> cats = new THashMap<String, BusinessCategory>();
		RowReader rr = dataSource.getBusinessCategories();
		int count = 0;
		while(rr.next()) {
			count++;
			String className = rr.getString("business_category_class");
			String description = rr.getString("business_category_description");
			String naicsCode = rr.getString("naics_code");
			cats.put(className, new BusinessCategory(className, description, naicsCode));
		}
		rr.close();
		logger.info("Business Category Count: {}", count);
		status.counts.put("business_categories", count);
		return cats;
	}
	
	private Map<UUID, IOccupant> buildOccupantMap(WordMapBuilder wordMapBuilder, 
			Map<String, BusinessCategory> businessCategories, TIntObjectHashMap<ISite> siteIdMap) {
		Map<UUID,IOccupant> occupantsByUuid = new THashMap<UUID,IOccupant>();
		RowReader rr = dataSource.getOccupants();
		int count = 0;
		while(rr.next()) {
			int id = rr.getInt("occupant_id");
			UUID uuid = UUID.fromString(rr.getString("occupant_uuid"));
			int parentId = rr.getInt("occupant_site_id");
			ISite parentSite = siteIdMap.get(parentId);
			if(parentSite == null) {
				logger.warn("CONSTRAINT VIOLATION: Occupant refers to non-existent Site: " + parentId);
				continue;				
			}
			count++;
			String name = rr.getString("occupant_name"); 
			String description = rr.getString("occupant_description");
			String aliasAddress = rr.getString("alias_address");
			String contactPhone = rr.getString("contact_phone");
			String contactEmail = rr.getString("contact_email");
			String contactFax = rr.getString("contact_fax");
			String websiteUrl = rr.getString("website_url");
			String imageUrl = rr.getString("image_url");
			String keywords = rr.getString("keywords");
			String busCatClass = rr.getString("business_category_class");
			BusinessCategory busCat = businessCategories.get(busCatClass);
			LocalDate dateUpdated = normalizeDate(rr.getDate("date_updated"));
			LocalDate dateAdded = normalizeDate(rr.getDate("date_added"));
			//String custodianId = rr.getString("custodian_id");
			//String sourceDataId = rr.getString("source_data_id");
			String customStyleName = rr.getString("custom_style_name");
			IOccupant occ = OccupantFactory.createOccupant(id, uuid, siteIdMap.get(parentId), name, description, aliasAddress,
					contactPhone, contactEmail, contactFax, websiteUrl, imageUrl, 
					busCat, dateUpdated, dateAdded, customStyleName);
			occupantsByUuid.put(uuid, occ);
			List<String> keywordList = occupantKeywordIndex.add(occ, keywords);
			occ.setKeywords(keywordList);
		}
		rr.close();
		logger.info("Occupants Loaded: {}", count);
		status.counts.put("occupants", count);
		
		// build the occupantNameIndex
		InvertedIndexBuilder<IOccupant> builder = new InvertedIndexBuilder<IOccupant>();
		for(IOccupant occ : occupantsByUuid.values()) {
			String unMappedName = occ.getName() + " " + occ.getSite().getFullSiteName();
			if(unMappedName != null && !unMappedName.isEmpty()) {
				builder.addItem(mapWordsArray(unMappedName, true), occ);
			}
		}
		occupantNameIndex = builder.build();
		
		// build the partialTagIndex
		partialTagIndex = new PartialTagIndex(occupantKeywordIndex.getTags());
		
		return occupantsByUuid;
	}

	public String mapWord(String in) {
		String mapped = abbrMappings.get(in.toUpperCase());
		if(mapped != null) {
			return mapped;
		}
		return in;
	}
	
	public String mapWords(String in) {
		return mapWords(in, false);
	}
	
	public String mapWords(String in, boolean includeUDs) {
		String[] words = mapWordsArray(in, includeUDs);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < words.length; i++) {
			if(i > 0) {
				sb.append(" ");
			}
			sb.append(words[i]);
		}
		return sb.toString();
	}
	
	public String[] mapWordsArray(String in, boolean includeUDs) {
		in = DraLexicalRules.clean(in);
		String[] words = GeocoderUtil.wordSplit(in);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < words.length; i++) {
			if(i > 0) {
				sb.append(" ");
			}
			words[i] = GeocoderUtil.removeOrdinal(words[i]);
			String newWord = abbrMappings.get(words[i].toUpperCase());
			if(newWord != null) {
				words[i] = newWord;
			}
			if(includeUDs && unitDesignators.contains(words[i])) {
				words[i] = dataSource.getConfig().getDefaultUnitDesignator();
			}
		}
		return words;
	}
	
	private Map<String, String> buildAbbreviationMappings(
			WordMapBuilder wordMapBuilder) {
		// build a list of all canonical strings
		Set<String> canonicalWords = new THashSet<String>();
		
		// Add provinces
		wordMapBuilder.addWord("AB", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Alberta", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("BC", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("British Columbia", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("MB", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Manitoba", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("NB", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("New Brunswick", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("NL", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Newfoundland and Labrador", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("NS", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Nova Scotia", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("NT", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Northwest Territory", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("NU", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Nunavut", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("ON", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Ontario", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("PE", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Prince Edward Island", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("QC", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Quebec", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("SK", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Saskatchewan", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("YT", WordClass.STATE_PROV_TERR);
		wordMapBuilder.addWord("Yukon Territory", WordClass.STATE_PROV_TERR);
		
		// Add unitDesignators
		// keep a "list" (Set) of all unit designators in order to allow for aliasing
		unitDesignators = new HashSet<String>();
		wordMapBuilder.addWord(getConfig().getDefaultUnitDesignator(), WordClass.UNIT_DESIGNATOR);
		Stream<UnitDesignator> uds = dataSource.getUnitDesignators();
		uds.forEach(ud-> {
			String canonicalForm = ud.getCanonicalForm().toUpperCase();
			unitDesignators.add(canonicalForm);
			canonicalWords.add(canonicalForm);
			wordMapBuilder.addWord(canonicalForm, WordClass.UNIT_DESIGNATOR);
		});
		
		// Add Types
		RowReader rr = dataSource.getStreetTypes();
		while(rr.next()) {
			String streetType = rr.getString("street_type");
			if(streetType != null && !streetType.isEmpty()) {
				canonicalWords.add(streetType.toUpperCase());
				wordMapBuilder.addWord(streetType, WordClass.STREET_TYPE);
			}
		}
		rr.close();
		
		// Special case to handle the fact that Rue needs to map to St for french
		// but Rue is also a valid street type on its own
		wordMapBuilder.addWordMapping("Rue", "St");
		
		// Add directionals
		rr = dataSource.getStreetDirs();
		while(rr.next()) {
			String streetDir = rr.getString("street_direction");
			if(streetDir != null && !streetDir.isEmpty()) {
				canonicalWords.add(streetDir.toUpperCase());
				wordMapBuilder.addWord(streetDir, WordClass.STREET_DIRECTIONAL);
			}
		}
		rr.close();
		
		// Add qualifiers
		rr = dataSource.getStreetQualifiers();
		while(rr.next()) {
			String streetQualifier = rr.getString("street_qualifier");
			if(streetQualifier != null && !streetQualifier.isEmpty()) {
				canonicalWords.add(streetQualifier);
				wordMapBuilder.addWord(streetQualifier, WordClass.STREET_QUALIFIER);
			}
		}
		rr.close();
		
		// build a map from each string on either side of an abbreviation mapping
		// to a set containing all the strings that are transitively connected
		// we essentially ignore the "direction" of the mappings, and identify
		// a direction later based on the canonical forms or just use the shortest string
		Map<String, Set<String>> mappingSets = new THashMap<String, Set<String>>();
		
		Stream<AbbreviationMapping> abbrMaps = dataSource.getAbbreviationMappings();
		int count = 0;
		for(Iterator<AbbreviationMapping> it = abbrMaps.iterator(); it.hasNext(); ) {
			count++;
			AbbreviationMapping abbrMap = it.next();
			String form1 = abbrMap.getAbbreviatedForm().toUpperCase();
			String form2 = abbrMap.getLongForm().toUpperCase();
			Set<String> set1 = mappingSets.get(form1);
			Set<String> set2 = mappingSets.get(form2);
			if(set1 != null) {
				if(set2 != null) {
					// need to merge the sets
					set1.addAll(set2);
					// repoint all members of set2 to set1
					for(String str : set2) {
						mappingSets.put(str, set1);
					}
				} else {
					// set1 is not null, so add form2 to it
					set1.add(form2);
					mappingSets.put(form2, set1);
				}
			} else if(set2 != null) {
				// set2 is not null, so add form1 to it
				set2.add(form1);
				mappingSets.put(form1, set2);
			} else {
				// neither form is in any set yet, make a new set
				Set<String> set = new THashSet<String>();
				set.add(form1);
				set.add(form2);
				mappingSets.put(form1, set);
				mappingSets.put(form2, set);
			}
		}
		rr.close();
		
		logger.debug("Abbreviation Mapping count: {}", count);
		status.counts.put("abbreviation_mappings", count);
		
		Map<String, String> abbrMappings = new THashMap<String, String>(count);
		
		// loop over the sets and identify the target form
		Set<Set<String>> sets = new HashSet<Set<String>>(mappingSets.values());
		for(Set<String> set : sets) {
			boolean ignore = false;
			String canonicalForm = null;
			String shortest = null;
			for(String str : set) {
				if(canonicalWords.contains(str)) {
					if(canonicalForm == null) {
						canonicalForm = str;
					} else {
						// we will ignore this entire set of mappings for now
						logger.warn("Ignoring some abbreviation mappings; "
								+ "at least two canonical forms (" + canonicalForm
								+ ", " + str + ") map to eachother: {}", set);
						ignore = true;
						break;
					}
				} else if(shortest == null || str.length() < shortest.length()) {
					shortest = str;
				}
			}
			// if we are ignoring this group, continue to the next
			if(ignore) {
				continue;
			}
			if(canonicalForm == null) {
				// just use the shortest string as the target of these mappings
				canonicalForm = shortest;
			}
			// loop over all the words and add the mappings
			for(String str : set) {
				if(!str.equals(canonicalForm)) {
					abbrMappings.put(str, canonicalForm);
					wordMapBuilder.addWordMapping(str, canonicalForm);
				}
			}
			
		}
		
		// return the abbrMappings to be used when loading the street, locality, and site names
		return abbrMappings;
	}
	
	public GeocoderConfig getConfig() {
		return dataSource.getConfig();
	}
	
	public static GeometryFactory getGeometryFactory() {
		return geometryFactory;
	}
	
	public BlockFaceInterpolator getInterpolator() {
		return interpolator;
	}
	
	public void close() {
		dataSource.close();
		abbrMappings = null;
		dataSource = null;
		interpolator = null;
		intersectionKDTree = null;
		localityIdMap = null;
		localityNameTrie = null;
		apKDTree = null;
		siteKDTree = null;
		siteNameIndex = null;
		sitesByUuid = null;
		streetNameTrie = null;
		unitDesignators = null;
		wordMap = null;
	}
	
	public void loadSiteDetailsById(SiteAddress siteAddr, ISite site, AccessPoint accessPoint) {
		if(site != null) {
			siteAddr.setSiteID(site.getUuid().toString());
			siteAddr.setSiteStatus(site.getStatus());
			siteAddr.setSiteRetireDate(site.getRetireDate());
			siteAddr.setSiteChangeDate(site.getChangeDate());
			siteAddr.setSID(site.getId());
		}
		if(accessPoint != null) {
			siteAddr.setNarrativeLocation(accessPoint.getNarrativeLocation());			
		}
		return;
	}
	
	public List<GeocodeMatch> lookupExact(GeocodeQuery query) {
		return exactMatchLookup.query(query);
	}
	
	public SiteAddress getSiteByUuid(UUID uuid, LocationDescriptor ld,
			int setBack) {
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
		}
		ISite site = sitesByUuid.get(uuid);
		if(site == null) {
			return null;
		}
		if(site.getPrimaryAccessPoint() == null) {
			return null;
		}
		SiteAddress address = new SiteAddress(site, null);
		loadSiteDetailsById(address, site,	site.getPrimaryAccessPoint());
		address.resolveLocation(this, site, ld, setBack);
		return address;
	}
	
	public ISite getRawSiteByUuid(UUID uuid) {
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
		}
		return sitesByUuid.get(uuid);
	}
	
	public ArrayList<SiteAddress> getSubSitesByUuid(UUID uuid, LocationDescriptor ld,
			int setBack) {
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
		}
		ISite parentSite = sitesByUuid.get(uuid);
		if(parentSite.getPrimaryAccessPoint() == null) {
			return new ArrayList<SiteAddress>(0);
		}
		ArrayList<SiteAddress> sites = new ArrayList<SiteAddress>(parentSite.getChildren().size());
		for(ISite site : parentSite.getChildren()) {
			SiteAddress address = new SiteAddress(site, null);
			loadSiteDetailsById(address, site, site.getPrimaryAccessPoint());
			address.resolveLocation(this, site, ld, setBack);
			sites.add(address);
		}
		return sites;
	}
	
	public List<SiteAddress> getSitesWithin(int maxFeatures, Polygon bbox, LocationDescriptor ld,
			int setBack, boolean excludeUnits, boolean onlyCivic) {
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
		}
		if(ld.equals(LocationDescriptor.ACCESS_POINT)) {
			MultiFilter<AccessPoint> filter = new MultiFilter<AccessPoint>();
			filter.add(new PointLocationBboxFilter<AccessPoint>(bbox));
			if(excludeUnits) {
				filter.add(ExcludeUnitsAccessPointFilter.get());
			}
			if(onlyCivic) {
				filter.add(OnlyCivicAccessPointFilter.get());
			}
			List<AccessPoint> results = apKDTree.search(
					centerOf(bbox), maxFeatures, radiusOf(bbox), filter);
			return apListToSiteAddressList(results, ld, setBack);			
		} else {
			MultiFilter<ISite> filter = new MultiFilter<ISite>(2);
			filter.add(new PointLocationBboxFilter<ISite>(bbox));
			if(excludeUnits) {
				filter.add(ExcludeUnitsSiteFilter.get());
			}
			if(onlyCivic) {
				filter.add(OnlyCivicSiteFilter.get());
			}
			List<ISite> results = siteKDTree.search(
					centerOf(bbox), maxFeatures, radiusOf(bbox), filter);
			return siteListToSiteAddressList(results, ld, setBack);
		}
	}
	
	public List<SiteAddress> getNearestNSites(int maxFeatures, Point p, Integer maxDistance,
			LocationDescriptor ld, int setBack, boolean excludeUnits, boolean onlyCivic) {
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
		}
		if(ld.equals(LocationDescriptor.ACCESS_POINT)) {
			MultiFilter<AccessPoint> filter = new MultiFilter<AccessPoint>();
			if(excludeUnits) {
				filter.add(ExcludeUnitsAccessPointFilter.get());
			}
			if(onlyCivic) {
				filter.add(OnlyCivicAccessPointFilter.get());
			}
			List<AccessPoint> results = apKDTree.search(p, maxFeatures, maxDistance, filter);
			return apListToSiteAddressList(results, ld, setBack);			
		} else {
			MultiFilter<ISite> filter = new MultiFilter<ISite>();
			if(excludeUnits) {
				filter.add(ExcludeUnitsSiteFilter.get());
			}
			if(onlyCivic) {
				filter.add(OnlyCivicSiteFilter.get());
			}
			List<ISite> results = siteKDTree.search(p, maxFeatures, maxDistance, filter);
			return siteListToSiteAddressList(results, ld, setBack);
		}
	}

	private List<SiteAddress> apListToSiteAddressList(List<AccessPoint> aps, LocationDescriptor ld, int setBack) {
		List<SiteAddress> resultList = new ArrayList<SiteAddress>(aps.size());
		for(AccessPoint ap : aps) {
			SiteAddress address = new SiteAddress(ap.getSite(), ap);
			loadSiteDetailsById(address, ap.getSite(), ap);
			BlockFace face = null;
			if(ap instanceof CivicAccessPoint) {
				face = ((CivicAccessPoint)ap).getBlockFace();
			}
			address.resolveLocation(this, ap.getSite(), ap.getPoint(), ld, ap, face, false, setBack);
			resultList.add(address);
		}
		return resultList;
	}

	private List<SiteAddress> siteListToSiteAddressList(List<ISite> sites, LocationDescriptor ld, int setBack) {
		List<SiteAddress> resultList = new ArrayList<SiteAddress>(sites.size());
		for(ISite site : sites) {
			SiteAddress address = new SiteAddress(site, null);
			loadSiteDetailsById(address, site, site.getPrimaryAccessPoint());
			address.resolveLocation(this, site, ld, setBack);
			resultList.add(address);
		}
		return resultList;
	}
	
	public StreetIntersectionAddress getIntersectionByUuid(UUID uuid) {
		StreetIntersection intr = intersectionsByUuid.get(uuid);
		return new StreetIntersectionAddress(intr);
	}
	
	public List<StreetIntersectionAddress> getIntersectionsWithin(int maxFeatures, Polygon bbox,
			int minDegree, int maxDegree) {
		Filter<StreetIntersection> multiFilter = new MultiFilter<StreetIntersection>(
				new StreetIntersectionDegreeFilter(minDegree, maxDegree),
				new PointLocationBboxFilter<StreetIntersection>(bbox));
		List<StreetIntersection> results = intersectionKDTree.search(
				centerOf(bbox), maxFeatures, radiusOf(bbox), multiFilter);
		List<StreetIntersectionAddress> resultList = new ArrayList<StreetIntersectionAddress>(
				maxFeatures);
		for(StreetIntersection result : results) {
			resultList.add(new StreetIntersectionAddress(result));
		}
		return resultList;
	}
	
	public List<StreetIntersectionAddress> getNearestNIntersections(int maxFeatures, Point p,
			Integer maxDistance, int minDegree, int maxDegree) {
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
		}
		List<StreetIntersection> results = intersectionKDTree.search(p, maxFeatures,
				maxDistance, new StreetIntersectionDegreeFilter(minDegree, maxDegree));
		List<StreetIntersectionAddress> resultList = new ArrayList<StreetIntersectionAddress>(
				maxFeatures);
		for(StreetIntersection result : results) {
			resultList.add(new StreetIntersectionAddress(result));
		}
		return resultList;
	}

	public OccupantAddress getOccupantByUuid(UUID uuid,
			LocationDescriptor ld, int setBack) {
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
		}
		IOccupant occ = occupantsByUuid.get(uuid);
		if(occ == null) {
			return null;
		}
		if(occ.getSite().getPrimaryAccessPoint() == null) {
			return null;
		}
		OccupantAddress address = new OccupantAddress(occ, null);
		loadSiteDetailsById(address, occ.getSite(),	occ.getSite().getPrimaryAccessPoint());
		address.resolveLocation(this, occ.getSite(), ld, setBack);
		return address;
	}

	public List<OccupantAddress> getOccupantsWithin(int maxFeatures, String tags, 
			Polygon bbox, LocationDescriptor ld, int setBack) {
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
		}
		Collection<IOccupant> occs;
		if(tags == null || tags.isEmpty()) {
			occs = occupantKDTree.search(centerOf(bbox), maxFeatures, radiusOf(bbox), 
					new PointLocationBboxFilter<IOccupant>(bbox));
		} else {
			occs = occupantKeywordIndex.query(tags);
			Filters.filter(new PointLocationBboxFilter<IOccupant>(bbox), occs);
		}
		maxFeatures = Math.min(maxFeatures, occs.size());
		List<IOccupant> occList = new ArrayList<IOccupant>(occs);
		Collections.sort(occList, new ILocation.DistanceComparator(bbox.getCentroid()));
		
		List<OccupantAddress> resultList = new ArrayList<OccupantAddress>(maxFeatures);
		for(IOccupant occ : occList.subList(0, maxFeatures)) {
			OccupantAddress address = new OccupantAddress(occ, null);
			loadSiteDetailsById(address, occ.getSite(), occ.getSite().getPrimaryAccessPoint());
			address.resolveLocation(this, occ.getSite(), ld, setBack);
			resultList.add(address);
		}
		return resultList;
	}

	public List<OccupantAddress> getNearestNOccupants(int maxFeatures, String tags,  
			Point point, Integer maxDistance, LocationDescriptor ld,
			int setBack) {
		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
		}
		Collection<IOccupant> occs;
		if(tags == null || tags.isEmpty()) {
			occs = occupantKDTree.search(point, maxFeatures, maxDistance);
		} else {
			occs = occupantKeywordIndex.query(tags);
			if(maxDistance != null) {
				Filters.filter(new PointLocationBboxFilter<IOccupant>(point, maxDistance), occs);
			}
		}
		maxFeatures = Math.min(maxFeatures, occs.size());
		List<IOccupant> occList = new ArrayList<IOccupant>(occs);
		Collections.sort(occList, new ILocation.DistanceComparator(point));
		
		List<OccupantAddress> resultList = new ArrayList<OccupantAddress>(maxFeatures);
		for(IOccupant occ : occList.subList(0, maxFeatures)) {
			OccupantAddress address = new OccupantAddress(occ, null);
			loadSiteDetailsById(address, occ.getSite(),	occ.getSite().getPrimaryAccessPoint());
			address.resolveLocation(this, occ.getSite(), ld, setBack);
			resultList.add(address);
		}
		return resultList;
	}

	public StreetSegment getStreetSegmentById(int id) {
		return streetSegmentIdMap.get(id);
	}

//  Opted out of this functionality for now
//	public List<StreetSegment> getNearestNStreetSegments(int maxFeatures, Point p,
//			Integer maxDistance) {
//		if(!featureSet.contains(GeocoderFeature.REVERSE_GEOCODE)) {
//			throw new FeatureNotSupportedException(GeocoderFeature.REVERSE_GEOCODE);
//		}
//		List<StreetSegment> results = streetSegmentRTree.search(p, maxFeatures, maxDistance);
//		return results;
//	}

	private Point centerOf(Polygon bbox) {
		Coordinate[] coords = bbox.getCoordinates();
		Double x = (coords[0].x + coords[2].x) / 2;
		Double y = (coords[0].y + coords[2].y) / 2;
		return geometryFactory.createPoint(new Coordinate(x, y));
	}
	
	private Integer radiusOf(Polygon bbox) {
		Coordinate[] coords = bbox.getCoordinates();
		return (int)Math.round(Math.sqrt(
				((coords[0].x - coords[2].x) * (coords[0].x - coords[2].x))
						+ ((coords[0].y - coords[2].y) * (coords[0].y - coords[2].y))) / 2);
	}
	
	public WordMap getWordMap() {
		return wordMap;
	}

	public List<String> getTags(String partialTag, int maxResults) {
		return partialTagIndex.lookup(partialTag, maxResults);
	}

	public ZonedDateTime getDate(DateType source) {
		if(dates != null) {
			return dates.get(source);
		}
		return null;
	}

	public SystemStatus getStatus() {
		return status;
	}
}
