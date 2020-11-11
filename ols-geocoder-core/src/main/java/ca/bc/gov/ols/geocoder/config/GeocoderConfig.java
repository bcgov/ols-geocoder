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
package ca.bc.gov.ols.geocoder.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.config.ConfigurationParameter;
import ca.bc.gov.ols.enums.DividerType;
import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.geocoder.api.data.MatchFault;
import ca.bc.gov.ols.geocoder.api.data.MatchFault.MatchElement;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.util.GeomParseUtil;

public class GeocoderConfig {
	public static final String VERSION = "4.1.0";
	public static final String LOGGER_PREFIX = "BGEO.";
	public static final PrecisionModel BASE_PRECISION_MODEL = new PrecisionModel(1000);
	private static final Logger logger = LoggerFactory.getLogger(LOGGER_PREFIX
			+ GeocoderConfig.class.getCanonicalName());

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final LocalDate NOT_RETIRED_DATE = LocalDate.parse("9999-12-31", DATE_FORMATTER);
	
	protected GeocoderConfigurationStore configStore;
	protected int baseSrsCode = -1;
	protected Polygon baseSrsBounds;
	protected String dataSourceClassName;
	protected String dataSourceBaseFileUrl;
	protected String apiUrl;
	protected String copyrightLicenseURI;
	protected String copyrightNotice;
	protected int defaultLookAtRange;
	protected int defaultSetBack;
	protected String defaultUnitDesignator;
	protected String disclaimer;
	protected String glossaryBaseURL;
	protected String kmlStylesUrl;
	protected String occupantCategoryKmlStyleUrl;
	protected String occupantCustomKmlStyleUrl;
	protected Map<String, Integer> matchFaultPenalties = new HashMap<String, Integer>();
	protected EnumMap<MatchPrecision, Integer> matchPrecisionPoints =
			new EnumMap<MatchPrecision, Integer>(MatchPrecision.class);
	protected int maxWithinResults;
	protected double blockFaceOffset;
	protected String moreInfoUrl;
	protected String privacyStatement;
	protected String fallbackAddress;
	protected int resultsLimit;
	protected List<String> parcelKeys = Collections.emptyList();
	protected boolean parcelKeysRequired = true;
	protected int generateUsingExistingRanges;

	protected EnumMap<RoadClass, Float> roadBaseWidths = new EnumMap<RoadClass, Float>(
			RoadClass.class);
	protected EnumMap<DividerType, Float> roadDividerWidths = new EnumMap<DividerType, Float>(
			DividerType.class);
	protected EnumMap<RoadClass, Float> roadLaneWidths = new EnumMap<RoadClass, Float>(
			RoadClass.class);
	protected float roadNarrowMultiplier;

	public GeocoderConfig() {
		loadDefaults();
	}

