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
import ca.bc.gov.ols.geocoder.parser.generator.AddressParserGenerator;
import ca.bc.gov.ols.geocoder.parser.generator.Rule;
import ca.bc.gov.ols.geocoder.parser.generator.RuleChoice;
import ca.bc.gov.ols.geocoder.parser.generator.RuleOperator;
import ca.bc.gov.ols.geocoder.parser.generator.RuleSequence;
import ca.bc.gov.ols.geocoder.parser.generator.RuleTerm;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddressParseTest
{
	@Test
	@Tag("Prod")
	public void testTest()
	{
		AddressParser parser = createParser();
		run(parser, "123 1/2");
	}

	@Tag("Prod")
	@Test
	public void testAddress()
	{
		AddressParser parser = createParser();

		run(parser, "123 main");
		run(parser, "123 n main");
		run(parser, "123 n san pedro");
		run(parser, "123 n 13th");
	}

	void run(AddressParser parser, String sentence)
	{
		run(parser, sentence, true);
	}

	void run(AddressParser parser, String sentence, boolean expected)
	{
		parser.setTrace(true);
		BasicParseDerivationHandler handler = new BasicParseDerivationHandler();
		parser.parse(sentence, false, handler);
		boolean isValid = handler.getDerivations().size() > 0;
		assertTrue(isValid == expected);
	}

	AddressParser createParser()
	{
		AddressParserGenerator parserGen = new AddressParserGenerator();

		Rule ruleAddr = new RuleSequence("addr", true, new RuleTerm[] {
				new RuleTerm("number"),
				new RuleTerm("directional", RuleOperator.OPTION),
				new RuleTerm("name"),
		});
		parserGen.addRule(ruleAddr);

		parserGen.addRule(new RuleTerm("number", "NUMBER"));

		parserGen.addRule(new RuleTerm("directional", "STREET_DIRECTIONAL"));

		parserGen.addRule(new RuleChoice("name", true, new RuleTerm[] {
				new RuleTerm("STREET_NAME_BODY"),
				new RuleTerm("NAME", RuleOperator.STAR)
		}));

		WordMapBuilder wordMapBuilder = new WordMapBuilder();
		wordMapBuilder.addWord("N", WordClass.STREET_DIRECTIONAL);
		WordMap wordMap = new TrieWordMap(wordMapBuilder.getWordMap());
		Lexer lexer = new Lexer(new DraLexicalRules(), wordMap);
		parserGen.setLexer(lexer);

		return parserGen.getParser();
	}

}
