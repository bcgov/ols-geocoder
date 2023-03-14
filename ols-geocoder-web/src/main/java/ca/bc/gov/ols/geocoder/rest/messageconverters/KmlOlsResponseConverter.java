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
import java.time.LocalDateTime;
import java.util.Iterator;

import org.apache.commons.text.StringEscapeUtils;
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
import ca.bc.gov.ols.geocoder.api.data.GeocoderAddress;
import ca.bc.gov.ols.geocoder.api.data.IntersectionMatch;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.ILocation;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;

@Component
public class KmlOlsResponseConverter extends AbstractHttpMessageConverter<OlsResponse> {
	
	private static final String GEOCODED = "geocoded";
	private static final String REVERSE = "reverse";
	
	@Autowired
	private IGeocoder geocoder;
	private OlsResponse response;
	
	public KmlOlsResponseConverter() {
		super(new MediaType("application", "vnd.google-earth.kml+xml",
				Charset.forName("UTF-8")));
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
		this.response = response;
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		response.reproject(config.getBaseSrsCode(), 4326);
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" "
				+ "xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\r\n");
		if(response.getResponseObj() == null) {
			out.write("</kml>");
			return;
		} else if(response.getResponseObj() instanceof SearchResults) {
			out.write(searchResultsToKML((SearchResults)response.getResponseObj(), config, response));
		} else if(response.getResponseObj() instanceof SiteAddress) {
			out.write(singleSiteDocHeader((SiteAddress)response.getResponseObj(), config, response));
			out.write(siteAddressToKML((SiteAddress)response.getResponseObj(), config));
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress) {
			out.write(singleIntersectionDocHeader(
					(StreetIntersectionAddress)response.getResponseObj(), config, response));
			out.write(streetIntersectionAddressToKML(
					(StreetIntersectionAddress)response.getResponseObj(), config));
		} else if(response.getResponseObj() instanceof SiteAddress[]) {
			out.write(siteAddressesToKML((SiteAddress[])response.getResponseObj(), config, response));
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress[]) {
			out.write(streetIntersectionAddressesToKML(
					(StreetIntersectionAddress[])response.getResponseObj(), config, response));
		} else {
			// should never get here but this is handy for debugging if we do
			out.write(("KML output not supported for " + response.getResponseObj().getClass()
					.getCanonicalName()));
		}
		out.write(("</Document>\r\n</kml>"));
		out.flush();
	}
	
	String singleSiteDocHeader(SiteAddress addr, GeocoderConfig config, OlsResponse response) {
		return "<Document>\r\n"
				+ "<name>Results for " + escape(addr.getAddressString()) + "</name>\r\n"
				+ "<open>1</open>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"searchTimestamp\"><value>" + escape(LocalDateTime.now())
				+ "</value></Data>\r\n"
				+ "<Data name=\"version\"><value>" + escape(GeocoderConfig.VERSION)
				+ "</value></Data>\r\n"
				+ "<Data name=\"disclaimer\"><value>"
				+ escape(config.getDisclaimer()) + "</value></Data>\r\n"
				+ "<Data name=\"privacyStatement\"><value>"
				+ escape(config.getPrivacyStatement()) + "</value></Data>\r\n"
				+ "<Data name=\"copyrightNotice\"><value>"
				+ escape(config.getCopyrightNotice())
				+ "</value></Data>\r\n"
				+ "<Data name=\"copyrightLicense\"><value>"
				+ escape(config.getCopyrightLicense())
				+ "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ "<styleUrl>" 
				+ (response.getExtraInfo("occupantQuery").equals("true") ? 
						config.getOccupantCategoryKmlStyleUrl() : config.getKmlStylesUrl())  
				+ "#reverse_results_heading</styleUrl>\r\n";
	}
	
	String singleIntersectionDocHeader(StreetIntersectionAddress intersection,
			GeocoderConfig config, OlsResponse response) {
		return "<Document>\r\n"
				+ "<name>Results for " + escape(intersection.getAddressString()) + "</name>\r\n"
				+ "<open>1</open>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"searchTimestamp\"><value>" + escape(LocalDateTime.now())
				+ "</value></Data>\r\n"
				+ "<Data name=\"version\"><value>" + escape(GeocoderConfig.VERSION)
				+ "</value></Data>\r\n"
				+ "<Data name=\"minDegree\"><value>" + response.getExtraInfo("minDegree")
				+ "</value></Data>\r\n"
				+ "<Data name=\"maxDegree\"><value>" + response.getExtraInfo("maxDegree")
				+ "</value></Data>\r\n"
				+ "<Data name=\"disclaimer\"><value>"
				+ escape(config.getDisclaimer()) + "</value></Data>\r\n"
				+ "<Data name=\"privacyStatement\"><value>"
				+ escape(config.getPrivacyStatement()) + "</value></Data>\r\n"
				+ "<Data name=\"copyrightNotice\"><value>"
				+ escape(config.getCopyrightNotice())
				+ "</value></Data>\r\n"
				+ "<Data name=\"copyrightLicense\"><value>"
				+ escape(config.getCopyrightLicense())
				+ "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ "<styleUrl>" + config.getKmlStylesUrl()
				+ "#reverse_intersection_results_heading</styleUrl>\r\n";
	}
	
	String searchResultsToKML(SearchResults results, GeocoderConfig config,
			OlsResponse response) {
		// TODO need to handle date formatting
		StringBuilder buf = new StringBuilder("<Document>\r\n"
				+ "<name>Results for " + escape(results.getQueryAddress()) + "</name>\r\n"
				+ "<open>1</open>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"searchTimestamp\"><value>" + escape(results.getSearchTimeStamp())
				+ "</value></Data>\r\n"
				+ "<Data name=\"executionTime\"><value>"
				+ escape(results.getExecutionTime())
				+ "</value></Data>\r\n"
				+ "<Data name=\"version\"><value>" + escape(GeocoderConfig.VERSION)
				+ "</value></Data>\r\n"
				+ "<Data name=\"baseDataDate\"><value>" + escape(results.getProcessingDate())
				+ "</value></Data>\r\n"
				+ "<Data name=\"minScore\"><value>" + escape(results.getMinScore())
				+ "</value></Data>\r\n"
				+ "<Data name=\"maxResults\"><value>" + escape(results.getMaxResults())
				+ "</value></Data>\r\n"
				+ "<Data name=\"echo\"><value>" + escape(results.getIsEcho())
				+ "</value></Data>\r\n"
				+ "<Data name=\"interpolation\"><value>" + escape(results.getInterpolation())
				+ "</value></Data>\r\n"
				+ "<Data name=\"locationDescriptor\"><value>"
				+ escape(results.getLocationDescriptor()) + "</value></Data>\r\n"
				+ "<Data name=\"setBack\"><value>" + escape(results.getSetBack())
				+ "</value></Data>\r\n"
				+ (response.getExtraInfo("occupantQuery").equals("true") ? results.getTags() : "")
				+ "<Data name=\"disclaimer\"><value>"
				+ escape(config.getDisclaimer()) + "</value></Data>\r\n"
				+ "<Data name=\"privacyStatement\"><value>"
				+ escape(config.getPrivacyStatement()) + "</value></Data>\r\n"
				+ "<Data name=\"copyrightNotice\"><value>"
				+ escape(config.getCopyrightNotice())
				+ "</value></Data>\r\n"
				+ "<Data name=\"copyrightLicense\"><value>"
				+ escape(config.getCopyrightLicense())
				+ "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ "<styleUrl>" 
				+ (response.getExtraInfo("occupantQuery").equals("true") ? 
						config.getOccupantCategoryKmlStyleUrl() : config.getKmlStylesUrl())  
				+ "#results_heading</styleUrl>\r\n");
		Iterator<GeocodeMatch> it = results.getMatches().iterator();
		while(it.hasNext()) {
			buf.append(geocodeMatchToKML(it.next(), config));
		}
		buf.append("");
		return buf.toString();
	}
	
	String geocodeMatchToKML(GeocodeMatch match, GeocoderConfig config) {
		if(match instanceof AddressMatch) {
			return addressMatchToKML((AddressMatch)match, config);
		} else if(match instanceof IntersectionMatch) {
			return intersectionMatchToKML((IntersectionMatch)match, config);
		}
		return "";
	}
	
	String addressMatchToKML(AddressMatch match, GeocoderConfig config) {
		SiteAddress addr = match.getAddress();
		String occupantStr = "";
		if(addr instanceof OccupantAddress) {
			OccupantAddress occ = (OccupantAddress)addr;
			occupantStr = "<Data name=\"occupantName\"><value>" + escape(occ.getOccupantName())
					+ "</value></Data>\r\n"
					+ "<Data name=\"occupantID\"><value>" + escape(occ.getOccupantId())
					+ "</value></Data>\r\n"
					+ "<Data name=\"occupantAliasAddress\"><value>" + escape(occ.getOccupantAliasAddress())
					+ "</value></Data>\r\n"
					+ "<Data name=\"occupantDescription\"><value>" + escape(occ.getOccupantDescription())
					+ "</value></Data>\r\n"
					+ "<Data name=\"contactEmail\"><value>" + escape(occ.getContactEmail())
					+ "</value></Data>\r\n"
					+ "<Data name=\"contactPhone\"><value>" + escape(occ.getContactPhone())
					+ "</value></Data>\r\n"
					+ "<Data name=\"contactFax\"><value>" + escape(occ.getContactFax())
					+ "</value></Data>\r\n"
					+ "<Data name=\"websiteUrl\"><value>" + escape(occ.getWebsiteUrl())
					+ "</value></Data>\r\n"
					+ "<Data name=\"imageUrl\"><value>" + escape(occ.getImageUrl())
					+ "</value></Data>\r\n"
					+ "<Data name=\"keywords\"><value>" + escape(occ.getKeywords())
					+ "</value></Data>\r\n"
					+ "<Data name=\"businessCategoryClass\"><value>" + escape(occ.getBusinessCategoryClass())
					+ "</value></Data>\r\n"
					+ "<Data name=\"businessCategoryDescription\"><value>" + escape(occ.getBusinessCategoryDescription())
					+ "</value></Data>\r\n"
					+ "<Data name=\"naicsCode\"><value>" + escape(occ.getNaicsCode())
					+ "</value></Data>\r\n"
					+ "<Data name=\"dateOccupantUpdated\"><value>" + escape(occ.getDateOccupantUpdated())
					+ "</value></Data>\r\n"
					+ "<Data name=\"dateOccupantAdded\"><value>" + escape(occ.getDateOccupantAdded())
					+ "</value></Data>\r\n";
		}

		String result = "<Placemark>\r\n" +
				"<name>"
				+ escape(addr.getAddressString())
				+ "</name>\r\n"
				+ "<Snippet maxLines=\"1\">Score: "
				+ match.getScore()
				+ "  Precision: "
				+ escape(match.getPrecision())
				+ "</Snippet>\r\n"
				+ getLookAt(addr, config)
				+ "<styleUrl>"
				+ getStyleUrl(config, GEOCODED, addr) + "</styleUrl>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"fullAddress\"><value>" + escape(addr.getAddressString())
				+ "</value></Data>\r\n"
				+ "<Data name=\"score\"><value>" + match.getScore() + "</value></Data>\r\n"
				+ "<Data name=\"matchPrecision\"><value>" + escape(match.getPrecision())
				+ "</value></Data>\r\n"
				+ "<Data name=\"precisionPoints\"><value>" + match.getPrecisionPoints()
				+ "</value></Data>\r\n"
				+ "<Data name=\"faults\"><value>" + match.getFaults() + "</value></Data>\r\n";
		if(!response.isBrief()) {
			result += "<Data name=\"siteName\"><value>" + escape(addr.getSiteName())
				+ "</value></Data>\r\n"
				+ "<Data name=\"unitDesignator\"><value>" + escape(addr.getUnitDesignator())
				+ "</value></Data>\r\n"
				+ "<Data name=\"unitNumber\"><value>" + escape(addr.getUnitNumber())
				+ "</value></Data>\r\n"
				+ "<Data name=\"unitNumberSuffix\"><value>" + escape(addr.getUnitNumberSuffix())
				+ "</value></Data>\r\n"
				+ "<Data name=\"civicNumber\"><value>"
				+ escape(GeocoderUtil.formatCivicNumber(addr.getCivicNumber()))
				+ "</value></Data>\r\n"
				+ "<Data name=\"civicNumberSuffix\"><value>" + escape(addr.getCivicNumberSuffix())
				+ "</value></Data>\r\n"
				+ "<Data name=\"streetName\"><value>" + escape(addr.getStreetName())
				+ "</value></Data>\r\n"
				+ "<Data name=\"streetType\"><value>" + escape(addr.getStreetType())
				+ "</value></Data>\r\n"
				+ "<Data name=\"isStreetTypePrefix\"><value>"
				+ escape(addr.isStreetTypePrefix()) + "</value></Data>\r\n"
				+ "<Data name=\"streetDirection\"><value>" + escape(addr.getStreetDirection())
				+ "</value></Data>\r\n"
				+ "<Data name=\"isStreetDirectionPrefix\"><value>"
				+ escape(addr.isStreetDirectionPrefix()) + "</value></Data>\r\n"
				+ "<Data name=\"streetQualifier\"><value>" + escape(addr.getStreetQualifier())
				+ "</value></Data>\r\n"
				+ "<Data name=\"localityName\"><value>" + escape(addr.getLocalityName())
				+ "</value></Data>\r\n";
		}
		result += "<Data name=\"localityType\"><value>" + escape(addr.getLocalityType())
			+ "</value></Data>\r\n"
			+ "<Data name=\"electoralArea\"><value>" + escape(addr.getElectoralArea())
			+ "</value></Data>\r\n";
		if(!response.isBrief()) {
			result += "<Data name=\"provinceCode\"><value>" + escape(addr.getStateProvTerr())
				+ "</value></Data>\r\n";
		}
		result += "<Data name=\"locationPositionalAccuracy\"><value>"
				+ escape(addr.getLocationPositionalAccuracy()) + "</value></Data>\r\n"
				+ "<Data name=\"locationDescriptor\"><value>"
				+ escape(addr.getLocationDescriptor())
				+ "</value></Data>\r\n"
				+ "<Data name=\"siteID\"><value>" + escape(addr.getSiteID())
				+ "</value></Data>\r\n"
				+ "<Data name=\"siteUrl\"><value>" + escape(addr.getSiteID())
				+ "</value></Data>\r\n"
				+ "<Data name=\"blockID\"><value>" + escape(addr.getStreetSegmentID())
				+ "</value></Data>\r\n";
		if(!response.isBrief()) {
			result += "<Data name=\"fullSiteDescriptor\"><value>"
				+ escape(addr.getFullSiteDescriptor())
				+ "</value></Data>\r\n";
		}
		// narrativeLocation is intentionally output as accessNotes
		result +=  "<Data name=\"accessNotes\"><value>" + escape(addr.getNarrativeLocation())
				+ "</value></Data>\r\n"
				+ "<Data name=\"siteStatus\"><value>" + escape(addr.getSiteStatus())
				+ "</value></Data>\r\n";
		if(!response.isBrief()) {
			result += "<Data name=\"siteRetireDate\"><value>" + escape(addr.getSiteRetireDate())
				+ "</value></Data>\r\n"
				+ "<Data name=\"changeDate\"><value>" + escape(addr.getSiteChangeDate())
				+ "</value></Data>\r\n"
				+ "<Data name=\"isOfficial\"><value>" + (addr.isPrimary() ? "true" : "false")
				+ "</value></Data>\r\n";
		}
		result += occupantStr
				+ "</ExtendedData>\r\n"
				+ getPoint(addr)
				+ "</Placemark>";
		return result;
	}
	
	private String getStyleUrl(GeocoderConfig config, String type,
			GeocoderAddress addr) {
		if(addr instanceof OccupantAddress) {
			OccupantAddress occ = (OccupantAddress)addr;
			String customStyle = occ.getCustomStyleName();
			if(customStyle != null && !customStyle.isEmpty()) {
				return config.getOccupantCustomKmlStyleUrl() + "#" + type + "_" + customStyle;
			}
			return config.getOccupantCategoryKmlStyleUrl() + "#" + type + "_" + occ.getBusinessCategoryClass();
		}
		return config.getKmlStylesUrl() + "#" + type + "_" + addr.getLocationDescriptor() + "_"
				+ addr.getLocationPositionalAccuracy().toString();
	}
	
	private String getLookAt(GeocoderAddress addr, GeocoderConfig config) {
		if(addr.getLocation() == null) {
			return "";
		}
		return "<LookAt><longitude>" + addr.getLocation().getX() + "</longitude>" +
				"<latitude>" + addr.getLocation().getY() + "</latitude>" +
				"<altitude>0</altitude><heading>0</heading><tilt>0</tilt>" +
				"<range>" + config.getDefaultLookAtRange() + "</range>" + "</LookAt>\r\n";
		
	}
	
	private String getPoint(ILocation loc) {
		if(loc.getLocation() == null) {
			return "";
		}
		return "<Point><coordinates>" + loc.getLocation().getX() + ","
				+ loc.getLocation().getY() + "</coordinates></Point>\r\n";
	}

	String intersectionMatchToKML(IntersectionMatch match, GeocoderConfig config) {
		StreetIntersectionAddress addr = match.getAddress();
		return "<Placemark>\r\n"
				+ "<name>" + escape(addr.getAddressString()) + "</name>\r\n"
				+ "<Snippet maxLines=\"1\">Score: " + match.getScore()
				+ "  matchPrecision: " + escape(match.getPrecision())
				+ "</Snippet>\r\n"
				+ getLookAt(addr, config)
				+ "<styleUrl>"
				+ getStyleUrl(config, GEOCODED, addr) + "</styleUrl>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"fullAddress\"><value>" + escape(addr.getAddressString())
				+ "</value></Data>\r\n"
				+ "<Data name=\"intersectionName\"><value>" + escape(addr.getName())
				+ "</value></Data>\r\n"
				+ "<Data name=\"score\"><value>" + match.getScore() + "</value></Data>\r\n"
				+ "<Data name=\"matchPrecision\"><value>" + escape(match.getPrecision())
				+ "</value></Data>\r\n"
				+ "<Data name=\"precisionPoints\"><value>" + match.getPrecisionPoints()
				+ "</value></Data>\r\n"
				+ "<Data name=\"faults\"><value>" + match.getFaults() + "</value></Data>\r\n"
				+ "<Data name=\"localityName\"><value>" + escape(addr.getLocalityName())
				+ "</value></Data>\r\n"
				+ "<Data name=\"localityType\"><value>" + escape(addr.getLocalityType())
				+ "</value></Data>\r\n"
				+ "<Data name=\"provinceCode\"><value>" + escape(addr.getStateProvTerr())
				+ "</value></Data>\r\n"
				+ "<Data name=\"locationPositionalAccuracy\"><value>"
				+ escape(addr.getLocationPositionalAccuracy()) + "</value></Data>\r\n"
				+ "<Data name=\"locationDescriptor\"><value>"
				+ escape(addr.getLocationDescriptor())
				+ "</value></Data>\r\n"
				+ "<Data name=\"intersectionID\"><value>" + escape(addr.getID())
				+ "</value></Data>\r\n"
				+ "<Data name=\"degree\"><value>" + escape(addr.getDegree())
				+ "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ getPoint(addr)
				+ "</Placemark>\r\n";
	}
	
	String siteAddressesToKML(SiteAddress[] addrs, GeocoderConfig config,
			OlsResponse response) {
		StringBuilder buf = new StringBuilder("<Document>\r\n"
				+ "<name>Results for "
				+ (response.getExtraInfo("occupantQuery").equals("true") ? "Occupant" : "Site")
				+ " Search</name>\r\n"
				+ "<open>1</open>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"searchTimestamp\"><value>" + escape(LocalDateTime.now())
				+ "</value></Data>\r\n"
				+ (response.getExtraInfo("occupantQuery").equals("true") ? escape(response.getExtraInfo("tags")) : "")
				+ "<Data name=\"version\"><value>" + escape(GeocoderConfig.VERSION)
				+ "</value></Data>\r\n"
				+ "<Data name=\"executionTime\"><value>" + response.getExtraInfo("executionTime")
				+ "</value></Data>\r\n"
				+ (response.getExtraInfo("tags").isEmpty() ? "" : "<Data name=\"tags\"><value>" 
						+ response.getExtraInfo("tags") + "</value></Data>\r\n")
				+ escape(config.getDisclaimer()) + "</value></Data>\r\n"
				+ "<Data name=\"privacyStatement\"><value>"
				+ escape(config.getPrivacyStatement()) + "</value></Data>\r\n"
				+ escape("http://www2.gov.bc.ca/gov/admin/privacy.page") + "</value></Data>\r\n"
				+ "<Data name=\"copyrightNotice\"><value>"
				+ escape(config.getCopyrightNotice())
				+ "</value></Data>\r\n"
				+ "<Data name=\"copyrightLicense\"><value>"
				+ escape(config.getCopyrightLicense())
				+ "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ "<styleUrl>" 
				+ (response.getExtraInfo("occupantQuery").equals("true") ? 
						config.getOccupantCategoryKmlStyleUrl() : config.getKmlStylesUrl())  
				+ "#reverse_results_heading</styleUrl>\r\n");
		for(SiteAddress addr : addrs) {
			buf.append(siteAddressToKML(addr, config));
		}
		return buf.toString();
	}
	
	String siteAddressToKML(SiteAddress addr, GeocoderConfig config) {
		String occupantStr = "";
		if(addr instanceof OccupantAddress) {
			OccupantAddress occ = (OccupantAddress)addr;
			occupantStr = "<Data name=\"occupantName\"><value>" + escape(occ.getOccupantName())
					+ "</value></Data>\r\n"
					+ "<Data name=\"occupantID\"><value>" + escape(occ.getOccupantId())
					+ "</value></Data>\r\n"
					+ "<Data name=\"occupantAliasAddress\"><value>" + escape(occ.getOccupantAliasAddress())
					+ "</value></Data>\r\n"
					+ "<Data name=\"occupantDescription\"><value>" + escape(occ.getOccupantDescription())
					+ "</value></Data>\r\n"
					+ "<Data name=\"contactEmail\"><value>" + escape(occ.getContactEmail())
					+ "</value></Data>\r\n"
					+ "<Data name=\"contactPhone\"><value>" + escape(occ.getContactPhone())
					+ "</value></Data>\r\n"
					+ "<Data name=\"contactFax\"><value>" + escape(occ.getContactFax())
					+ "</value></Data>\r\n"
					+ "<Data name=\"websiteUrl\"><value>" + escape(occ.getWebsiteUrl())
					+ "</value></Data>\r\n"
					+ "<Data name=\"imageUrl\"><value>" + escape(occ.getImageUrl())
					+ "</value></Data>\r\n"
					+ "<Data name=\"keywords\"><value>" + escape(occ.getKeywords())
					+ "</value></Data>\r\n"
					+ "<Data name=\"businessCategoryClass\"><value>" + escape(occ.getBusinessCategoryClass())
					+ "</value></Data>\r\n"
					+ "<Data name=\"businessCategoryDescription\"><value>" + escape(occ.getBusinessCategoryDescription())
					+ "</value></Data>\r\n"
					+ "<Data name=\"naicsCode\"><value>" + escape(occ.getNaicsCode())
					+ "</value></Data>\r\n"
					+ "<Data name=\"dateOccupantUpdated\"><value>" + escape(occ.getDateOccupantUpdated())
					+ "</value></Data>\r\n"
					+ "<Data name=\"dateOccupantAdded\"><value>" + escape(occ.getDateOccupantAdded())
					+ "</value></Data>\r\n";
		}
		return "<Placemark>\r\n"
				+ "<name>"
				+ escape(addr.getAddressString())
				+ "</name>\r\n"
				+ "<Snippet/>\r\n"
				+ getLookAt(addr, config)
				+ "<styleUrl>"
				+ getStyleUrl(config, REVERSE, addr) + "</styleUrl>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"fullAddress\"><value>" + escape(addr.getAddressString())
				+ "</value></Data>\r\n"
				+ "<Data name=\"siteName\"><value>" + escape(addr.getSiteName())
				+ "</value></Data>\r\n"
				+ "<Data name=\"unitDesignator\"><value>" + escape(addr.getUnitDesignator())
				+ "</value></Data>\r\n"
				+ "<Data name=\"unitNumber\"><value>" + escape(addr.getUnitNumber())
				+ "</value></Data>\r\n"
				+ "<Data name=\"unitNumberSuffix\"><value>" + escape(addr.getUnitNumberSuffix())
				+ "</value></Data>\r\n"
				+ "<Data name=\"civicNumber\"><value>"
				+ escape(GeocoderUtil.formatCivicNumber(addr.getCivicNumber()))
				+ "</value></Data>\r\n"
				+ "<Data name=\"civicNumberSuffix\"><value>" + escape(addr.getCivicNumberSuffix())
				+ "</value></Data>\r\n"
				+ "<Data name=\"streetName\"><value>" + escape(addr.getStreetName())
				+ "</value></Data>\r\n"
				+ "<Data name=\"streetType\"><value>" + escape(addr.getStreetType())
				+ "</value></Data>\r\n"
				+ "<Data name=\"isStreetTypePrefix\"><value>"
				+ escape(addr.isStreetTypePrefix()) + "</value></Data>\r\n"
				+ "<Data name=\"streetDirection\"><value>" + escape(addr.getStreetDirection())
				+ "</value></Data>\r\n"
				+ "<Data name=\"isStreetDirectionPrefix\"><value>"
				+ escape(addr.isStreetDirectionPrefix()) + "</value></Data>\r\n"
				+ "<Data name=\"streetQualifier\"><value>" + escape(addr.getStreetQualifier())
				+ "</value></Data>\r\n"
				+ "<Data name=\"localityName\"><value>" + escape(addr.getLocalityName())
				+ "</value></Data>\r\n"
				+ "<Data name=\"localityType\"><value>" + escape(addr.getLocalityType())
				+ "</value></Data>\r\n"
				+ "<Data name=\"electoralArea\"><value>" + escape(addr.getElectoralArea())
				+ "</value></Data>\r\n"
				+ "<Data name=\"provinceCode\"><value>" + escape(addr.getStateProvTerr())
				+ "</value></Data>\r\n"
				+ "<Data name=\"locationPositionalAccuracy\"><value>"
				+ escape(addr.getLocationPositionalAccuracy()) + "</value></Data>\r\n"
				+ "<Data name=\"locationDescriptor\"><value>"
				+ escape(addr.getLocationDescriptor())
				+ "</value></Data>\r\n"
				+ "<Data name=\"siteID\"><value>" + escape(addr.getSiteID())
				+ "</value></Data>\r\n"
				+ "<Data name=\"siteUrl\"><value>" + escape(addr.getSiteID())
				+ "</value></Data>\r\n"
				+ "<Data name=\"blockID\"><value>" + escape(addr.getStreetSegmentID())
				+ "</value></Data>\r\n"
				+ "<Data name=\"fullSiteDescriptor\"><value>"
				+ escape(addr.getFullSiteDescriptor()) + "</value></Data>\r\n"
				// narrativeLocation is intentionally output as accessNotes
				+ "<Data name=\"accessNotes\"><value>" + escape(addr.getNarrativeLocation())
				+ "</value></Data>\r\n"
				+ "<Data name=\"siteStatus\"><value>" + escape(addr.getSiteStatus())
				+ "</value></Data>\r\n"
				+ "<Data name=\"siteRetireDate\"><value>" + escape(addr.getSiteRetireDate())
				+ "</value></Data>\r\n"
				+ "<Data name=\"changeDate\"><value>" + escape(addr.getSiteChangeDate())
				+ "</value></Data>\r\n"
				+ "<Data name=\"isOfficial\"><value>" + (addr.isPrimary() ? "true" : "false")
				+ "</value></Data>\r\n"
				+ occupantStr
				+ "</ExtendedData>\r\n"
				+ getPoint(addr)
				+ "</Placemark>";
	}
	
	String streetIntersectionAddressesToKML(StreetIntersectionAddress[] addrs,
			GeocoderConfig config, OlsResponse response) {
		StringBuilder buf = new StringBuilder("<Document>\r\n"
				+ "<name>Results for Intersection Search</name>\r\n"
				+ "<open>1</open>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"searchTimestamp\"><value>" + escape(LocalDateTime.now())
				+ "</value></Data>\r\n"
				+ "<Data name=\"version\"><value>" + escape(GeocoderConfig.VERSION)
				+ "</value></Data>\r\n"
				+ "<Data name=\"executionTime\"><value>" + response.getExtraInfo("executionTime")
				+ "</value></Data>\r\n"
				+ "<Data name=\"minDegree\"><value>" + response.getExtraInfo("minDegree")
				+ "</value></Data>\r\n"
				+ "<Data name=\"maxDegree\"><value>" + response.getExtraInfo("maxDegree")
				+ "</value></Data>\r\n"
				+ "<Data name=\"disclaimer\"><value>"
				+ escape(config.getDisclaimer()) + "</value></Data>\r\n"
				+ "<Data name=\"privacyStatement\"><value>"
				+ escape(config.getPrivacyStatement()) + "</value></Data>\r\n"
				+ "<Data name=\"copyrightNotice\"><value>"
				+ escape(config.getCopyrightNotice())
				+ "</value></Data>\r\n"
				+ "<Data name=\"copyrightLicense\"><value>"
				+ escape(config.getCopyrightLicense())
				+ "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ "<styleUrl>" + config.getKmlStylesUrl()
				+ "#reverse_intersection_results_heading</styleUrl>\r\n");
		for(StreetIntersectionAddress addr : addrs) {
			buf.append(streetIntersectionAddressToKML(addr, config));
		}
		return buf.toString();
	}
	
	String streetIntersectionAddressToKML(StreetIntersectionAddress addr,
			GeocoderConfig config) {
		return "<Placemark>\r\n"
				+ "<name>" + escape(addr.getAddressString()) + "</name>\r\n"
				+ "<Snippet/>\r\n"
				+ getLookAt(addr, config)
				+ "<styleUrl>" + getStyleUrl(config, REVERSE, addr) + "</styleUrl>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"fullAddress\"><value>" + escape(addr.getAddressString())
				+ "</value></Data>\r\n"
				+ "<Data name=\"intersectionName\"><value>" + escape(addr.getName())
				+ "</value></Data>\r\n"
				+ "<Data name=\"localityName\"><value>" + escape(addr.getLocalityName())
				+ "</value></Data>\r\n"
				+ "<Data name=\"localityType\"><value>" + escape(addr.getLocalityType())
				+ "</value></Data>\r\n"
				+ "<Data name=\"provinceCode\"><value>" + escape(addr.getStateProvTerr())
				+ "</value></Data>\r\n"
				+ "<Data name=\"locationPositionalAccuracy\"><value>"
				+ escape(addr.getLocationPositionalAccuracy()) + "</value></Data>\r\n"
				+ "<Data name=\"locationDescriptor\"><value>"
				+ escape(addr.getLocationDescriptor())
				+ "</value></Data>\r\n"
				+ "<Data name=\"intersectionID\"><value>" + escape(addr.getID())
				+ "</value></Data>\r\n"
				+ "<Data name=\"degree\"><value>" + escape(addr.getDegree())
				+ "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ getPoint(addr)
				+ "</Placemark>";
	}
	

	String escape(Object field) {
		if(field == null) {
			return "";
		}
		field = OlsResponseWriter.formatDate(field);
		return StringEscapeUtils.escapeXml10(field.toString());
	}
	
}
