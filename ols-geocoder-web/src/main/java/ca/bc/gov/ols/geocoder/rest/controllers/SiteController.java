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
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
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
@RequestMapping("/sites")
@CrossOrigin
public class SiteController {
	
	@Autowired
	private IGeocoder geocoder;
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public OlsResponse getSite(@PathVariable("id") String id,
			SharedParameters params, BindingResult bindingResult) {
		UuidParam uuid = new UuidParam(id);
		if(uuid.getErrorMessage() != null) {
			throw new InvalidParameterException(uuid.getErrorMessage());
		}
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		
		SiteAddress addr = geocoder.getDatastore().getSiteByUuid(uuid.getValue(),
				params.getLocationDescriptor(), params.getSetBack());
		if(addr == null) {
			throw new NotFoundException("No site found.");
		}
		
		OlsResponse response = new OlsResponse(addr);
		response.setParams(params);
		return response;
	}
	
	@RequestMapping(value = "/{id}/subsites", method = RequestMethod.GET)
	public OlsResponse getSubSites(@PathVariable("id") String id,
			SharedParameters params, BindingResult bindingResult) {
		UuidParam uuid = new UuidParam(id);
		if(uuid.getErrorMessage() != null) {
			throw new InvalidParameterException(uuid.getErrorMessage());
		}
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		
		List<SiteAddress> addrs = geocoder.getDatastore().getSubSitesByUuid(uuid.getValue(), 
				params.getLocationDescriptor(), params.getSetBack());
		if(addrs.size() == 0) {
			throw new NotFoundException("No site found.");
		}
		OlsResponse response = new OlsResponse(addrs.toArray(new SiteAddress[addrs.size()]));
		response.setParams(params);
		return response;
	}
	
	@RequestMapping(value = "/nearest", method = RequestMethod.GET)
	public OlsResponse getNearestSite(ReverseGeocodeParameters params, BindingResult bindingResult) {
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
		
		if(addrs.size() < 1) {
			throw new NotFoundException("No site found.");
		}
		OlsResponse response = new OlsResponse(addrs.get(0));
		response.setParams(params);
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}
	
	@RequestMapping(value = "/near", method = RequestMethod.GET)
	public OlsResponse getSitesNear(ReverseGeocodeParameters params, BindingResult bindingResult) {
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
	
	@RequestMapping(value = "/within", method = RequestMethod.GET)
	public OlsResponse getSitesWithin(ReverseGeocodeParameters params, BindingResult bindingResult) {
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
