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
package ca.bc.gov.ols.geocoder.rest.batch;

import java.io.IOException;

import ca.bc.gov.ols.rowreader.CsvRowReader;
import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.rest.controllers.GeocodeParameters;

// For Instant Batch
public class GeocoderBatchProcessor {

	private IGeocoder geocoder;
	private GeocodeParameters params;
	private CsvRowReader rr;
	private BatchStatsCalculator stats;

	public GeocoderBatchProcessor(GeocodeParameters params, IGeocoder geocoder) {
		this.params = params;
		this.geocoder = geocoder;
	}
	
	public void start() throws IOException {
		rr = new CsvRowReader(params.getFile().getInputStream(), GeocoderDataStore.getGeometryFactory());
		stats = new BatchStatsCalculator();
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

		// handle additional query parameters
//		qry.setMaxResults(params.getmaxResults);
//		qry.setSetBack(setBack);
//		qry.setMinScore(minScore);
//		qry.setQuickMatch(quickMatch);
//		qry.setEcho(echo);
//		qry.setInterpolation(interpolation);
//		qry.setLocationDescriptor(locationDescriptor);
//
//
//		qry.setEcho(echo);
		SearchResults results = geocoder.geocode(qry);
//		results.setInterpolation(qry.getInterpolation());
//		results.setSrsCode(outputSRS);
		
		stats.record(results.getExecutionTime().doubleValue(), results.getBestScore());
		return results;
	}
	
	public BatchStatsCalculator stop() {
		stats.stop();
		return stats;
	}
	
	public BatchStatsCalculator getStats() {
		return stats;
	}
}
