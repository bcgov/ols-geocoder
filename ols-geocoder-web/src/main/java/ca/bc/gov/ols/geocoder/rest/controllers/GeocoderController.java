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

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.Interpolation;
import ca.bc.gov.ols.geocoder.rest.GeotoolsGeometryReprojector;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.geocoder.status.BasicStatus;

@RestController
@CrossOrigin
public class GeocoderController {
	final static Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ GeocoderController.class.getCanonicalName());
	
	@Autowired
	private IGeocoder geocoder;
	
	@Operation(
		summary = "Service root endpoint",
		description = "Root endpoint that performs a basic geocode of 'BC' to verify the service is operational. "
				+ "Returns the full search results as a GeoJSON FeatureCollection."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "A basic search result confirming the service is operational.")
	})
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public OlsResponse geocoderPing() {
		GeocodeQuery query = new GeocodeQuery("BC");
		SearchResults results = geocoder.geocode(query);
		OlsResponse response = new OlsResponse(results);
		response.setParams(query);
		return response;
	}
	
	@Operation(
		summary = "Service health check",
		description = "Simple health check endpoint that verifies the geocoder service is operational. "
				+ "Returns HTTP 200 OK if the service is running."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Service is running.")
	})
	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public void geocoder() {
		GeocodeQuery query = new GeocodeQuery("BC");
		geocoder.geocode(query);
	}

	@Operation(
		summary = "Geocode an address",
		description = "Geocodes an address string and returns matching physical addresses. "
				+ "Results include the full address, matched location, and scoring information. "
				+ "Supports fuzzy matching, auto-complete, and various precision filters. "
				+ "Returns a GeoJSON FeatureCollection of matching addresses."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "A list of matching sites or intersections and their physical locations. "
				+ "See https://github.com/bcgov/ols-geocoder/blob/gh-pages/geocoder-developer-guide.md#resource-representations-in-http-responses")
	})
	@RequestMapping(value = "/addresses", method = RequestMethod.GET)
	public OlsResponse geocoder(
			@ParameterObject GeocodeQuery query, BindingResult bindingResult) {
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
	
	@Operation(
		summary = "Geocode a site address",
		description = "Geocodes an address string and returns matching site addresses without interpolation. "
				+ "This is an alias for the /addresses endpoint with interpolation set to NONE. "
				+ "Returns a GeoJSON FeatureCollection of matching site addresses."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "A list of matching sites and their physical locations. "
				+ "See https://github.com/bcgov/ols-geocoder/blob/gh-pages/geocoder-developer-guide.md#resource-representations-in-http-responses")
	})
	@RequestMapping(value = "/sites", method = RequestMethod.GET)
	public OlsResponse siteGeocoder(
			@ParameterObject GeocodeQuery query, BindingResult errors) {
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
	
	@Operation(
		summary = "Service status",
		description = "Returns the current status of the geocoder service, including version information, "
				+ "data processing timestamps, and record counts for the road network, parcels, and site addresses."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "A JSON response containing details on Geocoder data and any startup warning or errors.")
	})
	@RequestMapping(value = "/status", method = {RequestMethod.GET})
	public BasicStatus status() {
		return new BasicStatus(geocoder.getStatus());
	}

// For Slow Query API 
//	@RequestMapping(value = "/status/slowQueries", method = {RequestMethod.GET})
//	public QueryLog[] statusByType() {
//		return geocoder.getStatus().slowQueries.get();
//	}
	
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
