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

public class BooleanConverter implements Converter<String, Boolean> {
	
	@Override
	public Boolean convert(String in) {
		if(in == null || in.isEmpty()) {
			return null;
		} else if("0".equals(in)
				|| "n".equalsIgnoreCase(in)
				|| "no".equalsIgnoreCase(in)
				|| "f".equalsIgnoreCase(in)
				|| "false".equalsIgnoreCase(in)) {
			return Boolean.FALSE;
		} else if("1".equals(in)
				|| "y".equalsIgnoreCase(in)
				|| "yes".equalsIgnoreCase(in)
				|| "t".equalsIgnoreCase(in)
				|| "true".equalsIgnoreCase(in)) {
			return Boolean.TRUE;
		} else {
			throw new IllegalArgumentException(
					"Parameter must be one of 1, 0, y, n, yes, no, t, f, true, false");
		}
	}
	
}
