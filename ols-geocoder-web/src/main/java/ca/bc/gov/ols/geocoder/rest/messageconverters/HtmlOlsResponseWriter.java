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
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

public class HtmlOlsResponseWriter implements OlsResponseWriter {

	protected Writer out;
	protected GeocoderConfig config;
	
	public HtmlOlsResponseWriter(Writer out, GeocoderConfig config) {
		this.out = out;
		this.config = config;
	}

	@Override
	public void documentHeader() throws IOException {
		out.write(("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/>"
				+ "<title>Geocoding Results</title></head><body>"));
	}

	@Override
	public void documentFooter() throws IOException {
		out.write("</body></html>");
	}

	@Override
	public void empty() throws IOException {
		out.write("Response is empty!");
	}

	@Override
	public void field(String fieldName, Object fieldValue, boolean forceString) throws IOException {
		field(fieldName, fieldName, fieldValue);
	}

	@Override
	public void field(String fieldName, Object fieldValue) throws IOException {
		field(fieldName, fieldName, fieldValue);
	}

	@Override
	public void field(String glossaryName, String fieldName, Object fieldValue, boolean forceString) throws IOException {
		field(glossaryName, fieldName, fieldValue);
	}
	
	@Override
	public void field(String glossaryName, String fieldName, Object fieldValue) throws IOException {
		out.write("<tr><td class=\"name\"><a href=\"" + config.getGlossaryBaseUrl()
				+ "#" + glossaryName + "\">" + fieldName + ":</a></td><td class=\"value\">"
				+ escape(fieldValue) + "</td></tr>\n");
	}

	@Override
	public void fieldListHeader() throws IOException {
		out.write("<h1>Results</h1>");
		featureCollectionHeader();
	}

	@Override
	public void fieldListFooter() throws IOException {
		featureCollectionFooter();
	}

	@Override
	public void searchResultsHeader() throws IOException {
		out.write("<h1>Geocoding Results</h1>");
		featureCollectionHeader();
	}

	@Override
	public void searchResultsFooter() throws IOException {
		featureCollectionFooter();
	}

	@Override
	public void matchesHeader() throws IOException {
		out.write("<tr><td class=\"name\">matches:</td><td class=\"value\"><table>\n");		
	}

	@Override
	public void matchesFooter() throws IOException {
		out.write("</table></td></tr>\n");		
	}

	@Override
	public void featureCollectionHeader() throws IOException {
		out.write("<div class=\"results\"><table>\n");
	}

	@Override
	public void featureCollectionFooter() throws IOException {
		out.write("</table></div>");
	}

	/**
	 * Escapes a single value
	 * 
	 * @param field the value to escape
	 */
	static String escape(Object field)
	{
		if(field == null) {
			return "";
		}
		field = OlsResponseWriter.formatDate(field);
		return StringEscapeUtils.escapeXml10(field.toString());
	}

	@Override
	public void unknown(Object responseObj) throws IOException {
		out.write(("HMTL output not supported for " + responseObj.getClass()
				.getCanonicalName()));
	}

	@Override
	public void noMatches() throws IOException {
		out.write("No matches found for the specified query parameters.");	
	}

	@Override
	public void featureHeader(Point loc) throws IOException {
		out.write("<tr><td><div class=\"results\"><table>");
		field("location", (loc == null ? "" : (loc.getX() + ", " + loc.getY())));	
	}
	
	@Override
	public void featureFooter() throws IOException {
		out.write("</table></div></td></tr>\n");
	}

	@Override
	public void faultsHeader() throws IOException {
		out.write("<tr><td class=\"name\"><a href=\"" + config.getGlossaryBaseUrl()
		+ "#faults\">faults:</a></td><td class=\"value\"><table>");		
	}

	@Override
	public void faultsFooter() throws IOException {
		out.write("</table></td></tr>\n");
	}

	@Override
	public void faultHeader() throws IOException {
		out.write("<tr><td><div class=\"results\"><table>");	}

	@Override
	public void faultFooter() throws IOException {
		out.write("</table></div></td></tr>\n");
	}

}
