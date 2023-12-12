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
import java.io.OutputStreamWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;

@Component
public class XhtmlOlsResponseConverter extends AbstractHttpMessageConverter<OlsResponse> {
	
	@Autowired
	private IGeocoder geocoder;
	
	public XhtmlOlsResponseConverter() {
		super(MediaType.APPLICATION_XHTML_XML);
	}
	
	@Override
	protected boolean supports(Class<?> clazz) {
		return OlsResponse.class.isAssignableFrom(clazz);
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}
	
	@Override
	protected OlsResponse readInternal(Class<? extends OlsResponse> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return null;
	}
	
	@Override
	protected void writeInternal(OlsResponse response, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		OutputStreamWriter out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		response.reproject(config.getBaseSrsCode(), response.getOutputSRS());
		XhtmlOlsResponseWriter responseWriter = new XhtmlOlsResponseWriter(out, config);
		OlsResponseReader responseReader = new OlsResponseReader(response, responseWriter, config);
		responseReader.convert();
		out.flush();
	}
}
