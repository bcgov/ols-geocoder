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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.Word;
import ca.bc.gov.ols.geocoder.data.indexing.WordClass;
import ca.bc.gov.ols.geocoder.data.indexing.WordMap;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.libpostal.libpostal_address_parser_options_t;
import org.bytedeco.libpostal.libpostal_address_parser_response_t;

import static org.bytedeco.libpostal.global.postal.*;

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
//	private static String dataDir = "/usr/local/libpostal/";
//	private static String dataDir = "src/main/resources/libpostal_data/";
//	private static String dataDir = "/Users/abolyach/bc_work/ols-geocoder/ols-geocoder-web/src/main/resources/libpostal_data/";
	private static String dataDir = "/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/libpostal_data/";

	public Lexer(LexicalRules rules, WordMap wordMap)
	{
		this.rules = rules;
		this.wordMap = wordMap;
	}
	
	public List<List<MisspellingOf<Word>>> lex(String sentence, boolean allowMisspellings, boolean autoComplete, List<String> nonWords)
	{
		sentence = rules.cleanSentence(sentence);
		sentence = rules.runSpecialRules(sentence);
		List<List<MisspellingOf<Word>>> toks = new ArrayList<List<MisspellingOf<Word>>>();
		URL url = this.getClass()
		.getClassLoader()
		.getResource("libpostal_data/data_version");
		System.out.println("PATH");
		System.out.println(url.getPath());

//		String dataDir = "";
//		dataDir = url.getPath();
//		dataDir = "/" + FilenameUtils.getPath(dataDir);
//		System.out.println(dataDir);

		boolean setup1 = libpostal_setup_datadir(dataDir);
		boolean setup2 = libpostal_setup_parser_datadir(dataDir);
		boolean setup3 = libpostal_setup_language_classifier_datadir(dataDir);
		try {
			if (setup1 && setup2 && setup3) {
				libpostal_address_parser_options_t options = libpostal_get_address_parser_default_options();
				BytePointer address = null;
				try {
					address = new BytePointer(sentence, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				libpostal_address_parser_response_t response = libpostal_parse_address(address, options);

				long count = response.num_components();
				for (int i = 0; i < count; i++) {
					Word word = new Word(response.components(i).getString().toUpperCase());
					String wordClass = response.labels(i).getString();
					List<MisspellingOf<Word>> sub_toks = new ArrayList<MisspellingOf<Word>>();
					switch (wordClass) {
						case "house_number":
							word.addClass(WordClass.NUMBER);
							sub_toks.add(0, new MisspellingOf<Word>(word, 0, word.getWord()));
							break;
						case "road":
							String[] atoms = word.getWord().split(rules.getTokenDelimiterRegex());
							List<List<MisspellingOf<Word>>> temp_toks = tokenize(atoms, allowMisspellings, autoComplete, nonWords);
							toks.addAll(temp_toks);
							break;
						case "unit":
							word.addClass(WordClass.UNIT_DESIGNATOR);
							sub_toks.add(0, new MisspellingOf<Word>(word, 0, word.getWord()));
							break;
						case "level":
							word.addClass(WordClass.FLOOR);
							sub_toks.add(0, new MisspellingOf<Word>(word, 0, word.getWord()));
							break;
						case "entrance":
							word.addClass(WordClass.FRONT_GATE);
							sub_toks.add(0, new MisspellingOf<Word>(word, 0, word.getWord()));
							break;
						case "po_box":
						case "postcode":
						case "suburb":
						case "city_district":
							word.addClass(WordClass.POSTAL_ADDRESS_ELEMENT);
							sub_toks.add(0, new MisspellingOf<Word>(word, 0, word.getWord()));
							break;
						case "city":
							word.addClass(WordClass.LOCALITY_NAME);
							sub_toks.add(0, new MisspellingOf<Word>(word, 0, word.getWord()));
							break;
						case "state":
							word.addClass(WordClass.STATE_PROV_TERR);
							sub_toks.add(0, new MisspellingOf<Word>(word, 0, word.getWord()));
							break;
	//					case "country_region":
	//						break;
	//					case "country":
	//						word.addClass(WordClass.);
	//						break;
	//					case "world_region":
	//						break;
	//					case "house":
	//						break;
	//					case "category":
	//						break;
	//					case "near":
	//						break;
	//					case "island":
	//						break;
	//					case "staircase":
	//						break;
						default:
							word.addClass(WordClass.UNRECOGNIZED);
							sub_toks.add(0, new MisspellingOf<Word>(word, 0, word.getWord()));
							break;
					}
					if (!sub_toks.isEmpty()) {
						toks.add(sub_toks);
					}
	//				System.out.println(response.labels(i).getString() + " " + response.components(i).getString());
				}
	//			libpostal_teardown();
	//			libpostal_teardown_parser();
	//			libpostal_teardown_language_classifier();
			} else {
				System.out.println("Cannot setup libpostal, check if the training data is available at the specified path!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return toks;

////		 TODO I don't think we need to use splits/joins anymore
//		String[] atoms = sentence.split(rules.getTokenDelimiterRegex());
//		String[] atomsSplit = runSplitRules(atoms);
//		String[] atomsJoin = runJoinRules(atomsSplit);
////		 TODO need to change tokenize logic - since we already assigned WordClasses before even going to tokenize method
//		return tokenize(atoms, allowMisspellings, autoComplete, nonWords);

	}
	
	public List<List<MisspellingOf<Word>>> lexField(String sentence, EnumSet<WordClass> wc) {
		if(sentence == null || sentence.isEmpty()) {
			return Collections.emptyList();
		}
		// TODO need to do something with the list of nonwords?
		List<String> nonWords = new ArrayList<String>();
		List<List<MisspellingOf<Word>>> words = lex(sentence, true, false, nonWords);
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
							word.getError(), word.getMisspelling()));
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
	
	public List<List<MisspellingOf<Word>>> tokenize(String[] atoms, boolean allowMisspellings, boolean autoComplete, List<String> nonWords)
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
			// now every word is at least an UNRECOGNIZED so non-words should always be blank 
			List<MisspellingOf<Word>> tok = wordMap.mapWord(atom, allowMisspellings, autoComplete && (i == atoms.length - 1));
			if(tok.size() > 0) {
				tokList.add(tok);
			} else {
				nonWords.add(atom);
			}
		}
		return tokList;
	}
	
}
