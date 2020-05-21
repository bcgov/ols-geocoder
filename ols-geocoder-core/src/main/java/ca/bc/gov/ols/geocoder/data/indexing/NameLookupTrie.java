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
package ca.bc.gov.ols.geocoder.data.indexing;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NameLookupTrie<T> {
	
	private static final int[] table = new int[128];
	static int nextTableId = 0;
	
	static {
		addToTable('A');
		addToTable('B');
		addToTable('C');
		addToTable('D');
		addToTable('E');
		addToTable('F');
		addToTable('G');
		addToTable('H');
		addToTable('I');
		addToTable('J');
		addToTable('K');
		addToTable('L');
		addToTable('M');
		addToTable('N');
		addToTable('O');
		addToTable('P');
		addToTable('Q');
		addToTable('R');
		addToTable('S');
		addToTable('T');
		addToTable('U');
		addToTable('V');
		addToTable('W');
		addToTable('X');
		addToTable('Y');
		addToTable('Z');
		addToTable('0');
		addToTable('1');
		addToTable('2');
		addToTable('3');
		addToTable('4');
		addToTable('5');
		addToTable('6');
		addToTable('7');
		addToTable('8');
		addToTable('9');
		addToTable('/');
		addToTable(' ');
	}
	
	private static void addToTable(int letter) {
		table[letter] = nextTableId++;
	}
	
	private Set<T> storedValues;
	
	private Object[] branches = new Object[nextTableId];
	
	public NameLookupTrie(Map<String, Set<T>> itemMap) {
		for(Map.Entry<String, Set<T>> entry : itemMap.entrySet()) {
			add(entry.getKey().toUpperCase().toCharArray(), 0, entry.getValue());
		}
	}
	
	/**
	 * The extra parameter notSet is to differentiate this version of the constructor from the
	 * previous one, as they are otherwise identical after type erasure.
	 * 
	 * @param itemMap the map to use to build the trie
	 * @param notSet an ignored boolean value, the presence of which specifies that the map is not to a set of objects
	 */
	public NameLookupTrie(Map<String, T> itemMap, boolean notSet) {
		for(Map.Entry<String, T> entry : itemMap.entrySet()) {
			Set<T> itemSet = new THashSet<T>();
			itemSet.add(entry.getValue());
			add(entry.getKey().toUpperCase().toCharArray(), 0, itemSet);
		}
	}
	
	public NameLookupTrie(Collection<T> items) {
		for(T item : items) {
			Set<T> itemSet = new THashSet<T>();
			itemSet.add(item);
			add(item.toString().toUpperCase().toCharArray(), 0, itemSet);
		}
	}
	
	private NameLookupTrie() {
	}
	
	@SuppressWarnings("unchecked")
	private void add(char[] str, int pos, Set<T> items) {
		if(pos == str.length) {
			if(storedValues == null) {
				storedValues = new THashSet<T>();
			}
			storedValues.addAll(items);
			return;
		}
		if(branches[table[str[pos]]] == null) {
			branches[table[str[pos]]] = new NameLookupTrie<T>();
		}
		((NameLookupTrie<T>)branches[table[str[pos]]]).add(str, pos + 1, items);
	}

	public List<MisspellingOf<T>> query(String query, int error) {
		return query(query, error, false);
	}

	public List<MisspellingOf<T>> query(String query, int error, boolean autoComplete) {
		List<MisspellingOf<T>> results = new ArrayList<MisspellingOf<T>>();
		query(query.toUpperCase().toCharArray(), 0, error, error, autoComplete, results, -1, false);
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public Set<T> queryExact(String query) {
		char[] str = query.toUpperCase().toCharArray();
		NameLookupTrie<T> trie = this;
		for(int pos = 0; pos < str.length; pos++) {
			trie = (NameLookupTrie<T>)trie.branches[table[str[pos]]];
			if(trie == null) {
				return Collections.emptySet();
			}
		}
		if(trie.storedValues == null) {
			return Collections.emptySet();
		}
		return trie.storedValues;
	}

	@SuppressWarnings("unchecked")
	public List<MisspellingOf<T>> queryExactWordPrefix(String query) {
		char[] str = query.toUpperCase().toCharArray();
		NameLookupTrie<T> trie = this;
		for(int pos = 0; pos < str.length; pos++) {
			trie = (NameLookupTrie<T>)trie.branches[table[str[pos]]];
			if(trie == null) {
				return Collections.emptyList();
			}
		}
		List<MisspellingOf<T>> results = new ArrayList<MisspellingOf<T>>();
		if(trie.storedValues != null) {
			for(T value : trie.storedValues) {
				results.add(new MisspellingOf<T>(value, 0));
			}
		}
		// now look for any further matches after a space character
		trie = (NameLookupTrie<T>)trie.branches[table[' ']];
		if(trie != null) {
			List<T> partialMatches = new ArrayList<T>();
			trie.findAllChildren(partialMatches);
			for(T partialMatch : partialMatches) {
				results.add(new MisspellingOf<T>(partialMatch, 1));
			}
		}
		
		return results;
	}
	
	@SuppressWarnings("unchecked")
	private void findAllChildren(List<T> results) {
		if(storedValues != null) {
			results.addAll(storedValues);
		}
		for(int i = 0; i < branches.length; i++) {
			if(branches[i] != null) {
				((NameLookupTrie<T>)branches[i]).findAllChildren(results);
			}
		}		
	}

	// lastInserted keeps track of the id of the last character we considered to be an "insertion error"
	// to prevent us from accidentally calling that same character a "deletion error" later
	// we have to remember the character for as long as the same character is repeated to prevent similar problems 
	@SuppressWarnings("unchecked")
	private void query(char[] str, int pos, int remainingError, int maxError, boolean autoComplete,
			List<MisspellingOf<T>> results, int lastInserted, boolean justDeleted) {
		
		// if next character exists
		if(pos < str.length && branches[table[str[pos]]] != null) {
			((NameLookupTrie<T>)branches[table[str[pos]]]).query(str, pos + 1, remainingError,
					maxError, autoComplete, results, lastInserted == table[str[pos]] ? lastInserted : -1, false);
		}
		
		// if we're at the end of the string
		if(pos == str.length) {
			if(storedValues != null) {
				for(T storedValue : storedValues) {
					results.add(new MisspellingOf<T>(storedValue, (maxError - remainingError)));
				}
			}
			// if autoComplete is enabled and we are at the end of a perfect match
			if(autoComplete && remainingError == maxError) {
				// find all strings of which this is a prefix
				List<T> children = new ArrayList<T>();
				findAllChildren(children);
				for(T child : children) {
					// add them to the results list, with a maxError
					results.add(new MisspellingOf<T>(child, maxError));					
				}
			}
		}
		
		// if we still have some room for errors
		if(remainingError > 0) {
			// allow for a deletion error
			for(int i = 0; i < branches.length; i++) {
				if(lastInserted == -1 && branches[i] != null && (pos >= str.length || (i != table[str[pos]]))) {
					((NameLookupTrie<T>)branches[i]).query(str, pos, remainingError - 1, maxError,
							autoComplete, results, lastInserted, true);
				}
			}
			// if there is still string left
			if(pos < str.length) {
				// allow for an incorrect character
				for(int i = 0; i < branches.length; i++) {
					if(i != lastInserted && branches[i] != null && i != table[str[pos]]) {
						((NameLookupTrie<T>)branches[i]).query(str, pos + 1, remainingError - 1,
								maxError, autoComplete, results, -1, false);
					}
				}
				// allow for an insertion error
				// but not immediately after a deletion; that is handled as an incorrect character
				if(!justDeleted) {
					query(str, pos + 1, remainingError - 1, maxError, autoComplete, results, table[str[pos]], false);
				}
//				if(pos + 1 < str.length && branches[table[str[pos + 1]]] != null) {
//					((NameLookupTrie<T>)branches[table[str[pos + 1]]]).query(str, pos + 2, remainingError - 1,
//							maxError, results, table[str[pos]]);
//				}
				
				// allow for a transposition error (but only if the characters aren't equal)
				if(pos + 1 < str.length && str[pos] != str[pos + 1]) {
					if(branches[table[str[pos + 1]]] != null) {
						NameLookupTrie<T> b = (NameLookupTrie<T>)branches[table[str[pos + 1]]];
						if(b.branches[table[str[pos]]] != null) {
							((NameLookupTrie<T>)b.branches[table[str[pos]]]).query(str, pos + 2,
									remainingError - 1, maxError, autoComplete, results, -1, false);
						}
					}
				}
			}
		}
	}
	
}
