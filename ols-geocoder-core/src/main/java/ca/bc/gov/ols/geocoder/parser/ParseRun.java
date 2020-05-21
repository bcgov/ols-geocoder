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
package ca.bc.gov.ols.geocoder.parser;

import java.util.List;

import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.Word;
import ca.bc.gov.ols.geocoder.data.indexing.WordClass;

/**
 * A ParseRun keeps track of the current state of the parsing run - and at the end, stores the final
 * list of ParseDerivations.
 * 
 * During the parsing process, the ParseRun can output tracing information for debugging purposes.
 * 
 * @author chodgson
 * 
 */
public class ParseRun {
	private List<List<MisspellingOf<Word>>> words;
	private MisspellingOf<Word>[] chosenWords;
	private int wordIndex = 0;
	private WordClass[] wordClasses;
	private Label[] labels;
	private Label unrecognizedLabel = new Label("unrecognized");
	private boolean isTracing = false;
	private int curError = 0;
	private int unrecognizedCount = 0;
	ParseDerivationHandler handler;
	private List<String> nonWords;
	
	@SuppressWarnings("unchecked")
	public ParseRun(List<List<MisspellingOf<Word>>> toks, List<String> nonWords,
			ParseDerivationHandler handler) {
		words = toks;
		this.nonWords = nonWords;
		this.handler = handler;
		chosenWords = new MisspellingOf[toks.size()];
		wordClasses = new WordClass[words.size()];
		labels = new Label[words.size()];
	}
	
	public void setTrace(boolean isTracing) {
		this.isTracing = isTracing;
	}
	
	public boolean hasNext() {
		return wordIndex < words.size();
	}
	
	public void push(MisspellingOf<Word> word, WordClass wordCls, Label label) {
		chosenWords[wordIndex] = word;
		wordClasses[wordIndex] = wordCls;
		labels[wordIndex] = label;
		wordIndex++;
		if(word.getError() > 0) {
			curError++;
		}
	}
	
	public void pop() {
		wordIndex--;
		if(chosenWords[wordIndex].getError() > 0) {
			curError--;
		}
	}
	
	public List<MisspellingOf<Word>> token() {
		return words.get(wordIndex);
	}
	
	public boolean recordDerivation() {
		ParseDerivation deriv = new ParseDerivation(chosenWords, wordClasses, labels, nonWords);
		traceParseComplete(true, deriv.toString());
		return handler.handleDerivation(deriv);
	}
	
	public void traceMatchSymbol(MatchState state, Object wordValue) {
		if(isTracing) {
			System.out.println("Accepted: " + toString(words, wordIndex)
					+ "  Word: " + wordValue
					+ "   Class: " + state.wordClass
					+ "   Rule: " + state.getRuleName());
		}
	}
	
	public void traceMatchSymbol(String ruleName, WordClass clz, Object value) {
		if(isTracing) {
			System.out.println("Accepted: " + toString(words, wordIndex)
					+ "  Word: " + value
					+ "   Class: " + clz
					+ "   Rule: " + ruleName);
		}
	}
	
	public void traceParseComplete(boolean isValid) {
		traceParseComplete(isValid, null);
	}
	
	public void traceParseComplete(boolean isValid, String info) {
		String msg = "*** VALID PARSE ***";
		if(!isValid) {
			msg = "--- INVALID ---";
		}
		if(info != null) {
			msg += " : " + info;
		}
		if(isTracing) {
			System.out.println(msg);
			System.out.println();
		}
	}
	
	private static String toString(List<List<MisspellingOf<Word>>> words) {
		return toString(words, words.size());
	}
	
	private static String toString(List<List<MisspellingOf<Word>>> words, int num) {
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < num; i++) {
			if(i > 0) {
				buf.append(" ");
			}
			buf.append(words.get(i).toString());
		}
		return buf.toString();
	}
	
	public int getCurError() {
		return curError;
	}
	
	public Label getUnrecognizedLabel() {
		return unrecognizedLabel;
	}
	
	public int getUnrecognizedCount() {
		return unrecognizedCount;
	}
	
	public void incrementUnrecognizedCount() {
		unrecognizedCount++;
	}
	
	public void decrementUnrecognizedCount() {
		unrecognizedCount--;
	}
	
}
