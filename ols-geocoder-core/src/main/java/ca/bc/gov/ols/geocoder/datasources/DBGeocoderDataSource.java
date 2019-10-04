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


import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public interface DBGeocoderDataSource extends GeocoderDataSource {
	
	Connection getConnection();
	
	void releaseConnection(Connection conn);
	
	Point pointFromResultSet(ResultSet rs, String columnPrefix) throws SQLException;
	
	LineString lineStringFromResultSet(ResultSet rs, String column) throws SQLException;
	
}
