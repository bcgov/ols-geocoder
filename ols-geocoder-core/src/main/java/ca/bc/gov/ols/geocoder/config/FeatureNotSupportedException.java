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

import ca.bc.gov.ols.geocoder.data.enumTypes.GeocoderFeature;

public class FeatureNotSupportedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	// private GeocoderFeature feature;

	public FeatureNotSupportedException(GeocoderFeature feature) {
		super("The requested feature(" + feature + ") is not supported.");
		// this.feature = feature;
	}
	
}
