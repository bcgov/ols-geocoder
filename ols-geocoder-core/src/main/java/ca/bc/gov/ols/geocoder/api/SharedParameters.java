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

import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;

public class SharedParameters {

	private String callback = "jsonp";
	private int outputSRS = 4326;
	private boolean asAttachment = false;
	protected String tags;
	private boolean brief = false;
	private Integer maxResults;
	private int setBack = 0;
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
