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
import java.io.Writer;
import java.nio.charset.Charset;

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
import ca.bc.gov.ols.geocoder.rest.exceptions.ErrorMessage;

@Component
public class KmlErrorMessageConverter extends AbstractHttpMessageConverter<ErrorMessage> {
	
	@Autowired
	private IGeocoder geocoder;
	
	public KmlErrorMessageConverter() {
		super(new MediaType("application", "vnd.google-earth.kml+xml",
				Charset.forName("UTF-8")));
	}
	
	@Override
	protected boolean supports(Class<?> clazz) {
		return ErrorMessage.class.isAssignableFrom(clazz);
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}
	
	@Override
	protected ErrorMessage readInternal(Class<? extends ErrorMessage> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return null;
	}
	
	@Override
	protected void writeInternal(ErrorMessage message, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" "
				+ "xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\r\n");
		
		out.write(errorMessageToKML(message, config));
		out.write("</kml>");
		out.flush();
	}
	
	static String errorMessageToKML(ErrorMessage message, GeocoderConfig config) {
		return "<Document>\r\n"
				+ "<name>Error Message</name>\r\n"
				+ "<open>1</open>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"errorMessage\"><value>" + message.getMessage()
				+ "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ "<styleUrl>" + config.getKmlStylesUrl()
				+ "#error_message</styleUrl>\r\n"
				+ "</Document>\r\n";
	}
	
}
