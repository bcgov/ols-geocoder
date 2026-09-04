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
package ca.bc.gov.ols.geocoder.api;

import io.swagger.v3.oas.annotations.Parameter;

import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;

public class SharedParameters {

	@Parameter(description = "If provided the JSON result will be wrapped in a function call of the given name (for JSONP support).",
			example = "jsonp")
	private String callback = "jsonp";

	@Parameter(description = "The EPSG code of the spatial reference system (SRS) to use for output geometries.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "4326"),
			example = "4326")
	private int outputSRS = 4326;

	@Parameter(description = "If true the result is returned as a file attachment.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "false"))
	private boolean asAttachment = false;

	@Parameter(description = "A set of user-defined tags to apply to the query results.")
	protected String tags;

	@Parameter(description = "If true the results are returned in brief format, omitting detailed address and location properties.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "false"))
	private boolean brief = false;

	@Parameter(description = "The maximum number of results to return.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "1"))
	private Integer maxResults;

	@Parameter(description = "The setback distance in metres from the street segment. A value of -1 indicates the centroid of the address range.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "0"))
	private int setBack = 0;

	@Parameter(description = "Describes the nature of the address location.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "any"))
	protected LocationDescriptor locationDescriptor = LocationDescriptor.ANY;

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public int getOutputSRS() {
		return outputSRS;
	}

	public void setOutputSRS(int outputSRS) {
		this.outputSRS = outputSRS;
	}

	public boolean isAsAttachment() {
		return asAttachment;
	}

	public void setAsAttachment(boolean asAttachment) {
		this.asAttachment = asAttachment;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getTags() {
		return tags;
	}

	public void setBrief(boolean brief) {
		this.brief = brief;
	}

	public boolean isBrief() {
		return brief;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		// Clamp
		if(maxResults < 1) {
			maxResults = 1;
		}
		this.maxResults = maxResults;
	}	

	public void setMaxFeatures(int maxFeatures) {
		if(maxResults == null) {
			setMaxResults(maxFeatures);
		}
	}

	public int getSetBack() {
		return setBack;
	}

	public void setSetBack(int setBack) {
		// Clamp
		if(setBack < -1) {
			setBack = -1;
		}
		this.setBack = setBack;
	}

	public LocationDescriptor getLocationDescriptor() {
		return locationDescriptor;
	}

	public void setLocationDescriptor(LocationDescriptor ld) {
		this.locationDescriptor = ld;
	}
	
}
