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

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gson.stream.JsonWriter;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

import org.locationtech.jts.geom.Point;

public class JsonOlsResponseWriter implements OlsResponseWriter {

	private JsonWriter jw;
	protected GeocoderConfig config;
	
	public JsonOlsResponseWriter(Writer out, GeocoderConfig config) {
		jw = new JsonWriter(out);
		this.config = config;
	}

	@Override
	public void documentHeader() throws IOException {
	}

	@Override
	public void documentFooter() throws IOException {
	}

	@Override
	public void empty() throws IOException {
		jw.beginObject();
		jw.endObject();
	}

	@Override
	public void field(String fieldName, Object fieldValue) throws IOException {
		field(fieldName, fieldName, fieldValue, false);
	}

	@Override
	public void field(String fieldName, Object fieldValue, boolean forceString) throws IOException {
		field(fieldName, fieldName, fieldValue, forceString);
	}

	@Override
	public void field(String glossaryName, String fieldName, Object fieldValue) throws IOException {
		field(fieldName, fieldName, fieldValue, false);
	}
	
	@Override
	public void field(String glossaryName, String fieldName, Object fieldValue, boolean forceString) throws IOException {
		if(fieldName == "srsCode") {
			jw.name("crs");
				jw.beginObject();
				jw.name("type").value("EPSG");
				jw.name("properties");
					jw.beginObject();
					jw.name("code").jsonValue(escape(fieldValue));
					jw.endObject();
				jw.endObject();
				
		} else {
			jw.name(fieldName).jsonValue(escape(fieldValue, forceString));
		}
	}

	@Override
	public void fieldListHeader() throws IOException {
		jw.beginObject();
	}

	@Override
	public void fieldListFooter() throws IOException {
		jw.endObject();
	}

	@Override
	public void searchResultsHeader() throws IOException {
		jw.beginObject();
		jw.name("type").value("FeatureCollection");
	}

	@Override
	public void searchResultsFooter() throws IOException {
		jw.endObject();
	}

	@Override
	public void matchesHeader() throws IOException {
		jw.name("features");
		jw.beginArray();
	}

	@Override
	public void matchesFooter() throws IOException {
		jw.endArray();		
	}

	@Override
	public void featureCollectionHeader() throws IOException {
		jw.beginObject();
		jw.name("type").value("FeatureCollection");
		jw.name("features");
		jw.beginArray();
	}

	@Override
	public void featureCollectionFooter() throws IOException {
		jw.endArray();
		jw.endObject();
	}

	/**
	 * Escapes a single value
	 * 
	 * @param field the value to escape
	 */
	static String escape(Object field) {
		return escape(field, false);
	}

	static String escape(Object field, boolean forceString)
	{
		if(field == null) {
			return "\"\"";
		}
		field = OlsResponseWriter.formatDate(field);
		if(!forceString && field.toString().matches("\\A-?[0-9]+\\.?[0-9]*\\z")) {
			// if numeric, return as is
			return field.toString();
		}
		// otherwise, add quotes and escape internal quotes
		return "\"" + StringEscapeUtils.escapeJson(field.toString()) + "\"";
	}

	@Override
	public void unknown(Object responseObj) throws IOException {
		jw.value(("JSON output not supported for " + responseObj.getClass()
				.getCanonicalName()));
	}

	@Override
	public void noMatches() throws IOException {
		jw.beginArray();
		jw.value("No matches found for the specified query parameters.");
		jw.endArray();
	}

	@Override
	public void featureHeader(Point loc) throws IOException {
		jw.beginObject();
		jw.name("type").value("Feature");
		jw.name("geometry");
		if(loc == null) {
			jw.nullValue();
		} else {
			jw.beginObject();
			jw.name("type").value("Point");
			jw.name("crs");
				jw.beginObject();
				jw.name("type").value("EPSG");
				jw.name("properties");
					jw.beginObject();
					jw.name("code").value(loc.getSRID());
					jw.endObject();
				jw.endObject();
			jw.name("coordinates");
				jw.beginArray();
				jw.value(loc.getX());
				jw.value(loc.getY());
				jw.endArray();
			jw.endObject();
		}
		jw.name("properties");
		jw.beginObject();
	}

	@Override
	public void featureFooter() throws IOException {
		jw.endObject(); // properties
		jw.endObject(); // feature
	}

	@Override
	public void faultsHeader() throws IOException {
		jw.name("faults");
		jw.beginArray();
	}

	@Override
	public void faultsFooter() throws IOException {
		jw.endArray();
	}

	@Override
	public void faultHeader() throws IOException {
		jw.beginObject();
	}

	@Override
	public void faultFooter() throws IOException {
		jw.endObject();
	}

}
