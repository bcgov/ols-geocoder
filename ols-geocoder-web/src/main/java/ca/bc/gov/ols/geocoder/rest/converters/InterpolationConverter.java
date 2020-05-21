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

import org.springframework.core.convert.converter.Converter;

import ca.bc.gov.ols.geocoder.data.enumTypes.Interpolation;

public class InterpolationConverter implements Converter<String, Interpolation> {
	
	@Override
	public Interpolation convert(String in) {
		return Interpolation.convert(in);
	}
	
}
