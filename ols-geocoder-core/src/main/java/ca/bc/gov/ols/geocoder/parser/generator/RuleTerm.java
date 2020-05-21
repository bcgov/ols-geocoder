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
 * A rule term is a symbol, followed by an optional unary {@link RuleOperator}.
 * The symbol references the name of either another rule, or a word class.
 * 
 * @author mbdavis
 *
 */
public class RuleTerm extends Rule
{
	private String symbol;
	private RuleOperator op = null;
	
	public RuleTerm(String label, String symbol, RuleOperator op)
	{
		super(label, true);
		this.symbol = symbol;
		this.op = op;
	}

	public RuleTerm(String symbol, RuleOperator op)
	{
		super(symbol, false);
		this.symbol = symbol;
		this.op = op;
	}
	
	public RuleTerm(String symbol)
	{
		super(symbol, false);
		this.symbol = symbol;
	}

	public RuleTerm(String label, String symbol)
	{
		super(label, true);
		this.symbol = symbol;
	}
	
	public State build(Label label, State next, RuleStateBuilder builder)
	{
		// override the parent label if we have our own
		if(getLabel() != null ) label = getLabel();
				
		State s = builder.buildStateForSymbol(symbol, getName(), label, next);
		
		if (op != null) {
			if (op == RuleOperator.STAR)
				s = StateFactory.createStar(getName(), label, s);
			else if (op == RuleOperator.OPTION)
				s = StateFactory.createOption(getName(), label, s);
		
			s.setNext(next);
		}
		return s;
	}

	public String toString()
	{
		return symbol + RuleOperator.symbol(op); 
	}
}
