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
public class AddressParser
{
	public static final int MAX_ALLOWABLE_MISPELLED_WORDS = 2;
	
	private Lexer lexer;
	State stateMachine;
	private boolean isTracing = false;
	
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
			System.out.println();
			System.out.println("Parsing sentence: " + sentence);
		}
		List<String> nonWords = new ArrayList<String>();
		List<List<MisspellingOf<Word>>> toks = lexer.lex(sentence, autoComplete, nonWords);
		
		// System.out.println(toks); // DEBUG Mispellings
		
		// try first with all words
		boolean cont = parse(toks, nonWords, handler);
		if(!cont || toks.size() == 0) {
			return;
		}
		
		// try removing a word as garbage
		List<MisspellingOf<Word>> removedTok = toks.remove(toks.size() - 1);
		nonWords.add(removedTok.get(0).get().getWord());
		for(int i = toks.size() - 1; i >= -1; i--) {
			cont = parse(toks, nonWords, handler);
			if(!cont) {
				return;
			}
			if(i == -1) {
				break;
			}
			List<MisspellingOf<Word>> nextTok = toks.get(i);
			toks.set(i, removedTok);
			removedTok = nextTok;
			nonWords.clear();
			nonWords.add(removedTok.get(0).get().getWord());
		}
	}
	
	public boolean parse(List<List<MisspellingOf<Word>>> toks, List<String> nonWords,
			ParseDerivationHandler handler) {
		if(isTracing) {
			System.out.println("Tokenization: ");
			System.out.println(toks.toString());
		}
		ParseRun parseRun = new ParseRun(toks, nonWords, handler);
		parseRun.setTrace(isTracing);
		boolean cont = stateMachine.parse(parseRun);
		return cont;
	}
	
}
