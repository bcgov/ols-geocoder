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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.data.ISite;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.PidsResponse;
import ca.bc.gov.ols.geocoder.rest.converters.UuidParam;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.geocoder.rest.exceptions.ErrorMessage;

@RestController
@RequestMapping("/parcels")
@CrossOrigin
public class ParcelController {
	
	@Autowired
	private IGeocoder geocoder;
	
	@RequestMapping(value = "/pids/{siteUuid}", method = RequestMethod.GET)
	public OlsResponse getPids(@PathVariable("siteUuid") String siteUuidStr,
			SharedParameters params, BindingResult bindingResult) {
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
		} else {
			pr = new PidsResponse(site.getUuid(), site.getPids());
		}
		
		OlsResponse response = new OlsResponse(pr);
		response.setParams(params);
		return response;
	}

	@RequestMapping(value = "/addresses", method = RequestMethod.GET)
	public OlsResponse getAddresses(@RequestParam("pids") String pids,
			SharedParameters params, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) throw new InvalidParameterException(bindingResult);
		List<SiteAddress> addresses = new ArrayList<SiteAddress>();
		for(String rawPid : pids.split(",")) {
			String pid = rawPid.trim();
			if(!pid.matches("[0-9]{9}")) {
				throw new InvalidParameterException(new ErrorMessage("Invalid PID: " + pid
						+ ". A PID must contain exactly 9 digits."));
			}
			SiteAddress address = geocoder.getDatastore().getSiteByPid(pid,
					params.getLocationDescriptor(), params.getSetBack());
			if(address == null) {
				throw new InvalidParameterException(new ErrorMessage("No civic address was found for PID: " + pid));
			}
			addresses.add(address);
		}
		OlsResponse response = new OlsResponse(addresses.toArray(new SiteAddress[addresses.size()]));
		response.setParams(params);
		return response;
	}
	
}
