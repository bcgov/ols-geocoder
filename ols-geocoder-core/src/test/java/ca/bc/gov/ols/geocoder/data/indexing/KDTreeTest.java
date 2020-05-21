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
package ca.bc.gov.ols.geocoder.data.indexing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.ILocation;
import ca.bc.gov.ols.geocoder.data.indexing.KDTree;
import ca.bc.gov.ols.util.StopWatch;

public class KDTreeTest extends TestCase {
	
	public void testKDTree() throws SQLException, ClassNotFoundException {
		GeometryFactory gf = new GeometryFactory(GeocoderConfig.BASE_PRECISION_MODEL,
				3005);
		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection(
				"jdbc:postgresql://192.168.50.5/bgeo_load_test", "bgeo", "bgeo");
		Statement stmt = conn.createStatement();
		StopWatch sw = new StopWatch();
		sw.start();
		ResultSet rs = stmt
				.executeQuery("Select st_x(geometry) as x, st_y(geometry) as y from bgeo_sites");
		ArrayList<PointProxy> items = new ArrayList<PointProxy>();
		ArrayList<PointProxy> items2 = new ArrayList<PointProxy>();
		int nextId = 1;
		while(rs.next()) {
			Double x = rs.getDouble("x");
			Double y = rs.getDouble("y");
			PointProxy p = new PointProxy(nextId++, gf.createPoint(new Coordinate(x, y)));
			items.add(p);
			items2.add(p);
		}
		conn.close();
		sw.stop();
		System.out.println(items.size() + " site points loaded in " + sw.getElapsedTime() + "ms");
		System.out
				.println("Memory in use after loading(Megs): "
						+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000));
		sw.start();
		KDTree<PointProxy> tree = new KDTree<PointProxy>(items);
		sw.stop();
		System.out.println("KDTree built in " + sw.getElapsedTime() + "ms");
		System.out
				.println("Memory in use after Building KDTree(Megs): "
						+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000));
		
		sw.start();
		int tests = 10000;
		for(int i = 0; i < tests; i++) {
			List<PointProxy> results = tree.search(items.get(i).getLocation(), 10, null);
			if(!results.get(0).getLocation().equals(items.get(i).getLocation())) {
				System.out.println("ERROR!");
				System.out.println(" query: " + items.get(i));
				System.out.println(" results: " + results);
			}
		}
		sw.stop();
		System.out.println("KDTree searched " + tests + " times in "
				+ sw.getElapsedTime() + "ms ("
				+ (double)sw.getElapsedTime() / (double)tests + "ms/query)");
		/*
		 * Iterable<PrioNode<PointProxy>> results = tree.search(items.get(0).getLocation(), 10, -1);
		 * 
		 * for(PrioNode<PointProxy> result : results) { System.out.println("Point: " +
		 * result.item.getLocation() + " priority: " + result.priority + " dist: " +
		 * Math.sqrt(result.priority)); }
		 */
	}
}

class PointProxy implements ILocation {
	int id;
	Point p;
	
	public PointProxy(int id, Point p) {
		this.id = id;
		this.p = p;
	}
	
	@Override
	public Point getLocation() {
		return p;
	}
	
	@Override
	public String toString() {
		return "" + id + " (" + p + ")";
	}
}
