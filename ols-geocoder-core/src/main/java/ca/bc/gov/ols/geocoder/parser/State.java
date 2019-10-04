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

import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.Word;
import ca.bc.gov.ols.geocoder.data.indexing.WordClass;

public abstract class State {
	protected String ruleName = null;
	private Label label = null;
	protected State next = null;
	
	protected State() {
	}
	
	protected State(State next) {
		setNext(next);
	}
	
	/**
	 * Sets any null next states to be the argument state
	 * 
	 * @param next the next state
	 */
	public void setNext(State next) {
		this.next = next;
	}
	
	public State getNext() {
		return next;
	}
	
	public void setRuleName(String ruleName) {
		if(ruleName == null) {
			throw new IllegalArgumentException("null rule name");
		}
		this.ruleName = ruleName;
	}
	
	public String getRuleName() {
		return ruleName;
	}
	
	public void setLabel(Label label) {
		this.label = label;
	}
	
	public Label getLabel() {
		return label;
	}
	
	/**
	 * Parse this state. Return true to continue parsing, or false to cancel further parsing.
	 * 
	 * @param run the ParseRun to use
	 * @return true to continue parsing, false to bail out and cancel further parsing
	 */
	public abstract boolean parse(ParseRun run);
	
	protected boolean parseNext(ParseRun run) {
		// Assert: next != null => invalid construction of state machine
		return next.parse(run);
	}
	
}

class EmptyState extends State {
	
	public EmptyState(State next) {
		super(next);
	}
	
	@Override
	public boolean parse(ParseRun run) {
		return getNext().parse(run);
	}
}

class MatchState extends State {
	WordClass wordClass = null;
	
	public MatchState(WordClass tokenClass) {
		this.wordClass = tokenClass;
	}
	
	@Override
	public boolean parse(ParseRun run) {
		/*
		 * // empty symbol if (tokenType == null) { parseNext(parse); return; }
		 */
		
		if(!run.hasNext()) {
			return true;
		}
		run.traceMatchSymbol(this, run.token());
		
		boolean match = false;
		boolean keepGoing = true;
		for(MisspellingOf<Word> tok : run.token()) {
			if(tok.get().inClass(wordClass)
					&& (tok.getError() == 0 || run.getCurError() < AddressParser.MAX_ALLOWABLE_MISPELLED_WORDS)) {
				// match - push parse run state
				match = true;
				run.push(tok, wordClass, getLabel());
				keepGoing = parseNext(run);
				run.pop();
			}
			if(!keepGoing) {
				return false;
			}
		}
		// as a last resort, try calling the word "unrecognized" and just eat it
		// if(run.getUnrecognizedCount() < AddressParser.MAX_UNRECOGNIZABLE) {
		// run.push(run.token().get(0), WordClass.UNRECOGNIZED, run.getUnrecognizedLabel());
		// run.incrementUnrecognizedCount();
		// keepGoing = parse(run);
		// run.pop();
		// run.decrementUnrecognizedCount();
		// if(!keepGoing) {
		// return false;
		// }
		// }
		
		if(!match) {
			run.traceParseComplete(false);
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "MATCH{" + getLabel() + "}(" + wordClass + ")";
	}
}

class ChoiceState extends State {
	private State[] choice = null;
	
	public ChoiceState(State[] choice) {
		this.choice = choice;
	}
	
	@Override
	public void setNext(State next) {
		this.next = next;
		for(State s : choice) {
			s.setNext(next);
		}
	}
	
	@Override
	public boolean parse(ParseRun run) {
		for(State s : choice) {
			if(!s.parse(run)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CHOICE{" + getLabel() + "}(");
		for(State s : choice) {
			if(sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(s);
		}
		sb.append(")");
		return sb.toString();
	}
}

class StarState extends State {
	private State s = null;
	
	public StarState(State s) {
		this.s = s;
		s.setNext(this);
	}
	
	@Override
	public boolean parse(ParseRun run) {
		if(parseNext(run) && s.parse(run)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "STAR{" + getLabel() + "}(" + s + ")";
	}
}

class OptionState extends State {
	private State s = null;
	
	public OptionState(State s) {
		this.s = s;
	}
	
	@Override
	public void setNext(State next) {
		super.setNext(next);
		s.setNext(next);
	}
	
	@Override
	public boolean parse(ParseRun run) {
		if(s.parse(run) && parseNext(run)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "OPT{" + getLabel() + "}(" + s + ")";
	}
}

class SequenceState extends State {
	// the tail of the sequence
	private State tail = null;
	
	public SequenceState(State head, State tail) {
		this.next = head;
		this.tail = tail;
		if(tail == null) {
			System.out.println("suck it");
		}
	}
	
	@Override
	public void setNext(State next) {
		// update the tail's next
		tail.setNext(next);
	}
	
	@Override
	public boolean parse(ParseRun run) {
		return parseNext(run);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SEQ{" + getLabel() + "}(");
		State s = next;
		while(true) {
			sb.append(" ");
			sb.append(s);
			if(s == tail) {
				break;
			}
			if(s instanceof SequenceState) {
				s = ((SequenceState)s).tail.getNext();
			} else {
				s = s.getNext();
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
}

class FinalState extends State {
	public FinalState() {
		
	}
	
	@Override
	public void setNext(State next) {
		throw new UnsupportedOperationException("Should never be called");
	}
	
	@Override
	public boolean parse(ParseRun run) {
		// done if all tokens have been consumed
		if(!run.hasNext()) {
			return run.recordDerivation();
		}
		// otherwise, this is not a valid parse, so don't record it
		return true;
		
		// eat any remaining tokens as unrecognized
		// int count = 0;
		// while(run.hasNext() && run.getUnrecognizedCount() < AddressParser.MAX_UNRECOGNIZABLE) {
		// run.push(run.token().get(0), WordClass.UNRECOGNIZED, run.getUnrecognizedLabel());
		// run.incrementUnrecognizedCount();
		// count++;
		// }
		// boolean keepGoing = run.recordDerivation();
		// // put the tokens back
		// while(count > 0) {
		// run.pop();
		// run.decrementUnrecognizedCount();
		// count--;
		// }
		// return keepGoing;
	}
	
	@Override
	public String toString() {
		return "$";
	}
	
}
