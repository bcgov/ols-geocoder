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

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A RegExMatcher is a Predicate which identifies strings that match a specified regular expression.
 * 
 * @author chodgson
 *
 */
public class RegExMatcher implements Predicate<String> {
	private Pattern pattern;
	
	public RegExMatcher(String regex)
	{
		pattern = Pattern.compile(regex);
	}
	
	public boolean test(String value)
	{
		return pattern.matcher(value).matches();
	}
}
