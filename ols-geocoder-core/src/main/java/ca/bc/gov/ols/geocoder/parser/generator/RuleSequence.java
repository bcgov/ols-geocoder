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
 * A sequence rule is a sequence of {@link RuleTerm}s.
 *  
 * @author mbdavis
 *
 */
public class RuleSequence extends Rule 
{
	private Rule[] terms;
	
	public RuleSequence(String name, boolean isLabel, RuleTerm[] terms)
	{
		super(name, isLabel);
		this.terms = terms;
	}
	
	public State build(Label label, State next, RuleStateBuilder builder)
	{
		// override the parent label if we have our own
		if(getLabel() != null ) label = getLabel();
		
		// build the list from the tail to the head so you can always assign the "next" state
		State tail = null, head = next;
		for (int i = terms.length - 1; i >= 0; i--) {
			State s = terms[i].build(label, head, builder);
			head = s;
			// if this is the last state in the sequence
			if(i == terms.length - 1) {
				// point the tail at it
				tail = s;
			}
		}
		
		return StateFactory.createSequence(getName(), getLabel(), head, tail);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (Rule r : terms) 
		{
			if (sb.length() > 0) sb.append(" ");
			sb.append(r);
		}
		return sb.toString();
	}
}
