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

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

@Component
public class CsvStringConverter extends AbstractHttpMessageConverter<String> {
	public CsvStringConverter() {
		super(new MediaType("text", "csv", Charset.forName("UTF-8")));
	}
	
	@Override
	protected boolean supports(Class<?> clazz) {
		return String.class.isAssignableFrom(clazz);
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}
	
	@Override
	protected String readInternal(Class<? extends String> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return null;
	}
	
	@Override
	protected void writeInternal(String str, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		out.write(str);
		out.flush();
	}
	
}
