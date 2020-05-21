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
package ca.bc.gov.ols.admin;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Comparator;

import ca.bc.gov.ols.geocoder.config.LocalityMapping;

public class LocalityMappingComparator implements Comparator<LocalityMapping> {
	
	private String orderBy;
	private TIntObjectHashMap<String> localityIdMap;
	
	public LocalityMappingComparator(String orderBy, TIntObjectHashMap<String> localityIdMap) {
		this.orderBy = orderBy != null ? orderBy : "input_string";
		this.localityIdMap = localityIdMap;
	}

	@Override
	public int compare(LocalityMapping lm1, LocalityMapping lm2) {
		int diff;
		switch(orderBy) {
		case "locality":
			// locality name, input_string, confidence
			diff = localityIdMap.get(lm1.getLocalityId())
					.compareTo(localityIdMap.get(lm2.getLocalityId()));
			if(diff == 0) {
				diff = lm1.getInputString().toLowerCase()
						.compareTo(lm2.getInputString().toLowerCase());
			}
			if(diff == 0) {
				diff = lm1.getConfidence() - lm2.getConfidence();
			}
			return diff;
		case "confidence":
			// confidence, locality_id, input_string
			diff = lm1.getConfidence() - lm2.getConfidence();
			if(diff == 0) {
				diff = localityIdMap.get(lm1.getLocalityId())
						.compareTo(localityIdMap.get(lm2.getLocalityId()));
			}
			if(diff == 0) {
				diff = lm1.getInputString().toLowerCase()
						.compareTo(lm2.getInputString().toLowerCase());
			}
			return diff;
		case "input_string":	
		default:
			// input_string, locality_id, confidence
			diff = lm1.getInputString().toLowerCase()
					.compareTo(lm2.getInputString().toLowerCase());
			if(diff == 0) {
				diff = localityIdMap.get(lm1.getLocalityId())
						.compareTo(localityIdMap.get(lm2.getLocalityId()));
			}
			if(diff == 0) {
				diff = lm1.getConfidence() - lm2.getConfidence();
			}
			return diff;
		}
	}	

}
