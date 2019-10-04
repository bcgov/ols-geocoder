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
package ca.bc.gov.ols.geocoder.lexer;

import ca.bc.gov.ols.util.StringSet;

public class LexicalRules {

	private String tokenDelimiterRegex = "\\s+";
	private StringSet skipTokens = null;
	private CleanRule[] cleanRules = {};
	private SplitRule[] splitRules = {};
	private JoinRule[] joinRules = {};
	
	public boolean isSkipValue(String val)
	{
		if (skipTokens == null) return false;
		return skipTokens.contains(val);
	}

	public String cleanSentence(String sentence) {
		for(CleanRule rule : cleanRules) {
			sentence = rule.clean( sentence );
		}
		return sentence;
	}

	public StringSet getSkipTokens() {
		return skipTokens;
	}

	public void setSkipTokens(StringSet skipTokens) {
		this.skipTokens = skipTokens;
	}

	public CleanRule[] getCleanRules() {
		return cleanRules;
	}

	public void setCleanRules(CleanRule[] cleanRules) {
		this.cleanRules = cleanRules;
	}

	public void setTokenDelimiterRegex(String tokenDelimiterRegex) {
		this.tokenDelimiterRegex = tokenDelimiterRegex;
	}

	public void setSplitRules(SplitRule[] splitRules) {
		this.splitRules = splitRules;
	}

	public void setJoinRules(JoinRule[] joinRules) {
		this.joinRules = joinRules;
	}

	public SplitRule[] getSplitRules() {
		return splitRules;
	}

	public JoinRule[] getJoinRules() {
		return joinRules;
	}

	public String getTokenDelimiterRegex() {
		return tokenDelimiterRegex;
	}

	public String runSpecialRules(String sentence) {
		return sentence;
	}
	

}
