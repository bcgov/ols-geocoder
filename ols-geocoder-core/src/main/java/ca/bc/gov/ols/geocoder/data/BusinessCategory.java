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
package ca.bc.gov.ols.geocoder.data;

public class BusinessCategory {
	private final String className;
	private final String description;
	private final String naicsCode;
	
	public BusinessCategory(String className, String description, String naicsCode) {
		this.className = className;
		this.description = description;
		this.naicsCode = naicsCode;
	}

	public String getClassName() {
		return className;
	}

	public String getDescription() {
		return description;
	}

	public String getNaicsCode() {
		return naicsCode;
	}
	
}
