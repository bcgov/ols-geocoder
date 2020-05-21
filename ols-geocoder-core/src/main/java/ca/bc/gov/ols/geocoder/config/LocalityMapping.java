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

public class LocalityMapping implements Comparable<LocalityMapping> {

	private int localityId;
	private String inputString;
	private int confidence;
	
	public LocalityMapping(int localityId, String inputString, int confidence) {
		this.localityId = localityId;
		this.inputString = inputString;
		this.confidence = confidence;
	}

	public LocalityMapping(RowReader rr) {
		localityId = rr.getInt("locality_id");
		inputString = rr.getString("input_string");
		confidence = rr.getInt("confidence");
	}
	
	public LocalityMapping(JsonReader jsonReader, List<String> messages) 
			throws IOException {
		jsonReader.beginObject();
		while(jsonReader.hasNext()) {
			switch(jsonReader.nextName()) {
			case "locality_id":
				localityId = jsonReader.nextInt();
				break;
			case "input_string":
				inputString = jsonReader.nextString();
				break;
			case "confidence":
				confidence = jsonReader.nextInt();
				break;
			default:
				messages.add("Unexpected key/value: " + jsonReader.getPath() 
						+ " = " + jsonReader.nextString());
			}
		}
		jsonReader.endObject();
	}

	public int getLocalityId() {
		return localityId;
	}

	public String getInputString() {
		return inputString;
	}

	public int getConfidence() {
		return confidence;
	}

	public void setConfidence(int confidence) {
		this.confidence = confidence;
	}

	@Override
	public int compareTo(LocalityMapping other) {
		int comp = Integer.compare(localityId, other.localityId);
		if(comp == 0) {
			comp = inputString.compareTo(other.inputString);
		}
		return comp;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof LocalityMapping) {
			LocalityMapping o = (LocalityMapping)other;
			if(localityId == o.localityId 
					&& inputString.equals(o.inputString)
					&& confidence == o.confidence) {
				return true;
			}
		}
		return false;
	}

	public boolean sameMappingAs(LocalityMapping other) {
		if(localityId == other.localityId 
				&& inputString.equals(other.inputString)) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return ("" + localityId + inputString + confidence).hashCode();
	}

}
