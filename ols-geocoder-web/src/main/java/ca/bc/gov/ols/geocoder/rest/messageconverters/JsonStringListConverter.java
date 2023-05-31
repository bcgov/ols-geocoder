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
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

@Component
public class JsonStringListConverter extends AbstractHttpMessageConverter<List<String>> {

	public JsonStringListConverter() {
		super(MediaType.APPLICATION_JSON);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return List.class.isAssignableFrom(clazz);
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	@Override
	protected List<String> readInternal(Class<? extends List<String>> clazz,
			HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		return null;
	}
	
	@Override
	protected void writeInternal(List<String> list, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		out.write("[");
		boolean first = true;
		for(String tag : list) {
			if(first) {
				first = false;
			} else {
				out.write(",");
			}
			out.write("\"" + StringEscapeUtils.escapeJson(tag) + "\"");
		}
		out.write("]");
		out.flush();
	}
	
}