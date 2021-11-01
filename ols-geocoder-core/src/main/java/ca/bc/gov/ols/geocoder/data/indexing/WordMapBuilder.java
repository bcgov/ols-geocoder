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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.bc.gov.ols.geocoder.dra.DraLexicalRules;

public class WordMapBuilder {
	
	/* used to store the mappings used to eventually build the overall wordMap trie */
	private Map<String, Set<Word>> wordMap;
	
	private Map<String, Word> wordList;
	
	public static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("\\s+");
	
	/**
	 * Initializes the WordMap builder to start accepting words, mappings, and classifications.
	 * After all such details have been added, call build to get the final WordMap structure.
	 */
	public WordMapBuilder() {
		wordMap = new THashMap<String, Set<Word>>();
		wordList = new THashMap<String, Word>();
		// addPhrase("BRITISH COLUMBIA", WordClass.STATE_PROV_TERR);
		// addPhrase("BC", WordClass.STATE_PROV_TERR);
		// addPhrase("CB", WordClass.STATE_PROV_TERR);
		addWord("AND", WordClass.AND);
		addWord("FLR", WordClass.FLOOR);
		addWord(DraLexicalRules.POSTAL_ADDRESS_ELEMENT, WordClass.POSTAL_ADDRESS_ELEMENT);
		addWord(DraLexicalRules.FRONT_GATE, WordClass.FRONT_GATE);
		addWord(DraLexicalRules.OCCUPANT_SEPARATOR, WordClass.OCCUPANT_SEPARATOR);
//		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//		for(int i = 0; i < alphabet.length(); i++) {
//			addWord(alphabet.substring(i, i + 1), WordClass.SUFFIX);
//		}
//		addWord("1/2", WordClass.SUFFIX);
	}
	
	/**
	 * Adds an phrase of unmapped words with the specified classification.
	 * 
	 * @param text string representation of the canonical forms of the words, separated by spaces
	 * @param wordClass classification associated with the words
	 */
	public void addPhrase(String text, WordClass wordClass) {
		text = text.toUpperCase();
		Matcher m = WORD_SPLIT_PATTERN.matcher(text);
		int last_match = 0;
		while(m.find()) {
			addWord(text.substring(last_match, m.start()), wordClass);
			last_match = m.end();
		}
		addWord(text.substring(last_match), wordClass);
	}
	
	/**
	 * Adds a mapping from one word to another.
	 * 
	 * @param fromWord the word to map from
	 * @param toWord the word to map to
	 */
	public void addWordMapping(String fromWord, String toWord) {
		fromWord = fromWord.toUpperCase();
		toWord = toWord.toUpperCase();
		Word word = getWord(toWord);
		addToWordMap(fromWord, word);
		addToWordMap(toWord, word);
	}
	
	/**
	 * Adds a word with the specified classification.
	 * 
	 * @param wordStr string representation of the canonical form of the word
	 * @param wordClass classification associated with the word
	 */
	public void addWord(String wordStr, WordClass wordClass) {
		wordStr = wordStr.toUpperCase();
		Word word = getWord(wordStr);
		word.addClass(wordClass);
		addToWordMap(wordStr, word);
	}
	
	/**
	 * Gets a Word from the list or makes a new one and adds it to the list. All Words in the
	 * wordMap must also be in the list to prevent duplication, and allow for overlaps in
	 * classifications.
	 * 
	 * @param wordStr a string representation of a word being added to the wordMap
	 * @return a Word representation which is in the wordList
	 */
	private Word getWord(String wordStr) {
		Word word = wordList.get(wordStr);
		if(word == null) {
			word = new Word(wordStr);
			wordList.put(wordStr, word);
		}
		return word;
	}
	
	/**
	 * Adds a word to the map. If the fromWord already exists, adds the new word to the set of
	 * mappings, otherwise adds a new mapping from the formWord to a set containing only the toWord.
	 * 
	 * @param fromWord a String representing a possible user input
	 * @param toWord a Word representing the canonical form and its possible classifications
	 */
	private void addToWordMap(String fromWord, Word toWord) {
		Set<Word> wordSet = wordMap.get(fromWord);
		if(wordSet == null) {
			wordSet = new CopyOnWriteArraySet<Word>();
			wordMap.put(fromWord, wordSet);
		} 
		wordSet.add(toWord);
	}
	
	
	/**
	 * Gets the wordMap structure. Intended to be used once after all words are added, to pass into the WordMap constructor
	 * @return the wordMap structure based on the words added to the map
	 */
	public Map<String, Set<Word>> getWordMap() {
		return wordMap;
	}

	/**
	 * Gets the wordList structure. Intended to be used once after all words are added, to pass into the WordMap constructor
	 * @return the wordList structure based on the words added to the map
	 */
	public Map<String, Word> getWordList() {
		return wordList;
	}

	public static void main(String[] args) {
		WordMapBuilder db = new WordMapBuilder();
		db.addPhrase("Saint Andrews", WordClass.STREET_NAME_BODY);
		db.addPhrase("Andrews Landing", WordClass.LOCALITY_NAME);
		db.addWordMapping("Saint", "st");
		db.addWordMapping("Street", "st");
		db.addWord("St", WordClass.STREET_TYPE);
		db.addWord("St", WordClass.STREET_NAME_BODY);
		System.out.println(db.wordMap.toString());
	}
	
}