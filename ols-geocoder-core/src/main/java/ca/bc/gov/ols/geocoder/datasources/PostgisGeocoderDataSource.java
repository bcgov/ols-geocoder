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
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//
//import org.apache.commons.dbcp.ConnectionFactory;
//import org.apache.commons.dbcp.DriverManagerConnectionFactory;
//import org.apache.commons.dbcp.PoolableConnectionFactory;
//import org.apache.commons.dbcp.PoolingDataSource;
//import org.apache.commons.pool.impl.GenericObjectPool;
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
 * Provides the Postgres/PostGIS-specific functionality of the GeocoderDataSource.
 * 
 * @author chodgson
 * 
 */
public class PostgisGeocoderDataSource /*extends AbstractDBGeocoderDataSource*/ {
//	private final static Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
//			+ PostgisGeocoderDataSource.class.getCanonicalName());
//	
//	private Properties props;
//	private PoolingDataSource dataSource;
//	GenericObjectPool connectionPool;
//	
//	public PostgisGeocoderDataSource(Properties props, GeometryFactory geometryFactory) {
//		super(props, geometryFactory);
//		try {
//			logger.info("PostgisGeocoderDataSource: connecting to: "
//					+ props.getProperty("dataSource.jdbcUrl")
//					+ " user: " + props.getProperty("dataSource.username"));
//			this.props = props;
//			initDataSource();
//			Connection conn = getConnection();
//			try {
//				setConfig(new DBGeocoderConfig(conn, schema + "." + configurationParametersTable,
//						props, geometryFactory));
//			} finally {
//				releaseConnection(conn);
//			}
//			logger.info("PostgisGeocoderDataSource: Configuration Data Loaded");
//		} catch(SQLException sqle) {
//			throw new RuntimeException(sqle);
//		}
//	}
//	
//	final private void initDataSource() {
//		try {
//			Class.forName("org.postgresql.Driver");
//			
//			connectionPool = new GenericObjectPool();
//			connectionPool.setMaxActive(Integer.parseInt(props
//					.getProperty("dataSource.maxConnections")));
//			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
//					props.getProperty("dataSource.jdbcUrl"),
//					props.getProperty("dataSource.username"),
//					props.getProperty("dataSource.password"));
//			new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false,
//					true);
//			
//			dataSource = new PoolingDataSource(connectionPool);
//		} catch(ClassNotFoundException cnfe) {
//			throw new RuntimeException(cnfe);
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
//				+ "st_asbinary(geometry) as geom FROM " + schema + "." + streetSegmentsTable;
//		return executeQuery(query);
//	}
//	
//	@Override
//	public LineString lineStringFromResultSet(ResultSet rs, String column) throws SQLException {
//		WKBReader reader = new WKBReader(geometryFactory);
//		byte[] bytes = rs.getBytes(column);
//		try {
//			LineString ls = (LineString)reader.read(bytes);
//			return ls;
//		} catch(ParseException pe) {
//			throw new RuntimeException("ParseException while parsing LineString WKB", pe);
//		}
//	}
//	
//	// Not used for now
//	public RowReader getNearestSites(Connection conn, Point p, int maxDistance,
//			LocationDescriptor ld) {
//		WKTWriter writer = new WKTWriter(2);
//		String pointWkt = writer.write(p);
//		// TODO: test performance using knn <-> operator
//		String query = "SELECT site_uuid, " + getPointQuery("geometry", "") + " "
//				+ "FROM " + schema + "." + sitesTable + " s "
//				+ "WHERE st_dwithin(s.geometry, st_geomfromtext('"
//				+ pointWkt + "'," + getSRID(sitesTable) + "), " + maxDistance
//				+ ") "
//				+ "AND s.parent_site_id IS NULL "
//				+ "ORDER BY st_distance(s.geometry,st_geomfromtext('"
//				+ pointWkt + "'," + getSRID(sitesTable) + ")) "
//				+ "LIMIT 1";
//		return executeQuery(query);
//		
//	}
//	
//	// not used for now
//	public RowReader getSitesWithin(Polygon poly) {
//		WKTWriter writer = new WKTWriter(2);
//		String polyWkt = writer.write(poly);
//		String query = "SELECT site_uuid FROM " + schema + "." + sitesTable + " s "
//				+ "WHERE st_dwithin(s.geometry,st_geomfromtext('"
//				+ polyWkt + "'," + getSRID(sitesTable) + "), 0)"
//				+ "AND s.parent_site_id is null "
//				+ "LIMIT " + getConfig().getMaxWithinResults();
//		return executeQuery(query);
//	}
//	
//	// not used for now
//	public RowReader getNearestIntersectionResultSet(Point point,
//			int maxDistance, int minDegree, int maxDegree) {
//		WKTWriter writer = new WKTWriter(2);
//		String pointWkt = writer.write(point);
//		String query = "SELECT street_intersection_id, " + getPointQuery("geometry", "") + " "
//				+ "FROM " + schema + "." + streetIntersectionsTable + " si "
//				+ "WHERE st_dwithin(si.geometry, st_geomfromtext('"
//				+ pointWkt + "'," + getSRID(streetIntersectionsTable) + "), " + maxDistance
//				+ ") " + "AND degree BETWEEN " + minDegree + " AND " + maxDegree + " "
//				+ "ORDER BY st_distance(si.geometry,st_geomfromtext('"
//				+ pointWkt + "'," + getSRID(streetIntersectionsTable) + ")) "
//				+ "LIMIT 1";
//		return executeQuery(query);
//	}
//	
//	// not used for now
//	public RowReader getIntersectionsWithinResultSet(Polygon poly,
//			int minDegree, int maxDegree) {
//		WKTWriter writer = new WKTWriter(2);
//		String polyWkt = writer.write(poly);
//		String query = "SELECT street_intersection_id "
//				+ "FROM " + schema + "." + streetIntersectionsTable + " "
//				+ "WHERE st_dwithin(geometry,st_geomfromtext('"
//				+ polyWkt + "'," + getSRID(streetIntersectionsTable) + "), 0) "
//				+ "AND degree BETWEEN " + minDegree + " AND " + maxDegree + " "
//				+ "LIMIT " + getConfig().getMaxWithinResults();
//		return executeQuery(query);
//		
//	}
//	
//	@Override
//	protected String getSridQuery(String tableName) {
//		String query = "SELECT srid FROM geometry_columns "
//				+ "WHERE f_table_name = '" + tableName.toLowerCase()
//				+ "' and f_table_schema = '" + schema + "'";
//		return query;
//	}
//	
//	@Override
//	protected String getPointQuery(String column, String prefix) {
//		return "st_x(" + column + ") as " + prefix + "x, st_y(" + column + ") as " + prefix
//				+ "y ";
//	}
//	
//	@Override
//	public void close() {
//		logger.info("PostgisGeocoderDataSource: closing DataSource");
//		try {
//			connectionPool.close();
//			if(Boolean.parseBoolean(props.getProperty("dataSource.deregisterOnClose"))) {
//				Driver driver = DriverManager.getDriver(props.getProperty("dataSource.jdbcUrl"));
//				DriverManager.deregisterDriver(driver);
//			}
//			dataSource = null;
//		} catch(Exception e) {
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
//				+ "." + rangeGenerationLogsTable + "(LOG_DATE, LOG_DATA) VALUES(NOW(),'"
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
//		
//	}
//	
//	@Override
//	public void writeRanges(Map<Integer, RbStreetSegment> segmentIdMap, int maxApId) {
//		writeRanges(segmentIdMap, maxApId, "");
//	}
//	
//	@Override
//	public void writeRanges(Map<Integer, RbStreetSegment> segmentIdMap, int maxApId, String tableSuffix) {
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
//			PreparedStatement apPstmt = conn
//					.prepareStatement(
//					"INSERT INTO "
//							+ schema
//							+ "."
//							+ accessPointsTable + tableSuffix
//							+ " ("
//							+ "access_point_id, ap_type, site_id, street_segment_id, locality_id, "
//							+ "civic_number_suffix, positional_accuracy, narrative_location, "
//							+ "is_primary_ind, civic_number, access_point_status, retire_date, "
//							+ "last_changed_date, range_type, geometry) "
//							+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
//							+ "st_setSRID(st_geomFromText(?)," + geometryFactory.getSRID()
//							+ "))");
//			int segCount = 0;
//			int apCount = 0;
//			WKTWriter wktw = new WKTWriter();
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
//								apPstmt.setString(15, wktw.write(site.getAccessPoint()));
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