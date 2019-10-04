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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.AbbreviationMapping;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.config.LocalityMapping;
import ca.bc.gov.ols.geocoder.config.UnitDesignator;
import ca.bc.gov.ols.rowreader.RowReader;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * The AbstractGeocoderDataSource provides implementations for the GeocoderDataSource methods which
 * work across databases, preventing duplication of identical queries for different database
 * implementations.
 * 
 * @author chodgson
 * 
 */
public abstract class AbstractDBGeocoderDataSource implements DBGeocoderDataSource {
	private final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ AbstractDBGeocoderDataSource.class.getCanonicalName());
	
	private GeocoderConfig config;
	protected GeometryFactory geometryFactory;
	protected String schema = "bgeo";
	protected String tablePrefix = "bgeo_";
	protected String streetIntersectionsTable;
	protected String streetLocalityCentroidsTable;
	protected String localitiesTable;
	protected String localityTypesTable;
	protected String localityMappingsTable;
	protected String abbreviationMappingsTable;
	protected String streetNamesTable;
	protected String streetNameOnSegXrefTable;
	protected String unitDesignatorsTable;
	protected String sitesTable;
	protected String stateProvTerrTable;
	protected String accessPointsTable;
	protected String occupantsTable;
	protected String businessCategoriesTable;
	protected String configurationParametersTable;
	protected String streetSegmentsTable;
	protected String rangeGenerationLogsTable;
	
	/* Map from table name (all caps) to the local SRID */
	private static Map<String, Integer> sridMap = new HashMap<String, Integer>();
	
	public AbstractDBGeocoderDataSource(Properties props, GeometryFactory geometryFactory) {
		this.geometryFactory = geometryFactory;
		this.schema = props.getProperty("dataSource.schema", this.schema);
		this.tablePrefix = props.getProperty("dataSource.tablePrefix", this.tablePrefix);
		streetIntersectionsTable = tablePrefix + "street_intersections";
		streetLocalityCentroidsTable = tablePrefix + "street_locality_centroids";
		localitiesTable = tablePrefix + "localities";
		localityTypesTable = tablePrefix + "locality_types";
		localityMappingsTable = tablePrefix + "locality_mappings";
		abbreviationMappingsTable = tablePrefix + "abbreviation_mappings";
		streetNamesTable = tablePrefix + "street_names";
		streetNameOnSegXrefTable = tablePrefix + "street_name_on_seg_xref";
		unitDesignatorsTable = tablePrefix + "unit_designators";
		sitesTable = tablePrefix + "sites";
		stateProvTerrTable = tablePrefix + "state_prov_terrs";
		accessPointsTable = tablePrefix + "access_points";
		occupantsTable = tablePrefix + "occupants";
		businessCategoriesTable = tablePrefix + "business_categories";
		configurationParametersTable = tablePrefix + "configuration_parameters";
		streetSegmentsTable = tablePrefix + "street_segments";
		rangeGenerationLogsTable = tablePrefix + "range_generation_logs";
	}
	
	@Override
	public RowReader getIntersections() {
		String query = "SELECT street_intersection_id, intersection_uuid, degree, "
				+ getPointQuery("geometry", "")
				+ "FROM " + schema + "." + streetIntersectionsTable;
		return executeQuery(query);
	}
	
	@Override
	public RowReader getStreetLocalityCentroids() {
		String query = "SELECT locality_id, street_name_id, "
				+ getPointQuery("geometry", "")
				+ "FROM " + schema + "." + streetLocalityCentroidsTable;
		return executeQuery(query);
	}
	
	@Override
	public RowReader getStateProvTerrs() {
		String query = "SELECT state_prov_terr_id, state_prov_terr_name, country_code, "
				+ getPointQuery("geometry", "")
				+ "FROM " + schema + "." + stateProvTerrTable + " spt ";
		return executeQuery(query);
	}
	
	@Override
	public RowReader getLocalities() {
		String query = "SELECT locality_id, locality_name, lt.name as locality_type_name, "
				+ "state_prov_terr_id, "
				+ getPointQuery("geometry", "")
				+ "FROM " + schema + "." + localitiesTable + " loc "
				+ "LEFT JOIN " + schema + "." + localityTypesTable + " lt "
				+ "ON loc.locality_type_id = lt.locality_type_id";
		return executeQuery(query);
	}
	
	@Override
	public Stream<LocalityMapping> getLocalityMappings() {
		String query = "SELECT input_string, locality_id, confidence "
				+ "FROM " + schema + "." + localityMappingsTable + " "
				+ "WHERE ACTIVE_IND = 'Y' ORDER BY confidence DESC";
		return executeQuery(query).asStream(LocalityMapping::new);
	}
	
	@Override
	public Stream<AbbreviationMapping> getAbbreviationMappings() {
		String query = "SELECT abbreviated_form, long_form "
				+ "FROM " + schema + "." + abbreviationMappingsTable;
		return executeQuery(query).asStream(AbbreviationMapping::new);
	}
	
	@Override
	public RowReader getStreetTypes() {
		String query = "SELECT distinct street_type FROM " + schema + "." + streetNamesTable;
		return executeQuery(query);
	}
	
	@Override
	public RowReader getStreetDirs() {
		String query = "SELECT distinct street_direction FROM " + schema + "." + streetNamesTable;
		return executeQuery(query);
	}
	
	@Override
	public RowReader getStreetQualifiers() {
		String query = "SELECT distinct street_qualifier FROM " + schema + "." + streetNamesTable;
		return executeQuery(query);
	}
	
	@Override
	public Stream<UnitDesignator> getUnitDesignators() {
		String query = "SELECT canonical_form "
				+ "FROM " + schema + "." + unitDesignatorsTable;
		return executeQuery(query).asStream(UnitDesignator::new);
	}
	
	@Override
	public RowReader getStreetNames() {
		String query = "SELECT street_name_id, name_body, street_type, street_direction, street_qualifier, "
				+ "street_type_is_prefix_ind, street_direction_is_prefix_ind "
				+ "FROM " + schema + "." + streetNamesTable;
		return executeQuery(query);
	}
	
	@Override
	public RowReader getStreetNameOnSegments() {
		String query = "SELECT street_name_id, street_segment_id, is_primary_ind "
				+ "FROM " + schema + "." + streetNameOnSegXrefTable;
		// + " WHERE street_segment_id = 141457"; // TESTING ONLY
		return executeQuery(query);
	}
	
