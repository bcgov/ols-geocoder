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
package ca.bc.gov.ols.geocoder.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.Word;
import ca.bc.gov.ols.geocoder.data.indexing.WordClass;
import ca.bc.gov.ols.geocoder.data.indexing.WordMap;

/**
 * A Lexer takes an input string and separates it into Words, associating the matching WordClasses
 * with each word (or token) in the string. LexicalRules are passed in when it is created, which
 * define the way in which words are extracted from the input.
 * 
 * @author chodgson
 * 
 */
public class Lexer
{
	private LexicalRules rules;
	private WordMap wordMap;
	private static String[] STRING_ARRAY_TYPE = new String[0];
	
	public Lexer(LexicalRules rules, WordMap wordMap)
	{
		this.rules = rules;
		this.wordMap = wordMap;
	}
	
	public List<List<MisspellingOf<Word>>> lex(String sentence, boolean autoComplete, List<String> nonWords)
	{
		sentence = " " + sentence;
		sentence = rules.cleanSentence(sentence);
		sentence = rules.runSpecialRules(sentence);
		String[] atoms = sentence.split(rules.getTokenDelimiterRegex());
		
		String[] atomsSplit = runSplitRules(atoms);
		String[] atomsJoin = runJoinRules(atomsSplit);
		return tokenize(atomsJoin, autoComplete, nonWords);
		
	}
	
	public List<List<MisspellingOf<Word>>> lexField(String sentence, EnumSet<WordClass> wc) {
		if(sentence == null || sentence.isEmpty()) {
			return Collections.emptyList();
		}
		// TODO need to do something with the list of nonwords?
		List<String> nonWords = new ArrayList<String>();
		List<List<MisspellingOf<Word>>> words = lex(sentence, false, nonWords);
		List<List<MisspellingOf<Word>>> finalWords = new ArrayList<List<MisspellingOf<Word>>>();
		for(List<MisspellingOf<Word>> wordList : words) {
			Iterator<MisspellingOf<Word>> wordIt = wordList.iterator();
			List<MisspellingOf<Word>> finalWordList = new ArrayList<MisspellingOf<Word>>();
			finalWords.add(finalWordList);
			while(wordIt.hasNext()) {
				MisspellingOf<Word> word = wordIt.next();
				if(word.get().inAnyClass(wc)) {
					EnumSet<WordClass> wordClasses = word.get().getClasses();
					wordClasses.retainAll(wc);
					finalWordList.add(new MisspellingOf<Word>(
							new Word(word.get().getWord(), wordClasses),
							word.getError()));
				}
			}
		}
		return finalWords;
	}
	
	private String[] runSplitRules(String[] atoms)
	{
		if(rules.getSplitRules() == null) {
			return atoms;
		}
		
		List<String> splitAtoms = new ArrayList<String>();
		
		for(int i = 0; i < atoms.length; i++) {
			String atom = atoms[i];
			runSplitRules(atom, splitAtoms);
		}
		return splitAtoms.toArray(STRING_ARRAY_TYPE);
	}
	
	private void runSplitRules(String atom, List<String> splitAtoms)
	{
		for(SplitRule rule : rules.getSplitRules()) {
			if(runSplitRule(rule, atom, splitAtoms)) {
				return;
			}
		}
		// otherwise just keep the atom unsplit
		splitAtoms.add(atom);
	}
	
	private boolean runSplitRule(SplitRule splitter, String atom, List<String> splits)
	{
		splitter.process(atom);
		boolean isMatched = splitter.isMatched();
		if(splitter.isMatched()) {
			String[] result = splitter.getResult();
			if(result[0] != null) {
				splits.add(result[0]);
			}
			if(result[1] != null) {
				splits.add(result[1]);
			}
		}
		return isMatched;
	}
	
	private String[] runJoinRules(String[] atoms)
	{
		if(rules.getJoinRules() == null) {
			return atoms;
		}
		
		String[] joinAtoms = atoms;
		for(JoinRule rule : rules.getJoinRules()) {
			joinAtoms = rule.process(joinAtoms);
		}
		return joinAtoms;
	}
	
	public List<List<MisspellingOf<Word>>> tokenize(String[] atoms, boolean autoComplete, List<String> nonWords)
	{
		List<List<MisspellingOf<Word>>> tokList = new ArrayList<List<MisspellingOf<Word>>>();
		for(int i = 0; i < atoms.length; i++) {
			String rawAtom = atoms[i];
			
			String atom = rawAtom.toUpperCase();
			
			// check if token should be skipped
			if(atom.isEmpty() || rules.isSkipValue(atom)) {
				continue;
			}
			// last parameter (autoComplete) is true if autoComplete is true and this is the last token
			List<MisspellingOf<Word>> tok = wordMap.mapWord(atom, autoComplete && (i == atoms.length - 1));
			if(tok.size() > 0) {
				tokList.add(tok);
			} else {
				nonWords.add(atom);
			}
		}
		return tokList;
	}
	
}
