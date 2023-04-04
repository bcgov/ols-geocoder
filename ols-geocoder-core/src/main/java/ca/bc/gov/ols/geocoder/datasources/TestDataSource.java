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
package ca.bc.gov.ols.geocoder.datasources;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.AbbreviationMapping;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.config.LocalityMapping;
import ca.bc.gov.ols.geocoder.config.UnitDesignator;
import ca.bc.gov.ols.rowreader.DateType;
import ca.bc.gov.ols.rowreader.FlexObj;
import ca.bc.gov.ols.rowreader.FlexObjListRowReader;
import ca.bc.gov.ols.rowreader.RowReader;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

public class TestDataSource implements GeocoderDataSource {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ TestDataSource.class.getCanonicalName());

	private Set<String> streetTypes = new THashSet<String>();
	private Map<String, Integer> streetTypeSchema = FlexObj
			.createSchema(new String[] {"street_type"});
	
	private Set<String> streetDirs = new THashSet<String>();
	private Map<String, Integer> streetDirSchema = FlexObj
			.createSchema(new String[] {"street_direction"});
	
	private Set<String> streetQualifiers = new THashSet<String>();
	private Map<String, Integer> streetQualifierSchema = FlexObj
			.createSchema(new String[] {"street_qualifier"});
	
	private Set<String> streetMuniQuads = new THashSet<String>();
	private Map<String, Integer> streetMuniQuadSchema = FlexObj
			.createSchema(new String[] {"street_muni_quad"});
	
	private Set<String> unitDesignators = new THashSet<String>();
	private Map<String, Integer> unitDesignatorSchema = FlexObj
			.createSchema(new String[] {"canonical_form"});
	
	private TIntObjectHashMap<FlexObj> streetNames = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> streetNameSchema = FlexObj.createSchema(new String[] {
			"street_name_id", "name_body", "street_type",
			"street_direction", "street_qualifier", "street_muni_quad",
			"street_type_is_prefix_ind", "street_direction_is_prefix_ind"});
	
	private List<FlexObj> streetNameOnSegments = new ArrayList<FlexObj>();
	private Map<String, Integer> streetNameOnSegmentSchema = FlexObj.createSchema(
			new String[] {"street_name_id", "street_segment_id", "is_primary_ind"});
	
	private TIntObjectHashMap<FlexObj> stateProvTerrs = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> stateProvTerrSchema = FlexObj.createSchema(
			new String[] {"state_prov_terr_id", "state_prov_terr_name", "country_code",
					"geom"});
	
	private TIntObjectHashMap<FlexObj> localities = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> localitySchema = FlexObj.createSchema(
			new String[] {"locality_id", "locality_name", "locality_type_name",
					"state_prov_terr_id", "geom", "locality_type_id", "electoral_area_id"});

	private TIntObjectHashMap<FlexObj> electoralAreas = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> electoralAreaSchema = FlexObj.createSchema(
			new String[] {"electoral_area_id", "electoral_area_name"});

	private TIntObjectHashMap<FlexObj> streetSegments = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> streetSegmentSchema = FlexObj.createSchema(
			new String[] {"street_segment_id",
					"start_intersection_id", "end_intersection_id",
					"road_class", "lane_restriction", "travel_direction", "divider_type",
					"left_locality_id", "right_locality_id", "num_lanes_left", "num_lanes_right",
					"first_address_left", "last_address_left",
					"first_address_right", "last_address_right",
					"address_parity_left", "address_parity_right",
					"first_address_left_type_2", "last_address_left_type_2",
					"first_address_right_type_2", "last_address_right_type_2",
					"address_parity_left_type_2", "address_parity_right_type_2",
					"geom"});
	
	private List<FlexObj> streetLocalityCentroids = new ArrayList<FlexObj>();
	private Map<String, Integer> streetLocalityCentroidSchema = FlexObj.createSchema(
			new String[] {"locality_id", "street_name_id", "geom"});
	
	private TIntObjectHashMap<FlexObj> intersections = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> intersectionSchema = FlexObj.createSchema(
			new String[] {"street_intersection_id", "intersection_uuid", "degree", "geom"});
	
	private List<FlexObj> localityMappings = new ArrayList<FlexObj>();
	private Map<String, Integer> localityMappingSchema = FlexObj.createSchema(
			new String[] {"input_string", "locality_id", "confidence"});
	
	private List<FlexObj> abbreviationMappings = new ArrayList<FlexObj>();
	private Map<String, Integer> abbreviationMappingSchema = FlexObj.createSchema(
			new String[] {"abbreviated_form", "long_form"});
	
	private TIntObjectHashMap<FlexObj> sites = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> siteSchema = FlexObj.createSchema(
			new String[] {"site_id", "site_uuid", "parent_site_id", "site_name",
					"location_descriptor", "positional_accuracy",
					"unit_designator", "unit_number", "unit_number_suffix", 
					"site_status", "retire_date", "last_changed_date",
					"narrative_location", "geom"});
	
	private TIntObjectHashMap<FlexObj> accessPoints = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> accessPointSchema = FlexObj.createSchema(
			new String[] {"access_point_id", "ap_type", "range_type", "site_id",
					"street_segment_id", "locality_id", "civic_number",
					"civic_number_suffix", "positional_accuracy",
					"is_primary_ind", "geom"});

	private TIntObjectHashMap<FlexObj> occupants = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> occupantSchema = FlexObj.createSchema(
			new String[] {"occupant_id", "occupant_uuid", "occupant_site_id", "occupant_name", 
					"occupant_description", "alias_address",
					"contact_phone", "contact_email", "contact_fax",
					"website_url", "image_url", "keywords", "business_category_class",
					"date_updated", "date_added", "custodian_id", "source_data_id",
					"custom_style_name"});

	private List<FlexObj> businessCategories = new ArrayList<FlexObj>();
	private Map<String, Integer> businessCategorySchema = FlexObj.createSchema(
			new String[] {"business_category_class", "business_category_description", 
					"naics_code"});


	private TIntObjectHashMap<FlexObj> combinedSitesPost = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> combinedSitesPostSchema = FlexObj.createSchema(
			new String[] {"site_id", "site_uuid", "parent_site_id", "site_name",
					"location_descriptor", "unit_designator",
					"unit_number", "unit_number_suffix", "site_positional_accuracy",
					"site_status", "site_change_date", "site_retire_date", "site_albers_", "ap_type", "is_primary_ind", "narrative_location",
					"access_positional_accuracy", "civic_number", "civic_number_suffix", "access_point_status",
					"access_retire_date", "access_albers_", "full_address", "street_segment_id",
					"locality_id", "pids", "input_name"});

	private TIntObjectHashMap<FlexObj> streetSegmentsPost = new TIntObjectHashMap<FlexObj>();
	private Map<String, Integer> streetSegmentsPostSchema = FlexObj.createSchema(
			new String[] {"street_segment_id",
					"first_address_left", "first_address_right",
					"last_address_left", "last_address_right", "address_parity_left", "address_parity_right",
					"left_locality_id", "right_locality_id", "left_electoral_area_id", "right_electoral_area_id",
					"num_lanes_left", "num_lanes_right", "road_class", "lane_restriction", "travel_direction",
					"divider_type", "start_intersection_id", "end_intersection_id",
					"geom"});

	private GeocoderConfig config;
	
	public TestDataSource(GeocoderConfig config, GeometryFactory gf) {
		this.config = config;

		electoralAreas.put(1, new FlexObj(electoralAreaSchema, new Object[] { 1, "Victoria" }));
		electoralAreas.put(2, new FlexObj(electoralAreaSchema, new Object[] { 2, "Vancouver" }));

		// AbbreviationMappings
		abbreviationMappings.add(new FlexObj(abbreviationMappingSchema,
				new Object[] {"St", "Street"}));
		abbreviationMappings.add(new FlexObj(abbreviationMappingSchema,
				new Object[] {"BC", "British Columbia"}));
		
		// StateProvTerrs IDs in 10-range
		stateProvTerrs.put(10, new FlexObj(stateProvTerrSchema,
				new Object[] {10, "BC", "CA",
						gf.createPoint(new Coordinate(1, 1))
				}));
		
		// Localities IDs in 100-range
		localities.put(101, new FlexObj(localitySchema,
				new Object[] {101, "Victoria", "City", 10,
						gf.createPoint(new Coordinate(1, 1)), 1, 1
				}));
		localities.put(102, new FlexObj(localitySchema,
				new Object[] {102, "Vancouver", "City", 10,
						gf.createPoint(new Coordinate(1, 1)), 1, 2
				}));
		
		// Business Category IDs in the 200-range
		businessCategories.add(new FlexObj(businessCategorySchema,
				new Object[] {"professionalScientificAndTechnicalServices", 
				"Professional, scientific and technical services", "54"}));
		
		// Intersections IDs in 1000-range
		intersections.put(1001, new FlexObj(intersectionSchema,
				new Object[] {1001, "00000000-0000-0000-0000-000000001001", 1,
						gf.createPoint(new Coordinate(1, 1))
				}));
		intersections.put(1002, new FlexObj(intersectionSchema,
				new Object[] {1002, "00000000-0000-0000-0000-000000001002", 1,
						gf.createPoint(new Coordinate(2, 2))
				}));
		
		// StreetSegments IDs in 2000-range
		streetSegments.put(2001, new FlexObj(streetSegmentSchema,
				new Object[] {2001, 1001, 1002, "local", "N", "B", "N", 101, 101, 1, 1,
						1201, 1299, 1200, 1298, "O", "E", 1201, 1299, 1200, 1298, "O", "E",
						gf.createLineString(new Coordinate[]
						{new Coordinate(1, 1), new Coordinate(2, 2)})
				}));
		
		// StreetNames IDs in 3000-range
		streetNames.put(3001, new FlexObj(streetNameSchema,
				new Object[] {3001, "Douglas", "St", null, null, null, "N", "N"}));
		streetTypes.add("St");
		
		streetNameOnSegments.add(new FlexObj(streetNameOnSegmentSchema,
				new Object[] {3001, 2001, "Y"}));
		
		// StreetLocalityCentroids
		streetLocalityCentroids.add(new FlexObj(streetLocalityCentroidSchema,
				new Object[] {101, 3001, gf.createPoint(new Coordinate(1, 1))}));
		
		// Sites IDs in 4000-range
		sites.put(4001, new FlexObj(siteSchema, new Object[] {
				4001, "00000000-0000-0000-0000-000000004001", null, "Museum",
				"parcelPoint", "high", null, null, null,
				"A", LocalDate.of(9999, 12, 31), LocalDate.of(2015, 1, 1), 
				"Access Notes", "Narrative Location",
				gf.createPoint(new Coordinate(1, 1))
		}));

		sites.put(4002, new FlexObj(siteSchema, new Object[] {
				4002, "00000000-0000-0000-0000-000000004002", null, "Sayward Building",
				"parcelPoint", "high", null, null, null,
				"A", LocalDate.of(9999,12,31), LocalDate.of(2015,1,1), 
				"Access Notes", "Narrative Location",
				gf.createPoint(new Coordinate(2, 2))
		}));

		sites.put(4003, new FlexObj(siteSchema, new Object[] {
				4003, "00000000-0000-0000-0000-000000004003", 4002, null,
				"parcelPoint", "high", null, "419", null,
				"A", LocalDate.of(9999,12,31), LocalDate.of(2015,1,1), 
				"Access Notes", "Narrative Location",
				gf.createPoint(new Coordinate(2, 2))
		}));

		// AccessPoints IDs in 5000-range
		accessPoints.put(5001, new FlexObj(accessPointSchema,
				new Object[] {5001, "NCAP", 1, 4001,
						2001, 101, null, null, "medium", "Y",
						gf.createPoint(new Coordinate(1, 1))
				}));

		accessPoints.put(5002, new FlexObj(accessPointSchema,
				new Object[] {5002, "CAP", 1, 4002,
						2001, 101, 1207, null, "medium", "Y",
						gf.createPoint(new Coordinate(2, 2))
				}));
		
		// Occupant ID in 6000-range
		occupants.put(6001, new FlexObj(occupantSchema,
				new Object[] {6001, "00000000-0000-0000-0000-000000006001", 4002,
					"Refractions Research", "Software Development and Consulting", 
					"Suite 419 – 1207 Douglas Street Victoria, British Columbia, Canada, V8W 2E7",
					"(250)383-3022", "info@refractions.net", null,
					"http://www.refractions.net", "http://www.refractions.net/contact/sayward.jpg", 
					"Software;Consulting;", "professionalScientificAndTechnicalServices",
					LocalDate.of(2015,1,1), LocalDate.of(1979,1,1), "1", "1", ""
				}));

		combinedSitesPost.put(4002, new FlexObj(combinedSitesPostSchema,
				new Object[] {4002, "00000000-0000-0000-0000-000000004002", null, null, null,
						"parcelPoint", null, null, "medium", "A", LocalDate.of(2015,1,1), null, gf.createPoint(new Coordinate(1, 1)), "CAP", "Y", null, "medium", 4002, "A", null, null,
						gf.createPoint(new Coordinate(1, 1)), "Suite 419 – 1207 Douglas Street Victoria, British Columbia, Canada, V8W 2E7", 2001, 101, "028726014", "BCA"
				}));

		streetSegmentsPost.put(2001, new FlexObj(streetSegmentsPostSchema,
				new Object[] {2001, 4001, 4001, 4002, 4002, "E", "O", 101, 101, 1, 1, 1, 1, "local", "N", "B", "n", 1001, 1002,
						gf.createLineString(new Coordinate[]
								{new Coordinate(1, 1), new Coordinate(2, 2)})
				}));
	}
	
	@Override
	public RowReader getIntersections() {
		return new FlexObjListRowReader(intersections.valueCollection());
	}
	
	@Override
	public RowReader getStateProvTerrs() {
		return new FlexObjListRowReader(stateProvTerrs.valueCollection());
	}
	
	@Override
	public RowReader getStreetLocalityCentroids() {
		return new FlexObjListRowReader(streetLocalityCentroids);
	}
	
	@Override
	public RowReader getLocalities() {
		return new FlexObjListRowReader(localities.valueCollection());
	}

	@Override
	public RowReader getElectoralAreas() {
		return new FlexObjListRowReader(electoralAreas.valueCollection());
	}

	@Override
	public Stream<LocalityMapping> getLocalityMappings() {
		return new FlexObjListRowReader(localityMappings).asStream(LocalityMapping::new);
	}
	
	@Override
	public RowReader getStreetSegments() {
		return new FlexObjListRowReader(streetSegments.valueCollection());
	}
	
	@Override
	public RowReader getStreetNames() {
		return new FlexObjListRowReader(streetNames.valueCollection());
	}
	
	@Override
	public RowReader getStreetNameOnSegments() {
		return new FlexObjListRowReader(streetNameOnSegments);
	}
	
	@Override
	public Stream<AbbreviationMapping> getAbbreviationMappings() {
		return new FlexObjListRowReader(abbreviationMappings).asStream(AbbreviationMapping::new);
	}
	
	@Override
	public RowReader getStreetTypes() {
		ArrayList<FlexObj> streetTypeList = new ArrayList<FlexObj>(streetTypes.size());
		for(String type : streetTypes) {
			streetTypeList.add(new FlexObj(streetTypeSchema, new Object[] {type}));
		}
		return new FlexObjListRowReader(streetTypeList);
	}
	
	@Override
	public RowReader getStreetDirs() {
		ArrayList<FlexObj> streetDirList = new ArrayList<FlexObj>(streetDirs.size());
		for(String dir : streetDirs) {
			streetDirList.add(new FlexObj(streetDirSchema, new Object[] {dir}));
		}
		return new FlexObjListRowReader(streetDirList);
	}
	
	@Override
	public RowReader getStreetQualifiers() {
		ArrayList<FlexObj> streetQualList = new ArrayList<FlexObj>(streetQualifiers.size());
		for(String qual : streetQualifiers) {
			streetQualList.add(new FlexObj(streetQualifierSchema, new Object[] {qual}));
		}
		return new FlexObjListRowReader(streetQualList);
	}
	
	// not used yet
	public RowReader getStreetMuniQuads() {
		ArrayList<FlexObj> streetMuniQuadList = new ArrayList<FlexObj>(streetMuniQuads.size());
		for(String muniQuad : streetMuniQuads) {
			streetMuniQuadList.add(new FlexObj(streetMuniQuadSchema, new Object[] {muniQuad}));
		}
		return new FlexObjListRowReader(streetMuniQuadList);
	}
	
	@Override
	public Stream<UnitDesignator> getUnitDesignators() {
		ArrayList<FlexObj> unitDesignatorsList = new ArrayList<FlexObj>(unitDesignators.size());
		for(String ud : unitDesignators) {
			unitDesignatorsList.add(new FlexObj(streetMuniQuadSchema, new Object[] {ud}));
		}
		return new FlexObjListRowReader(unitDesignatorsList).asStream(UnitDesignator::new);
	}

	@Override
	public RowReader getBusinessCategories() {
		return new FlexObjListRowReader(businessCategories);
	}

	@Override
	public RowReader getOccupants() {
		return new FlexObjListRowReader(occupants.valueCollection());
	}

	@Override
	public RowReader getStreetSegmentsPost() {
		return new FlexObjListRowReader(streetSegmentsPost.valueCollection());
	}

	@Override
	public RowReader getCombinedSitesPost() {
		return new FlexObjListRowReader(combinedSitesPost.valueCollection());
	}
	
	@Override
	public GeocoderConfig getConfig() {
		return config;
	}
	
	@Override
	public void close() {
		logger.debug("TestDataSource.close() called");
	}

	@Override
	public RowReader getCombinedSites() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public RowReader getSid2Pids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<DateType, ZonedDateTime> getDates() {
		return null;
	}

}
