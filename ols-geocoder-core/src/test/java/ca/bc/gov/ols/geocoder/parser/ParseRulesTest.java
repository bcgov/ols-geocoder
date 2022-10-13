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

import ca.bc.gov.ols.geocoder.data.indexing.TrieWordMap;
import ca.bc.gov.ols.geocoder.data.indexing.WordClass;
import ca.bc.gov.ols.geocoder.data.indexing.WordMap;
import ca.bc.gov.ols.geocoder.data.indexing.WordMapBuilder;
import ca.bc.gov.ols.geocoder.dra.DraLexicalRules;
import ca.bc.gov.ols.geocoder.lexer.Lexer;
import ca.bc.gov.ols.geocoder.parser.generator.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParseRulesTest
{
	@Tag("Prod")
	@Test
	public void testA_BStar_C()
	{
		AddressParser parser = createParser();
		
		run(parser, "a c");
		run(parser, "a c d");
		run(parser, "A B C");
		run(parser, "a b b b b b c");
		run(parser, "a b b b b b c d");
		run(parser, "A B", false);
	}
	
	void run(AddressParser parser, String sentence)
	{
		run(parser, sentence, true);
	}
	
	void run(AddressParser parser, String sentence, boolean expected)
	{
		System.out.println();
		System.out.println("Parsing sentence: " + sentence);
		BasicParseDerivationHandler handler = new BasicParseDerivationHandler();
		parser.parse(sentence, false, handler);
		boolean isValid = handler.getDerivations().size() > 0;
		
		assertTrue(isValid == expected);
	}
	
	AddressParser createParser()
	{
		AddressParserGenerator parserGen = new AddressParserGenerator();
		
		Rule ruleTop = new RuleSequence("main", true, new RuleTerm[] {
				new RuleTerm("A"),
				new RuleTerm("bees", RuleOperator.OPTION),
				new RuleTerm("C"),
				new RuleTerm("D", RuleOperator.OPTION)
		});
		
		parserGen.addRule(ruleTop);

		parserGen.addRule(new RuleChoice("A", true, new RuleTerm[] {
				new RuleTerm("LETTER")
		}));
		RuleTerm b = new RuleTerm("B", "LETTER", RuleOperator.STAR);
		parserGen.addRule(b);

		Rule c = new RuleTerm("C", "UNIT_DESIGNATOR");
		parserGen.addRule(c);
		Rule d = new RuleTerm("D", "LETTER");
		parserGen.addRule(d);

		parserGen.addRule(new RuleChoice("bees", true, new RuleTerm[] {
				new RuleTerm("LETTER"),
				b
		}));

		WordMapBuilder wordMapBuilder = new WordMapBuilder();
		wordMapBuilder.addWord("C", WordClass.UNIT_DESIGNATOR);
		WordMap wordMap = new TrieWordMap(wordMapBuilder.getWordMap());
		Lexer lexer = new Lexer(new DraLexicalRules(), wordMap);
		parserGen.setLexer(lexer);
		
		return parserGen.getParser();
	}
	
}
