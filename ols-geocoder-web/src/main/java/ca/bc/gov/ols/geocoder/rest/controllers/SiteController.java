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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.rest.GeotoolsGeometryReprojector;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.converters.UuidParam;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.util.StopWatch;

@RestController
@RequestMapping("/sites")
@CrossOrigin
@Tag(name = "sites", description = "Physical site and address resources")
public class SiteController {
	
	@Autowired
	private IGeocoder geocoder;
	
	@Operation(
		summary = "Find a site by ID",
		description = "Returns a specific site address based on its unique ID. "
				+ "The ID can be either the site's UUID or its legacy numeric ID. "
				+ "Returns a GeoJSON FeatureCollection containing the site as a Point, "
				+ "with properties including the site name, civic number, street, and location metadata."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The site with the requested ID in Site Address Representation. "
				+ "See https://github.com/bcgov/ols-geocoder/blob/gh-pages/geocoder-developer-guide.md#site-address-representation")
	})
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public OlsResponse getSite(
			@Parameter(description = "The site's UUID or legacy numeric ID", required = true,
					example = "a810e87b-7f99-4898-a19c-1493e1d25e25")
			@PathVariable("id") String id,
			@ParameterObject SharedParameters params, BindingResult bindingResult) {
		UuidParam uuid = new UuidParam(id);
		if(uuid.getErrorMessage() != null) {
			throw new InvalidParameterException(uuid.getErrorMessage());
		}
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		
		SiteAddress addr = geocoder.getDatastore().getSiteByUuid(uuid.getValue(),
				params.getLocationDescriptor(), params.getSetBack());
		
		OlsResponse response;
		if(addr == null) {
			response = new OlsResponse(new SiteAddress[0]);
		} else {
			response = new OlsResponse(addr);
		}
		response.setParams(params);
		return response;
	}
	
	@Operation(
		summary = "Find sub-sites of a site",
		description = "Returns all sub-sites (child addresses) of a specific site, identified by its UUID. "
				+ "Sub-sites are individual units or suites within a multi-occupancy site. "
				+ "Returns a GeoJSON FeatureCollection containing the sub-sites as Points."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Returns all sub-sites of a given site in Site Address Representation. "
				+ "See https://github.com/bcgov/ols-geocoder/blob/gh-pages/geocoder-developer-guide.md#site-address-representation")
	})
	@RequestMapping(value = "/{id}/subsites", method = RequestMethod.GET)
	public OlsResponse getSubSites(
			@Parameter(description = "The parent site's UUID", required = true,
					example = "a810e87b-7f99-4898-a19c-1493e1d25e25")
			@PathVariable("id") String id,
			@ParameterObject SharedParameters params, BindingResult bindingResult) {
		UuidParam uuid = new UuidParam(id);
		if(uuid.getErrorMessage() != null) {
			throw new InvalidParameterException(uuid.getErrorMessage());
		}
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		
		List<SiteAddress> addrs = geocoder.getDatastore().getSubSitesByUuid(uuid.getValue(), 
				params.getLocationDescriptor(), params.getSetBack());
		
		OlsResponse response;
		if(addrs.size() == 0) {
			response = new OlsResponse(new SiteAddress[0]);
		} else {
			response = new OlsResponse(addrs.toArray(new SiteAddress[addrs.size()]));
		}
		response.setParams(params);
		return response;
	}
	
	@Operation(
		summary = "Find the site nearest to a point",
		description = "Finds the single site address nearest to the specified point. "
				+ "Optionally filters by location descriptor and exclusion of access points. "
				+ "Returns a GeoJSON FeatureCollection containing the nearest site as a Point, "
				+ "with properties including the site name, civic number, street, and location metadata."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The nearest site in Site Address Representation. "
				+ "See https://github.com/bcgov/ols-geocoder/blob/gh-pages/geocoder-developer-guide.md#site-address-representation")
	})
	@RequestMapping(value = "/nearest", method = RequestMethod.GET)
	public OlsResponse getNearestSite(
			@ParameterObject ReverseGeocodeParameters params, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		
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
		List<SiteAddress> addrs = geocoder.getDatastore().getNearestNSites(1, params.getPoint(),
				params.getMaxDistance(), params.getLocationDescriptor(), 
				params.getSetBack(), params.getExcludeUnits(), params.isOnlyCivic());
		sw.stop();
		
		OlsResponse response;
		if(addrs.size() < 1) {
			//throw new NotFoundException("No site found.");\
			response = new OlsResponse(new SiteAddress[0]);
		} else {
			response = new OlsResponse(addrs.get(0));
		}
		response.setParams(params);
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}
	
	@Operation(
		summary = "Find multiple sites nearest to a point",
		description = "Finds up to maxResults site addresses nearest to the specified point. "
				+ "Optionally filters by location descriptor and exclusion of access points. "
				+ "Returns a GeoJSON FeatureCollection containing the nearest sites as Points, "
				+ "with properties including site names, civic numbers, streets, and location metadata."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "A list of selected sites near a given point in order of closest to farthest. "
				+ "Each site is in Site Address Representation. "
				+ "See https://github.com/bcgov/ols-geocoder/blob/gh-pages/geocoder-developer-guide.md#site-address-representation")
	})
	@RequestMapping(value = "/near", method = RequestMethod.GET)
	public OlsResponse getSitesNear(
			@ParameterObject ReverseGeocodeParameters params, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		GeocoderConfig config = geocoder.getDatastore().getConfig();
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
		List<SiteAddress> addrs = geocoder.getDatastore().getNearestNSites(
				maxResults, params.getPoint(), params.getMaxDistance(),
				params.getLocationDescriptor(), params.getSetBack(), params.getExcludeUnits(), params.isOnlyCivic());
		sw.stop();
		
		OlsResponse response = new OlsResponse(addrs.toArray(new SiteAddress[addrs.size()]));
		response.setParams(params);
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}
	
	@Operation(
		summary = "Find sites within a bounding box",
		description = "Finds up to maxResults site addresses that are within the specified bounding box. "
				+ "Optionally filters by location descriptor and exclusion of access points. "
				+ "Returns a GeoJSON FeatureCollection containing the sites as Points, "
				+ "with properties including site names, civic numbers, streets, and location metadata."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "A list of selected sites within the given area. "
				+ "Each site is in Site Address Representation. "
				+ "See https://github.com/bcgov/ols-geocoder/blob/gh-pages/geocoder-developer-guide.md#site-address-representation")
	})
	@RequestMapping(value = "/within", method = RequestMethod.GET)
	public OlsResponse getSitesWithin(
			@ParameterObject ReverseGeocodeParameters params, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		
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
		List<SiteAddress> addrs = geocoder.getDatastore().getSitesWithin(
				maxResults, params.getBbox(), params.getLocationDescriptor(),
				params.getSetBack(), params.getExcludeUnits(), params.isOnlyCivic());
		sw.stop();
		OlsResponse response = new OlsResponse(addrs.toArray(new SiteAddress[addrs.size()]));
		response.setParams(params);
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}
	
}
