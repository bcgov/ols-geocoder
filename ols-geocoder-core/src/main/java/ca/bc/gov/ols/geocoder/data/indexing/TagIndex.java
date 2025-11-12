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

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagIndex<T> {
	
	Map<String,TagIndexEntry> index;
	
	/**
	 * Creates a new tag index.
	 */
	public TagIndex() {
		index = new THashMap<String,TagIndexEntry>();
	}
	
	/**
	 * Adds the item to the index with the specified tags. String duplication is minimized
	 * as individual tag strings are effectively interned, and references to the persistent
	 * tags strings are returned for external reference. 
	 * @param item item to be added to the index
	 * @param tags semicolon separated list of tag strings by which the item should indexed
	 * @return a list of the tag strings used, to be stored in the item as a reference
	 */
	public List<String> add(T item, String tags) {
		if(tags == null || tags.isEmpty()) {
			return Collections.emptyList();
		}
		String[] tagArray = tags.toLowerCase().split(";");
		// return a list of the tag strings, re-using the string used in the index as a form of interning
		List<String> tagList = new ArrayList<String>(tagArray.length);
		for(String tag : tagArray) {
			tag = tag.trim();
			TagIndexEntry tie = index.get(tag);
			if(tie == null) {
				tie = new TagIndexEntry(tag);
				index.put(tie.getTag(), tie);
			}
			tie.addItem(item);
			tagList.add(tie.tag);
		}
		return tagList;
	}
	
	/**
	 * Queries the tag index for items that match all of the tags listed in the tags parameter.
	 * @param tags semicolon separated list of tag strings 
	 * @return a set of items matching all the specified tags
	 */
	public Set<T> query(String tags) {
		if(tags == null || tags.isEmpty()) {
			return Collections.emptySet();
		}
		String[] tagArray = tags.toLowerCase().split(";");
		Set<T> results = null;
		for(String tag : tagArray) {
			TagIndexEntry tie = index.get(tag);
			if(tie == null) {
				return Collections.emptySet();
			}
			if(results == null) {
				results = new THashSet<T>(tie.items);
			} else {
				results.retainAll(tie.items);
			}
		}
		if(results == null) {
			return Collections.emptySet();
		}
		return results;
	}
	
	public Set<String> getTags() {
		return Collections.unmodifiableSet(index.keySet());
	}
	
	class TagIndexEntry {
		final String tag;
		Set<T> items;

		TagIndexEntry(String tag) {
			this.tag = tag;
			this.items = new THashSet<T>();
		}
		
		String getTag() {
			return tag;
		}
		
		Set<T> getItems() {
			return items;
		}
		
		void addItem(T item) {
			items.add(item);
		}
	}
}
