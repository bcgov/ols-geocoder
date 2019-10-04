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

import gnu.trove.map.hash.THashMap;

import java.time.LocalDateTime;
import java.util.Map;

import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.api.data.ModifiableLocation;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;

/**
 * The OlsResponse class wraps a single instance of any result/output object, and holds any
 * parameters necessary to render the results for output. For convenience, it also knows how to
 * reproject all types of response objects.
 * 
 */
public class OlsResponse {
	
	private Object responseObj = null;
	private Integer outputSRS = null;
	private String callback = null;
	private boolean brief = false;
	private String errorMsg = null;
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
				results.setSearchTimeStamp(LocalDateTime.now());
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
