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
import ca.bc.gov.ols.geocoder.data.ISite;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.PidsResponse;
import ca.bc.gov.ols.geocoder.rest.converters.UuidParam;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;

@RestController
@RequestMapping("/parcels")
@CrossOrigin
@Tag(name = "parcels", description = "Parcel and PID lookup resources")
public class ParcelController {
	
	@Autowired
	private IGeocoder geocoder;
	
	@Operation(
		summary = "Find PIDs for a site",
		description = "Returns the Property Identifier (PID) and related parcel information for a specific site, "
				+ "identified by its UUID. Results include the site UUID and a comma-separated list of PIDs."
	)
	@RequestMapping(value = "/pids/{siteUuid}", method = RequestMethod.GET)
	public OlsResponse getPids(
			@Parameter(description = "The site's UUID", required = true,
					example = "a810e87b-7f99-4898-a19c-1493e1d25e25")
			@PathVariable("siteUuid") String siteUuidStr,
			@ParameterObject SharedParameters params, BindingResult bindingResult) {
		UuidParam siteUuid = new UuidParam(siteUuidStr);
		if(siteUuid.getErrorMessage() != null) {
			throw new InvalidParameterException(siteUuid.getErrorMessage());
		}
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		
		ISite site = geocoder.getDatastore().getRawSiteByUuid(siteUuid.getValue());
		
		PidsResponse pr;
		if(site == null) {
			pr = new PidsResponse(null, null);
		}
		
		pr = new PidsResponse(site.getUuid(), site.getPids());
		
		OlsResponse response = new OlsResponse(pr);
		response.setParams(params);
		return response;
	}
	
}
