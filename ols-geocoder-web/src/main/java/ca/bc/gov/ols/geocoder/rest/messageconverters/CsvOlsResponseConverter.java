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
import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.IntersectionMatch;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.rest.PidsResponse;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;

@Component
public class CsvOlsResponseConverter extends AbstractHttpMessageConverter<OlsResponse> {
	
	@Autowired
	private IGeocoder geocoder;
	
	public CsvOlsResponseConverter() {
		super(new MediaType("text", "csv", Charset.forName("UTF-8")));
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
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		response.reproject(config.getBaseSrsCode(), response.getOutputSRS());
		if(response.getResponseObj() == null) {
			return;
		} else if(response.getResponseObj() instanceof SearchResults) {
			out.write(searchResultsToCSV((SearchResults)response.getResponseObj(), response));
		} else if(response.getResponseObj() instanceof SiteAddress) {
			out.write(siteAddressesToCSV(new SiteAddress[] {(SiteAddress)response.getResponseObj()}, response));
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress) {
			out.write(streetIntersectionAddressesToCSV(new StreetIntersectionAddress[] {(StreetIntersectionAddress)response
					.getResponseObj()}));
		} else if(response.getResponseObj() instanceof SiteAddress[]) {
			out.write(siteAddressesToCSV((SiteAddress[])response.getResponseObj(), response));
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress[]) {
			out.write(streetIntersectionAddressesToCSV((StreetIntersectionAddress[])response
					.getResponseObj()));
		} else if(response.getResponseObj() instanceof PidsResponse) {
			out.write(pidsResponseToCSV((PidsResponse)response.getResponseObj()));
		} else {
			// should never get here but this is handy for debugging if we do
			out.write(("Output not supported for " + response.getResponseObj().getClass()
					.getCanonicalName()));
		}
		out.flush();
	}
	
