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
package ca.bc.gov.ols.geocoder;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;

/**
 * A "dummy" implementation of the Geocder interface, that always mirrors its input into its output
 * and returns an arbitrary point.
 * 
 * @author chodgson
 * 
 */
public class DummyGeocoder implements IGeocoder {
	
	private GeometryFactory geometryFactory;
	
	public DummyGeocoder(GeometryFactory geometryFactory) {
		this.geometryFactory = geometryFactory;
	}
	
	@Override
	public SearchResults geocode(GeocodeQuery query) {
		return getDummyResults(query, geometryFactory);
	}
	
	@Override
	public GeocoderDataStore getDatastore() {
		// TODO return a dummy datastore here
		return null;
	}
	
	public static SearchResults getDummyResults(GeocodeQuery query, GeometryFactory geometryFactory) {
		List<GeocodeMatch> matches = new ArrayList<GeocodeMatch>();
		SiteAddress siteAddress = new SiteAddress();
		if(query.getAddressString() != null) {
			siteAddress.setStreetName(query.getAddressString());
		} else {
			siteAddress.setSiteName(query.getSiteName());
			siteAddress.setUnitNumber(query.getUnitNumber());
			siteAddress.setUnitNumberSuffix(query.getUnitNumberSuffix());
			siteAddress.setUnitDesignator(query.getUnitDesignator());
			siteAddress.setCivicNumber(GeocoderUtil.parseCivicNumber(query.getCivicNumber()));
			siteAddress.setCivicNumberSuffix(query.getCivicNumberSuffix());
			siteAddress.setStreetName(query.getStreetName());
			siteAddress.setStreetType(query.getStreetType());
			siteAddress.setStreetDirection(query.getStreetDirection());
			siteAddress.setStreetQualifier(query.getStreetQualifier());
			siteAddress.setLocalityName(query.getLocalityName());
			siteAddress.setStateProvTerr(query.getStateProvTerr());
		}
		siteAddress.setLocation(geometryFactory.createPoint(
				new Coordinate(1195385.285, 382490.195)));
		siteAddress.setLocationPositionalAccuracy(PositionalAccuracy.LOW);
		siteAddress.setLocationDescriptor(LocationDescriptor.ACCESS_POINT);
		GeocodeMatch gm = new AddressMatch(siteAddress, MatchPrecision.BLOCK, 99);
		gm.setYourId(query.getYourId());
		matches.add(gm);
		return new SearchResults(query, matches, null);
	}
	
	@Override
	public GeocoderConfig getConfig() {
		return new GeocoderConfig();
	}
	
}
