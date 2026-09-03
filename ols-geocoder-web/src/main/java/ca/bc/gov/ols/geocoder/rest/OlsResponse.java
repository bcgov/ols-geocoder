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
package ca.bc.gov.ols.geocoder.rest;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.ModifiableLocation;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import gnu.trove.map.hash.THashMap;

/**
 * The OlsResponse class wraps a single instance of any result/output object, and holds any
 * parameters necessary to render the results for output. For convenience, it also knows how to
 * reproject all types of response objects.
 * 
 */
@Schema(description = "The standard response envelope for all geocoder API responses. "
		+ "Contains the response object (which may be a single result, an array of results, or a search results object), "
		+ "along with metadata about the query parameters and output settings.")
public class OlsResponse {
	
	@Schema(description = "The response payload, which varies by endpoint. "
			+ "May be a single address or intersection object, an array of such objects, "
			+ "or a SearchResults object containing matches and summary information.")
	private Object responseObj = null;

	@Schema(description = "The EPSG code of the spatial reference system used for the output geometries.",
			example = "4326")
	private Integer outputSRS = null;

	@Schema(description = "The JSONP callback function name, if one was requested.")
	private String callback = null;

	@Schema(description = "Whether the results are in brief format, omitting detailed properties.")
	private boolean brief = false;

	@Schema(description = "An error message, if the request resulted in an error. Null if the request was successful.")
	private String errorMsg = null;

	@Schema(description = "Additional metadata about the query, such as execution time, "
			+ "minDegree, maxDegree, and other parameters used.")
	private Map<String, String> extraInfo = new THashMap<String, String>();
	
	public OlsResponse(Object responseObj) {
		this.responseObj = responseObj;
	}

	public void setParams(SharedParameters params) {
		callback = params.getCallback();
		outputSRS = params.getOutputSRS();
		brief = params.isBrief();
	}
	
	public boolean isError() {
		return errorMsg != null;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
	public void setErrorMsg(String msg) {
		this.errorMsg = msg;
	}
	
	public Object getResponseObj() {
		return responseObj;
	}
	
	public Integer getOutputSRS() {
		return outputSRS;
	}
	
	public void setOutputSRS(Integer outputSRS) {
		this.outputSRS = outputSRS;
	}
	
	public String getCallback() {
		return callback;
	}
	
	public boolean isBrief() {
		return brief;
	}

	public void setBrief(boolean brief) {
		this.brief = brief;
	}

	public void setExtraInfo(String key, String value) {
		extraInfo.put(key, value);
	}
	
	public String getExtraInfo(String key) {
		String value = extraInfo.get(key);
		if(value == null) {
			return "";
		}
		return value;
	}
	
	public void setCallback(String callback) {
		this.callback = callback;
	}
	
	public void reproject(Integer fromSrsCode, int toSrsCode) {
		if(toSrsCode != fromSrsCode) {
			LocationReprojector lr = new LocationReprojector(fromSrsCode, toSrsCode);
			if(responseObj instanceof SearchResults) {
				SearchResults results = (SearchResults)responseObj;
				results.setSrsCode(toSrsCode);
				lr.reproject(results.getMatches());
				lr.reprecision(results.getMatches());
			} else if(responseObj instanceof ModifiableLocation) {
				// handles SiteAddress and StreetIntersectionAddress
				lr.reproject((ModifiableLocation)responseObj);
				lr.reprecision((ModifiableLocation)responseObj);
			} else if(responseObj instanceof ModifiableLocation[]) {
				// handles SiteAddress and StreetIntersectionAddress
				lr.reproject((ModifiableLocation[])responseObj);
				lr.reprecision((ModifiableLocation[])responseObj);
			}
		} else {
			LocationReprojector lr = new LocationReprojector(fromSrsCode, toSrsCode);
			if(responseObj instanceof SearchResults) {
				SearchResults results = (SearchResults)responseObj;
				results.setSrsCode(toSrsCode);
				lr.reprecision(results.getMatches());
			} else if(responseObj instanceof ModifiableLocation) {
				// handles SiteAddress and StreetIntersectionAddress
				lr.reprecision((ModifiableLocation)responseObj);
			} else if(responseObj instanceof ModifiableLocation[]) {
				// handles SiteAddress and StreetIntersectionAddress
				lr.reprecision((ModifiableLocation[])responseObj);
			}
		}
	}
}
