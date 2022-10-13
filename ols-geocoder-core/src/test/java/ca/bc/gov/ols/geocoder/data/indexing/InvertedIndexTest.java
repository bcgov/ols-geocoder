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
import java.util.Collections;
import java.util.Map.Entry;

import ca.bc.gov.ols.geocoder.data.indexing.InvertedIndex.InvertedIndexBuilder;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class InvertedIndexTest {

	@Test
	@Tag("Dev")
	public void testInvertedIndex() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection(
				"jdbc:postgresql://192.168.50.7/bgeo_name_ncap_test",
				"bgeo", "bgeo");
		Statement stmt = conn.createStatement();
		InvertedIndexBuilder<String> builder = new InvertedIndexBuilder<String>();
		// ResultSet rs = stmt.executeQuery("select distinct name_body from bgeo_street_names");
		ResultSet rs = stmt
				.executeQuery("Select site_name from bgeo_sites where site_name is not null");
		int itemCount = 0;
		while(rs.next()) {
			// String name = rs.getString("name_body");
			String name = rs.getString("site_name");
			builder.addItem(GeocoderUtil.wordSplit(name), name);
			itemCount++;
		}
		conn.close();
		InvertedIndex<String> ii = builder.build();
		ArrayList<WeightedWord> allWords = new ArrayList<WeightedWord>();
		int sum = 0;
		for(Entry<String, Integer> entry : ii.wordCounts.entrySet()) {
			String key = entry.getKey();
			Integer docCount = entry.getValue();
			Set<String> items = ii.wordToItemMap.get(key);
			int keyCount;
			if(items == null) {
				keyCount = 0;
			} else {
				keyCount = items.size();
			}
			// System.out.println(key + " : " + docCount + " | " + keyCount);
			allWords.add(new WeightedWord(key + " : " + keyCount, docCount));
			sum += keyCount;
		}
		Collections.sort(allWords);
		for(WeightedWord word : allWords) {
			System.out.println(word);
		}
		System.out.println("addItem() Count: " + itemCount);
		System.out.println("builder.docs.size(): " + builder.docs.size());
		System.out.println("builder.items.size(): " + builder.items.size());
		System.out.println("numItems: " + ii.numItems);
		System.out.println("numKeys: " + allWords.size());
		System.out.println("number of items referenced by keys: " + sum);
		Set<String> result = ii.query(new String[] {"Royal"});
		System.out.println(result.toString());
	}
}
