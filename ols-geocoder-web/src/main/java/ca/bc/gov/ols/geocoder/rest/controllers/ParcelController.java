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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.data.ISite;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.PidsResponse;
import ca.bc.gov.ols.geocoder.rest.converters.UuidParam;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.rowreader.DateType;

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
		long startNanos = System.nanoTime();
		for(String rawPid : pids.split(",")) {
			String pid = rawPid.trim();
			SiteAddress address = new SiteAddress();
			address.setPid(pid);
			if(!pid.matches("[0-9]{9}")) {
				address.setError("Invalid PID: " + pid
						+ ". A PID must contain exactly 9 digits.");
				addresses.add(address);
				continue;
			}
			address = geocoder.getDatastore().getSiteByPid(pid,
					params.getLocationDescriptor(), params.getSetBack());
			if(address == null) {
				address = new SiteAddress();
				address.setPid(pid);
				address.setError("No civic address was found for PID: " + pid);
			}
			addresses.add(address);
		}
		OlsResponse response = new OlsResponse(addresses.toArray(new SiteAddress[addresses.size()]));
		response.setParams(params);
		response.setExtraInfo("encoding", "utf-8");
		response.setExtraInfo("searchTimestamp", LocalDateTime.now().toString());
		response.setExtraInfo("executionTime", String.valueOf((System.nanoTime() - startNanos) / 1000000f));
		response.setExtraInfo("version", ca.bc.gov.ols.geocoder.config.GeocoderConfig.VERSION);
		ZonedDateTime baseDataDate = geocoder.getDatastore().getDate(DateType.PROCESSING_DATE);
		response.setExtraInfo("baseDataDate", baseDataDate == null ? "" : baseDataDate.toString());
		response.setExtraInfo("disclaimer", geocoder.getDatastore().getConfig().getDisclaimer());
		response.setExtraInfo("privacyStatement", geocoder.getDatastore().getConfig().getPrivacyStatement());
		response.setExtraInfo("copyrightNotice", geocoder.getDatastore().getConfig().getCopyrightNotice());
		return response;
	}
	
}
