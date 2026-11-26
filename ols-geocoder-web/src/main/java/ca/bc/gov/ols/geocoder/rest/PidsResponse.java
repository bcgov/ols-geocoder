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

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the Property Identifiers (PIDs) associated with a site.")
public class PidsResponse {

	@Schema(description = "The unique identifier (UUID) of the site.",
			example = "a810e87b-7f99-4898-a19c-1493e1d25e25")
	private final UUID siteUuid;

	@Schema(description = "A comma-separated list of Property Identifiers (PIDs) for the site's parcels.",
			example = "005-570-168;005-570-169")
	private final String pids;
	
	public PidsResponse(UUID siteUuid, String pids) {
		this.siteUuid = siteUuid;
		this.pids = pids;
	}

	public UUID getSiteUuid() {
		return siteUuid;
	}

	public String getPids() {
		return pids;
	}
}
