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

import java.util.Arrays;
import java.util.List;

import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.NameLookupTrie;
import ca.bc.gov.ols.util.StopWatch;
import junit.framework.TestCase;

public class NameLookupTrieTest extends TestCase {
	
	private static final boolean OUTPUT_TIME = true;
	
	public void testWithoutError() {
		List<String> strings = Arrays.asList(new String[] {
				"fu", "bar", "fubar"
		});
		NameLookupTrie<String> trie = new NameLookupTrie<String>(strings);
		testQuery(trie, "fu", 0, new String[] {"fu"});
		testQuery(trie, "fubar", 0, new String[] {"fubar"});
		testQuery(trie, "nothing", 0, new String[] {});
	}
	
	public void testWithError() {
		List<String> strings = Arrays.asList(new String[] {
				"fu", "bar", "fubar"
		});
		NameLookupTrie<String> trie = new NameLookupTrie<String>(strings);
		testQuery(trie, "fu", 2, new String[] {"fu"});
		testQuery(trie, "bar", 2, new String[] {"bar", "fubar"});
		testQuery(trie, "fubar", 2, new String[] {"bar", "fubar"});
	}
	
	public void testTransposition() {
		List<String> strings = Arrays.asList(new String[] {
				"cat", "bat", "bar", "scat", "car", "cars", "card", "act", "actor"
		});
		NameLookupTrie<String> trie = new NameLookupTrie<String>(strings);
		//testQuery(trie, "act", 1, new String[] {"cat", "act"});
		testQuery(trie, "act", 2, new String[] {"cat", "bat", "scat", "car", "act", "actor"});
		//testQuery(trie, "nothing", 2, new String[] {});
	}
	
	public void testRealNames() {
		List<String> strings = Arrays.asList(new String[] {
				"tamarack", "tamarac", "tamany", "tamarack lake", "tamarind", "tamarisk",
				"tambellini", "tamihi", "tammy", "tanager", "tampico", "tan", "taneda",
				"taneeyah", "tanemura", "tanger", "tanglewood"
		});
		NameLookupTrie<String> trie = new NameLookupTrie<String>(strings);
		testQuery(trie, "tamarack", 2, new String[] {"tamarack", "tamarac", "tamarisk"});
	}

	@SuppressWarnings("unchecked")
	public void testQueryExactWordPrefix() {
		List<String> strings = Arrays.asList(new String[] {
				"black", "black bear", "black creek" 
		});
		NameLookupTrie<String> trie = new NameLookupTrie<String>(strings);
		String query = "black";
		StopWatch sw = new StopWatch();
		sw.start();
		@SuppressWarnings("rawtypes")
		List results = trie.queryExactWordPrefix(query);
		sw.stop();
		if(OUTPUT_TIME) {
			System.out.println("time for queryExactWordPrefix '" + query + "': " + sw.getElapsedTime() + "ms");
		}
		compareResults(results, new String[] {"black", "black bear", "black creek"});
	}
	
	public void testAutoComplete() {
		List<String> strings = Arrays.asList(new String[] {
				"black", "black bear", "black creek", "blackness"
		});
		NameLookupTrie<String> trie = new NameLookupTrie<String>(strings);
		
		// with autocomplete
		//testQuery(trie, "black", 2, new String[] {"black", "black bear", "black creek"});
		//testQuery(trie, "bla", 2, new String[] {"black", "black bear", "black creek"});
		
		// without autocomplete
		testQuery(trie, "bl", 2, new String[] {});
		testQuery(trie, "bla", 2, new String[] {"black"});
		testQuery(trie, "black creek", 2, new String[] {"black creek"});
		testQuery(trie, "blackacreek", 2, new String[] {"black creek"});
	}

	@SuppressWarnings("unchecked")
	private void testQuery(NameLookupTrie<String> trie, String query, int error,
			String[] expectedResults) {
		StopWatch sw = new StopWatch();
		sw.start();
		@SuppressWarnings("rawtypes")
		List results = trie.query(query, error);
		sw.stop();
		if(OUTPUT_TIME) {
			System.out.println("time for query '" + query + "' with error " + error
					+ ": " + sw.getElapsedTime() + "ms");
		}
		compareResults(results, expectedResults);
	}

	@SuppressWarnings("unchecked")
	private void compareResults(List<Object> actualResults, String[] expectedResults) {
		for(String e : expectedResults) {
			boolean found = false;
			for(Object result : actualResults) {
				String str = null;
				if(result instanceof MisspellingOf<?>) {
					str = (((MisspellingOf<String>)result).get());
				} else {
					str = result.toString();
				}
				if(str.equals(e)) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
		// TODO: This is no longer always true since we added misspellings, 
		// because previously the strings were returned as a set, preventing duplicates
		// now it is possible for duplicate strings to be added through different interpretations
		// of the errors - eg. a transposition could be just two errors in a row
		// the amount of different interpretations grows with the number of allowed errors
		assertEquals(expectedResults.length, actualResults.size());
	}

}
