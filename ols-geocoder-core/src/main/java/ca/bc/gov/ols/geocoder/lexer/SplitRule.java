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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitRule 
{
	public static SplitRule createSplitByDelimiter(String splitRegEx)
	{
		SplitRule splitter = new SplitRule();
		splitter.initSplitByDelimiter(splitRegEx);
		return splitter;
	}
	public static SplitRule createSplit(String splitRegEx)
	{
		SplitRule splitter = new SplitRule();
		splitter.initSplit(splitRegEx);
		return splitter;
	}
	
	private String splitRegEx;
	private Pattern pat;
	private String matchRegEx1;
	private String matchRegEx2;
	private Pattern pat1;
	private Pattern pat2;
	
	private boolean isMatched = false;
	private String[] result;
	
	public SplitRule()
	{
		
	}
	
	private void initSplitByDelimiter(String splitRegEx)
	{
		this.splitRegEx = splitRegEx;
		pat = Pattern.compile(splitRegEx);
	}
	private void initMatch(String matchRegEx1, String matchRegEx2)
	{
		this.matchRegEx1 = matchRegEx1;
		this.matchRegEx2 = matchRegEx2;
		pat1 = Pattern.compile(matchRegEx1);	
		pat2 = Pattern.compile(matchRegEx2);	
	}
	private void initSplit(String matchRegEx)
	{
		this.matchRegEx1 = matchRegEx;
		pat = Pattern.compile(matchRegEx1);		
	}
	
	public void process(String input)
	{
		if (splitRegEx != null) {
			result = split(input);
			if (result.length == 2) {
				isMatched = true;
			}
			return;
		}
		else if (matchRegEx2 == null) {
			result = matchGroup(input);
		}
		else {
			result =  match(input);
		}
	}
	
	public boolean isMatched()
	{
		return isMatched;
	}
	
	public String[] getResult()
	{
		return result;
	}
	
	private String[] split(String input)
	{
		return pat.split(input, 2);
	}
	
	private String[] matchGroup(String input)
	{
		Matcher matcher = pat.matcher(input);
		isMatched = matcher.matches();
		if (isMatched) {
			result = new String[2];
			result[0] = matcher.group(1);
			result[1] = matcher.group(2);

		}
		return result;
	}

	
	private String[] match(String input)
	{
		String[] extract = new String[2];
		extract[0] = matchExtract(input, pat1);
		extract[1] = matchExtract(input, pat2);
		isMatched = extract[0] != null && extract[1] != null; 

		return extract;
	}
	
	private static String matchExtract(String input, Pattern pat)
	{
		Matcher matcher = pat.matcher(input);
		return input.substring(matcher.start(), matcher.end());		
	}
}