	public GeocoderConfig(GeocoderConfigurationStore configStore, GeometryFactory gf) {
		loadDefaults();

		this.configStore = configStore;

		Stream<ConfigurationParameter> configParams = configStore.getConfigParams();
		configParams.forEach(configParam -> {;
			String name = configParam.getConfigParamName();
			String value = configParam.getConfigParamValue();
			try {
				if("apiUrl".equals(name)) {
					apiUrl = value;
				} else if("baseSrsCode".equals(name)) {
					baseSrsCode = Integer.parseInt(value);
				} else if("baseSrsBounds".equals(name)) {
					baseSrsBounds = GeomParseUtil.parseBbox(value, gf);
				} else if("copyrightLicenseURI".equals(name)) {
					copyrightLicenseURI = value;
				} else if("copyrightNotice".equals(name)) {
					copyrightNotice = value;
				} else if("dataSource.className".contentEquals(name)) {
					dataSourceClassName = value;
				} else if("dataSource.baseFileUrl".contentEquals(name)) {
					dataSourceBaseFileUrl = value;
				} else if("defaultLookAtRange".equals(name)) {
					defaultLookAtRange = Integer.parseInt(value);
				} else if("defaultSetBack".equals(name)) {
					defaultSetBack = Integer.parseInt(value);
				} else if("defaultUnitDesignator".equals(name)) {
					defaultUnitDesignator = value;
				} else if("disclaimer".equals(name)) {
					disclaimer = value;
				} else if("glossaryBaseURL".equals(name)) {
					glossaryBaseURL = value;
				} else if("kmlStylesUrl".equals(name)) {
					kmlStylesUrl = value;
				} else if("occupantCategoryKmlStyleUrl".equals(name)) {
					occupantCategoryKmlStyleUrl = value;
				} else if("occupantCustomKmlStyleUrl".equals(name)) {
					occupantCustomKmlStyleUrl = value;
				} else if(name.startsWith("fault.")) {
					// Example: fault.LOCALITY.notMatched
					String[] nameParts = name.split("\\.");
					matchFaultPenalties.put(nameParts[1] + "." + nameParts[2], Integer.parseInt(value));
				} else if(name.startsWith("precision.")) {
					matchPrecisionPoints.put(MatchPrecision.convert(name.substring(10)),
							Integer.parseInt(value));
				} else if("maxWithinResults".equals(name)) {
					maxWithinResults = Integer.parseInt(value);
				} else if("blockFaceOffset".equals(name)) {
					blockFaceOffset = Double.parseDouble(value);
				} else if("moreInfoUrl".equals(name)) {
					moreInfoUrl = value;
				} else if("privacyStatement".equals(name)) {
					privacyStatement = value;
				} else if("fallbackAddress".equals(name)) {
					fallbackAddress = value;
				} else if("resultsLimit".equals(name)) {
					resultsLimit = Integer.parseInt(value);
				} else if("generateUsingExistingRanges".equals(name)) {
					generateUsingExistingRanges = Integer.parseInt(value);
				} else if(name.startsWith("roadBaseWidth.")) {
					roadBaseWidths.put(RoadClass.convert(name.substring(14)),
							Float.parseFloat(value));
				} else if(name.startsWith("roadLaneWidth.")) {
					roadLaneWidths.put(RoadClass.convert(name.substring(14)),
							Float.parseFloat(value));
				} else if(name.startsWith("roadDividerWidth.")) {
					roadDividerWidths.put(DividerType.convert(name.substring(17)),
							Float.parseFloat(value));
				} else if("roadNarrowMultiplier".equals(name)) {
					roadNarrowMultiplier = Float.parseFloat(value);
				} else if("parcelKeys".equals(name)) {
					if(value != null) {
						parcelKeys = Collections.unmodifiableList(Arrays.asList(value.split(",")));
					}
				} else if("parcelKeysRequired".equals(name)) {
					parcelKeysRequired = Boolean.valueOf(value);
				} else if(!"Admin Access Key".equals(name)) {
					logger.warn("Unused configuration parameter '{}' with value '{}'", name, value);
				}
			} catch(IllegalArgumentException iae) {
				logger.warn("Unparseable configuration parameter '{}' with value '{}'", name, value);
			}
		});
	}

	public int getBaseSrsCode() {
		return baseSrsCode;
	}

	public Polygon getBaseSrsBounds() {
		return baseSrsBounds;
	}

	public String getVersion() {
		return VERSION;
	}

