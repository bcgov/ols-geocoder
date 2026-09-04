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
package ca.bc.gov.ols.geocoder.api.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.data.enumTypes.Interpolation;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * SearchResults holds all of the information related to the result of a geocode or other search. It
 * also support JAXB-based xml output.
 * 
 * @author chodgson
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Schema(description = "The complete search results for a geocode query, including match information, scoring, metadata, and the list of matching addresses.")
public class SearchResults {
	
	@XmlElement
	@Schema(description = "The original query address that was geocoded.")
	private String queryAddress;
	
	@XmlElementWrapper
	@XmlElementRef
	@Schema(description = "The list of matching addresses, sorted by score (highest first). Each match includes the full address, location, and scoring details.")
	private List<GeocodeMatch> matches;
	
	@XmlElement
	@Schema(description = "The EPSG code of the spatial reference system used for the output geometries.")
	private int srsCode;
	
	@XmlElement
	@Schema(description = "The maximum number of search results returned.")
	private int maxResults;
	
	@XmlElement
	@Schema(description = "The minimum score required for a match to be returned.")
	private int minScore;
	
	@XmlElement
	@Schema(description = "The distance (in metres) to move the access point away from the curb towards the inside of the parcel.")
	private int setBack;

	@XmlElement
	@Schema(description = "A semicolon-separated list of tags associated with the results.")
	private String tags;

	@XmlElement
	@Schema(description = "Whether unmatched address details are included in the results.")
	private boolean isEcho;
	
	@XmlElement
	@Schema(description = "Whether the results include addresses with known civic numbers.")
	private boolean knownAddresses;
	
	@XmlElement
	@Schema(description = "The timestamp when the search was executed.")
	private LocalDateTime searchTimeStamp;
	
	@XmlElement
	@Schema(description = "The execution time of the search in seconds.")
	private BigDecimal executionTime;
	
	@XmlElement
	@Schema(description = "The date and time when the underlying geocoder data was last processed.")
	private ZonedDateTime processingDate;
	
	@XmlElement
	@Schema(description = "A legal disclaimer for the geocoder service.")
	private String disclaimer;
	
	@XmlElement
	@Schema(description = "The interpolation method used for address matching.")
	private Interpolation interpolation;
	
	@XmlElement
	@Schema(description = "The privacy statement for the geocoder service.")
	private String privacyStatement;
	
	@XmlElement
	@Schema(description = "The copyright notice for the geocoder data.")
	private String copyrightNotice;
	
	@XmlElement
	@Schema(description = "The copyright license for the geocoder data.")
	private String copyrightLicense;
	
	@XmlElement
	@Schema(description = "The location descriptor used for the output addresses.")
	private LocationDescriptor locationDescriptor;
	
	// response body attribute
	@XmlElement
	@Schema(description = "The character encoding of the output. Defaults to 'ascii'.")
	private String encoding = "ascii";
	
	public SearchResults() {
	}
	
	/**
	 * Accepts a query and list of matches (assumed to be sorted by score, highest first) and
	 * creates a SearchResults Object.
	 * 
	 * @param query the query used to create these results
	 * @param matches the results that matched the query
	 * @param processingDate the date
	 */
	public SearchResults(GeocodeQuery query, List<GeocodeMatch> matches, ZonedDateTime processingDate) {
		this.queryAddress = query.getQueryAddress();
		this.matches = matches;
		this.maxResults = query.getMaxResults();
		this.minScore = query.getMinScore();
		this.setBack = query.getSetBack();
		this.tags = query.getTags();
		this.isEcho = query.isEcho();
		this.locationDescriptor = query.getLocationDescriptor();
		this.processingDate = processingDate;
		this.searchTimeStamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		this.executionTime = new BigDecimal(String.format("%.3f",
				query.getExecutionTimeNanos() / 1000000f));
	}
	
	public List<GeocodeMatch> getMatches() {
		return matches;
	}
	
	public GeocodeMatch getBestMatch() {
		if(matches.size() == 0) {
			return null;
		}
		return matches.get(0);
	}
	
	public Integer getBestScore() {
		GeocodeMatch match = getBestMatch();
		if(match == null) {
			return 0;
		}
		return match.getScore();
	}
	
	public String getQueryAddress() {
		return queryAddress;
	}
	
	public Interpolation getInterpolation() {
		return interpolation;
	}
	
	public int getSetBack() {
		return setBack;
	}

	public String getTags() {
		return tags;
	}

	public boolean getIsEcho() {
		return isEcho;
	}
	
	public int getMinScore() {
		return minScore;
	}
	
	public int getMaxResults() {
		return maxResults;
	}
	
	public int getSrsCode() {
		return srsCode;
	}
	
	public void setSrsCode(int srsCode) {
		this.srsCode = srsCode;
	}
	
	public void setInterpolation(Interpolation interp) {
		this.interpolation = interp;
	}
	
	public LocalDateTime getSearchTimeStamp() {
		return searchTimeStamp;
	}
	
	public BigDecimal getExecutionTime() {
		return executionTime;
	}
	
	public String getDisclaimer() {
		return disclaimer;
	}
	
	public String getPrivacyStatement() {
		return privacyStatement;
	}
	
	public String getCopyrightNotice() {
		return copyrightNotice;
	}
	
	public String getCopyrightLicense() {
		return copyrightLicense;
	}
	
	public LocationDescriptor getLocationDescriptor() {
		return locationDescriptor;
	}
	
	public void setLocationDescriptor(LocationDescriptor locationDescriptor) {
		this.locationDescriptor = locationDescriptor;
	}

	public ZonedDateTime getProcessingDate() {
		return processingDate;
	}

	public String getEncoding() {
		if(encoding == null || encoding.isBlank()) {
			return "ascii";
		}
		return encoding;
	}

	public void setEncoding(String encoding) {
		if(encoding == null || encoding.isBlank()) {
			this.encoding = "ascii";
			return;
		}
		String normalized = encoding.trim().toLowerCase();
		if("ascii".equals(normalized)
				|| "extended-ascii".equals(normalized)
				|| "utf-8".equals(normalized)) {
			this.encoding = normalized;
		} else {
			this.encoding = "utf-8";
		}
	}
}
