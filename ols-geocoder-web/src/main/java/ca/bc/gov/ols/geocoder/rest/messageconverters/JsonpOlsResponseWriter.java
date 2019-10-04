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
package ca.bc.gov.ols.geocoder.rest.messageconverters;

import java.io.IOException;
import java.io.Writer;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

public class JsonpOlsResponseWriter extends JsonOlsResponseWriter {

	private Writer out;
	private String callback;
	
	public JsonpOlsResponseWriter(Writer out, GeocoderConfig config, String callback) {
		super(out, config);
		this.out = out;
		this.callback = callback;
	}

	@Override
	public void documentHeader() throws IOException {
		out.write(callback + "(");
	}

	@Override
	public void documentFooter() throws IOException {
		out.write(");");
	}

	
}
