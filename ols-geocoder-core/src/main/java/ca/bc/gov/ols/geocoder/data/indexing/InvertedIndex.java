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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvertedIndex<T> {
	
	private static final double MIN_REQUIRED_WEIGHT = 0.6;
	int numItems;
	Map<String, Set<T>> wordToItemMap;
	Map<String, Integer> wordCounts;
	
	private InvertedIndex(InvertedIndexBuilder<T> builder) {
		numItems = builder.docs.size();
		wordCounts = builder.wordCounts;
		wordToItemMap = new THashMap<String, Set<T>>();
		for(int docIdx = 0; docIdx < numItems; docIdx++) {
			for(String word : getImportantWords(builder.docs.get(docIdx))) {
				Set<T> items = wordToItemMap.get(word);
				if(items == null) {
					items = new THashSet<T>();
					wordToItemMap.put(word, items);
				}
				items.add(builder.items.get(docIdx));
			}
		}
	}
	
	private List<String> getImportantWords(String[] doc) {
		int[] counts = new int[doc.length];
		for(int i = 0; i < doc.length; i++) {
			Integer count = wordCounts.get(doc[i]);
			if(count == null) {
				counts[i] = 0;
			} else {
				counts[i] = count;
			}
		}
		double idf[] = new double[doc.length];
		WeightedWord[] words = new WeightedWord[doc.length];
		double totalIDF = 0;
		// calculate IDF for each word and the total
		for(int wordIdx = 0; wordIdx < doc.length; wordIdx++) {
			idf[wordIdx] = counts[wordIdx] == 0 ? 0 : Math.log((double)(numItems + 1)
					/ (double)counts[wordIdx]);
			totalIDF += idf[wordIdx];
		}
		if(totalIDF == 0) {
			// none of the words provided exist in any doc
			return Collections.emptyList();
		}
		// calculate relative weight for each word
		for(int wordIdx = 0; wordIdx < doc.length; wordIdx++) {
			words[wordIdx] = new WeightedWord(doc[wordIdx], idf[wordIdx] / totalIDF);
		}
		// the higher relative weighted words are important
		double totalWeight = 0;
		Arrays.sort(words);
		int wordIdx = doc.length - 1;
		List<String> importantWords = new ArrayList<String>(doc.length);
		while(totalWeight < MIN_REQUIRED_WEIGHT) {
			importantWords.add(words[wordIdx].getWord());
			totalWeight += words[wordIdx].getWeight();
			wordIdx--;
		}
		return importantWords;
	}
	
	public Set<T> query(String[] queryDoc) {
		List<String> importantWords = getImportantWords(queryDoc);
		Set<T> matches = new THashSet<T>();
		for(String word : importantWords) {
			Set<T> items = wordToItemMap.get(word);
			if(items != null) {
				matches.addAll(items);
			}
		}
		return matches;
	}
	
	public static class InvertedIndexBuilder<T> {
		List<String[]> docs;
		List<T> items;
		Map<String, Integer> wordCounts;
		
		public InvertedIndexBuilder() {
			docs = new ArrayList<String[]>();
			items = new ArrayList<T>();
			wordCounts = new THashMap<String, Integer>();
		}
		
		public void addItem(String[] document, T item) {
			docs.add(document);
			items.add(item);
			for(int i = 0; i < document.length; i++) {
				document[i] = document[i].toUpperCase();
				Integer count = wordCounts.get(document[i]);
				if(count == null) {
					wordCounts.put(document[i], 1);
				} else {
					wordCounts.put(document[i], count + 1);
				}
			}
		}
		
		public InvertedIndex<T> build() {
			return new InvertedIndex<T>(this);
		}
		
	}
}

class WeightedWord implements Comparable<WeightedWord> {
	private final String word;
	private final double weight;
	private static final DecimalFormat DF = new DecimalFormat("0.###");
	
	public WeightedWord(String word, double weight) {
		this.word = word;
		this.weight = weight;
	}
	
	public String getWord() {
		return word;
	}
	
	public double getWeight() {
		return weight;
	}
	
	@Override
	public int compareTo(WeightedWord otherWord) {
		if(weight == otherWord.weight) {
			return word.compareTo(otherWord.word);
		} else if(weight > otherWord.weight) {
			return 1;
		}
		return -1;
	}
	
	@Override
	public String toString() {
		return word + "/" + DF.format(weight);
	}
}