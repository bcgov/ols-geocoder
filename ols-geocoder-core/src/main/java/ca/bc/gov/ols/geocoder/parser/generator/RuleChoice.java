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

import ca.bc.gov.ols.geocoder.parser.Label;
import ca.bc.gov.ols.geocoder.parser.State;
import ca.bc.gov.ols.geocoder.parser.StateFactory;

/**
 * A choice rule is a choice between a set of {@link RuleTerm}s.
 * 
 * @author mbdavis
 *
 */
public class RuleChoice extends Rule 
{
	private Rule[] term;
	
	public RuleChoice(String name, boolean isLabel, RuleTerm[] term)
	{
		super(name, isLabel);
		this.term = term;
	}
	
	public State build(Label label,State next, RuleStateBuilder builder)
	{
		// override the parent label if we have our own
		if(getLabel() != null ) label = getLabel();
		
		State[] states = new State[term.length];
		for (int i = 0; i < states.length; i++) {
			states[i] = term[i].build(label, next, builder);
		}
		State ch = StateFactory.createChoice(getName(), label, states);
		ch.setNext(next);
		return ch;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (Rule r : term) 
		{
			if (sb.length() > 0) sb.append(" | ");
			sb.append(r);
		}
		return sb.toString();
	}
	
}
