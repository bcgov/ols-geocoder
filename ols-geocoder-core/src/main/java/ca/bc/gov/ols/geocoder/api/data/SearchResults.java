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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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

/**
 * SearchResults holds all of the information related to the result of a geocode or other search. It
 * also support JAXB-based xml output.
 * 
 * @author chodgson
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResults {
	
	@XmlElement
	private String queryAddress;
	
	@XmlElementWrapper
	@XmlElementRef
	private List<GeocodeMatch> matches;
	
	@XmlElement
	private int srsCode;
	
	@XmlElement
	private int maxResults;
	
	@XmlElement
	private int minScore;
	
	@XmlElement
	private int setBack;

	@XmlElement
	private String tags;

	@XmlElement
	private boolean isEcho;
	
	@XmlElement
	private boolean knownAddresses;
	
	@XmlElement
	private LocalDateTime searchTimeStamp;
	
	@XmlElement
	private BigDecimal executionTime;
	
	@XmlElement
	private ZonedDateTime processingDate;
	
	@XmlElement
	private String disclaimer;
	
	@XmlElement
	private Interpolation interpolation;
	
	@XmlElement
	private String privacyStatement;
	
	@XmlElement
	private String copyrightNotice;
	
	@XmlElement
	private String copyrightLicense;
	
	@XmlElement
	private LocationDescriptor locationDescriptor;
	
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
	
	public void setSearchTimeStamp(LocalDateTime timeStamp) {
		this.searchTimeStamp = timeStamp;
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
}
