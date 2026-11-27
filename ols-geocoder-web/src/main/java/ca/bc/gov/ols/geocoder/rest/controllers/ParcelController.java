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

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.data.ISite;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.PidsResponse;
import ca.bc.gov.ols.geocoder.rest.converters.UuidParam;
import ca.bc.gov.ols.geocoder.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.rowreader.DateType;

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
	@ApiResponse(responseCode = "200", description = "A comma-separated string containing all the parcel identifiers associated with the requested site ID.")
	@GetMapping("/pids/{siteUuid}")
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
		
		GeocoderDataStore datastore = geocoder.getDatastore();
		ISite site = datastore == null ? null : datastore.getRawSiteByUuid(siteUuid.getValue());
		
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

	@GetMapping("/addresses")
	public OlsResponse getAddresses(@RequestParam("pids") String pids,
			SharedParameters params, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) throw new InvalidParameterException(bindingResult);
		GeocoderDataStore datastore = geocoder.getDatastore();
		List<SiteAddress> addresses = new ArrayList<SiteAddress>();
		long startNanos = System.nanoTime();
		for(String rawPid : pids.split(",")) {
			String pid = rawPid.trim();
			SiteAddress address = null;
			if(!pid.matches("\\d{9}")) {
				address = new SiteAddress();
				address.setPid(pid);
				address.setError("Invalid PID: " + pid
						+ ". A PID must contain exactly 9 digits.");
				addresses.add(address);
				continue;
			}
			if(datastore != null) {
				address = datastore.getSiteByPid(pid,
						params.getLocationDescriptor(), params.getSetBack());
			}
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
		response.setExtraInfo("searchTimestamp", LocalDateTime.now(Clock.systemUTC()).toString());
		response.setExtraInfo("executionTime", String.valueOf((System.nanoTime() - startNanos) / 1000000f));
		response.setExtraInfo("version", ca.bc.gov.ols.geocoder.config.GeocoderConfig.VERSION);
		ZonedDateTime baseDataDate = datastore == null ? null : datastore.getDate(DateType.PROCESSING_DATE);
		response.setExtraInfo("baseDataDate", baseDataDate == null ? "" : baseDataDate.toString());
		response.setExtraInfo("disclaimer", datastore == null ? "" : datastore.getConfig().getDisclaimer());
		response.setExtraInfo("privacyStatement", datastore == null ? "" : datastore.getConfig().getPrivacyStatement());
		response.setExtraInfo("copyrightNotice", datastore == null ? "" : datastore.getConfig().getCopyrightNotice());
		return response;
	}
	
}
