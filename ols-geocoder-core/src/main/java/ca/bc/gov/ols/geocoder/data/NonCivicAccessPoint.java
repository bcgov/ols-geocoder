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
package ca.bc.gov.ols.geocoder.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

import org.locationtech.jts.geom.Point;

public class NonCivicAccessPoint extends AccessPoint {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ NonCivicAccessPoint.class.getCanonicalName());
	
	private StreetSegment streetSegment;
	private final Locality locality;
	private final String electoralArea;
	
	public NonCivicAccessPoint(ISite site,
			Locality locality, String electoralArea,
			Point point,
			PositionalAccuracy positionalAccuracy,
			String narrativeLocation) {
		super(site, point, positionalAccuracy,
				narrativeLocation);
		this.locality = locality;
		this.electoralArea = electoralArea;
	}
	
	public StreetSegment getStreetSegment() {
		return streetSegment;
	}
	
	public void setStreetSegment(StreetSegment streetSegment) {
		if(this.streetSegment != null) {
			logger.error("Attempt to modify immutable NonCivicAccessPoint.streetSegment");
		} else {
			this.streetSegment = streetSegment;
		}
	}
	
	public Locality getLocality() {
		return locality;
	}
	
	public String getElectoralArea() {
		return electoralArea;
	}
}
