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
package ca.bc.gov.ols.geocoder.parser.generator;

import ca.bc.gov.ols.geocoder.data.indexing.WordClass;
import ca.bc.gov.ols.geocoder.parser.Label;
import ca.bc.gov.ols.geocoder.parser.State;
import ca.bc.gov.ols.geocoder.parser.StateFactory;

public class RuleStateBuilder
{
	private AddressParserGenerator parserGen;
	
	public RuleStateBuilder(AddressParserGenerator parserGen)
	{
		this.parserGen = parserGen;
	}
	
	public State buildStateForSymbol(String symbolName, String ruleName, Label label, State next)
	{
		/**
		 * Build appropriate state for symbol - if rule, then build state for rule - if tokenclass,
		 * then build Match state
		 */
		if(parserGen.isRule(symbolName)) {
			Rule rule = parserGen.getRule(symbolName);
			return rule.build(label, next, this);
		}
		
		// must be a valid token class
		WordClass clz = WordClass.valueOf(symbolName);
		if(clz == null) {
			throw new IllegalStateException("Unknown grammar symbol: " + symbolName);
		}
		State match = StateFactory.createMatch(ruleName, label, clz);
		match.setNext(next);
		return match;
	}
	
}
