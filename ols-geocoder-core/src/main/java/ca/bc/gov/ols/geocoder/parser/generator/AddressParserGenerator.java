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

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import ca.bc.gov.ols.geocoder.data.indexing.WordClass;
import ca.bc.gov.ols.geocoder.lexer.Lexer;
import ca.bc.gov.ols.geocoder.parser.AddressParser;
import ca.bc.gov.ols.geocoder.parser.State;
import ca.bc.gov.ols.geocoder.parser.StateFactory;

/**
 * An AddressParserGenerator accepts {@link WordClass}es and {@link Rule}s and builds an
 * {@link AddressParser} to parse strings using those rules.
 * 
 * @author chodgson
 * 
 */
public class AddressParserGenerator
{
	private Map<String, Rule> ruleMap = new THashMap<String, Rule>();
	private Rule startRule = null;
	private Lexer lexer;
	
	public AddressParserGenerator() {
	}
	
	public void setLexer(Lexer lexer) {
		this.lexer = lexer;
	}
	
	public void addRule(Rule rule) {
		if(startRule == null) {
			startRule = rule;
		}
		ruleMap.put(rule.getName(), rule);
	}
	
	public boolean isRule(String symbol) {
		return ruleMap.containsKey(symbol);
	}
	
	public Rule getStartRule() {
		return startRule;
	}
	
	public Rule getRule(String ruleName) {
		return ruleMap.get(ruleName);
	}
	
	public State getStateMachine() {
		RuleStateBuilder rsb = new RuleStateBuilder(this);
		State finalState = StateFactory.createFinal();
		State machine = startRule.build(null, finalState, rsb);
		// System.out.println(machine.toString()); // DEBUG
		return machine;
	}
	
	public AddressParser getParser() {
		return new AddressParser(lexer, getStateMachine());
	}
	
}
