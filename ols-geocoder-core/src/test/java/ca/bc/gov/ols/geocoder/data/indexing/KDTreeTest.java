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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.ILocation;
import ca.bc.gov.ols.util.StopWatch;
import junit.framework.TestCase;

public class KDTreeTest extends TestCase {
	
	private static final int NUMBER_OF_POINTS = 12000000;
	private static final int NUMBER_OF_TESTS = 40000;
	
	public void testKDTree() throws SQLException, ClassNotFoundException {
		GeometryFactory gf = new GeometryFactory(GeocoderConfig.BASE_PRECISION_MODEL, 3005);
		StopWatch sw = new StopWatch();
		sw.start();
		ArrayList<PointProxy> items = new ArrayList<PointProxy>();
		//ArrayList<PointProxy> items2 = new ArrayList<PointProxy>();
		for(int i = 1; i <= NUMBER_OF_POINTS; i++) {
			PointProxy p = new PointProxy(i, gf.createPoint(new Coordinate(Math.random()*1000000, Math.random()*100000)));
			items.add(p);
			//items2.add(p);
		}
		sw.stop();
		System.out.println(items.size() + " random points generated in " + sw.getElapsedTime() + "ms");
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
		for(int i = 0; i < NUMBER_OF_TESTS; i++) {
			List<PointProxy> results = tree.search(items.get(i).getLocation(), 10, null);
			if(!results.get(0).getLocation().equals(items.get(i).getLocation())) {
				System.out.println("ERROR!");
				System.out.println(" query: " + items.get(i));
				System.out.println(" results: " + results);
			}
		}
		sw.stop();
		System.out.println("KDTree searched " + NUMBER_OF_TESTS + " times in "
				+ sw.getElapsedTime() + "ms ("
				+ (double)sw.getElapsedTime() / (double)NUMBER_OF_TESTS + "ms/query)");
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
