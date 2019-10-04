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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

/**
 * This class is used by the JSP files to get a connection to the database as specified by the
 * configuration properties file.
 * 
 * @author chodgson
 * 
 */
public class GeocoderDBConnectionFactory {
	final static Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ GeocoderDBConnectionFactory.class.getCanonicalName());
	
	public static enum ConnectionType {
		POSTGRESQL, ORACLE
	};
	
	private static Connection conn = null;
	private static Connection adminConn = null;
	private static ConnectionType type = null;
	
	public static Connection getConnection() {
		try {
			if(conn == null || conn.isClosed()) {
				Properties props = GeocoderFactory.getBootstrapConfigFromEnvironment();
				String jdbcUrl = props.getProperty("dataSource.jdbcUrl");
				if(jdbcUrl != null) {
					if(jdbcUrl.startsWith("jdbc:oracle:thin:")) {
						type = ConnectionType.ORACLE;
						Class.forName("oracle.jdbc.OracleDriver");
					} else if(jdbcUrl.startsWith("jdbc:postgresql:")) {
						type = ConnectionType.POSTGRESQL;
						Class.forName("org.postgresql.Driver");
					} else {
						throw new RuntimeException("Invalid JDBC URL in properties file: "
								+ jdbcUrl);
					}
					// logger.debug("Connection Type:" + type.toString());
					conn = DriverManager.getConnection(jdbcUrl,
							props.getProperty("dataSource.username"),
							props.getProperty("dataSource.password"));
				} else {
					logger.error("No JDBC URL found in properties file.");
					throw new RuntimeException("No JDBC URL found in properties file.");
				}
			}
			return conn;
		} catch(ClassNotFoundException cnfe) {
			throw new RuntimeException("Exception loading database driver", cnfe);
		} catch(SQLException sqle) {
			throw new RuntimeException("Error connecting to database", sqle);
		}
	}
	
	public static Connection getAdminConnection() {
		try {
			if(adminConn == null || adminConn.isClosed()) {
				Properties props = GeocoderFactory.getBootstrapConfigFromEnvironment();
				String jdbcUrl = props.getProperty("dataSource.jdbcUrl");
				if(jdbcUrl != null) {
					if(jdbcUrl.startsWith("jdbc:oracle:thin:")) {
						type = ConnectionType.ORACLE;
						Class.forName("oracle.jdbc.OracleDriver");
					} else if(jdbcUrl.startsWith("jdbc:postgresql:")) {
						type = ConnectionType.POSTGRESQL;
						Class.forName("org.postgresql.Driver");
					} else {
						logger.error("Invalid JDBC URL in properties file: " + jdbcUrl);
						throw new RuntimeException("Invalid JDBC URL in properties file: "
								+ jdbcUrl);
					}
					// logger.debug("connecting to DB:" + jdbcUrl + "  Usr:" + username );
					String adminUsername = props.getProperty("dataSourceAdmin.username",
							props.getProperty("dataSource.username"));
					String adminPassword = props.getProperty("dataSourceAdmin.password",
							props.getProperty("dataSource.password"));
					adminConn = DriverManager.getConnection(jdbcUrl, adminUsername,
							adminPassword);
				} else {
					throw new RuntimeException("No JDBC URL found in properties file.");
				}
			}
			return adminConn;
		} catch(ClassNotFoundException cnfe) {
			throw new RuntimeException("Exception loading database driver", cnfe);
		} catch(SQLException sqle) {
			throw new RuntimeException("Error connecting to database", sqle);
		}
	}
	
	public static ConnectionType getConnectionType() {
		return type;
	}
	
}
