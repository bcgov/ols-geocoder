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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.data.StreetSegment;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.geocoder.rest.exceptions.NotFoundException;

@RestController
@RequestMapping("/blocks")
public class BlockController {
	
	@Autowired
	private IGeocoder geocoder;
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public OlsResponse getBlock(@PathVariable("id") int id,
			SharedParameters params, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		
		StreetSegment seg = geocoder.getDatastore().getStreetSegmentById(id);
		if(seg == null) {
			throw new NotFoundException("No block found.");
		}
		
		OlsResponse response = new OlsResponse(seg);
		response.setParams(params);
		return response;
	}

//  Opted out of this functionality for now.
//
//	@RequestMapping(value = "/nearestPoint", method = RequestMethod.GET)
//	public OlsResponse getNearestPoint(
//			ReverseGeocodeParameters params, BindingResult bindingResult) {
//		GeocoderConfig config = geocoder.getDatastore().getConfig();
//		if(bindingResult.hasErrors()) {
//			throw new InvalidParameterException(bindingResult);
//		}
//		params.resolveAndValidate(config,
//				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
//				new GeotoolsGeometryReprojector());
//		if(params.getPoint() == null) {
//			String errMsg = "The point parameter must be provided.";
//			throw new IllegalArgumentException(errMsg);
//		}
//		
//		if(!config.getBaseSrsBounds().contains(params.getPoint())) {
//			throw new IllegalArgumentException("point coordinates not same projection as outputSRS");
//		}
//		
//		StopWatch sw = new StopWatch();
//		sw.start();
//		List<StreetSegment> segs = geocoder.getDatastore().getNearestNStreetSegments(1,
//				params.getPoint(), params.getMaxDistance());
//		sw.stop();
//		
//		if(segs.size() < 1) {
//			throw new NotFoundException("No block found.");
//		}
//		StreetSegment closestSeg = segs.get(0);
//		
//		int setBack = params.getSetBack();
//		LocationDescriptor ld = params.getLocationDescriptor();
//		if(ld != LocationDescriptor.ROUTING_POINT 
//				&& ld != LocationDescriptor.ACCESS_POINT
//				&& ld != LocationDescriptor.BLOCK_FACE_POINT) {
//			ld = LocationDescriptor.ROUTING_POINT;
//		}
//		if(ld == LocationDescriptor.ROUTING_POINT) {
//			setBack = -1;
//		} else if(ld == LocationDescriptor.BLOCK_FACE_POINT) {
//			setBack = Integer.MIN_VALUE;
//		}
//		Point resultPoint = geocoder.getDatastore().getInterpolator().extrapolate(params.getPoint(), closestSeg, setBack);
//		
//		PointOnBlock pob = new PointOnBlock(closestSeg.getSegmentId(), resultPoint, params.getLocationDescriptor());
//		OlsResponse response = new OlsResponse(pob);
//		response.setCallback(params.getCallback());
//		response.setOutputSRS(params.getOutputSRS());
//		response.setExtraInfo("executionTime", "" + sw.getElapsedTime());
//		return response;
//	}
	
}