	public String getDataSourceBaseFileUrl() {
		return dataSourceBaseFileUrl;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public String getCopyrightLicense() {
		return copyrightLicenseURI;
	}

	public String getCopyrightNotice() {
		return copyrightNotice;
	}

	public int getDefaultLookAtRange() {
		return defaultLookAtRange;
	}

	public int getDefaultSetBack() {
		return defaultSetBack;
	}

	public String getDefaultUnitDesignator() {
		return defaultUnitDesignator;
	}

	public String getDisclaimer() {
		return disclaimer;
	}

	public String getGlossaryBaseUrl() {
		return glossaryBaseURL;
	}

	public String getKmlStylesUrl() {
		return kmlStylesUrl;
	}

	public String getOccupantCategoryKmlStyleUrl() {
		return occupantCategoryKmlStyleUrl;
	}

	public String getOccupantCustomKmlStyleUrl() {
		return occupantCustomKmlStyleUrl;
	}

	public MatchFault getLocalityAliasFault(String text, int confidence) {
		return new MatchFault(text,
				MatchFault.MatchElement.LOCALITY, "isAlias",
				matchFaultPenalties.get("LOCALITY.isAlias") * (100 - confidence) / 100);
	}

	public MatchFault getSiteNamePartialMatchFault(String text, double coefficient) {
		return new MatchFault(text,
				MatchFault.MatchElement.SITE_NAME, "partialMatch",
				(int)Math.round(matchFaultPenalties.get("SITE_NAME.partialMatch") * coefficient));
	}

	public MatchFault getOccupantNamePartialMatchFault(String text, double coefficient) {
		return new MatchFault(text,
				MatchFault.MatchElement.OCCUPANT_NAME, "partialMatch",
				(int)Math.round(matchFaultPenalties.get("OCCUPANT_NAME.partialMatch") * coefficient));
	}

	public MatchFault getUnrecognizedMatchFault(String unrecognized) {
		// penalty value is the base penalty plus 10% for each word
		return new MatchFault(unrecognized, MatchFault.MatchElement.UNRECOGNIZED, "notAllowed", matchFaultPenalties.get("UNRECOGNIZED.notAllowed"));
	}

	public MatchFault getMatchFault(String text, MatchElement element, String fault) {
		Integer penalty = matchFaultPenalties.get(element.toString() + "." + fault);
		if(penalty != null) {
			return new MatchFault(text, element, fault, penalty);
		}
		logger.warn("No Match Fault Penalty set for '{}'.", element.toString() + "." + fault);
		return null;
	}

	public int getMatchPrecisionPoints(MatchPrecision precision) {
		Integer points = matchPrecisionPoints.get(precision);
		if(points != null) {
			return points.intValue();
		}
		logger.warn("No Match Precision Points set for '{}'.", precision);
		return 0;
	}

	public int getMaxWithinResults() {
		return maxWithinResults;
	}

	public double getBlockFaceOffset() {
		return blockFaceOffset;
	}

	public String getMoreInfoUrl() {
		return moreInfoUrl;
	}

	public String getPrivacyStatement() {
		return privacyStatement;
	}

	public String getFallbackAddress() {
		return fallbackAddress;
	}

	public int getResultsLimit() {
		return resultsLimit;
	}

	public int getGenerateUsingExistingRanges() {
		return generateUsingExistingRanges;
	}

	public float getRoadBaseWidth(RoadClass roadClass) {
		return roadBaseWidths.get(roadClass);
	}

	public float getRoadDividerWidth(DividerType dividerType) {
		return roadDividerWidths.get(dividerType);
	}

	public float getRoadLaneWidth(RoadClass roadClass) {
		return roadLaneWidths.get(roadClass);
	}

	public float getRoadNarrowMultiplier() {
		return roadNarrowMultiplier;
	}

	protected void loadDefaults() {
		// basic data to make things behave reasonably
		dataSourceClassName = "ca.bc.gov.ols.geocoder.datasources.FileGeocoderDataSource";
		baseSrsCode = 3005;
		//baseSrsBounds = GeomParseUtil.parseBbox(value, gf);
		resultsLimit = 100;
		defaultSetBack = 0;
		defaultLookAtRange = 400;
		defaultUnitDesignator = "UNIT";
		kmlStylesUrl = "http://openmaps.gov.bc.ca/kml/bgeo_results_styles.kml";
		occupantCategoryKmlStyleUrl = "http://openmaps.gov.bc.ca/kml/occupant_category_styles.kml";
		occupantCustomKmlStyleUrl = "http://openmaps.gov.bc.ca/kml/occupant_custom_styles.kml";
		moreInfoUrl = "http://www.data.gov.bc.ca/dbc/geographic/locate/geocoding.page?";
		glossaryBaseURL = "http://www.data.gov.bc.ca/dbc/geographic/locate/physical_address_geo/glossary_of_terms.page?WT.svl=LeftNav";
		maxWithinResults = 100;
		blockFaceOffset = 0.5;

		fallbackAddress = null;
		generateUsingExistingRanges = 0;

		copyrightLicenseURI = "http://www.data.gov.bc.ca/local/dbc/docs/license/OGL-vbc2.0.pdf";
		copyrightNotice = "Copyright 2015 Province of British Columbia - Open Government License";

		matchPrecisionPoints.put(MatchPrecision.OCCUPANT, 100);
		matchPrecisionPoints.put(MatchPrecision.SITE, 100);
		matchPrecisionPoints.put(MatchPrecision.UNIT, 100);
		matchPrecisionPoints.put(MatchPrecision.CIVIC_NUMBER, 100);
		matchPrecisionPoints.put(MatchPrecision.INTERSECTION, 100);
		matchPrecisionPoints.put(MatchPrecision.BLOCK, 99);
		matchPrecisionPoints.put(MatchPrecision.STREET, 78);
		matchPrecisionPoints.put(MatchPrecision.LOCALITY, 68);
		matchPrecisionPoints.put(MatchPrecision.PROVINCE, 58);
		matchPrecisionPoints.put(MatchPrecision.NONE, 1);


		matchFaultPenalties.put("OCCUPANT_NAME.partialMatch", 10);
		matchFaultPenalties.put("OCCUPANT_NAME.notMatched", 10);

		// note that notMatched should be equal to or higher than partialMatch
		// otherwise partial matches could score lower than non-matches
		matchFaultPenalties.put("SITE_NAME.partialMatch", 10);
		matchFaultPenalties.put("SITE_NAME.notMatched", 10);
		matchFaultPenalties.put("SITE_NAME.missing", 0);
		matchFaultPenalties.put("SITE_NAME.spelledWrong", 1);

		matchFaultPenalties.put("UNIT_DESIGNATOR.missing", 1);
		matchFaultPenalties.put("UNIT_DESIGNATOR.isAlias", 1);
		matchFaultPenalties.put("UNIT_DESIGNATOR.notMatched", 1);
		matchFaultPenalties.put("UNIT_DESIGNATOR.spelledWrong", 1);

		matchFaultPenalties.put("UNIT_NUMBER.missing", 1);
		matchFaultPenalties.put("UNIT_NUMBER.notMatched", 1);
		matchFaultPenalties.put("UNIT_NUMBER.spelledWrong", 1);

		matchFaultPenalties.put("UNIT_NUMBER_SUFFIX.missing", 1);
		matchFaultPenalties.put("UNIT_NUMBER_SUFFIX.notMatched", 1);

		matchFaultPenalties.put("CIVIC_NUMBER.notInAnyBlock", 10);
		matchFaultPenalties.put("CIVIC_NUMBER.missing", 10);
		matchFaultPenalties.put("CIVIC_NUMBER_SUFFIX.notMatched", 1);

		// for CIVIC sites matches using SITE NAME (because no street name/civic number input)
		matchFaultPenalties.put("STREET.missing", 0);

		// STREET_NAME.notMatched is used for extra intersection streets that don't match, and for
		// locality fall-backs
		matchFaultPenalties.put("STREET_NAME.missing", 10);
		matchFaultPenalties.put("STREET_NAME.notMatched", 12);
		matchFaultPenalties.put("STREET_NAME.spelledWrong", 2);
		matchFaultPenalties.put("STREET_NAME.isAlias", 1);
		matchFaultPenalties.put("STREET_NAME.isHighwayAlias", 1);
		matchFaultPenalties.put("STREET_NAME.partialMatch", 1);

		matchFaultPenalties.put("STREET_DIRECTION.missing", 2);
		matchFaultPenalties.put("STREET_DIRECTION.notMatched", 2);
		matchFaultPenalties.put("STREET_DIRECTION.spelledWrong", 1);
		matchFaultPenalties.put("STREET_DIRECTION.notMatchedInHighway", 1);

		matchFaultPenalties.put("STREET_TYPE.missing", 6);
		matchFaultPenalties.put("STREET_TYPE.notMatched", 3);
		matchFaultPenalties.put("STREET_TYPE.spelledWrong", 1);

		matchFaultPenalties.put("STREET_QUALIFIER.missing", 1);
		matchFaultPenalties.put("STREET_QUALIFIER.notMatched", 1);
		matchFaultPenalties.put("STREET_QUALIFIER.spelledWrong", 1);

		matchFaultPenalties.put("LOCALITY.isAlias", 20);
		matchFaultPenalties.put("LOCALITY.notMatched", 35);
		matchFaultPenalties.put("LOCALITY.missing", 10);
		matchFaultPenalties.put("LOCALITY.spelledWrong", 2);
		matchFaultPenalties.put("LOCALITY.partialMatch", 1);
		matchFaultPenalties.put("LOCALITY.partialMatchToAlias", 30);

		matchFaultPenalties.put("PROVINCE.notMatched", 1);
		matchFaultPenalties.put("PROVINCE.missing", 1);
		matchFaultPenalties.put("PROVINCE.spelledWrong", 2);

		matchFaultPenalties.put("POSTAL_ADDRESS_ELEMENT.notAllowed", 1);

		matchFaultPenalties.put("UNRECOGNIZED.notAllowed", 5);

		matchFaultPenalties.put("MAX_RESULTS.too_low_to_include_all_best_matches", 0);

		matchFaultPenalties.put("ADDRESS.missing", 12);

		// default all roadClasses to 3m per lane + 1m base
		for(RoadClass rc : RoadClass.values()) {
			roadLaneWidths.put(rc, 3f);
			roadBaseWidths.put(rc, 1f);
		}
		roadLaneWidths.put(RoadClass.ALLEYWAY, 2.5f);
		roadBaseWidths.put(RoadClass.ALLEYWAY, 0f);
		roadLaneWidths.put(RoadClass.ARTERIAL_MAJOR, 3f);
		roadBaseWidths.put(RoadClass.ARTERIAL_MAJOR, 0.5f);
		roadLaneWidths.put(RoadClass.ARTERIAL_MINOR, 3f);
		roadBaseWidths.put(RoadClass.ARTERIAL_MINOR, 0.5f);
		roadLaneWidths.put(RoadClass.COLLECTOR_MAJOR, 3f);
		roadBaseWidths.put(RoadClass.COLLECTOR_MAJOR, 0.5f);
		roadLaneWidths.put(RoadClass.COLLECTOR_MINOR, 3f);
		roadBaseWidths.put(RoadClass.COLLECTOR_MINOR, 0.5f);
		roadLaneWidths.put(RoadClass.FERRY, 0f);
		roadBaseWidths.put(RoadClass.FERRY, 0f);
		roadLaneWidths.put(RoadClass.FREEWAY, 3f);
		roadBaseWidths.put(RoadClass.FREEWAY, 2f);
		roadLaneWidths.put(RoadClass.HIGHWAY_MAJOR, 3f);
		roadBaseWidths.put(RoadClass.HIGHWAY_MAJOR, 1.5f);
		roadLaneWidths.put(RoadClass.HIGHWAY_MINOR, 3f);
		roadBaseWidths.put(RoadClass.HIGHWAY_MINOR, 1f);
		roadLaneWidths.put(RoadClass.LANE, 2.5f);
		roadBaseWidths.put(RoadClass.LANE, 0f);
		roadLaneWidths.put(RoadClass.LOCAL, 3f);
		roadBaseWidths.put(RoadClass.LOCAL, 1f);
		roadLaneWidths.put(RoadClass.RAMP, 3f);
		roadBaseWidths.put(RoadClass.RAMP, 2f);
		roadLaneWidths.put(RoadClass.RECREATION, 3f);
		roadBaseWidths.put(RoadClass.RECREATION, 1f);
		roadLaneWidths.put(RoadClass.RESOURCE, 2.5f);
		roadBaseWidths.put(RoadClass.RESOURCE, 0f);
		roadLaneWidths.put(RoadClass.RESTRICTED, 3f);
		roadBaseWidths.put(RoadClass.RESTRICTED, 0.5f);
		roadLaneWidths.put(RoadClass.RUNWAY, 1f);
		roadBaseWidths.put(RoadClass.RUNWAY, 0f);
		roadLaneWidths.put(RoadClass.SERVICE, 3f);
		roadBaseWidths.put(RoadClass.SERVICE, 0.5f);
		roadLaneWidths.put(RoadClass.STRATA, 3f);
		roadBaseWidths.put(RoadClass.STRATA, 1f);
		roadLaneWidths.put(RoadClass.TRAIL, 1f);
		roadBaseWidths.put(RoadClass.TRAIL, 0f);
		roadLaneWidths.put(RoadClass.TRAIL_RECREATION, 1f);
		roadBaseWidths.put(RoadClass.TRAIL_RECREATION, 0f);

		roadDividerWidths.put(DividerType.HARD, 1f);
		roadDividerWidths.put(DividerType.SOFT, 2f);
		roadDividerWidths.put(DividerType.NONE, 0f);

		roadNarrowMultiplier = 0.75f;

	}

	public Stream<UnitDesignator> getUnitDesignators() {
		return configStore.getUnitDesignators();
	}

	public Stream<LocalityMapping> getLocalityMappings() {
		return configStore.getLocalityMappings();
	}

	public Stream<AbbreviationMapping> getAbbreviationMappings() {
		return configStore.getAbbrevMappings();
	}

	public void close() {
		configStore.close();
	}

	public List<String> getParcelKeys() {
		return parcelKeys;
	}

	public boolean getParcelKeysRequired() {
		return parcelKeysRequired;
	}

	public String getDataSourceClassName() {
		return dataSourceClassName;
	}

}
