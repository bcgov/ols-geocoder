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

import ca.bc.gov.ols.geocoder.parser.generator.AddressParserGenerator;
import ca.bc.gov.ols.geocoder.parser.generator.Rule;
import ca.bc.gov.ols.geocoder.parser.generator.RuleOperator;
import ca.bc.gov.ols.geocoder.parser.generator.RuleSequence;
import ca.bc.gov.ols.geocoder.parser.generator.RuleTerm;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParseRulesTest
{
	@Tag("Dev")
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
				new RuleTerm("bees"),
				new RuleTerm("C"),
				new RuleTerm("D", RuleOperator.OPTION)
		});
		
		parserGen.addRule(ruleTop);
		
		Rule rulebees = new RuleTerm("bees", "B", RuleOperator.STAR);
		parserGen.addRule(rulebees);
		
		return parserGen.getParser();
	}
	
}
