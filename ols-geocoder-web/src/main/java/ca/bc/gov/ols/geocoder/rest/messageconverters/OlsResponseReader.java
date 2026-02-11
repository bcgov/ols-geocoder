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
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

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
import ca.bc.gov.ols.geocoder.rest.PidsResponse;
import ca.bc.gov.ols.geocoder.rest.PointOnBlock;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;

public class OlsResponseReader {

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	private OlsResponse response;
	private OlsResponseWriter writer;
	private GeocoderConfig config;
	
	OlsResponseReader(OlsResponse response, OlsResponseWriter writer, GeocoderConfig config) {
		this.response = response;
		this.writer = writer;
		this.config = config;
	}
	
	public void convert() throws IOException {
		Object responseObj = response.getResponseObj();
		writer.documentHeader();
		if(responseObj == null) {
			writer.empty();
		} else if(responseObj instanceof SearchResults) {
			searchResults((SearchResults)responseObj);
		} else if(responseObj instanceof SiteAddress) {
			siteAddress((SiteAddress)responseObj);
		} else if(responseObj instanceof StreetIntersectionAddress) {
			streetIntersectionAddress((StreetIntersectionAddress)responseObj);
		} else if(responseObj instanceof SiteAddress[]) {
			SiteAddress[] addrs = (SiteAddress[])responseObj;
//			if(addrs.length == 0) {
//				writer.noMatches();
//			} else {
				writer.featureCollectionHeader();
				for(SiteAddress addr : addrs) {
					siteAddress(addr);
				}
				writer.featureCollectionFooter();
//			}
		} else if(responseObj instanceof StreetIntersectionAddress[]) {
			StreetIntersectionAddress[] addrs = (StreetIntersectionAddress[])responseObj;
//			if(addrs.length == 0) {
//				writer.noMatches();
//			} else {
				writer.featureCollectionHeader();
				for(StreetIntersectionAddress addr : addrs) {
					streetIntersectionAddress(addr);
				}
				writer.featureCollectionFooter();
//			}
		} else if(responseObj instanceof PointOnBlock) {
			pointOnBlock((PointOnBlock)responseObj);
		} else if(responseObj instanceof PidsResponse) {
			pidsResponse((PidsResponse)responseObj);
		} else {
			// should never get here but this is handy for debugging if we do
			writer.unknown(responseObj);
		}
		writer.documentFooter();
	}

	public void searchResults(SearchResults results) throws IOException {
		writer.searchResultsHeader();
		writer.field("queryAddress", results.getQueryAddress());
		writer.field("searchTimestamp", results.getSearchTimeStamp());
		writer.field("executionTime", results.getExecutionTime());
		writer.field("version", GeocoderConfig.VERSION);
		writer.field("baseDataDate", results.getProcessingDate());
		writer.field("srsCode", results.getSrsCode());
		writer.field("interpolation", results.getInterpolation());
		writer.field("echo", results.getIsEcho());
		writer.field("locationDescriptor", results.getLocationDescriptor());
		writer.field("setBack", results.getSetBack());
		writer.field("minScore", results.getMinScore());
		writer.field("maxResults", results.getMaxResults());
		writer.field("disclaimer", config.getDisclaimer());
		writer.field("privacyStatement", config.getPrivacyStatement());
		writer.field("copyrightNotice", config.getCopyrightNotice());
		writer.field("copyrightLicense", config.getCopyrightLicense());
		
		writer.matchesHeader();
		Iterator<GeocodeMatch> it = results.getMatches().iterator();
		while(it.hasNext()) {
			geocodeMatch(it.next());
		}
		writer.matchesFooter();
		writer.searchResultsFooter();
	}	
	
	public void geocodeMatch(GeocodeMatch match) throws IOException {
		if(match instanceof AddressMatch) {
			siteAddress(((AddressMatch)match).getAddress(), match);
		} else if(match instanceof IntersectionMatch) {
			streetIntersectionAddress(((IntersectionMatch)match).getAddress(), match);
		}
	}
	
	public void match(GeocodeMatch match) throws IOException {
		writer.field("score", match.getScore());
		writer.field("matchPrecision", match.getPrecision());
		writer.field("precisionPoints", match.getPrecisionPoints());
		writer.faultsHeader();
		for(MatchFault fault : match.getFaults()) {
			writer.faultHeader();
			writer.field("value", fault.getValue());
			writer.field("element", fault.getElement());
			writer.field("fault", fault.getFault());
			writer.field("penalty", fault.getPenalty());
			writer.faultFooter();
		}
		writer.faultsFooter();
	}
	
	public void siteAddress(SiteAddress addr) throws IOException {
		siteAddress(addr, null);
	}
	
