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
package ca.bc.gov.ols.geocoder.filters;

import ca.bc.gov.ols.geocoder.data.IOccupant;

public class TagFilter implements Filter<IOccupant> {
	
	private String[] tags;
	
	public TagFilter(String tagString) {
		if(tagString == null || tagString.isEmpty()) {
			tags = new String[0];
		} else {
			tags = tagString.toLowerCase().split(";");
		}
	}
	
	@Override
	public boolean pass(IOccupant occ) {
		for(String tag : tags) {
			if(occ.getKeywords().contains(tag)) {
				return true;
			}
		}
		return false;
	}

}