	static String searchResultsToCSV(SearchResults results, OlsResponse response) {
		StringBuilder sb = new StringBuilder(
				"\"fullAddress\"," +
				"\"intersectionName\"," +
				"\"score\"," +
				"\"matchPrecision\"," +
				"\"precisionPoints\"," +
				"\"faults\",");
		if(!response.isBrief()) {
			sb.append("\"siteName\"," +
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
					"\"localityName\",");
		}
		sb.append("\"localityType\"," +
				"\"electoralArea\",");
		if(!response.isBrief()) {
			sb.append("\"provinceCode\",");
		}
		sb.append("\"X\"," +
				"\"Y\"," +
				"\"srsCode\"," +
				"\"locationPositionalAccuracy\"," +
				"\"locationDescriptor\"," +
				"\"siteID\"," +
				"\"blockID\"," +
				"\"intersectionID\",");
		if(!response.isBrief()) {
			sb.append("\"fullSiteDescriptor\",");
		}
		sb.append("\"accessNotes\"," +
				"\"siteStatus\",");
		if(!response.isBrief()) {
			sb.append("\"siteRetireDate\"," +
				"\"changeDate\"," +
				"\"isOfficial\",");
		}
		sb.append("\"degree\"");
		if(response.getExtraInfo("occupantQuery").equals("true")) {
			sb.append(",\"occupantName\""
					+ ",\"occupantID\""
					+ ",\"occupantAliasAddress\""
					+ ",\"occupantDescription\""
					+ ",\"contactEmail\""
					+ ",\"contactPhone\""
					+ ",\"contactFax\""
					+ ",\"websiteUrl\""
					+ ",\"imageUrl\""
					+ ",\"keywords\""
					+ ",\"businessCategoryClass\""
					+ ",\"businessCategoryDescription\""
					+ ",\"naicsCode\""
					+ ",\"dateOccupantUpdated\""
					+ ",\"dateOccupantAdded\""
				);
		}
		sb.append("\n");
		for(GeocodeMatch match : results.getMatches()) {
			if(match instanceof AddressMatch) {
				SiteAddress addr = ((AddressMatch)match).getAddress();				
				sb.append(escape(addr.getAddressString()) + ","
						// IntersectionName
						+ ","
						+ match.getScore() + ","
						+ escape(match.getPrecision()) + ","
						+ match.getPrecisionPoints() + ","
						+ escape(match.getFaults().toString()) + ",");
				if(!response.isBrief()) {
					sb.append(escape(addr.getSiteName()) + ","
							+ escape(addr.getUnitDesignator()) + ","
							+ escape(addr.getUnitNumber()) + ","
							+ escape(addr.getUnitNumberSuffix()) + ","
							+ escape(GeocoderUtil.formatCivicNumber(addr.getCivicNumber()))
							+ ","
							+ escape(addr.getCivicNumberSuffix()) + ","
							+ escape(addr.getStreetName()) + ","
							+ escape(addr.getStreetType()) + ","
							+ escape(addr.isStreetTypePrefix()) + ","
							+ escape(addr.getStreetDirection()) + ","
							+ escape(addr.isStreetDirectionPrefix()) + ","
							+ escape(addr.getStreetQualifier()) + ","
							+ escape(addr.getLocalityName()) + ",");
				}
				sb.append(escape(addr.getLocalityType()) + ","
						+ escape(addr.getElectoralArea()) + ",");
				if(!response.isBrief()) {
					sb.append(escape(addr.getStateProvTerr()) + ",");
				}
				sb.append((addr.getLocation() == null ? ",," :
						(addr.getLocation().getX() + ","
								+ addr.getLocation().getY() + ","))
						+ addr.getSrsCode() + ","
						+ escape(addr.getLocationPositionalAccuracy()) + ","
						+ escape(addr.getLocationDescriptor()) + ","
						+ escape(addr.getSiteID()) + ","
						+ escape(addr.getStreetSegmentID()) + ","
						+ ","); // IntersectionID
				if(!response.isBrief()) {
					sb.append(escape(addr.getFullSiteDescriptor()) + ",");
				}
				// narrativeLocation is intentionally output as accessNotes
				sb.append(escape(addr.getNarrativeLocation()) + ","
						+ escape(addr.getSiteStatus()) + ",");
				if(!response.isBrief()) {
					sb.append(escape(addr.getSiteRetireDate()) + ","
							+ escape(addr.getSiteChangeDate()) + ","
							+ (addr.isPrimary() ? "\"Y\"" : "\"N\"") + ",");
				}
				// degree would go here but left empty for site
				
				if(addr instanceof OccupantAddress) {
					OccupantAddress occ = (OccupantAddress)addr;
					sb.append("," + escape(occ.getOccupantName()) 
							+ "," + escape(occ.getOccupantId())
							+ "," + escape(occ.getOccupantAliasAddress())
							+ "," + escape(occ.getOccupantDescription())
							+ "," + escape(occ.getContactEmail())
							+ "," + escape(occ.getContactPhone())
							+ "," + escape(occ.getContactFax())
							+ "," + escape(occ.getWebsiteUrl())
							+ "," + escape(occ.getImageUrl())
							+ "," + escape(occ.getKeywords())
							+ "," + escape(occ.getBusinessCategoryClass())
							+ "," + escape(occ.getBusinessCategoryDescription())
							+ "," + escape(occ.getNaicsCode())
							+ "," + escape(occ.getDateOccupantUpdated())
							+ "," + escape(occ.getDateOccupantAdded())
						);
				} else {
					sb.append(",,,,,,,,,,,,,,,,,");
				}
				sb.append("\n"); 
			} else if(match instanceof IntersectionMatch) {
				StreetIntersectionAddress addr = ((IntersectionMatch)match).getAddress();
				sb.append(escape(addr.getAddressString()) + ","
						+ escape(addr.getName()) + "," // IntersectionName
						+ match.getScore() + ","
						+ escape(match.getPrecision()) + ","
						+ match.getPrecisionPoints() + ","
						+ escape(match.getFaults().toString()) + ",");
				if(!response.isBrief()) {				
					// sitename,unitdesig,unitnum,untinumsuff,civnum,civcuff,streetname,streetType,isStreetTypePrefix,streetDirection,isStreetDirectionPrefix,streetQualifier
					sb.append( ",,,,,,,,,,,,"
							+ escape(addr.getLocalityName()) + ",");
				}
				sb.append(escape(addr.getLocalityType()) + ",,"); // blank electoralArea for intersection matches
				if(!response.isBrief()) {
					sb.append(escape(addr.getStateProvTerr()) + ",");
				}
				sb.append((addr.getLocation() == null ? ",," :
							(addr.getLocation().getX() + ","
								+ addr.getLocation().getY() + ","))
						+ addr.getSrsCode() + ","
						+ escape(addr.getLocationPositionalAccuracy()) + ","
						+ escape(addr.getLocationDescriptor()) + ","
						// siteId,blockID
						+ ",,"
						+ escape(addr.getID()) + ",");
				if(!response.isBrief()) {				
					// fullsitedesc,
					sb.append(",");
				}
				//narrativeloc,sitestatus,
				sb.append(",,");
				if(!response.isBrief()) {								
					//steretiredte,chngdate,isOfficial,
					sb.append(",,,");
				}
				sb.append(addr.getDegree() 
						+ ",,,,,,,,,,,,,,,,," //occupant fields
						+ "\n");
			}
		}
		return sb.toString();
	}
	