//	@Override
//	public RowReader getIntersectionByUuid(String uuid) {
//		String query = "SELECT street_intersection_id FROM " + schema
//				+ "." + streetIntersectionsTable + " "
//				+ "WHERE intersection_uuid = '" + uuid + "'";
//		return executeQuery(query);
//	}
	
//	@Override
//	public RowReader getSites() {
//		String query = "SELECT site_id, site_uuid, parent_site_id, site_name, "
//				+ "location_descriptor, positional_accuracy, "
//				+ "unit_designator, unit_number, unit_number_suffix, "
//				+ getPointQuery("geometry", "")
//				+ "from " + schema + "." + sitesTable;
//		return executeQuery(query);
//	}
	
//	@Override
//	public RowReader getRbSites() {
//		String query = "SELECT site.site_id, access_point_id, ap_type, range_type, "
//				+ "street_segment_id, interim_street_name_id, locality_id, "
//				+ "civic_number, civic_number_suffix, "
//				+ "ap.positional_accuracy, is_primary_ind, access_point_status, "
//				+ "narrative_location, ap.last_changed_date, ap.retire_date, "
//				+ getPointQuery("site.geometry", "") + " "
//				+ "from " + schema + "." + sitesTable + " site "
//				+ "join " + schema + "." + accessPointsTable + " ap "
//				+ "ON site.site_id = ap.site_id and ap.is_primary_ind = 'Y' "
//				+ "where range_type < 2";
//		// + " and site.site_id = 1000000"; // TESTING ONLY
//		return executeQuery(query);
//	}
	
