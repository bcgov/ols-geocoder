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

import java.util.EnumSet;

public class Word {
	private String word;
	private EnumSet<WordClass> classes;
	
	public Word(String word) {
		this.word = word;
		this.classes = EnumSet.noneOf(WordClass.class);
	}
	
	public Word(String word, WordClass wordClass) {
		this.word = word;
		this.classes = EnumSet.of(wordClass);
	}
	
	public Word(String word, EnumSet<WordClass> wordClasses) {
		this.word = word;
		this.classes = wordClasses;
	}
	
	public void addClass(WordClass wordClass) {
		classes.add(wordClass);
	}
	
	public String getWord() {
		return word;
	}
	
	public boolean inClass(WordClass wordClass) {
		return classes.contains(wordClass);
	}
	
	public boolean inAnyClass(EnumSet<WordClass> wordClasses) {
		EnumSet<WordClass> intersection = classes.clone();
		intersection.retainAll(wordClasses);
		return intersection.size() > 0;
	}
	
	public boolean removeClass(WordClass wordClass) {
		return classes.remove(wordClass);
	}
	
	public EnumSet<WordClass> getClasses() {
		return classes.clone();
	}
	
	public int numClasses() {
		return classes.size();
	}
	
	@Override
	public String toString() {
		return word + ":" + classes.toString();
	}
	
	@Override
	public int hashCode() {
		return word.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Word) {
			return this.word.equals(((Word)obj).word);
		}
		return false;
	}
	
}