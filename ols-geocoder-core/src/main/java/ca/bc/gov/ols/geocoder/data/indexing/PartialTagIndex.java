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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class PartialTagIndex {
	
	TreeMap<String, ArrayList<String>> data;
	
	public PartialTagIndex(Set<String> tags) {
		data = new TreeMap<String, ArrayList<String>>();
		for(String tag : tags) {
			String[] words = tag.split("\\s+");
			for(String word : words) {
				ArrayList<String> entries = data.get(word);
				if(entries == null) {
					entries = new ArrayList<String>();
					data.put(word,entries);
				}
				if(!entries.contains(tag)) {
					entries.add(tag);
				}
			}
		}
	}
	
	public List<String> lookup(String partialTag, int maxResults) {
		Set<String> tags = new HashSet<String>();
		SortedMap<String, ArrayList<String>> tail = data.tailMap(partialTag);
		for(Map.Entry<String,ArrayList<String>> entry : tail.entrySet()) {
			if(entry.getKey().startsWith(partialTag)) {
				tags.addAll(entry.getValue());
			}
			if(maxResults != -1 && tags.size() >= maxResults) {
				break;
			}
		}
		List<String> prefixTags = new ArrayList<String>(tags.size());
		List<String> otherTags = new ArrayList<String>(tags.size());
		for(String tag : tags) {
			if(tag.startsWith(partialTag)) {
				prefixTags.add(tag);
			} else {
				otherTags.add(tag);
			}
		}
		Collections.sort(prefixTags);
		
		// if there is room for more
		if(maxResults == -1 || prefixTags.size() < maxResults) {
			// sort and add the right amount
			Collections.sort(otherTags);
			if(maxResults != -1 && maxResults - prefixTags.size() < otherTags.size() ) {
				otherTags = otherTags.subList(0, maxResults - prefixTags.size());
			}
			prefixTags.addAll(otherTags);
		}
		return prefixTags;
	}
}

//class TagWordEntry implements Comparable<TagWordEntry>{
//	String word;
//	ArrayList<String> tags;
//
//	public TagWordEntry(String word) {
//		this.word = word;
//		this.tags = new ArrayList<String>();
//	}
//	
//	@Override
//	public int compareTo(TagWordEntry other) {
//		return word.compareTo(other.word);
//	}
//	
//	public void trimToSize() {
//		tags.trimToSize();
//	}
//}
