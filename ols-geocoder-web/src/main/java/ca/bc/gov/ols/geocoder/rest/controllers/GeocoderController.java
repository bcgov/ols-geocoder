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
package ca.bc.gov.ols.geocoder.rest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.Interpolation;
import ca.bc.gov.ols.geocoder.rest.GeotoolsGeometryReprojector;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

@RestController
@CrossOrigin
public class GeocoderController {
	final static Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ GeocoderController.class.getCanonicalName());
	
	@Autowired
	private IGeocoder geocoder;
	
	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public void geocoder() {
		GeocodeQuery query = new GeocodeQuery("BC");
		geocoder.geocode(query);
	}

	@RequestMapping(value = "/addresses", method = RequestMethod.GET)
	public OlsResponse geocoder(GeocodeQuery query, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		query.setIncludeOccupants(false);
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		query.resolveAndValidate(config,
				new GeometryFactory(new PrecisionModel(), query.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		
		// do the geocoding
		SearchResults results = geocoder.geocode(query);
		results.setInterpolation(query.getInterpolation());
		OlsResponse response = new OlsResponse(results);
		response.setParams(query);
		return response;
	}
	
	@RequestMapping(value = "/sites", method = RequestMethod.GET)
	public OlsResponse siteGeocoder(GeocodeQuery query, BindingResult errors) {
		// sites is just an alias for no interpolation
		query.setInterpolation(Interpolation.NONE);
		
		query.resolveAndValidate(geocoder.getDatastore().getConfig(),
				new GeometryFactory(new PrecisionModel(), query.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		
		// do the geocoding
		SearchResults results = geocoder.geocode(query);
		
		OlsResponse response = new OlsResponse(results);
		response.setParams(query);
		return response;
	}

// For Instant Batch	
//	@RequestMapping(value = "/batch", method = RequestMethod.POST)
//	public GeocoderBatchProcessor batch(GeocodeParameters params, BindingResult bindingResult) {
//			if(bindingResult.hasErrors()) {
//				throw new InvalidParameterException(bindingResult);
//			}
//			params.setIncludeOccupants(false);
//			GeocoderConfig config = geocoder.getDatastore().getConfig();
//			params.resolveAndValidate(config,
//					new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
//					new GeotoolsGeometryReprojector());
//		GeocoderBatchProcessor proc = new GeocoderBatchProcessor(params, geocoder);
//		return proc;
//	}
		
}
