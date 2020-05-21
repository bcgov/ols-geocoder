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

import ca.bc.gov.ols.geocoder.data.AccessPoint;
import ca.bc.gov.ols.geocoder.data.CivicAccessPoint;

public class OnlyCivicAccessPointFilter implements Filter<AccessPoint> {
	
	// we only ever need one of these; singleton
	private static OnlyCivicAccessPointFilter it = new OnlyCivicAccessPointFilter();
	
	public static OnlyCivicAccessPointFilter get() {
		return it;
	}

	private OnlyCivicAccessPointFilter() {
	}
	
	@Override
	public boolean pass(AccessPoint ap) {
		if(ap instanceof CivicAccessPoint) {
			return true;
		}
		return false;
	}

}
