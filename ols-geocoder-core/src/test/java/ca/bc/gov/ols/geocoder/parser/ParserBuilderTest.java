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

import ca.bc.gov.ols.geocoder.dra.DraLexicalRules;
import ca.bc.gov.ols.geocoder.lexer.Lexer;
import ca.bc.gov.ols.geocoder.parser.generator.AddressParserGenerator;
import ca.bc.gov.ols.geocoder.parser.generator.RuleSequence;
import ca.bc.gov.ols.geocoder.parser.generator.RuleTerm;
import ca.bc.gov.ols.junitFlags.DevTest;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(DevTest.class)
public class ParserBuilderTest extends TestCase {
	
	public void testWeirdCase() {
		AddressParserGenerator parserGen = new AddressParserGenerator();
		
		parserGen.addRule(new RuleSequence("streetDesc", false,
				new RuleTerm[] {
						new RuleTerm("streetName"),
						new RuleTerm("streetType")}));
		
		// Base Symbols - refer to wordClasses
		parserGen.addRule(new RuleTerm("streetType", "STREET_TYPE"));
		
		parserGen.addRule(new RuleSequence("streetName", true,
				new RuleTerm[] {
						new RuleTerm("STREET_NAME_BODY"),
						new RuleTerm("STREET_DIRECTIONAL")}));
		
		parserGen.setLexer(new Lexer(new DraLexicalRules(), null));
		// parserGen.setWordMap(datastore.getWordMap());
		AddressParser parser = parserGen.getParser();
		System.out.println(parser.stateMachine.toString());
	}
}