//	@Override
//	public RowReader getAccessPoints() {
//		String query = "SELECT access_point_id, ap_type, range_type, site_id, street_segment_id, locality_id, "
//				+ "civic_number, civic_number_suffix, positional_accuracy, is_primary_ind, "
//				+ getPointQuery("geometry", "")
//				+ "from " + schema + "." + accessPointsTable + " "
//				+ "where range_type > 0 AND range_type <= " + config.getNumRanges();
//		return executeQuery(query);
//	}
	
	@Override
	public RowReader getBusinessCategories() {
		String query = "SELECT business_category_class, business_category_description, naics_code "
				+ "FROM " + schema + "." + businessCategoriesTable;
		return executeQuery(query);		
	}
	
	@Override
	public RowReader getOccupants() {
		String query = "SELECT occupant_id, occupant_uuid, occupant_site_id, occupant_name, "
				+ "occupant_description, alias_address, contact_phone, "
				+ "contact_email, contact_fax, website_url, image_url, "
				+ "keywords, business_category_class, date_updated, date_added,"
				+ "custodian_id, source_data_id, custom_style_name " 
				+ "FROM " + schema + "." + occupantsTable;
		return executeQuery(query);		
	}
	
//	@Override
//	public RowReader getSubSitesByUuid(String uuid) {
//		String query = "SELECT s.site_uuid FROM " + schema + "." + sitesTable + " s, "
//				+ schema + "." + sitesTable + " p "
//				+ "WHERE s.parent_site_id = p.site_id and p.site_uuid = '" + uuid + "'";
//		return executeQuery(query);
//	}
	
//	@Override
//	public RowReader getSiteDetails(Integer siteId, Integer accessPointId) {
//		if(siteId == null || accessPointId == null) {
//			return null;
//		}
//		String query = "SELECT s.site_uuid, s.site_status, s.retire_date, s.last_changed_date, "
//				+ "ap.narrative_location "
//				+ "FROM " + schema + "." + sitesTable + " s, "
//				+ schema + "." + accessPointsTable + " ap "
//				+ "WHERE s.site_id = " + siteId + " AND ap.access_point_id = " + accessPointId;
//		return executeQuery(query);
//	}
	
//	@Override
//	public void disableRegenerateRangesOnNextStartup() {
//		String query = "UPDATE " + schema + "." + configurationParametersTable + " "
//				+ "SET config_param_value = '0' "
//				+ "WHERE config_param_name = 'regenerateRangesOnNextStartup'";
//		Connection conn = getConnection();
//		try {
//			Statement stmt = conn.createStatement();
//			logger.debug("Executing query: {}", query);
//			stmt.executeUpdate(query);
//			stmt.close();
//		} catch(SQLException sqle) {
//			throw new RuntimeException("SQLException occured while executing query " + query, sqle);
//		} finally {
//			releaseConnection(conn);
//		}
//	}
	
	@Override
	public Point pointFromResultSet(ResultSet rs, String columnPrefix) throws SQLException {
		double x = rs.getDouble(columnPrefix + "x");
		double y = rs.getDouble(columnPrefix + "y");
		return geometryFactory.createPoint(new Coordinate(x, y));
	}
	
	abstract protected String getSridQuery(String tableName);
	
	/**
	 * Returns the SRID for the specified table (the table name must be in ALL CAPS).
	 * 
	 * SDE ST_Geometry uses locally-defined SRID values which relate to the common EPSG codes. Thus
	 * on different databases a different SRID may be in use, and we need to know the correct one in
	 * order to load data (sites/access_points)
	 * 
	 * @return the SRID for the specified table
	 */
	protected int getSRID(String tableName) {
		Integer srid = sridMap.get(tableName);
		if(srid == null) {
			String query = getSridQuery(tableName);
			RowReader rr = executeQuery(query);
			rr.next();
			srid = rr.getInt("srid");
			sridMap.put(tableName, srid);
			rr.close();
		}
		return srid;
	}
	
	protected RowReader executeQuery(String query) {
		try {
			Connection conn = getConnection();
			Statement stmt = conn.createStatement();
			logger.debug("Executing query: {}", query);
			return new ResultSetRowReader(this, conn, stmt.executeQuery(query));
		} catch(SQLException sqle) {
			throw new RuntimeException("SQLException occured while executing query " + query, sqle);
		}
	}
	
	protected int executeUpdate(String query) {
		try {
			Connection conn = getConnection();
			Statement stmt = conn.createStatement();
			logger.debug("Executing update query: {}", query);
			return stmt.executeUpdate(query);
		} catch(SQLException sqle) {
			throw new RuntimeException("SQLException occured while executing query " + query, sqle);
		}
	}
	
	protected abstract String getPointQuery(String column, String prefix);
	
	@Override
	public GeocoderConfig getConfig() {
		return config;
	}
	
	protected void setConfig(GeocoderConfig config) {
		this.config = config;
	}
	
}