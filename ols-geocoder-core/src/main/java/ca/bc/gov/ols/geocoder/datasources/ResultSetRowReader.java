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
import java.time.LocalDate;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.rowreader.RowReader;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

public class ResultSetRowReader implements RowReader {
	
	protected DBGeocoderDataSource ds;
	protected Connection conn;
	protected ResultSet rs;
	
	public ResultSetRowReader(DBGeocoderDataSource ds, Connection conn, ResultSet rs) {
		this.ds = ds;
		this.conn = conn;
		this.rs = rs;
	}
	
	@Override
	public boolean next() {
		try {
			return rs.next();
		} catch(SQLException sqle) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException(sqle);
		}
	}
	
	@Override
	public Object getObject(String column) {
		try {
			return rs.getObject(column);
		} catch(SQLException sqle) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException(sqle);
		}
	}
	
	@Override
	public int getInt(String column) {
		Integer i = getInteger(column);
		if(i == null) {
			return NULL_INT_VALUE; 
		}
		return i;
	}
	
	@Override
	public Integer getInteger(String column) {
		try {
			return (Integer)rs.getObject(column);
		} catch(SQLException sqle) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException(sqle);
		}
	}
	
	@Override
	public double getDouble(String column) {
		try {
			return rs.getDouble(column);
		} catch(SQLException sqle) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException(sqle);
		}
	}
	
	@Override
	public String getString(String column) {
		try {
			return rs.getString(column);
		} catch(SQLException sqle) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException(sqle);
		}
	}
	
	@Override
	public Boolean getBoolean(String column) {
		try {
			return rs.getBoolean(column);
		} catch(SQLException sqle) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException(sqle);
		}
	}
	
	@Override
	public LocalDate getDate(String column) {
		try {
			return rs.getObject(column, LocalDate.class);
		} catch(SQLException sqle) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException(sqle);
		}
	}
	
	@Override
	public Point getPoint() {
		try {
			double x = rs.getDouble("x");
			double y = rs.getDouble("y");
			return GeocoderDataStore.getGeometryFactory().createPoint(new Coordinate(x, y));
		} catch(SQLException sqle) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException(sqle);
		}
	}
	
	@Override
	public Point getPoint(String column) {
		return (Point)getGeometry(column);
	}
	
	@Override
	public LineString getLineString() {
		return (LineString)getGeometry("geom");
	}
	
	@Override
	public LineString getLineString(String column) {
		return (LineString)getGeometry(column);
	}
	
	@Override
	public Polygon getPolygon() {
		return (Polygon)getGeometry("geom");
	}
	
	@Override
	public Polygon getPolygon(String column) {
		return (Polygon)getGeometry(column);
	}
	
	@Override
	public Geometry getGeometry() {
		return getGeometry("geom");
	}
	
	@Override
	public Geometry getGeometry(String column) {
		try {
			WKBReader reader = new WKBReader(GeocoderDataStore.getGeometryFactory());
			Geometry geom = reader.read(rs.getBytes(column));
			return geom;
		} catch(SQLException sqle) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException(sqle);
		} catch(ParseException pe) {
			try {
				rs.getStatement().close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} finally {
				ds.releaseConnection(conn);
			}
			throw new RuntimeException("ParseException while parsing geometry WKB", pe);
		}
	}
	
	@Override
	public void close() {
		try {
			rs.getStatement().close();
		} catch(SQLException sqle) {
			try {
				rs.close();
			} catch(SQLException e) {
				// we are already handling a exception, so ignore this one
			} 
			throw new RuntimeException(sqle);
		} finally {
			ds.releaseConnection(conn);
		}
	}
	
}
