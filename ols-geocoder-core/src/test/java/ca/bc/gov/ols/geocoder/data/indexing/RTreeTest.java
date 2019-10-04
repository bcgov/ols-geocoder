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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.indexing.IndexableEnvelope;
import ca.bc.gov.ols.geocoder.data.indexing.RTree;
import ca.bc.gov.ols.util.StopWatch;

public class RTreeTest extends TestCase {
	GeometryFactory gf = new GeometryFactory(GeocoderConfig.BASE_PRECISION_MODEL, 3005);

	public void testRTree101() {
		RTree<IndexableEnvelope> rTree = new RTree<IndexableEnvelope>(generateEnvelopeGrid(101));
		//System.out.println(rTree.toString());
	}

	public void testRTree100000() {
		int numEntries = 100001;
		StopWatch sw = new StopWatch();
		ArrayList<IndexableEnvelope> data = generateEnvelopeGrid(numEntries);
		sw.start();
		RTree<IndexableEnvelope> rTree = new RTree<IndexableEnvelope>(data);
		sw.stop();
		System.out.println("Time to build RTree index(ms): " + sw.getElapsedTime());
		sw.start();
		StopWatch sw2 = new StopWatch();
		int badCount = 0;
		for(IndexableEnvelope datum : data) {
			sw2.start();
			List<IndexableEnvelope> result = rTree.search(gf.createPoint(datum.centre()), 5, null);
			assertTrue(result.size() == 5);
			sw2.stop();
			if(sw2.getElapsedTime() > 10) {
				System.out.println(datum.getEnvelope());
				badCount++;
			}
		}
		sw.stop();
		System.out.println("Bads: " + badCount);
		System.out.println("Time to query RTree index " + numEntries + " times (ms): " + sw.getElapsedTime() 
				+ "(" + ((double)sw.getElapsedTime())/numEntries + "ms/query)");
	}

	public void testSTRtree100000() {
		int numEntries = 100000;
		StopWatch sw = new StopWatch();
		ArrayList<IndexableEnvelope> data = generateEnvelopeGrid(numEntries);
		sw.start();
		STRtree tree = new STRtree(data.size());
		for(IndexableEnvelope b : data) {
			tree.insert(b.getEnvelope(), b);
		}
		tree.build();
		sw.stop();
		System.out.println("Time to build STRtree index(ms): " + sw.getElapsedTime());
		sw.start();
		for(IndexableEnvelope datum : data) {
			@SuppressWarnings("rawtypes")
			List result = tree.query(datum.getEnvelope());
			assertTrue(result.size() > 1);
			assertTrue(result.size() < 10);
		}
		sw.stop();
		System.out.println("Time to query STRTree index " + numEntries + " times (ms): " + sw.getElapsedTime() 
				+ "(" + ((double)sw.getElapsedTime())/numEntries + "ms/query)");
	}

	private ArrayList<IndexableEnvelope> generateEnvelopeGrid(int num) {
		ArrayList<IndexableEnvelope> data = new ArrayList<IndexableEnvelope>(num);
		
		int sqRt = (int)Math.round(Math.ceil(Math.sqrt(num)));
		for(int i = 0; i < num; i++) {
			int x = i % sqRt;
			int y = i / sqRt;
			IndexableEnvelope env = new IndexableEnvelope();
			env.init(x, x+1, y, y+1);
			data.add(env);
		}
		return data;
	}
}
