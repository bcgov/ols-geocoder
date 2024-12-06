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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.Word;
import ca.bc.gov.ols.geocoder.lexer.Lexer;

/**
 * An AddressParser is created by an AddressParserGenerator to parse input strings into
 * ParseDerivations, using a ParseRun to keep track of the state of the parsing and output trace
 * information for debugging.
 * 
 * @author chodgson
 * 
 */
public class AddressParser {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX +
			AddressParser.class.getCanonicalName());

	public static final int MAX_ALLOWABLE_MISPELLED_WORDS = 2;
	
	private Lexer lexer;
	State stateMachine;
	private boolean isTracing = true;
	
	public AddressParser(Lexer tokenizer, State stateMachine)
	{
		this.lexer = tokenizer;
		this.stateMachine = stateMachine;
	}
	
	public void setTrace(boolean isTracing)
	{
		this.isTracing = isTracing;
	}
		
	public void parse(String sentence, boolean autoComplete, ParseDerivationHandler handler)
	{
		if(isTracing) {
			logger.trace("Parsing sentence: " + sentence);
		}

		// try first with no misspellings
		List<String> nonWords = new ArrayList<String>();
		List<List<MisspellingOf<Word>>> toks = lexer.lex(sentence, false, autoComplete, nonWords);
		
		boolean cont = parse(toks, nonWords, handler);
		if(!cont || toks.size() == 0) {
			return;
		}
		// if we have at least 90 we are done
		if(handler.getBestScore() >= 90) {
			return;
		}
		
		// try with misspellings
		nonWords = new ArrayList<String>();
		toks = lexer.lex(sentence, true, autoComplete, nonWords);
		cont = parse(toks, nonWords, handler);
		if(!cont || toks.size() == 0) {
			return;
		}
		
		// logger.trace(toks); // DEBUG Mispellings
		
		// if we have at least 90 we are done
		if(handler.getBestScore() >= 90) {
			return;
		}
		
		// try adding a front gate sequentially after each word
//		Word fg = new Word("/FG", WordClass.FRONT_GATE);
//		MisspellingOf<Word> fgMS = new MisspellingOf<Word>(fg, 0, fg.getWord());
//		List<MisspellingOf<Word>> fgTok = Arrays.asList(fgMS);
//		toks.add(1, fgTok);
//		for(int i = 1; i < toks.size(); i++) {
//			cont = parse(toks, nonWords, handler);
//			if(!cont) {
//				return;
//			}
//			if(i == toks.size() - 1) {
//				break;
//			}
//			List<MisspellingOf<Word>> nextTok = toks.get(i+1);
//			toks.set(i, nextTok);
//			toks.set(i+1, fgTok);
//		}

		// try removing a word as garbage
		// garbage now handled in parser grammar
//		List<MisspellingOf<Word>> removedTok = toks.remove(toks.size() - 1);
//		nonWords.add(removedTok.get(0).get().getWord());
//		for(int i = toks.size() - 1; i >= -1; i--) {
//			cont = parse(toks, nonWords, handler);
//			if(!cont) {
//				return;
//			}
//			if(i == -1) {
//				break;
//			}
//			List<MisspellingOf<Word>> nextTok = toks.get(i);
//			toks.set(i, removedTok);
//			removedTok = nextTok;
//			nonWords.clear();
//			nonWords.add(removedTok.get(0).get().getWord());
//		}
	}
	
	public boolean parse(List<List<MisspellingOf<Word>>> toks, List<String> nonWords,
			ParseDerivationHandler handler) {
		if(isTracing) {
			logger.trace("Tokenization: " + toks.toString());
		}
		ParseRun parseRun = new ParseRun(toks, nonWords, handler);
		parseRun.setTrace(isTracing);
		boolean cont = stateMachine.parse(parseRun);
		return cont;
	}
	
}
