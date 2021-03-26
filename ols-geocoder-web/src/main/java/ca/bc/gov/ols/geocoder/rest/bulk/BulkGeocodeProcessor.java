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
package ca.bc.gov.ols.geocoder.rest.bulk;

import java.io.IOException;
import java.time.LocalDateTime;

import ca.bc.gov.ols.rowreader.CsvRowReader;
import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.ModifiableLocation;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.rest.LocationReprojector;
import ca.bc.gov.ols.geocoder.rest.controllers.BulkGeocodeParameters;

public class BulkGeocodeProcessor {

	private IGeocoder geocoder;
	private BulkGeocodeParameters params;
	private CsvRowReader rr;
	private BulkStatsCalculator stats;
	private int fromSrsCode;
	private int toSrsCode;
	private LocationReprojector lr;

	public BulkGeocodeProcessor(BulkGeocodeParameters params, IGeocoder geocoder) {
		this.params = params;
		this.geocoder = geocoder;
		fromSrsCode =  geocoder.getConfig().getBaseSrsCode();
		toSrsCode = params.getOutputSRS();
		lr = new LocationReprojector(fromSrsCode, toSrsCode);
	}
	
	public void start(BulkStatsCalculator statsCalc) throws IOException {
		rr = new CsvRowReader(params.getFile().getInputStream(), GeocoderDataStore.getGeometryFactory());
		stats = statsCalc;
		stats.start();
		
	}
	
	public SearchResults next() {
		if(!rr.next()) {
			return null;
		}
		GeocodeQuery qry;

		String address = rr.getString("addressString");
		if(address != null && !address.isEmpty()) {
			qry = new GeocodeQuery(address);
		} else {
			qry = new GeocodeQuery();
			qry.setSiteName(rr.getString("siteName"));
			qry.setUnitDesignator(rr.getString("unitDesignator"));
			qry.setUnitNumber(rr.getString("unitNumber"));
			qry.setUnitNumberSuffix(rr.getString("unitNumberSuffix"));
			qry.setCivicNumber(rr.getString("civicNumber"));
			qry.setCivicNumberSuffix(rr.getString("civicNumberSuffix"));
			qry.setStreetName(rr.getString("streetName"));
			qry.setStreetType(rr.getString("streetType"));
			qry.setStreetDirection(rr.getString("streetDirection"));
			qry.setStreetQualifier(rr.getString("streetQualifier"));
			qry.setLocalityName(rr.getString("localityName"));
			qry.setStateProvTerr(rr.getString("province"));
		}

		String yourId = rr.getString("yourId");
		if(yourId != null) {
			qry.setYourId(yourId);
		}
		// handle additional query parameters
		qry.setMaxResults(params.getMaxResults());
		qry.setSetBack(params.getSetBack());
		qry.setMinScore(params.getMinScore());
		qry.setEcho(params.isEcho());
		qry.setInterpolation(params.getInterpolation());
		qry.setLocationDescriptor(params.getLocationDescriptor());
		qry.setOutputSRS(params.getOutputSRS());

		SearchResults results = geocoder.geocode(qry);
		if(toSrsCode != fromSrsCode) {
			lr.reproject(results.getMatches());
			lr.reprecision(results.getMatches());
		} else {
			lr.reprecision(results.getMatches());
		}
		
		results.setInterpolation(qry.getInterpolation());
		results.setSrsCode(params.getOutputSRS());
		
		stats.record(results.getExecutionTime().doubleValue(), results.getBestScore());
		return results;
	}
	
	public int getStartSeqNum() {
		return params.getStartSeqNum();
	}
	
	public BulkStatsCalculator stop() {
		stats.stop();
		return stats;
	}
	
	public BulkStatsCalculator getStats() {
		return stats;
	}

	public BulkGeocodeParameters getParams() {
		return params;
	}
}
