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

import org.springframework.web.multipart.MultipartFile;

import ca.bc.gov.ols.geocoder.api.GeocodeQuery;

public class BulkGeocodeParameters extends GeocodeQuery {
	private MultipartFile file;
	private int startSeqNum = 1;
	private int maxRequests = 0;
	private int maxTime = 0;

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}
	
	public int getStartSeqNum() {
		return startSeqNum;
	}

	public void setStartSeqNum(int startSeqNum) {
		this.startSeqNum = startSeqNum;
	}

	public int getMaxRequests() {
		return maxRequests;
	}
	
	public void setMaxRequests(int maxRequests) {
		this.maxRequests = maxRequests;
	}

	public int getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
	}
	
}