	static String siteAddressesToCSV(SiteAddress[] addrs, OlsResponse response) {
		// TODO need to handle date formatting
		StringBuilder sb = new StringBuilder("\"fullAddress\",");
		if(!response.isBrief()) {
			sb.append("\"siteName\","
					+ "\"unitDesignator\","
					+ "\"unitNumber\","
					+ "\"unitNumberSuffix\","
					+ "\"civicNumber\","
					+ "\"civicNumberSuffix\","
					+ "\"streetName\","
					+ "\"streetType\","
					+ "\"isStreetTypePrefix\","
					+ "\"streetDirection\","
					+ "\"isStreetDirectionPrefix\","
					+ "\"streetQualifier\","
					+ "\"localityName\",");
		}
		sb.append("\"localityType\","
				+ "\"electoralArea\",");
		if(!response.isBrief()) {
			sb.append("\"provinceCode\",");
		}
		sb.append("\"X\","
				+ "\"Y\","
				+ "\"srsCode\","
				+ "\"locationPositionalAccuracy\","
				+ "\"locationDescriptor\","
				+ "\"siteID\","
				+ "\"blockID\",");
		if(!response.isBrief()) {
			sb.append("\"fullSiteDescriptor\",");
		}
		sb.append("\"accessNotes\","
					+ "\"siteStatus\"");
		if(!response.isBrief()) {
			sb.append(",\"siteRetireDate\","
				+ "\"changeDate\","
				+ "\"isOfficial\"");
		}
		if(response.getExtraInfo("occupantQuery").equals("true")) {
			sb.append(",\"occupantName\""
					+ ",\"occupantID\""
					+ ",\"occupantAliasAddress\""
					+ ",\"occupantDescription\""
					+ ",\"contactEmail\""
					+ ",\"contactPhone\""
					+ ",\"contactFax\""
					+ ",\"websiteUrl\""
					+ ",\"imageUrl\""
					+ ",\"keywords\""
					+ ",\"businessCategoryClass\""
					+ ",\"businessCategoryDescription\""
					+ ",\"naicsCode\""
					+ ",\"dateOccupantUpdated\""
					+ ",\"dateOccupantAdded\""
					+ ",\"custodianId\""
					+ ",\"sourceDataId\"");
		}
		sb.append("\n");

		for(SiteAddress addr : addrs) {
			sb.append(escape(addr.getAddressString()) + ",");
			if(!response.isBrief()) {
				sb.append(escape(addr.getSiteName()) + ","
					+ escape(addr.getUnitDesignator()) + ","
					+ escape(addr.getUnitNumber()) + ","
					+ escape(addr.getUnitNumberSuffix()) + ","
					+ escape(GeocoderUtil.formatCivicNumber(addr.getCivicNumber())) + ","
					+ escape(addr.getCivicNumberSuffix()) + ","
					+ escape(addr.getStreetName()) + ","
					+ escape(addr.getStreetType()) + ","
					+ escape(addr.isStreetTypePrefix()) + ","
					+ escape(addr.getStreetDirection()) + ","
					+ escape(addr.isStreetDirectionPrefix()) + ","
					+ escape(addr.getStreetQualifier()) + ","
					+ escape(addr.getLocalityName()) + ",");
			}
			sb.append(escape(addr.getLocalityType()) + ","
					+ escape(addr.getElectoralArea()) + ",");
			if(!response.isBrief()) {
				sb.append(escape(addr.getStateProvTerr()) + ",");
			}
			sb.append((addr.getLocation() == null ? ",," :
						(addr.getLocation().getX() + ","
								+ addr.getLocation().getY() + ","))
					+ addr.getSrsCode() + ","
					+ escape(addr.getLocationPositionalAccuracy()) + ","
					+ escape(addr.getLocationDescriptor()) + ","
					+ escape(addr.getSiteID()) + ","
					+ escape(addr.getStreetSegmentID()) + ","); // aka blockID
			if(!response.isBrief()) {					
				sb.append(escape(addr.getFullSiteDescriptor()) + ",");
			}
			sb.append(// narrativeLocation is intentionally output as accessNotes
					escape(addr.getNarrativeLocation()) + ","
					+ escape(addr.getSiteStatus()));
			if(!response.isBrief()) {
				sb.append(escape("," + addr.getSiteRetireDate()) + ","
					+ escape(addr.getSiteChangeDate()) + ","
					+ (addr.isPrimary() ? "\"Y\"" : "\"N\""));
			}
			if(addr instanceof OccupantAddress) {
				OccupantAddress occ = (OccupantAddress)addr;
				sb.append("," + escape(occ.getOccupantName()) 
						+ "," + escape(occ.getOccupantId())
						+ "," + escape(occ.getOccupantAliasAddress())
						+ "," + escape(occ.getOccupantDescription())
						+ "," + escape(occ.getContactEmail())
						+ "," + escape(occ.getContactPhone())
						+ "," + escape(occ.getContactFax())
						+ "," + escape(occ.getWebsiteUrl())
						+ "," + escape(occ.getImageUrl())
						+ "," + escape(occ.getKeywords())
						+ "," + escape(occ.getBusinessCategoryClass())
						+ "," + escape(occ.getBusinessCategoryDescription())
						+ "," + escape(occ.getNaicsCode())
						+ "," + escape(occ.getDateOccupantUpdated())
						+ "," + escape(occ.getDateOccupantAdded())
					);
			} else {
				sb.append(",,,,,,,,,,,,,,,,,");
			}
			sb.append("\n"); 
		}
		return sb.toString();
	}
	
