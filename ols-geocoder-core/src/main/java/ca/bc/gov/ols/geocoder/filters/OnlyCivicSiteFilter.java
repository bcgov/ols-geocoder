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

import ca.bc.gov.ols.geocoder.data.CivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.ISite;

public class OnlyCivicSiteFilter implements Filter<ISite> {
	
	// we only ever need one of these; singleton
	private static OnlyCivicSiteFilter it = new OnlyCivicSiteFilter();
	
	public static OnlyCivicSiteFilter get() {
		return it;
	}

	private OnlyCivicSiteFilter() {
	}
	
	@Override
	public boolean pass(ISite site) {
		if(site.getPrimaryAccessPoint() instanceof CivicAccessPoint) {
			return true;
		}
		return false;
	}

}
