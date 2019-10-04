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
package ca.bc.gov.ols.geocoder.rest.batch;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.rest.LocationReprojector;

// For Instant Batch
public abstract class AbstractBatchResponseWriter extends AbstractHttpMessageConverter<GeocoderBatchProcessor> {

	@Autowired
	protected IGeocoder geocoder;
	

	public AbstractBatchResponseWriter(MediaType mediaType) {
		super(mediaType);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return GeocoderBatchProcessor.class.isAssignableFrom(clazz);
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	@Override
	protected GeocoderBatchProcessor readInternal(Class<? extends GeocoderBatchProcessor> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return null;
	}
	
	@Override
	protected void writeInternal(GeocoderBatchProcessor proc, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		//GeocoderConfig config = geocoder.getDatastore().getConfig();
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		//response.reproject(config.getBaseSrsCode(), response.getOutputSRS());
		// output header
		writeHeader(out, proc);
		proc.start();
		SearchResults results = proc.next();
		int seqNum = 0;
		while(results != null) {
			seqNum++;
			int resultNum = 0;
			// reproject the results
			//reprojectResults(results, results.getSrsCode());
			for(GeocodeMatch match : results.getMatches()) {
				resultNum++;
				// output the result match
				writeMatch(out, match, seqNum, resultNum, results.getExecutionTime());
			}
			results = proc.next();
		}
		// output footer
		proc.stop();
		writeFooter(out, proc);
		out.flush();
	}

	public void reprojectResults(SearchResults results, Integer srsCode) {
		LocationReprojector lr = new LocationReprojector(geocoder.getConfig().getBaseSrsCode(), srsCode);
		if(srsCode != geocoder.getDatastore().getConfig().getBaseSrsCode()) {
			results.setSrsCode(srsCode);
			results.setSearchTimeStamp(LocalDateTime.now());
			lr.reproject(results.getMatches());
			lr.reprecision(results.getMatches());
		} else {
			results.setSrsCode(srsCode);
			lr.reprecision(results.getMatches());
		}
	}

	protected abstract void writeHeader(Writer out, GeocoderBatchProcessor proc) throws IOException;

	protected abstract void writeMatch(Writer out, GeocodeMatch match, int seqNum, int resultNum, 
			BigDecimal executionTime) throws IOException;

	protected abstract void writeFooter(Writer out, GeocoderBatchProcessor proc) throws IOException;

}
