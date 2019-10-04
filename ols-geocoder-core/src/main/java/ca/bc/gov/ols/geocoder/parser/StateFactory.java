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

import ca.bc.gov.ols.geocoder.data.indexing.WordClass;

public class StateFactory {
	
	public static State createMatch(String ruleName, Label label, WordClass tokenClass) {
		if(tokenClass == null) {
			throw new IllegalArgumentException("null token class");
			// null labels are allowed
		}
		
		State s = new MatchState(tokenClass);
		s.setRuleName(ruleName);
		s.setLabel(label);
		return s;
	}
	
	public static State createChoice(String ruleName, Label label, State s1, State s2) {
		State s = new ChoiceState(new State[] {s1, s2});
		s.setRuleName(ruleName);
		s.setLabel(label);
		return s;
	}
	
	public static State createChoice(String ruleName, Label label, State[] states) {
		State s = new ChoiceState(states);
		s.setRuleName(ruleName);
		s.setLabel(label);
		return s;
	}
	
	public static State createStar(String ruleName, Label label, State state) {
		State s = new StarState(state);
		s.setRuleName(ruleName);
		s.setLabel(label);
		return s;
	}
	
	public static State createOption(String ruleName, Label label, State state) {
		OptionState s = new OptionState(state);
		s.setRuleName(ruleName);
		s.setLabel(label);
		return s;
	}
	
	public static State createSequence(String ruleName, Label label, State head, State tail) {
		SequenceState s = new SequenceState(head, tail);
		s.setRuleName(ruleName);
		s.setLabel(label);
		return s;
	}
	
	public static FinalState createFinal() {
		return new FinalState();
	}
	
}
