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

import java.security.InvalidParameterException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;



public class ApiKeyInterceptor implements HandlerInterceptor {

	private List<String> parcelKeys;

	public ApiKeyInterceptor(List<String> parcelKeys) {
		this.parcelKeys = parcelKeys;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		String apikey = request.getHeader("apikey");
		if((apikey != null && parcelKeys.contains(apikey))
				|| request.getServerName().equalsIgnoreCase("localhost")) {
			return true;
		}
		throw new InvalidParameterException("Invalid or no API key found in request");
	}

}