	public void siteAddress(SiteAddress addr, GeocodeMatch match) throws IOException {
		writer.featureHeader(addr.getLocation());
		writer.field("addressString", "fullAddress", addr.getAddressString());
		if(match != null) {
			match(match);
		}
		if(!response.isBrief()) {
			writer.field("siteName", addr.getSiteName());
			writer.field("unitDesignator", addr.getUnitDesignator());
			writer.field("unitNumber", addr.getUnitNumber());
			writer.field("unitNumberSuffix", addr.getUnitNumberSuffix());
			writer.field("civicNumber", GeocoderUtil.formatCivicNumber(addr.getCivicNumber()));
			writer.field("civicNumberSuffix", addr.getCivicNumberSuffix());
			writer.field("streetName", addr.getStreetName());
			writer.field("streetType", addr.getStreetType());
			writer.field("isStreetTypePrefix", addr.isStreetTypePrefix());
			writer.field("streetDirection", addr.getStreetDirection());
			writer.field("isStreetDirectionPrefix", addr.isStreetDirectionPrefix());
			writer.field("streetQualifier", addr.getStreetQualifier());
			writer.field("localityName", addr.getLocalityName());
			writer.field("streetAddress", addr.getStreetAddress());
		}
		writer.field("localityType", addr.getLocalityType());
		writer.field("electoralArea", addr.getElectoralArea());
		if(!response.isBrief()) {
			writer.field("provinceCode", addr.getStateProvTerr());
		}
		writer.field("locationPositionalAccuracy", addr.getLocationPositionalAccuracy());
		writer.field("locationDescriptor", addr.getLocationDescriptor());
		writer.field("siteID", addr.getSiteID());
		writer.field("blockID", addr.getStreetSegmentID());
		if(!response.isBrief()) {
			writer.field("fullSiteDescriptor", addr.getFullSiteDescriptor());
		}
		// narrativeLocation is intentionally output as accessNotes
		writer.field("accessNotes", addr.getNarrativeLocation());
		writer.field("siteStatus", addr.getSiteStatus());
		if(!response.isBrief()) {
			writer.field("siteRetireDate", addr.getSiteRetireDate());
			writer.field("changeDate", addr.getSiteChangeDate());
			writer.field("isOfficial", (addr.isPrimary() ? "true" : "false"));
		}
		if(addr instanceof OccupantAddress) {
			OccupantAddress occ = (OccupantAddress)addr;
			writer.field("occupantName", occ.getOccupantName());
			writer.field("occupantID", occ.getOccupantId());
			writer.field("occupantAliasAddress", occ.getOccupantAliasAddress());
			writer.field("occupantDescription", occ.getOccupantDescription());
			writer.field("contactEmail", occ.getContactEmail());
			writer.field("contactPhone", occ.getContactPhone());
			writer.field("contactFax", occ.getContactFax());
			writer.field("websiteUrl", occ.getWebsiteUrl());
			writer.field("imageUrl", occ.getImageUrl());
			writer.field("keywords", occ.getKeywords());
			writer.field("businessCategoryClass", occ.getBusinessCategoryClass());
			writer.field("businessCategoryDescription", occ.getBusinessCategoryDescription());
			writer.field("naicsCode", occ.getNaicsCode());
			writer.field("dateOccupantUpdated",occ.getDateOccupantUpdated());
			writer.field("dateOccupantAdded", occ.getDateOccupantAdded());
		}
		writer.featureFooter();
	}
	
	public void streetIntersectionAddress(StreetIntersectionAddress addr) throws IOException {
		streetIntersectionAddress(addr, null);
	}
	
	public void streetIntersectionAddress(StreetIntersectionAddress addr,
			GeocodeMatch match) throws IOException {
		writer.featureHeader(addr.getLocation());
		writer.field("addressString", "fullAddress", addr.getAddressString());
		if(match != null) {
			match(match);
		}
		writer.field("intersectionName", addr.getName());
		writer.field("localityName", addr.getLocalityName());
		writer.field("localityType", addr.getLocalityType());
		writer.field("provinceCode", addr.getStateProvTerr());
		writer.field("locationPositionalAccuracy", addr.getLocationPositionalAccuracy());
		writer.field("locationDescriptor", addr.getLocationDescriptor());
		writer.field("intersectionID", addr.getID());
		writer.field("degree", addr.getDegree());
		writer.featureFooter();
	}

	private void pointOnBlock(PointOnBlock pob) throws IOException {
		writer.featureHeader(pob.getLocation());
		writer.field("locationDescriptor", pob.getLocationDescriptor());
		writer.field("blockID", pob.getBlockId());
		writer.featureFooter();		
	}

	private void pidsResponse(PidsResponse pr) throws IOException {
		writer.fieldListHeader();
		writer.field("siteID", pr.getSiteUuid());
		writer.field("pids", pr.getPids(), true);
		writer.fieldListFooter();
	}


}
