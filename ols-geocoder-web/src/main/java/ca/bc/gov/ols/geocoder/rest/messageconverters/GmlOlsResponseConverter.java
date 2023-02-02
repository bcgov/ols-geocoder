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
import java.util.Iterator;

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
import ca.bc.gov.ols.geocoder.api.data.MatchFault;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;

@Component
public class GmlOlsResponseConverter extends AbstractHttpMessageConverter<OlsResponse> {
	
	@Autowired
	private IGeocoder geocoder;
	
	public GmlOlsResponseConverter() {
		super(new MediaType("application", "gml+xml",
				Charset.forName("UTF-8")), MediaType.APPLICATION_XML);
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
			return; // empty response
		}
		out.write("<?xml version='1.0'?>");
		if(response.getResponseObj() instanceof SearchResults) {
			out.write("<bgeo:searchResults xmlns:gml='http://www.opengis.net/gml' xmlns:bgeo='http://gov.bc.ca/bgeo'>\n"
					+ searchResultsToGML((SearchResults)response.getResponseObj(), config, response)
					+ "</bgeo:searchResults>\n");
		} else if(response.getResponseObj() instanceof SiteAddress) {
			out.write("<gml:feature xmlns:gml='http://www.opengis.net/gml' xmlns:bgeo='http://gov.bc.ca/bgeo'>\n"
					+ siteAddressToGML((SiteAddress)response.getResponseObj()) + "</gml:feature>");
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress) {
			out.write("<gml:feature xmlns:gml='http://www.opengis.net/gml' xmlns:bgeo='http://gov.bc.ca/bgeo'>\n"
					+ streetIntersectionAddressToGML((StreetIntersectionAddress)response
							.getResponseObj()) + "</gml:feature>");
		} else if(response.getResponseObj() instanceof SiteAddress[]) {
			out.write("<gml:featureCollection xmlns:gml='http://www.opengis.net/gml' xmlns:bgeo='http://gov.bc.ca/bgeo'>\n"
					+ siteAddressesToGML((SiteAddress[])response.getResponseObj())
					+ "</gml:featureCollection>");
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress[]) {
			out.write("<gml:featureCollection xmlns:gml='http://www.opengis.net/gml' xmlns:bgeo='http://gov.bc.ca/bgeo'>"
					+ streetIntersectionAddressesToGML((StreetIntersectionAddress[])response
							.getResponseObj())
					+ "</gml:featureCollection>");
		} else {
			// should never get here but this is handy for debugging if we do
			out.write("Output not supported for " + response.getResponseObj().getClass()
					.getCanonicalName());
		}
		out.flush();
	}
	
	static String searchResultsToGML(SearchResults results, GeocoderConfig config,
			OlsResponse response) {
		// TODO need to handle date formatting
		StringBuffer buf = new StringBuffer(
				"<bgeo:queryAddress>"
						+ escape(results.getQueryAddress())
						+ "</bgeo:queryAddress>\n"
						+ "<bgeo:searchTimestamp>"
						+ escape(results.getSearchTimeStamp())
						+ "</bgeo:searchTimestamp>\n"
						+ "<bgeo:executionTime>"
						+ escape(results.getExecutionTime())
						+ "</bgeo:executionTime>\n"
						+ "<bgeo:version>"
						+ escape(GeocoderConfig.VERSION)
						+ "</bgeo:version>\n"
						+ "<bgeo:baseDataDate>"
						+ escape(results.getProcessingDate().format(OlsResponseReader.DATE_FORMATTER))
						+ "</bgeo:baseDataDate>\n"
						+ "<bgeo:disclaimer>"
						+ escape(config.getDisclaimer())
						+ "</bgeo:disclaimer>\n"
						+ "<bgeo:privacyStatement>"
						+ escape(config.getPrivacyStatement())
						+ "</bgeo:privacyStatement>\n"
						+ "<bgeo:copyrightNotice>"
						+ escape(config.getCopyrightNotice())
						+ "</bgeo:copyrightNotice>\n"
						+ "<bgeo:copyrightLicense>"
						+ escape(config.getCopyrightLicense())
						+ "</bgeo:copyrightLicense>\n"
						+ "<bgeo:setBack>" + results.getSetBack() + "</bgeo:setBack>\n"
						+ "<bgeo:echo>" + results.getIsEcho() + "</bgeo:echo>\n"
						+ "<bgeo:interpolation>" + results.getInterpolation()
						+ "</bgeo:interpolation>\n"
						+ "<bgeo:locationDescriptor>" + results.getLocationDescriptor()
						+ "</bgeo:locationDescriptor>\n"
						+ "<bgeo:maxResults>" + results.getMaxResults() + "</bgeo:maxResults>\n"
						+ "<bgeo:minScore>" + results.getMinScore() + "</bgeo:minScore>\n"
						+ "<bgeo:srsCode>" + results.getSrsCode() + "</bgeo:srsCode>\n"
						+ "<bgeo:matches>\n<gml:FeatureCollection>\n");
		Iterator<GeocodeMatch> it = results.getMatches().iterator();
		while(it.hasNext()) {
			buf.append("<gml:featureMember>\n<bgeo:match>" + geocodeMatchToGML(it.next())
					+ "</bgeo:match>\n</gml:featureMember>\n");
		}
		buf.append("</gml:FeatureCollection>\n</bgeo:matches>\n");
		return buf.toString();
	}
	
	static String geocodeMatchToGML(GeocodeMatch match) {
		if(match instanceof AddressMatch) {
			return addressMatchToGML((AddressMatch)match);
		} else if(match instanceof IntersectionMatch) {
			return intersectionMatchToGML((IntersectionMatch)match);
		}
		return "";
	}
	
	static String addressMatchToGML(AddressMatch match) {
		SiteAddress addr = match.getAddress();
		StringBuffer buf = new StringBuffer(siteAddressToGML(addr)
				+ "<bgeo:score>" + match.getScore() + "</bgeo:score>\n"
				+ "<bgeo:matchPrecision>" + escape(match.getPrecision())
				+ "</bgeo:matchPrecision>\n"
				+ "<bgeo:precisionPoints>" + match.getPrecisionPoints()
				+ "</bgeo:precisionPoints>\n"
				+ "<bgeo:faults>\n");
		Iterator<MatchFault> it = match.getFaults().iterator();
		while(it.hasNext()) {
			buf.append("<bgeo:matchFault>" + matchFaultToGML(it.next()) + "</bgeo:matchFault>\n");
		}
		buf.append("</bgeo:faults>\n");
		return buf.toString();
	}
	
	static String intersectionMatchToGML(IntersectionMatch match) {
		StreetIntersectionAddress addr = match.getAddress();
		StringBuffer buf = new StringBuffer(streetIntersectionAddressToGML(addr) +
				"<bgeo:score>" + match.getScore() + "</bgeo:score>\n"
				+ "<bgeo:matchPrecision>" + escape(match.getPrecision())
				+ "</bgeo:matchPrecision>\n"
				+ "<bgeo:precisionPoints>" + match.getPrecisionPoints()
				+ "</bgeo:precisionPoints>\n"
				+ "\n");
		return buf.toString();
	}
	
	static String matchFaultToGML(MatchFault fault) {
		return "<bgeo:element>" + escape(fault.getElement()) + "</bgeo:element>\n"
				+ "<bgeo:fault>" + escape(fault.getFault()) + "</bgeo:fault>\n"
				+ "<bgeo:penalty>" + fault.getPenalty() + "</bgeo:penalty>\n";
	}
	
	static String siteAddressesToGML(SiteAddress[] addrs) {
		StringBuilder sb = new StringBuilder("<gml:FeatureCollection>\n");
		
		for(SiteAddress addr : addrs) {
			sb.append("<gml:featureMember>\n" + siteAddressToGML(addr) + "</gml:featureMember>\n");
		}
		sb.append("</gml:FeatureCollection>\n");
		return sb.toString();
	}
	
	static String siteAddressToGML(SiteAddress addr) {
		String entityName = "siteAddress";
		String occupantStr = "";
		if(addr instanceof OccupantAddress) {
			entityName = "occupantAddress";
			OccupantAddress occ = (OccupantAddress)addr;
			occupantStr = "<bgeo:occupantName>" + escape(occ.getOccupantName())
					+ "</bgeo:occupantName>\n"
					+ "<bgeo:occupantID>" + escape(occ.getOccupantId())
					+ "</bgeo:occupantID>\n"
					+ "<bgeo:occupantAliasAddress>" + escape(occ.getOccupantAliasAddress())
					+ "</bgeo:occupantAliasAddress>\n"
					+ "<bgeo:occupantDescription>" + escape(occ.getOccupantDescription())
					+ "</bgeo:occupantDescription>\n"
					+ "<bgeo:contactEmail>" + escape(occ.getContactEmail())
					+ "</bgeo:contactEmail>\n"
					+ "<bgeo:contactPhone>" + escape(occ.getContactPhone())
					+ "</bgeo:contactPhone>\n"
					+ "<bgeo:contactFax>" + escape(occ.getContactFax())
					+ "</bgeo:contactFax>\n"
					+ "<bgeo:websiteUrl>" + escape(occ.getWebsiteUrl())
					+ "</bgeo:websiteUrl>\n"
					+ "<bgeo:imageUrl>" + escape(occ.getImageUrl())
					+ "</bgeo:imageUrl>\n"
					+ "<bgeo:keywords>" + escape(occ.getKeywords())
					+ "</bgeo:keywords>\n"
					+ "<bgeo:businessCategoryClass>" + escape(occ.getBusinessCategoryClass())
					+ "</bgeo:businessCategoryClass>\n"
					+ "<bgeo:businessCategoryDescription>" + escape(occ.getBusinessCategoryDescription())
					+ "</bgeo:businessCategoryDescription>\n"
					+ "<bgeo:naicsCode>" + escape(occ.getNaicsCode())
					+ "</bgeo:naicsCode>\n"
					+ "<bgeo:dateOccupantUpdated>" + escape(occ.getDateOccupantUpdated())
					+ "</bgeo:dateOccupantUpdated>\n"
					+ "<bgeo:dateOccupantAdded>" + escape(occ.getDateOccupantAdded())
					+ "</bgeo:dateOccupantAdded>\n";
		}

		return "<bgeo:" + entityName + ">\n"
				+ "<bgeo:fullAddress>" + escape(addr.getAddressString()) + "</bgeo:fullAddress>\n"
				+ "<bgeo:intersectionName>" + "" + "</bgeo:intersectionName>\n"
				+ "<bgeo:siteName>" + escape(addr.getSiteName()) + "</bgeo:siteName>\n"
				+ "<bgeo:unitDesignator>" + escape(addr.getUnitDesignator())
				+ "</bgeo:unitDesignator>\n"
				+ "<bgeo:unitNumber>" + escape(addr.getUnitNumber()) + "</bgeo:unitNumber>\n"
				+ "<bgeo:unitNumberSuffix>" + escape(addr.getUnitNumberSuffix())
				+ "</bgeo:unitNumberSuffix>\n"
				+ "<bgeo:civicNumber>"
				+ escape(GeocoderUtil.formatCivicNumber(addr.getCivicNumber()))
				+ "</bgeo:civicNumber>\n"
				+ "<bgeo:civicNumberSuffix>" + escape(addr.getCivicNumberSuffix())
				+ "</bgeo:civicNumberSuffix>\n"
				+ "<bgeo:streetName>" + escape(addr.getStreetName()) + "</bgeo:streetName>\n"
				+ "<bgeo:streetType>" + escape(addr.getStreetType()) + "</bgeo:streetType>\n"
				+ "<bgeo:isStreetTypePrefix>" + escape(addr.isStreetTypePrefix())
				+ "</bgeo:isStreetTypePrefix>\n"
				+ "<bgeo:streetDirection>" + escape(addr.getStreetDirection())
				+ "</bgeo:streetDirection>\n"
				+ "<bgeo:isStreetDirectionPrefix>" + escape(addr.isStreetDirectionPrefix())
				+ "</bgeo:isStreetDirectionPrefix>\n"
				+ "<bgeo:streetQualifier>" + escape(addr.getStreetQualifier())
				+ "</bgeo:streetQualifier>\n"
				+ "<bgeo:localityName>" + escape(addr.getLocalityName()) + "</bgeo:localityName>\n"
				+ "<bgeo:localityType>" + escape(addr.getLocalityType()) + "</bgeo:localityType>\n"
				+ "<bgeo:electoralArea>" + escape(addr.getElectoralArea()) + "</bgeo:electoralArea>\n"
				+ "<bgeo:provinceCode>" + escape(addr.getStateProvTerr())
				+ "</bgeo:provinceCode>\n"
				+ "<bgeo:location>" + (addr.getLocation() == null ? "" :
						(addr.getLocation().getX() + ", " + addr.getLocation().getY()))
				+ "</bgeo:location>\n"
				+ "<bgeo:srsCode>" + escape(addr.getSrsCode()) + "</bgeo:srsCode>\n"
				+ "<bgeo:locationPositionalAccuracy>"
				+ escape(addr.getLocationPositionalAccuracy())
				+ "</bgeo:locationPositionalAccuracy>\n"
				+ "<bgeo:locationDescriptor>" + escape(addr.getLocationDescriptor())
				+ "</bgeo:locationDescriptor>\n"
				+ "<bgeo:siteID>" + escape(addr.getSiteID()) + "</bgeo:siteID>\n"
				+ "<bgeo:blockID>" + escape(addr.getStreetSegmentID()) + "</bgeo:blockID>\n"
				+ "<bgeo:fullSiteDescriptor>" + escape(addr.getFullSiteDescriptor())
				+ "</bgeo:fullSiteDescriptor>\n"
				// narrativeLocation is intentionally output as accessNotes
				+ "<bgeo:accessNotes>" + escape(addr.getNarrativeLocation()) 
				+ "</bgeo:accessNotes>\n"
				+ "<bgeo:siteStatus>" + escape(addr.getSiteStatus()) + "</bgeo:siteStatus>\n"
				+ "<bgeo:siteRetireDate>" + escape(addr.getSiteRetireDate())
				+ "</bgeo:siteRetireDate>\n"
				+ "<bgeo:changeDate>" + escape(addr.getSiteChangeDate()) + "</bgeo:changeDate>\n"
				+ "<bgeo:isOfficial>" + (addr.isPrimary() ? "true" : "false")
				+ "</bgeo:isOfficial>\n"
				+ occupantStr
				+ "</bgeo:" + entityName + ">\n";
	}
	
	static String streetIntersectionAddressesToGML(StreetIntersectionAddress[] addrs) {
		StringBuilder sb = new StringBuilder("<gml:FeatureCollection>\n");
		
		for(StreetIntersectionAddress addr : addrs) {
			sb.append("<gml:featureMember>\n" + streetIntersectionAddressToGML(addr)
					+ "</gml:featureMember>\n");
		}
		sb.append("</gml:FeatureCollection>\n");
		return sb.toString();
	}
	
	static String streetIntersectionAddressToGML(StreetIntersectionAddress addr) {
		return "<bgeo:streetIntersectionAddress>\n"
				+ "<bgeo:fullAddress>" + escape(addr.getAddressString()) + "</bgeo:fullAddress>\n"
				+ "<bgeo:intersectionName>" + escape(addr.getName()) + "</bgeo:intersectionName>\n"
				+ "<bgeo:localityName>" + escape(addr.getLocalityName()) + "</bgeo:localityName>\n"
				+ "<bgeo:localityType>" + escape(addr.getLocalityType()) + "</bgeo:localityType>\n"
				+ "<bgeo:provinceCode>" + escape(addr.getStateProvTerr())
				+ "</bgeo:provinceCode>\n"
				+ "<bgeo:location>" + (addr.getLocation() == null ? "" :
						(addr.getLocation().getX() + ", " + addr.getLocation().getY()))
				+ "</bgeo:location>\n"
				+ "<bgeo:srsCode>" + escape(addr.getSrsCode()) + "</bgeo:srsCode>\n"
				+ "<bgeo:locationPositionalAccuracy>"
				+ escape(addr.getLocationPositionalAccuracy())
				+ "</bgeo:locationPositionalAccuracy>\n"
				+ "<bgeo:locationDescriptor>" + escape(addr.getLocationDescriptor())
				+ "</bgeo:locationDescriptor>\n"
				+ "<bgeo:intersectionID>" + escape(addr.getID()) + "</bgeo:intersectionID>\n"
				+ "<bgeo:degree>" + escape(addr.getDegree()) + "</bgeo:degree>\n"
				+ "</bgeo:streetIntersectionAddress>\n";
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
		return (field.toString());
	}
}
