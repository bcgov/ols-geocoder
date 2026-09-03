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

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springdoc.core.annotations.ParameterObject;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.rest.GeotoolsGeometryReprojector;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.converters.UuidParam;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.util.StopWatch;

@RestController
@RequestMapping("/intersections")
@CrossOrigin
@Tag(name = "intersection", description = "The Intersection API")
public class IntersectionController {
	
	@Autowired
	private IGeocoder geocoder;
	
	@Operation(
		summary = "Find an intersection by ID",
		description = "Returns a specific intersection based on its unique ID. "
				+ "The ID can be either the intersection's UUID or its legacy numeric ID. "
				+ "Note that the returned location is not the actual intersection point, "
				+ "but a representative point on one of the intersecting streets. "
				+ "Returns a GeoJSON FeatureCollection containing the intersection as a Point, "
				+ "with properties including the site name, civic number ranges, and street metadata."
	)
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public OlsResponse getIntersection(
			@Parameter(description = "The intersection's UUID or legacy numeric ID", required = true,
					example = "74628473-1460-4d5f-8699-34964e8d3ba6")
			@PathVariable("id") String id,
			@ParameterObject SharedParameters params, BindingResult bindingResult) {
		UuidParam uuid = new UuidParam(id);
		if(uuid.getErrorMessage() != null) {
			throw new InvalidParameterException(uuid.getErrorMessage());
		}
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		
		StreetIntersectionAddress addr = geocoder.getDatastore().getIntersectionByUuid(
				uuid.getValue());
		OlsResponse response = new OlsResponse(addr);
		response.setParams(params);
		return response;
	}
	
	@Operation(
		summary = "Find the intersection nearest to a point",
		description = "Finds the single intersection nearest to the specified point. "
				+ "The returned location is not the actual intersection point, but a representative point "
				+ "on one of the intersecting streets. "
				+ "Optionally filters by street classification and intersection angle. "
				+ "Returns a GeoJSON FeatureCollection containing the nearest intersection as a Point, "
				+ "with properties including the site name, civic number ranges, and street metadata."
	)
	@RequestMapping(value = "/nearest", method = RequestMethod.GET)
	public OlsResponse getNearestIntersection(
			@Parameter(description = "The X (longitude) and Y (latitude) coordinate of the search point, "
					+ "in the format 'x,y'. Must be in the same SRS as the outputSRS parameter.",
					required = true, example = "-123.0,49.0")
			@ParameterObject ReverseGeocodeParameters params, BindingResult bindingResult) {
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
		
		OlsResponse response;
		if(addrs.size() < 1) {
			response = new OlsResponse(new StreetIntersectionAddress[0]);
		} else { 
			response = new OlsResponse(addrs.get(0));
		}
		response.setParams(params);
		response.setExtraInfo("minDegree", "" + params.getMinDegree());
		response.setExtraInfo("maxDegree", "" + params.getMaxDegree());
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}
	
	@Operation(
		summary = "Find multiple intersections nearest to a point",
		description = "Finds up to maxResults intersections nearest to the specified point. "
				+ "The returned locations are not the actual intersection points, but representative points "
				+ "on one of the intersecting streets. "
				+ "Optionally filters by street classification and intersection angle. "
				+ "Returns a GeoJSON FeatureCollection containing the nearest intersections as Points, "
				+ "with properties including site names, civic number ranges, and street metadata."
	)
	@RequestMapping(value = "/near", method = RequestMethod.GET)
	public OlsResponse getIntersectionsNear(
			@Parameter(description = "The X (longitude) and Y (latitude) coordinate of the search point, "
					+ "in the format 'x,y'. Must be in the same SRS as the outputSRS parameter.",
					required = true, example = "-123.0,49.0")
			@ParameterObject ReverseGeocodeParameters params, BindingResult bindingResult) {
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
	
	@Operation(
		summary = "Find intersections within a bounding box",
		description = "Finds up to maxResults intersections that are within the specified bounding box. "
				+ "The returned locations are not the actual intersection points, but representative points "
				+ "on one of the intersecting streets. "
				+ "Returns a GeoJSON FeatureCollection containing the intersections as Points, "
				+ "with properties including site names, civic number ranges, and street metadata."
	)
	@RequestMapping(value = "/within", method = RequestMethod.GET)
	public OlsResponse getIntersectionsWithin(
			@Parameter(description = "The bounding box to search within, in the format 'minx,miny,maxx,maxy'. "
					+ "Must be in the same SRS as the outputSRS parameter.",
					required = true, example = "-123.1,49.2,-123.0,49.3")
			@ParameterObject ReverseGeocodeParameters params, BindingResult bindingResult) {
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
