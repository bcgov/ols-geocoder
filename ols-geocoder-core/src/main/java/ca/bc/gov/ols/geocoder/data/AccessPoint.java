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

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

/**
 * An AccessPoint identifies a location used to access a site. The concrete implementations Civic
 * and NonCivic give further context to the location (address on a street or just within a locality)
 * Note that the AccessPoint class is intended to be immutable - the location field is technically
 * mutable but we don't ever do that.
 * 
 * @author chodgson
 * 
 */
public abstract class AccessPoint extends LocationBase {
	
	private final ISite site;
	private final PositionalAccuracy positionalAccuracy;
	
	private final String narrativeLocation;
	// we never actually use these two
	//private final PhysicalStatus status;
	//private final Date retireDate;
	
	protected AccessPoint(ISite site, Point location,
			PositionalAccuracy positionalAccuracy,
			String narrativeLocation) {
		super(location);
		this.site = site;
		//this.location = location;
		this.positionalAccuracy = positionalAccuracy;
		this.narrativeLocation = narrativeLocation;
	}
	
	protected AccessPoint(AccessPoint ap, Point location, PositionalAccuracy positionalAccuracy) {
		super(location);
		this.site = ap.site;
		this.narrativeLocation = ap.narrativeLocation;
		this.positionalAccuracy = positionalAccuracy;
	}
	
	public ISite getSite() {
		return site;
	}
	
	public Point getPoint() {
		return GeocoderDataStore.getGeometryFactory().createPoint(this);
	}
	
	public PositionalAccuracy getPositionalAccuracy() {
		return positionalAccuracy;
	}
	
	public String getNarrativeLocation() {
		return narrativeLocation;
	}

}
