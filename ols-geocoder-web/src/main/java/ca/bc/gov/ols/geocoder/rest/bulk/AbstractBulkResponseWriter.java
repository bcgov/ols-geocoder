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
package ca.bc.gov.ols.geocoder.rest.bulk;

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
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.rest.LocationReprojector;

// For Instant Batch
public abstract class AbstractBulkResponseWriter extends AbstractHttpMessageConverter<BulkGeocodeProcessor> {

	@Autowired
	protected IGeocoder geocoder;
	

	public AbstractBulkResponseWriter(MediaType mediaType) {
		super(mediaType);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return BulkGeocodeProcessor.class.isAssignableFrom(clazz);
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	@Override
	protected BulkGeocodeProcessor readInternal(Class<? extends BulkGeocodeProcessor> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return null;
	}
	
	@Override
	protected void writeInternal(BulkGeocodeProcessor proc, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		//response.reproject(config.getBaseSrsCode(), response.getOutputSRS());
		// output header
		writeHeader(out, proc);
		BulkStatsCalculator statsCalc = new BulkStatsCalculator();
		proc.start(statsCalc);
		int maxRequests = clampMax(config.getBulkMaxRequests(), proc.getParams().getMaxRequests()); 
		int maxTime = clampMax(config.getBulkTimeLimit(), proc.getParams().getMaxTime()) * 1000; 
		SearchResults results = proc.next();
		int seqNum = proc.getStartSeqNum();
		while(results != null) {
			int resultNum = 1;
			// reproject the results
			//reprojectResults(results, results.getSrsCode());
			for(GeocodeMatch match : results.getMatches()) {
				// output the result match
				writeMatch(out, match, seqNum, resultNum, results.getExecutionTime());
				resultNum++;
			}
			seqNum++;
			// break out if we've hit either limit
			if((maxRequests != 0 && statsCalc.getProcessedCount() >= maxRequests)
					|| (maxTime != 0 && statsCalc.getElapsedTime() >= maxTime)) {
				break;
			}
			results = proc.next();
		}
		// output footer
		proc.stop();
		writeFooter(out, proc);
		out.flush();
		//logger.info("Stats: " + proc.getStats().toString());
	}

	private int clampMax(int configMax, int paramMax) {
		if(configMax <= 0) return paramMax;
		if(paramMax <= 0) return configMax;
		return Math.min(configMax, paramMax);
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

	protected abstract void writeHeader(Writer out, BulkGeocodeProcessor proc) throws IOException;

	protected abstract void writeMatch(Writer out, GeocodeMatch match, int seqNum, int resultNum, 
			BigDecimal executionTime) throws IOException;

	protected abstract void writeFooter(Writer out, BulkGeocodeProcessor proc) throws IOException;

}
