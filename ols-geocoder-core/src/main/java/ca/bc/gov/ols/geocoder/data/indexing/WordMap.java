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
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import ca.bc.gov.ols.geocoder.dra.DraLexicalRules;
import ca.bc.gov.ols.geocoder.parser.RegExMatcher;

public interface WordMap {
	static final List<WordClassifier> classifiers = Collections.unmodifiableList(Arrays.asList(new WordClassifier[] {
			new WordClassifier(WordClass.NUMBER, new RegExMatcher(DraLexicalRules.RE_NUMBER)),
			//new WordClassifier(WordClass.NUMBER_WITH_SUFFIX, new RegExMatcher(DraLexicalRules.RE_NUMBER_WITH_SUFFIX)),
			//new WordClassifier(WordClass.UNIT_NUMBER_WORD, new RegExMatcher(DraLexicalRules.RE_UNIT_NUMBER)),
			new WordClassifier(WordClass.SUFFIX, new RegExMatcher(DraLexicalRules.RE_SUFFIX)),
			new WordClassifier(WordClass.LETTER, new RegExMatcher(DraLexicalRules.RE_LETTER)),
			new WordClassifier(WordClass.ORDINAL, new RegExMatcher(DraLexicalRules.RE_ORDINAL)),
			new WordClassifier(WordClass.UNRECOGNIZED, n -> !n.equals(DraLexicalRules.FRONT_GATE) && !n.equals(DraLexicalRules.RE_AND))
	}));

	List<MisspellingOf<Word>> mapWord(String fromWord, boolean allowMisspellings);

	List<MisspellingOf<Word>> mapWord(String fromWord, boolean allowMisspellings, boolean autoComplete);

	default Word classifyWord(String fromWord) {
		Word word = new Word(fromWord.toUpperCase());
		for(WordClassifier classifier : classifiers) {
			classifier.classify(word);
		}
		return word;
	}
}

class WordClassifier {
	private WordClass wordClass;
	private Predicate<String> matcher;
	
	public WordClassifier(WordClass wordClass, Predicate<String> matcher) {
		this.wordClass = wordClass;
		this.matcher = matcher;
	}
	
	public void classify(Word word) {
		if(matcher.test(word.getWord())) {
			word.addClass(wordClass);
		}
	}
}