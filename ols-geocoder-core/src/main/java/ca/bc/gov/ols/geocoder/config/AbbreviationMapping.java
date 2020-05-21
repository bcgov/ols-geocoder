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
package ca.bc.gov.ols.geocoder.config;

import java.io.IOException;
import java.util.List;

import com.google.gson.stream.JsonReader;

import ca.bc.gov.ols.rowreader.RowReader;

public class AbbreviationMapping implements Comparable<AbbreviationMapping>{

	private String abbreviatedForm;
	private String longForm;

	public AbbreviationMapping(String abbreviatedForm, String longForm) {
		this.abbreviatedForm = abbreviatedForm;
		this.longForm = longForm;
	}
	
	public AbbreviationMapping(RowReader rr) {
		this.abbreviatedForm = rr.getString("abbreviated_form");
		this.longForm = rr.getString("long_form");
	}

	public AbbreviationMapping(JsonReader jsonReader, List<String> messages)
			throws IOException {
		jsonReader.beginObject();
		while(jsonReader.hasNext()) {
			switch(jsonReader.nextName()) {
			case "abbreviated_form":
				abbreviatedForm = jsonReader.nextString();
				break;
			case "long_form":
				longForm = jsonReader.nextString();
				break;
			default:
				messages.add("Unexpected key/value: " + jsonReader.getPath() 
						+ " = " + jsonReader.nextString());
			}
		}
		jsonReader.endObject();
	}

	public String getAbbreviatedForm() {
		return abbreviatedForm;
	}

	public String getLongForm() {
		return longForm;
	}

	/**
	 * Compares based on key only
	 */
	@Override
	public int compareTo(AbbreviationMapping other) {
		int comp = abbreviatedForm.compareTo(other.abbreviatedForm);
		if(comp == 0) {
			comp = longForm.compareTo(other.longForm);
		}
		return comp;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof AbbreviationMapping) {
			AbbreviationMapping o = (AbbreviationMapping)other;
			if(abbreviatedForm.equals(o.abbreviatedForm) 
					&& longForm.equals(o.longForm)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (abbreviatedForm + longForm).hashCode();
	}

}
