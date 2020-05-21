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

//import java.sql.Connection;
//import java.sql.Driver;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;

//import oracle.jdbc.pool.OracleDataSource;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import ca.bc.gov.ols.DBGeocoderConfig;
//import ca.bc.gov.ols.GeocoderConfig;
//import ca.bc.gov.ols.data.enumTypes.LocationDescriptor;
//import ca.bc.gov.ols.datasources.rawdata.RawSite;
//import ca.bc.gov.ols.rangebuilder.RbStreetSegment;
//
//import com.vividsolutions.jts.geom.GeometryFactory;
//import com.vividsolutions.jts.geom.LineString;
//import com.vividsolutions.jts.geom.Point;
//import com.vividsolutions.jts.geom.Polygon;
//import com.vividsolutions.jts.io.ParseException;
//import com.vividsolutions.jts.io.WKBReader;
//import com.vividsolutions.jts.io.WKTWriter;

/**
 * Provides the Oracle-specific functionality of the GeocoderDataSource.
 * 
 * @author chodgson
 * 
 */

public class OracleGeocoderDataSource /*extends AbstractDBGeocoderDataSource */{
//	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
//			+ OracleGeocoderDataSource.class.getCanonicalName());
//	
//	private static Map<String, Integer> regIdMap = new HashMap<String, Integer>();
//	
//	private Properties props;
//	private OracleDataSource dataSource;
//	
//	public OracleGeocoderDataSource(Properties props, GeometryFactory geometryFactory) {
//		super(props, geometryFactory);
//		try {
//			logger.info("OracleGeocoderDataSource:Preconnection to jdbcUrl: "
//					+ props.getProperty("dataSource.jdbcUrl")
//					+ " username: " + props.getProperty("dataSource.username"));
//			this.props = props;
//			initDataSource();
//			Connection conn = getConnection();
//			try {
//				setConfig(new DBGeocoderConfig(conn, schema + "." + configurationParametersTable,
//						props, geometryFactory));
//			} finally {
//				releaseConnection(conn);
//			}
//			logger.info("OracleGeocoderDataSource: Configuration Data Loaded");
//		} catch(SQLException sqle) {
//			throw new RuntimeException(sqle);
//		}
//	}
//	
//	@SuppressWarnings("deprecation")
//	final private void initDataSource() {
//		try {
//			Properties cacheProperties = new Properties();
//			cacheProperties.put("MinLimit", "0");
//			cacheProperties.put("MaxLimit", props.getProperty("dataSource.maxConnections"));
//			cacheProperties.put("InitialLimit", props.getProperty("dataSource.initialConnections"));
//			
//			cacheProperties.put("InactivityTimeout", "300");
//			cacheProperties.put("ConnectionWaitTimeout", "10");
//			
//			dataSource = new OracleDataSource();
//			
//			dataSource.setConnectionCachingEnabled(true);
//			dataSource.setConnectionCacheProperties(cacheProperties);
//			dataSource.setURL(props.getProperty("dataSource.jdbcUrl"));
//			dataSource.setUser(props.getProperty("dataSource.username"));
//			dataSource.setPassword(props.getProperty("dataSource.password"));
//			
//			Properties properties = new Properties();
//			properties.put("defaultRowPrefetch", "512");
//			properties.put("oracle.jdbc.StreamChunkSize", "32000");
//			dataSource.setConnectionProperties(properties);
//		} catch(SQLException sqle) {
//			throw new IllegalArgumentException("Unable to create data source for "
//					+ props.getProperty("dataSource.jdbcUrl")
//					+ " username: " + props.getProperty("dataSource.username"), sqle);
//		}
//	}
//	
//	@Override
//	public RowReader getStreetSegments() {
//		String query = "SELECT street_segment_id, "
//				+ "start_intersection_id, end_intersection_id, "
//				+ "road_class, lane_restriction, travel_direction, divider_type, "
//				+ "left_locality_id, right_locality_id, num_lanes_left, num_lanes_right, "
//				+ "first_address_left, last_address_left,"
//				+ "first_address_right, last_address_right, "
//				+ "address_parity_left, address_parity_right, "
//				+ "first_address_left_type_2, last_address_left_type_2,"
//				+ "first_address_right_type_2, last_address_right_type_2, "
//				+ "address_parity_left_type_2, address_parity_right_type_2, "
//				+ "sde.st_asbinary(geometry) as geom "
//				+ "FROM " + schema + "." + streetSegmentsTable;
//		// + " WHERE street_segment_id = 141457"; // TESTING ONLY
//		return executeQuery(query);
//	}
//	
//	@Override
//	public LineString lineStringFromResultSet(ResultSet rs, String column) throws SQLException {
//		WKBReader reader = new WKBReader(geometryFactory);
//		try {
//			// Using blobs or binary streams causes excessive garbage creation
//			// and thus high GC overhead; getBytes seems to be most efficient
//			LineString ls = (LineString)reader.read(rs.getBytes(column));
//			return ls;
//		} catch(ParseException pe) {
//			throw new RuntimeException("ParseException while parsing LineString WKB", pe);
//		}
//	}
//	
//	// Not used for now
//	public RowReader getNearestSites(Point p, int maxDistance,
//			LocationDescriptor ld) {
//		WKTWriter writer = new WKTWriter(2);
//		String pointWkt = writer.write(p);
//		
//		String query = "SELECT site_uuid, " + getPointQuery("geometry", "") + " "
//				+ "FROM " + schema + "." + sitesTable + " s "
//				+ "WHERE sde.st_envintersects(s.geometry,sde.st_buffer(sde.st_geomfromtext('"
//				+ pointWkt
//				+ "'," + getSRID(sitesTable) + "), " + maxDistance + ") ) = 1 "
//				+ "AND s.parent_site_id IS NULL ";
//		/*
//		 * String query = "SELECT * FROM " + "(SELECT rownum, site_uuid " + "FROM " + schema + "." +
//		 * sitesTable + " s " +
//		 * "WHERE sde.st_envintersects(s.geometry,sde.st_buffer(sde.st_geomfromtext('" + pointWkt +
//		 * "'," + getSRID(sitesTable) + "), " + maxDistance + ") ) = 1 " +
//		 * "ORDER BY sde.st_distance(s.geometry,sde.st_geomfromtext('" + pointWkt + "'," +
//		 * getSRID(sitesTable) + "))) " + "where rownum = 1";
//		 */
//		return executeQuery(query);
//		
//	}
//	
//	// Not used for now
//	public RowReader getSitesWithin(Polygon poly) {
//		WKTWriter writer = new WKTWriter(2);
//		String polyWkt = writer.write(poly);
//		String query = "SELECT stmt.*, rownum FROM(SELECT site_uuid "
//				+ "FROM " + schema + "." + sitesTable + " s "
//				+ "WHERE sde.st_intersects(s.geometry,sde.st_geomfromtext('"
//				+ polyWkt + "'," + getSRID(sitesTable) + ")) = 1 "
//				+ "AND s.parent_site_id IS NULL "
//				+ ") stmt WHERE rownum <="
//				+ getConfig().getMaxWithinResults();
//		logger.debug("Executing query: {}", query);
//		return executeQuery(query);
//	}
//	
//	// Not used for now
//	public RowReader getNearestIntersection(Point point,
//			int maxDistance, int minDegree, int maxDegree) {
//		WKTWriter writer = new WKTWriter(2);
//		String pointWkt = writer.write(point);
//		
//		String query = "SELECT street_intersection_id, " + getPointQuery("geometry", "") + " "
//				+ "FROM " + schema + "." + streetIntersectionsTable + " "
//				+ "WHERE sde.st_envintersects(geometry,sde.st_buffer(sde.st_geomfromtext('"
//				+ pointWkt + "'," + getSRID(streetIntersectionsTable) + "), " + maxDistance
//				+ ")) = 1 "
//				+ "AND degree BETWEEN " + minDegree + " AND " + maxDegree;
//		
//		// String query = "SELECT street_intersection_id, rownum FROM "
//		// + "(SELECT street_intersection_id "
//		// + "FROM " + schema + "." + streetIntersectionsTable + " "
//		// + "WHERE sde.st_envintersects(geometry,sde.st_buffer(sde.st_geomfromtext('"
//		// + pointWkt + "'," + getSRID(streetIntersectionsTable) + "), " + maxDistance
//		// + ")) = 1 "
//		// + "AND degree BETWEEN " + minDegree + " AND " + maxDegree + " "
//		// + "ORDER BY sde.st_distance(geometry,sde.st_geomfromtext('"
//		// + pointWkt + "'," + getSRID(streetIntersectionsTable) + "))) "
//		// + "WHERE rownum = 1 ";
//		return executeQuery(query);
//	}
//	
//	// Not used for now
//	public RowReader getIntersectionsWithin(Polygon poly,
//			int minDegree, int maxDegree) {
//		WKTWriter writer = new WKTWriter(2);
//		String polyWkt = writer.write(poly);
//		String query = "SELECT street_intersection_id, rownum FROM (SELECT street_intersection_id "
//				+ "FROM " + schema + "." + streetIntersectionsTable + " "
//				+ "WHERE sde.st_intersects(geometry,sde.st_geomfromtext('"
//				+ polyWkt + "'," + getSRID(streetIntersectionsTable) + ")) = 1 "
//				+ "AND degree BETWEEN " + minDegree + " AND " + maxDegree + " "
//				+ ")stmt where rownum <="
//				+ getConfig().getMaxWithinResults();
//		return executeQuery(query);
//		
//	}
//	
//	@Override
//	protected String getSridQuery(String tableName) {
//		String query = "SELECT srid FROM sde.st_geometry_columns "
//				+ "WHERE table_name = '" + tableName.toUpperCase()
//				+ "' and OWNER = '" + schema.toUpperCase() + "'";
//		return query;
//	}
//	
//	private Integer getRegistrationId(String tableName) {
//		Integer regId = regIdMap.get(tableName);
//		if(regId == null) {
//			String query = "SELECT registration_id FROM sde.table_registry "
//					+ "WHERE table_name = '" + tableName.toUpperCase() + "' and owner = '"
//					+ schema.toUpperCase() + "'";
//			RowReader rr = executeQuery(query);
//			rr.next();
//			regId = rr.getInt("registration_id");
//			regIdMap.put(tableName, regId);
//			rr.close();
//		}
//		return regId;
//	}
//	
//	@Override
//	protected String getPointQuery(String column, String prefix) {
//		return "sde.st_x(" + column + ") as " + prefix + "x, sde.st_y(" + column + ") as " + prefix
//				+ "y ";
//	}
//	
//	@Override
//	@SuppressWarnings("deprecation")
//	public void close() {
//		logger.info("OracleGeocoderDataSource: closing DataSource");
//		try {
//			dataSource.close();
//			if(Boolean.parseBoolean(props.getProperty("dataSource.deregisterOnClose"))) {
//				Driver driver = DriverManager.getDriver(dataSource.getURL());
//				DriverManager.deregisterDriver(driver);
//			}
//			dataSource = null;
//		} catch(SQLException e) {
//			logger.error(e.getMessage(), e);
//		}
//	}
//	
//	@Override
//	public Connection getConnection() {
//		try {
//			Connection conn = dataSource.getConnection();
//			return conn;
//		} catch(SQLException sqle) {
//			throw new RuntimeException("SQLException while getting a connection", sqle);
//		}
//	}
//	
//	@Override
//	public void releaseConnection(Connection conn) {
//		try {
//			if(conn != null) {
//				conn.close();
//			}
//		} catch(SQLException sqle) {
//			throw new RuntimeException("SQLException while closing a connection", sqle);
//		}
//	}
//	
//	@Override
//	public void insertLogStats(String stats) {
//		String query = "INSERT INTO " + schema
//				+ "." + rangeGenerationLogsTable + "(LOG_DATE, LOG_DATA) VALUES(SYSDATE,'"
//				+ stats + "')";
//		executeUpdate(query);
//	}
//
//	@Override
//	public RowReader getStreetSegmentsPost() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public RowReader getCombinedSites() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public RowReader getCombinedSitesPost() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void writeRanges(Map<Integer, RbStreetSegment> segmentIdMap,
//			List<RawSite> extraSites, int maxApId) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void writeRanges(Map<Integer, RbStreetSegment> segmentIdMap,
//			List<RawSite> extraSites, int maxApId, String tableSuffix) {
//		// TODO Auto-generated method stub
//	}
//	
//	@Override
//	public void writeRanges(Map<Integer, RbStreetSegment> segmentIdMap, int maxApId) {
//		writeRanges(segmentIdMap, maxApId, "");
//	}
//	
//	@Override
//	public void writeRanges(Map<Integer, RbStreetSegment> segmentIdMap, int maxApId, String tableSuffix) {
//		// maxApId = 3000000; // TESTING ONLY
//		Connection conn = getConnection();
//		try {
//			Statement stmt;
//			// delete existing type2 accessPoints
//			String query = "DELETE FROM " + schema + "." + accessPointsTable + tableSuffix
//					+ " WHERE RANGE_TYPE = 2";
//			stmt = conn.createStatement();
//			stmt.executeUpdate(query);
//			stmt.close();
//			
//			// output resulting ranges and access points
//			logger.info("");
//			logger.info("Updating segment ranges and inserting type 2 access points in DB");
//			logger.info("-----------------------------------------------------------------");
//			conn.setAutoCommit(false);
//			PreparedStatement segPstmt = conn.prepareStatement(
//					"UPDATE " + schema + "." + streetSegmentsTable + tableSuffix + " SET "
//							+ "FIRST_ADDRESS_LEFT_TYPE_2=?, "
//							+ "LAST_ADDRESS_LEFT_TYPE_2=?, "
//							+ "FIRST_ADDRESS_RIGHT_TYPE_2=?, "
//							+ "LAST_ADDRESS_RIGHT_TYPE_2=?, "
//							+ "ADDRESS_PARITY_LEFT_TYPE_2=?, "
//							+ "ADDRESS_PARITY_RIGHT_TYPE_2=? "
//							+ "WHERE STREET_SEGMENT_ID=?");
//			int regId = getRegistrationId(accessPointsTable);
//			int srid = getSRID(accessPointsTable);
//			PreparedStatement apPstmt = conn
//					.prepareStatement(
//					"INSERT INTO " + schema + "." + accessPointsTable + tableSuffix + " ("
//							+ "access_point_id, ap_type, site_id, street_segment_id, locality_id, "
//							+ "civic_number_suffix, positional_accuracy, narrative_location, "
//							+ "is_primary_ind, civic_number, access_point_status, retire_date, "
//							+ "last_changed_date, range_type, objectid, geometry) "
//							+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
//							+ "sde.version_user_ddl.next_row_id('" + schema.toUpperCase() + "', " + regId + "),"
//							+ "sde.st_geometry(?,?,0,0," + srid + "))");
//			int segCount = 0;
//			int apCount = 0;
//			// WKBWriter wkbw = new WKBWriter();
//			try {
//				for(RbStreetSegment seg : segmentIdMap.values()) {
//					segPstmt.setObject(1,
//							seg.getFromLeft() == RangeBuilder.NULL_ADDR ? null : seg.getFromLeft());
//					segPstmt.setObject(2,
//							seg.getToLeft() == RangeBuilder.NULL_ADDR ? null : seg.getToLeft());
//					segPstmt.setObject(3,
//							seg.getFromRight() == RangeBuilder.NULL_ADDR ? null : seg
//									.getFromRight());
//					segPstmt.setObject(4,
//							seg.getToRight() == RangeBuilder.NULL_ADDR ? null : seg.getToRight());
//					segPstmt.setString(5, AddressScheme.parityToString(seg.getParityLeft()));
//					segPstmt.setString(6, AddressScheme.parityToString(seg.getParityRight()));
//					segPstmt.setInt(7, seg.getSegmentId());
//					segPstmt.addBatch();
//					segCount++;
//					for(Side side : Side.values()) {
//						for(RbSite site : seg.getSites(side)) {
//							// don't output RangeType of -1,
//							// these are just anchors, not actual sites.
//							if(site.getRangeType() >= 0) {
//								apPstmt.setInt(1, ++maxApId);
//								apPstmt.setString(2, site.getApType());
//								apPstmt.setInt(3, site.getSiteId());
//								apPstmt.setInt(4, seg.getSegmentId());
//								apPstmt.setInt(5, seg.getLocalityId(side));
//								apPstmt.setString(6, site.getCivicNumberSuffix());
//								apPstmt.setString(7, site.getPositionalAccuracy());
//								apPstmt.setString(8, site.getNarrativeLocation());
//								apPstmt.setString(9, site.getPrimaryInd());
//								apPstmt.setInt(10, site.getCivicNumber());
//								apPstmt.setString(11, site.getStatus());
//								apPstmt.setDate(12, site.getRetireDate());
//								apPstmt.setDate(13, site.getLastChangedDate());
//								apPstmt.setInt(14, 2);
//								// apPstmt.setString(16, wktw.write(site.getAccessPoint()));
//								// apPstmt.setBytes(16, wkbw.write(site.getAccessPoint()));
//								apPstmt.setDouble(15, site.getAccessPoint().getX());
//								apPstmt.setDouble(16, site.getAccessPoint().getY());
//								// apPstmt.executeUpdate();
//								apPstmt.addBatch();
//								apCount++;
//							}
//						}
//					}
//					if(segCount % 100000 == 0) {
//						logger.info(segCount + "/" + segmentIdMap.size() + " Segments updated.");
//					}
//					if(segCount % 100 == 0) {
//						segPstmt.executeBatch();
//						apPstmt.executeBatch();
//						conn.commit();
//					}
//				}
//				segPstmt.executeBatch();
//				apPstmt.executeBatch();
//				conn.commit();
//			} catch(SQLException sqle) {
//				StringWriter sw = new StringWriter();
//				PrintWriter pw = new PrintWriter(sw);
//				sqle.printStackTrace(pw);
//				sqle.getNextException().printStackTrace(pw);
//				logger.error(sw.toString());
//			} finally {
//				segPstmt.close();
//				apPstmt.close();
//				conn.setAutoCommit(true);
//			}
//			logger.info(segCount + "/" + segmentIdMap.size() + " Segments updated.");
//			logger.info(apCount + " Access Points inserted.");
//		} catch(SQLException sqle) {
//			throw new RuntimeException("SQLException occurred while updating ranges", sqle);
//		} finally {
//			releaseConnection(conn);
//		}
//	}


}