	static String streetIntersectionAddressesToCSV(StreetIntersectionAddress[] addrs) {
		StringBuilder sb = new StringBuilder("\"fullAddress\","
				+ "\"intersectionName\","
				+ "\"localityName\","
				+ "\"localityType\","
				+ "\"provinceCode\","
				+ "\"X\","
				+ "\"Y\","
				+ "\"srsCode\","
				+ "\"intersectionID\","
				+ "\"locationPositionalAccuracy\","
				+ "\"locationDescriptor\","
				+ "\"degree\"\n");
		for(StreetIntersectionAddress addr : addrs) {
			sb.append(escape(addr.getAddressString()) + ","
					+ escape(addr.getName()) + ","
					+ escape(addr.getLocalityName()) + ","
					+ escape(addr.getLocalityType()) + ","
					+ escape(addr.getStateProvTerr()) + ","
					+ (addr.getLocation() == null ? ",," :
							(addr.getLocation().getX() + "," + addr.getLocation().getY() + ","))
					+ addr.getSrsCode() + ","
					+ escape(addr.getID()) + ","
					+ escape(addr.getLocationPositionalAccuracy()) + ","
					+ escape(addr.getLocationDescriptor()) + ","
					+ escape(addr.getDegree()) + "\n");
		}
		return sb.toString();
	}
	
	private String pidsResponseToCSV(PidsResponse pr) {
		return "siteID,pids\n" + pr.getSiteUuid() + "," + pr.getPids();
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
		field = OlsResponseWriter.formatDate(field);
		if(field.toString().matches("\\A-?[0-9\\.]+\\z")) {
			// if numeric, return as is
			return field.toString();
		}
		// otherwise, add quotes and escape internal quotes
		return ('"' + field.toString().replaceAll("\"", "\\\\\"") + '"');
	}
}
