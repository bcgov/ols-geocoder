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

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.Word;
import ca.bc.gov.ols.geocoder.data.indexing.WordClass;

/**
 * A ParseDerivation represents one "possible parsing" of a given string. A single call to
 * {@link AddressParser#parse(String, boolean, ParseDerivationHandler)} can result in a {@link ParseRun} with
 * multiple ParseDerivations.
 * 
 * @author chodgson
 * 
 */
public class ParseDerivation
{
	// private Word[] tokens;
	// private WordClass[] tokenClass;
	// private Label[] labels;
	private ArrayList<String> labelList;
	private ArrayList<String> partList;
	private ArrayList<Integer> errorList;
	private List<String> nonWords;
	
	public ParseDerivation(MisspellingOf<Word>[] chosenWords, WordClass[] tokenClass,
			Label[] labels,
			List<String> nonWords) {
		// this.tokens = tokens;
		// this.tokenClass = tokenClass.clone(); // don't think we need the wordClasses - CMH
		// this.labels = labels.clone();
		labelList = new ArrayList<String>();
		partList = new ArrayList<String>();
		errorList = new ArrayList<Integer>();
		this.nonWords = new ArrayList<String>(nonWords);
		Label currLabel = null;
		StringBuilder sb = null;
		int error = 0;
		for(int i = 0; i < chosenWords.length; i++) {
			if(labels[i] != currLabel) {
				if(currLabel != null) {
					// save the current label contents
					labelList.add(currLabel.getName());
					partList.add(sb.toString());
					errorList.add(error);
				}
				currLabel = labels[i];
				sb = new StringBuilder();
				error = 0;
			}
			if(labels[i] != null) {
				// don't use spaces when appending streetDirections -- yes, a hack
				if(sb.length() > 0 && labels[i].getName() != "streetDirection") {
					sb.append(" ");
				}
				sb.append(chosenWords[i].get().getWord());
				error += chosenWords[i].getError();
			}
		}
		// put the last label in the map
		if(currLabel != null) {
			labelList.add(currLabel.getName());
			partList.add(sb.toString());
			errorList.add(error);
		}
		
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < labelList.size(); i++) {
			sb.append(labelList.get(i) + ": " + partList.get(i) + " ");
		}
		return sb.toString();
	}
	
	public String getPart(String label) {
		return getPart(label, 0);
	}
	
	public String getPart(String label, int index) {
		int count = 0;
		for(int i = 0; i < labelList.size(); i++) {
			if(label.equals(labelList.get(i))) {
				count++;
			}
			if(count > index) {
				return partList.get(i);
			}
		}
		return null;
	}
	
	public int getError(String label) {
		return getError(label, 0);
	}
	
	public int getError(String label, int index) {
		int count = 0;
		for(int i = 0; i < labelList.size(); i++) {
			if(label.equals(labelList.get(i))) {
				count++;
			}
			if(count > index) {
				return errorList.get(i);
			}
		}
		return 0;
	}
	
	public int getErrorBySeparator(String label, int index, String separatorLabel) {
		int count = 0;
		for(int i = 0; i < labelList.size(); i++) {
			if(separatorLabel.equals(labelList.get(i))) {
				count++;
			}
			if(count == index && label.equals(labelList.get(i))) {
				return errorList.get(i);
			}
		}
		return 0;
	}
	
	public String getPartBySeparator(String label, int index, String separatorLabel) {
		int count = 0;
		for(int i = 0; i < labelList.size(); i++) {
			if(separatorLabel.equals(labelList.get(i))) {
				count++;
			}
			if(count == index && label.equals(labelList.get(i))) {
				return partList.get(i);
			}
		}
		return null;
	}
	
	public int getPartCount(String label) {
		int count = 0;
		for(int i = 0; i < labelList.size(); i++) {
			if(label == labelList.get(i)) {
				count++;
			}
		}
		return count;
	}
	
	public List<String> getNonWords() {
		return nonWords;
	}
	
}
