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

import gnu.trove.set.hash.THashSet;

import java.util.Set;
import java.util.function.Predicate;

/**
 * A StringSetMatcher is a Predicate which identifies Strings belonging to a specified set of strings.
 * 
 * @author chodgson
 *
 */
public class StringSetMatcher implements Predicate<String> {
	Set<String> valueSet = new THashSet<String>();
	
	public StringSetMatcher(String value)
	{
		add(new String[] { value });
	}
	
	public StringSetMatcher(String v1, String v2)
	{
		add(new String[] { v1, v2 });
	}
	
	public StringSetMatcher(String[] values)
	{
		add(values);
	}
	
	public StringSetMatcher(Set<String> set) {
		valueSet = set;
	}
	
	private void add(String[] values)
	{
		for (String s : values) {
			valueSet.add(s);
		}
	}
	
	public boolean test(String value)
	{
		return valueSet.contains(value);
	}
}
