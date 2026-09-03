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

import java.util.EnumSet;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.rest.GeotoolsGeometryReprojector;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.converters.UuidParam;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.util.StopWatch;

@RestController
@RequestMapping("/occupants")
@CrossOrigin
@Tag(name = "occupants", description = "Occupant and business name resources")
public class OccupantController {

	@Autowired
	private IGeocoder geocoder;
	
	@Operation(
		summary = "Find addresses for an occupant search",
		description = "Geocodes an address string and returns matching physical addresses with occupant (business name) information. "
				+ "Results include the full address, occupancy info, and geographically matched location. "
				+ "If no matchPrecision is specified, defaults to OCCUPANT precision."
	)
	@RequestMapping(value = "/addresses", method = RequestMethod.GET)
	public OlsResponse geocoder(
			@ParameterObject GeocodeQuery query, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		query.setIncludeOccupants(true);
		// default the matchPrecision to OCCUPANT unless other matchPrecisions are specified
		if((query.getMatchPrecision() == null || query.getMatchPrecision().isEmpty())
				&& (query.getMatchPrecisionNot() == null || query.getMatchPrecisionNot().isEmpty())) {
			query.setMatchPrecision(EnumSet.of(MatchPrecision.OCCUPANT));		
		}
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		query.resolveAndValidate(config,
				new GeometryFactory(new PrecisionModel(), query.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		
		// do the geocoding
		String addrString = query.getAddressString();
		if(addrString != null && !addrString.isEmpty()
				&& !query.getAddressString().contains("**")) {
			query.setAddressString(addrString + " **");
		}
		SearchResults results = geocoder.geocode(query);
		results.setInterpolation(query.getInterpolation());
		OlsResponse response = new OlsResponse(results);
		response.setParams(query);
		response.setExtraInfo("occupantQuery", "true");
		return response;
	}

	@Operation(
		summary = "Find an occupant by ID",
		description = "Returns a specific occupant (business) based on its unique ID. "
				+ "The ID can be either the occupant's UUID or its legacy numeric ID. "
				+ "Returns a GeoJSON FeatureCollection containing the occupant address as a Point, "
				+ "with properties including the business name, civic address, and location metadata."
	)
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public OlsResponse getOccupant(
			@Parameter(description = "The occupant's UUID or legacy numeric ID", required = true,
					example = "06df4394-e127-4f03-8f6d-e99bb4a98df4")
			@PathVariable("id") String id,
			@ParameterObject SharedParameters params, BindingResult bindingResult) {
		UuidParam uuid = new UuidParam(id);
		if(uuid.getErrorMessage() != null) {
			throw new InvalidParameterException(uuid.getErrorMessage());
		}
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		
		OccupantAddress addr = geocoder.getDatastore().getOccupantByUuid(uuid.getValue(),
				params.getLocationDescriptor(), params.getSetBack());
		
		OlsResponse response;
		if(addr == null) {
			response = new OlsResponse(new OccupantAddress[0]);
		} else {
			response = new OlsResponse(addr);
		}
		
		response.setParams(params);
		response.setExtraInfo("occupantQuery", "true");
		return response;
	}
		
	@Operation(
		summary = "Find the occupant nearest to a point",
		description = "Finds the single occupant (business) nearest to the specified point. "
				+ "Optionally filter by tags and location descriptor. "
				+ "Returns a GeoJSON FeatureCollection containing the nearest occupant as a Point, "
				+ "with properties including the business name, civic address, and location metadata."
	)
	@RequestMapping(value = "/nearest", method = RequestMethod.GET)
	public OlsResponse getNearestOccupant(
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
		List<OccupantAddress> addrs = geocoder.getDatastore().getNearestNOccupants(1, params.getTags(),
				params.getPoint(), params.getMaxDistance(), params.getLocationDescriptor(), 
				params.getSetBack());
		sw.stop();

		OlsResponse response;
		if(addrs.size() < 1) {
			response = new OlsResponse(new OccupantAddress[0]);
		} else {
			response = new OlsResponse(addrs.get(0));
		}
		response.setParams(params);
		response.setExtraInfo("tags", params.getTags());
		response.setExtraInfo("occupantQuery", "true");
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}
	
	@Operation(
		summary = "Find multiple occupants nearest to a point",
		description = "Finds up to maxResults occupants (businesses) nearest to the specified point. "
				+ "Optionally filter by tags and location descriptor. "
				+ "Returns a GeoJSON FeatureCollection containing the nearest occupants as Points, "
				+ "with properties including business names, civic addresses, and location metadata."
	)
	@RequestMapping(value = "/near", method = RequestMethod.GET)
	public OlsResponse getOccupantsNear(
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
		List<OccupantAddress> addrs = geocoder.getDatastore().getNearestNOccupants(
				maxResults, params.getTags(), params.getPoint(), params.getMaxDistance(),
				params.getLocationDescriptor(), params.getSetBack());
		sw.stop();
		
		OlsResponse response = new OlsResponse(addrs.toArray(new OccupantAddress[addrs.size()]));
		response.setParams(params);
		response.setExtraInfo("tags", params.getTags());
		response.setExtraInfo("occupantQuery", "true");
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}
	
	@Operation(
		summary = "Find occupants within a bounding box",
		description = "Finds up to maxResults occupants (businesses) that are within the specified bounding box. "
				+ "Optionally filter by tags and location descriptor. "
				+ "Returns a GeoJSON FeatureCollection containing the occupants as Points, "
				+ "with properties including business names, civic addresses, and location metadata."
	)
	@RequestMapping(value = "/within", method = RequestMethod.GET)
	public OlsResponse getOccupantsWithin(
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
		List<OccupantAddress> addrs = geocoder.getDatastore().getOccupantsWithin(
				maxResults, params.getTags(), params.getBbox(), params.getLocationDescriptor(),
				params.getSetBack());
		sw.stop();
		OlsResponse response = new OlsResponse(addrs.toArray(new OccupantAddress[addrs.size()]));
		response.setParams(params);
		response.setExtraInfo("tags", params.getTags());
		response.setExtraInfo("occupantQuery", "true");
		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
		return response;
	}

	@Operation(
		summary = "Find matching occupant tags",
		description = "Returns a list of occupant tags (business names or categories) that match the given search string. "
				+ "Useful for auto-complete or tag lookup. Returns up to maxResults matching tags."
	)
	@RequestMapping(value = "/tags", method = RequestMethod.GET)
	public List<String> getTags(
			@Parameter(description = "The tag search string to match (prefix match).",
					example = "restaurant")
			@RequestParam(value="tag", required=false, defaultValue="") String tag, 
			@Parameter(description = "The maximum number of matching tags to return.",
					schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "6"))
			@RequestParam(value="maxResults", required=false, defaultValue="6") int maxResults) {
		return geocoder.getDatastore().getTags(tag, maxResults);
	}
}
