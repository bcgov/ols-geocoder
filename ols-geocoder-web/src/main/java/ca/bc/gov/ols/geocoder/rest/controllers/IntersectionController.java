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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.rest.GeotoolsGeometryReprojector;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.converters.UuidParam;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.geocoder.rest.exceptions.NotFoundException;
import ca.bc.gov.ols.util.StopWatch;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

@RestController
@RequestMapping("/intersections")
@CrossOrigin
public class IntersectionController {
	
	@Autowired
	private IGeocoder geocoder;
	
	// :[a-zA-Z0-9-]+
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public OlsResponse getIntersection(@PathVariable("id") String id,
			SharedParameters params, BindingResult bindingResult) {
		UuidParam uuid = new UuidParam(id);
		if(uuid.getErrorMessage() != null) {
			throw new InvalidParameterException(uuid.getErrorMessage());
		}
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		
		StreetIntersectionAddress addr = geocoder.getDatastore().getIntersectionByUuid(
				uuid.getValue());
		if(addr == null) {
			throw new NotFoundException("No intersection found.");
		}
		
		OlsResponse response = new OlsResponse(addr);
		response.setParams(params);
		return response;
	}
	
	@RequestMapping(value = "/nearest", method = RequestMethod.GET)
	public OlsResponse getNearestIntersection(
			ReverseGeocodeParameters params, BindingResult bindingResult) {
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolveAndValidate(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		if(params.getPoint() == null) {
			String errMsg = "The point parameter must be provided.";
			throw new IllegalArgumentException(errMsg);
		}
		
		if(!config.getBaseSrsBounds().contains(params.getPoint())) {
			throw new IllegalArgumentException("point coordinates not same projection as outputSRS");
		}
		
		StopWatch sw = new StopWatch();
		sw.start();
		List<StreetIntersectionAddress> addrs = geocoder.getDatastore().getNearestNIntersections(1,
				params.getPoint(), params.getMaxDistance(),
				params.getMinDegree(), params.getMaxDegree());
		sw.stop();
		
		if(addrs.size() < 1) {
			throw new NotFoundException("No intersection found.");
		}
		OlsResponse response = new OlsResponse(addrs.get(0));
		response.setParams(params);
		response.setExtraInfo("minDegree", "" + params.getMinDegree());
		response.setExtraInfo("maxDegree", "" + params.getMaxDegree());
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}
	
	@RequestMapping(value = "/near", method = RequestMethod.GET)
	public OlsResponse getIntersectionsNear(
			ReverseGeocodeParameters params, BindingResult bindingResult) {
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolveAndValidate(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		if(params.getPoint() == null) {
			String errMsg = "The point parameter must be provided.";
			throw new IllegalArgumentException(errMsg);
		}
		
		if(!config.getBaseSrsBounds().contains(params.getPoint())) {
			throw new IllegalArgumentException("point coordinates not same projection as outputSRS");
		}
		
		Integer maxResults = params.getMaxResults();
		int maxMaxResults = config.getMaxWithinResults();
		if(maxResults == null || maxResults > maxMaxResults) {
			maxResults = maxMaxResults;
		}
		
		StopWatch sw = new StopWatch();
		sw.start();
		List<StreetIntersectionAddress> addrs = geocoder.getDatastore().getNearestNIntersections(
				maxResults, params.getPoint(), params.getMaxDistance(),
				params.getMinDegree(), params.getMaxDegree());
		sw.stop();
		
		OlsResponse response = new OlsResponse(
				addrs.toArray(new StreetIntersectionAddress[addrs.size()]));
		response.setParams(params);
		response.setExtraInfo("minDegree", "" + params.getMinDegree());
		response.setExtraInfo("maxDegree", "" + params.getMaxDegree());
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}
	
	@RequestMapping(value = "/within", method = RequestMethod.GET)
	public OlsResponse getIntersectionsWithin(
			ReverseGeocodeParameters params, BindingResult bindingResult) {
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolveAndValidate(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		
		if(params.getBbox() == null) {
			String errMsg = "The bbox parameter must be provided.";
			throw new IllegalArgumentException(errMsg);
		}
		if(!config.getBaseSrsBounds().intersects(params.getBbox())) {
			throw new IllegalArgumentException("bbox coordinates not same projection as outputSRS");
		}
		
		Integer maxResults = params.getMaxResults();
		int maxMaxResults = config.getMaxWithinResults();
		if(maxResults == null || maxResults > maxMaxResults) {
			maxResults = maxMaxResults;
		}
		
		StopWatch sw = new StopWatch();
		sw.start();
		List<StreetIntersectionAddress> addrs = geocoder.getDatastore().getIntersectionsWithin(
				maxResults, params.getBbox(), params.getMinDegree(), params.getMaxDegree());
		sw.stop();
		
		OlsResponse response = new OlsResponse(
				addrs.toArray(new StreetIntersectionAddress[addrs.size()]));
		response.setParams(params);
		response.setExtraInfo("minDegree", "" + params.getMinDegree());
		response.setExtraInfo("maxDegree", "" + params.getMaxDegree());
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
		
	}
	
}
