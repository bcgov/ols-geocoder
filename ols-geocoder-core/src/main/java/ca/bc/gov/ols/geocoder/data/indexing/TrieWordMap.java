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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.bc.gov.ols.geocoder.dra.DraLexicalRules;

public class TrieWordMap implements WordMap {
	private static final int MAX_ALLOWABLE_SPELLING_DISTANCE = 1;
	private NameLookupTrie<Word> trie;
	private Pattern numberPattern;
	
	public TrieWordMap(Map<String, Set<Word>> map) {
		this.trie = new NameLookupTrie<Word>(map);
		numberPattern = Pattern.compile(DraLexicalRules.RE_NUMBER);
	}

	
	// unused
	// public List<List<Word>> mapWords(String text, int error) {
	// text = text.toUpperCase();
	// Matcher m = WordMapBuilder.WORD_SPLIT_PATTERN.matcher(text);
	// List<List<Word>> words = new
	// ArrayList<List<Word>>();
	// int last_match = 0;
	// while(m.find()) {
	// words.add(mapWord(text.substring(last_match, m.start()), error));
	// last_match = m.end();
	// }
	// words.add(mapWord(text.substring(last_match), error));
	// return words;
	// }
	
	@Override
	public List<MisspellingOf<Word>> mapWord(String fromWord, boolean allowMisspellings) {
		return mapWord(fromWord, false);
	}
	
	@Override
	public List<MisspellingOf<Word>> mapWord(String fromWord, boolean allowMisspellings, boolean autoComplete) {
		//String withOrdinal = fromWord;
		//fromWord = GeocoderUtil.removeOrdinal(fromWord);

		int error = 0;
		if(allowMisspellings) {
			error = MAX_ALLOWABLE_SPELLING_DISTANCE;
			// no errors allowed for numbers
			Matcher m = numberPattern.matcher(fromWord);
			int len = fromWord.length();
			if(len < 4 || m.matches()) {
				error = 0;
			} else if(len > 8) {
				error = 3;
			} else if(len > 5) {
				error = 2;
			}
		}
		List<MisspellingOf<Word>> words = trie.query(fromWord, error, autoComplete);
		// don't consider misspellings of words that have too many
		// spell-correction possibilities and are spelled correctly as-is
		// if(words.size() > 5 && words.get(0).getError() == 0) {
		// List<MisspellingOf<Word>> newWords = new ArrayList<MisspellingOf<Word>>();
		// newWords.add(words.get(0));
		// words = newWords;
		// }

		//TODO this is where classification needs to be redone
		
		Word word = classifyWord(fromWord);		

		// for allowing matches to site names that we don't know about (jTrac-559)
		if(words.size() == 0 || words.get(0).getError() != 0
				|| (!words.get(0).get().inClass(WordClass.NAME)
						&& !words.get(0).get().inClass(WordClass.FRONT_GATE)
						&& !words.get(0).get().inClass(WordClass.OCCUPANT_SEPARATOR)
						&& !words.get(0).get().inClass(WordClass.POSTAL_ADDRESS_ELEMENT))) {
			word.addClass(WordClass.NAME);
		}
		
//		if(!withOrdinal.equals(fromWord)) {
//			// the word is a number with an ordinal-like ending
//			// but the ordinal-like ending might just be a suffix (unit letter?)
//			Word wordWithOrdinal = classifyWord(withOrdinal);
//			// if the wordWithOrdinal looks like a number with suffix
//			if(wordWithOrdinal.inClass(WordClass.NUMBER_WITH_SUFFIX)) {
//				// then add this as an alternative interpretation 
//				words.add(0, new MisspellingOf<Word>(wordWithOrdinal,0, withOrdinal));
//			}
//			// if the word without the ordinal was classified as a number, remove that wordClass
//			word.removeClass(WordClass.NUMBER);
//		}

		// if the word classified into some extra categories
		if(word.numClasses() > 0) {
			words.add(0, new MisspellingOf<Word>(word, 0, word.getWord()));
		}
		// if word is otherwise unrecognized, add itself with the unrecognized class
		// if(words.size() == 0) {
		// word.addClass(WordClass.UNRECOGNIZED);
		// words.add(new MisspellingOf<Word>(word, 0));
		// }
		return words;
	}


}


