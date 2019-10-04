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
package ca.bc.gov.ols.geocoder.rest.converters;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.bc.gov.ols.geocoder.rest.exceptions.ErrorMessage;

public class UuidParam {
	private static Pattern UUID_RE = Pattern
			.compile("\\s*\\A[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\z\\s*");
	
	private UUID uuid = null;
	private ErrorMessage errorMessage = null;
	
	public UuidParam(String in) {
		if(in == null || in.isEmpty()) {
			throw new IllegalArgumentException("No value for parameter.");
		}
		Matcher matcher = UUID_RE.matcher(in);
		if(!matcher.matches()) {
			errorMessage = new ErrorMessage(
					"Parameter must be in UUID format XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX.");
		}
		uuid = UUID.fromString(in.trim());
	}
	
	public UUID getValue() {
		return uuid;
	}
	
	public ErrorMessage getErrorMessage() {
		return errorMessage;
	}
}
