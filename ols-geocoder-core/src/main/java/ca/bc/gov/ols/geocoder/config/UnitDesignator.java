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

public class UnitDesignator implements Comparable<UnitDesignator>{

	private String canonicalForm;
	
	public UnitDesignator(String canonicalForm) {
		this.canonicalForm = canonicalForm;
	}
	
	public UnitDesignator(RowReader rr) {
		this.canonicalForm = rr.getString("canonical_form");
	}
	public UnitDesignator(JsonReader jsonReader, List<String> messages)
			throws IOException {
		jsonReader.beginObject();
		while(jsonReader.hasNext()) {
			switch(jsonReader.nextName()) {
			case "canonical_form":
				canonicalForm = jsonReader.nextString();
				break;
			default:
				messages.add("Unexpected key/value: " + jsonReader.getPath() 
						+ " = " + jsonReader.nextString());
			}
		}
		jsonReader.endObject();
	}

	public String getCanonicalForm() {
		return canonicalForm;
	}

	/**
	 * Compares key values only.
	 */
	@Override
	public int compareTo(UnitDesignator other) {
		return canonicalForm.compareTo(other.canonicalForm);
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof UnitDesignator) {
			UnitDesignator o = (UnitDesignator)other;
			if(canonicalForm.equals(o.canonicalForm)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return canonicalForm.hashCode();
	}


}
