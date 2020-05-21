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

import ca.bc.gov.ols.geocoder.data.BlockFace;
import ca.bc.gov.ols.geocoder.data.enumTypes.Side;
import ca.bc.gov.ols.geocoder.data.indexing.BlockFaceIntervalTree;
import ca.bc.gov.ols.util.StopWatch;
import junit.framework.TestCase;

public class BlockFaceIntervalTreeTest extends TestCase {
	
	private static final boolean OUTPUT_TIME = true;
	
	public void testContinuous() {
		List<BlockFace> faces = new ArrayList<BlockFace>();
		faces.add(new BlockFace(null, Side.RIGHT, 1, 9, "C", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 10, 19, "C", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 20, 29, "C", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 30, 39, "C", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 40, 49, "C", 0, null, null, null));
		BlockFaceIntervalTree bfit = new BlockFaceIntervalTree(faces);
		testQuery(bfit, 5, 1);
		testQuery(bfit, 100, 0);
	}
	
	public void testEmptyMiddle() {
		List<BlockFace> faces = new ArrayList<BlockFace>();
		faces.add(new BlockFace(null, Side.RIGHT, 1, 9, "C", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 10, 19, "C", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 30, 39, "C", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 40, 49, "C", 0, null, null, null));
		BlockFaceIntervalTree bfit = new BlockFaceIntervalTree(faces);
		testQuery(bfit, 5, 1);
		testQuery(bfit, 21, 0);
		testQuery(bfit, 100, 0);
	}
	
	public void testParity() {
		List<BlockFace> faces = new ArrayList<BlockFace>();
		faces.add(new BlockFace(null, Side.RIGHT, 1, 9, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 2, 8, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 11, 19, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 10, 18, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 21, 29, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 20, 28, "E", 0, null, null, null));
		BlockFaceIntervalTree bfit = new BlockFaceIntervalTree(faces);
		testQuery(bfit, 5, 1);
		testQuery(bfit, 100, 0);
	}
	
	public void testPartialOverlaps() {
		List<BlockFace> faces = new ArrayList<BlockFace>();
		faces.add(new BlockFace(null, Side.RIGHT, 1, 9, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 2, 8, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 11, 19, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 10, 18, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 21, 29, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 20, 28, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 1, 19, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 2, 18, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 21, 29, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 20, 28, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 1, 99, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 2, 98, "E", 0, null, null, null));
		BlockFaceIntervalTree bfit = new BlockFaceIntervalTree(faces);
		testQuery(bfit, 5, 3);
		testQuery(bfit, 91, 1);
		testQuery(bfit, 100, 0);
	}
	
	private void testQuery(BlockFaceIntervalTree bfit, int query, int expectedResults) {
		StopWatch sw = new StopWatch();
		sw.start();
		List<BlockFace> results = bfit.query(query);
		sw.stop();
		if(OUTPUT_TIME) {
			System.out.println("time: " + sw.getElapsedTime() + "ms");
		}
		assertEquals(expectedResults, results.size());
	}
	
	public void testIterator() {
		List<BlockFace> faces = new ArrayList<BlockFace>();
		faces.add(new BlockFace(null, Side.RIGHT, 1, 9, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 2, 8, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 11, 19, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 10, 18, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 21, 29, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 20, 28, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 1, 19, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 2, 18, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 21, 29, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 20, 28, "E", 0, null, null, null));
		faces.add(new BlockFace(null, Side.RIGHT, 1, 99, "O", 0, null, null, null));
		faces.add(new BlockFace(null, Side.LEFT, 2, 98, "E", 0, null, null, null));
		BlockFaceIntervalTree bfit = new BlockFaceIntervalTree(faces);
		int count = 0;
		for(BlockFace face : bfit) {
			System.out.println(face);
			count++;
		}
		assertEquals(faces.size(), count);
	}
}
