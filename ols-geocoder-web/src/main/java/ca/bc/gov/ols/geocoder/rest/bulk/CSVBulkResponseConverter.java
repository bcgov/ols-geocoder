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
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.IntersectionMatch;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;

// For Instant Batch
@Component
public class CSVBulkResponseConverter extends AbstractBulkResponseWriter {
	private static final Logger logger = LoggerFactory.getLogger(CSVBulkResponseConverter.class);

	public CSVBulkResponseConverter() {
		super(new MediaType("text", "csv", Charset.forName("UTF-8")));
	}

	@Override
	protected void writeHeader(Writer out, BulkGeocodeProcessor proc) throws IOException {
		out.write("\"sequenceNumber\"," +
				"\"resultNumber\"," +
				"\"yourId\"," +
				"\"fullAddress\"," +
				"\"intersectionName\"," +
				"\"score\"," +
				"\"matchPrecision\"," +
				"\"precisionPoints\"," +
				"\"faults\"," +
				"\"siteName\"," +
				"\"unitDesignator\"," +
				"\"unitNumber\"," +
				"\"unitNumberSuffix\"," +
				"\"civicNumber\"," +
				"\"civicNumberSuffix\"," +
				"\"streetName\"," +
				"\"streetType\"," +
				"\"isStreetTypePrefix\"," +
				"\"streetDirection\"," +
				"\"isStreetDirectionPrefix\"," +
				"\"streetQualifier\"," +
				"\"localityName\"," +
				"\"localityType\"," +
				"\"electoralArea\"," +
				"\"provinceCode\"," +
				"\"X\"," +
				"\"Y\"," +
				"\"srsCode\"," +
				"\"locationPositionalAccuracy\"," +
				"\"locationDescriptor\"," +
				"\"siteID\"," +
				"\"blockID\"," +
				"\"intersectionID\"," +
				"\"fullSiteDescriptor\"," +
				"\"accessNotes\"," +
				"\"siteStatus\"," +
				"\"siteRetireDate\"," +
				"\"changeDate\"," +
				"\"isOfficial\"," +
				"\"degree\"," +
				"\"executionTime\"\n");
	}
	
	@Override
	protected void writeMatch(Writer out, GeocodeMatch match, int seqNum, int resultNum, BigDecimal executionTime) 
			throws IOException {
		out.write(seqNum + "," + resultNum + "," + escape(match.getYourId()) + ",");
		if(match instanceof AddressMatch) {
			SiteAddress addr = ((AddressMatch)match).getAddress();
			out.write(
					escape(addr.getAddressString()) + ","
							// IntersectionName
							+ ","
							+ match.getScore() + ","
							+ escape(match.getPrecision()) + ","
							+ match.getPrecisionPoints() + ","
							+ escape(match.getFaults().toString()) + ","
							+ escape(addr.getSiteName()) + ","
							+ escape(addr.getUnitDesignator()) + ","
							+ escape(addr.getUnitNumber()) + ","
							+ escape(addr.getUnitNumberSuffix()) + ","
							+ escape(addr.getCivicNumber()) + ","
							+ escape(addr.getCivicNumberSuffix()) + ","
							+ escape(addr.getStreetName()) + ","
							+ escape(addr.getStreetType()) + ","
							+ escape(addr.isStreetTypePrefix()) + ","
							+ escape(addr.getStreetDirection()) + ","
							+ escape(addr.isStreetDirectionPrefix()) + ","
							+ escape(addr.getStreetQualifier()) + ","
							+ escape(addr.getLocalityName()) + ","
							+ escape(addr.getLocalityType()) + ","
							+ escape(addr.getElectoralArea()) + ","
							+ escape(addr.getStateProvTerr()) + ","
							+ (addr.getLocation() == null ? ",," :
									(addr.getLocation().getX() + ","
											+ addr.getLocation().getY() + ","))
							+ addr.getSrsCode() + ","
							+ escape(addr.getLocationPositionalAccuracy()) + ","
							+ escape(addr.getLocationDescriptor()) + ","
							+ escape(addr.getSiteID()) + ","
							+ escape(addr.getStreetSegmentID()) + ","
							// IntersectionID
							+ ","
							+ escape(addr.getFullSiteDescriptor()) + ","
							+ escape(addr.getNarrativeLocation()) + ","
							+ escape(addr.getSiteStatus()) + ","
							+ escape(addr.getSiteRetireDate()) + ","
							+ escape(addr.getSiteChangeDate()) + ","
							+ (addr.isPrimary() ? "\"Y\"" : "\"N\"") + ","
							//intersection degree
							+ "," );
			
		} else if(match instanceof IntersectionMatch) {
			StreetIntersectionAddress addr = ((IntersectionMatch)match).getAddress();
			out.write(
					escape(addr.getAddressString()) + ","
							// IntersectionName
							+ escape(addr.getName()) + ","
							+ match.getScore() + ","
							+ escape(match.getPrecision()) + ","
							+ match.getPrecisionPoints() + ","
							+ escape(match.getFaults().toString()) + ","
							// sitenme,unitdesig,unitnum,untinumsuff,civnum,civcuff,streetname,streetType,isStreetTypePrefix,streetDirection,isStreetDirectionPrefix,streetQualifier
							+ ",,,,,,,,,,,,"
							+ escape(addr.getLocalityName()) + ","
							+ escape(addr.getLocalityType()) + ","
							+ escape(addr.getElectoralArea()) + ","
							+ escape(addr.getStateProvTerr()) + ","
							+ (addr.getLocation() == null ? ",," :
									(addr.getLocation().getX() + ","
											+ addr.getLocation().getY() + ","))
							+ addr.getSrsCode() + ","
							+ escape(addr.getLocationPositionalAccuracy()) + ","
							+ escape(addr.getLocationDescriptor()) + ","
							// siteId,blockID
							+ ",,"
							+ escape(addr.getID()) + ","
							// fullsitedesc,narrativeloc,sitestatus,steretiredte,chngdate,isprimary
							+ ",,,,,,"
							+ addr.getDegree());
		}
		out.write(executionTime + "\n");
	}
	
	@Override
	protected void writeFooter(Writer out, BulkGeocodeProcessor proc) throws IOException {
	}

	/**
	 * Escapes a single field of a CSV.
	 * 
	 * @param line List of elements to print.
	 */
	static String escape(Object field) {
		if(field == null) {
			return "";
		}
		// if numeric, return as is
		if(field.toString().matches("\\A-?[0-9\\.]+\\z")) {
			return field.toString();
		}
		// otherwise, add quotes and escape internal quotes
		return ('"' + field.toString().replaceAll("\"", "\\\\\"") + '"');
	}
	
}
