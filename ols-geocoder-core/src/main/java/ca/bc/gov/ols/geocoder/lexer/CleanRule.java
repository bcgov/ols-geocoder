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

public class CleanRule {
	private Pattern pattern;
	private String replacement;
	
	public CleanRule(String regex, String replacement) {
		pattern = Pattern.compile(regex);
		this.replacement = replacement;
	}
	
	public String clean(String input) {
        Matcher matcher = pattern.matcher(input);
        if(matcher.find()){
        	
        	return matcher.replaceAll(replacement);
        } else {
        	return input;
        }
	}
}