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

import java.time.LocalDate;
import java.util.List;

import ca.bc.gov.ols.geocoder.data.AccessPoint;
import ca.bc.gov.ols.geocoder.data.BusinessCategory;
import ca.bc.gov.ols.geocoder.data.IOccupant;

public class OccupantAddress extends SiteAddress {

	private String occupantId; // this is the UUID
	private String occupantName;
	private String occupantDescription;
	private String occupantAliasAddress;
	private String contactPhone;
	private String contactEmail;
	private String contactFax;
	private String websiteUrl;
	private String imageUrl;
	private List<String> keywordList;
	private String businessCategoryClass;
	private String businessCategoryDescription;
	private String naicsCode;
	private LocalDate dateOccupantUpdated;
	private LocalDate dateOccupantAdded;
	//private String custodianId;
	//private String sourceDataId;
	private String customStyleName;
	
	public OccupantAddress(IOccupant occ, AccessPoint ap) {
		super(occ.getSite(), ap);
		occupantId = occ.getUuid().toString();
		occupantName = occ.getName();
		occupantDescription = occ.getDescription();
		occupantAliasAddress = occ.getAliasAddress();
		contactPhone = occ.getContactPhone();
		contactEmail = occ.getContactEmail();
		contactFax = occ.getContactFax();
		websiteUrl = occ.getWebsiteUrl();
		imageUrl = occ.getImageUrl();
		keywordList = occ.getKeywords();
		BusinessCategory bc = occ.getBusinessCategory();
		businessCategoryClass = bc.getClassName();
		businessCategoryDescription = bc.getDescription();
		naicsCode = bc.getNaicsCode();
		dateOccupantUpdated = occ.getDateUpdated();
		dateOccupantAdded = occ.getDateAdded();
		//custodianId = occ.getCustodianId();
		//sourceDataId = occ.getSourceDataId();
		customStyleName = occ.getCustomStyleName();
	}

	public OccupantAddress() {
	}

	public String getOccupantId() {
		return occupantId;
	}

	public String getOccupantName() {
		return occupantName;
	}

	public void setOccupantName(String occupantName) {
		this.occupantName = occupantName;
	}

	public String getOccupantDescription() {
		return occupantDescription;
	}

	public String getOccupantAliasAddress() {
		return occupantAliasAddress;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public String getContactFax() {
		return contactFax;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public List<String> getKeywordList() {
		return keywordList;
	}
	
	public String getKeywords() {
		StringBuilder sb = new StringBuilder();
		for(String keyword : keywordList) {
			sb.append(keyword + ";");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	public String getBusinessCategoryClass() {
		return businessCategoryClass;
	}

	public String getBusinessCategoryDescription() {
		return businessCategoryDescription;
	}

	public String getNaicsCode() {
		return naicsCode;
	}

	public LocalDate getDateOccupantUpdated() {
		return dateOccupantUpdated;
	}

	public LocalDate getDateOccupantAdded() {
		return dateOccupantAdded;
	}

//	public String getCustodianId() {
//		return custodianId;
//	}
//
//	public String getSourceDataId() {
//		return sourceDataId;
//	}

	public String getCustomStyleName() {
		return customStyleName;
	}

	public String buildAddressString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(occupantName + " **");
		String fsd = getFullSiteDescriptor();
		// don't repeat the occupant/site name if they are identical
		if(!occupantName.equals(fsd)) {
			appendPart(sb, " ", getFullSiteDescriptor());
		}
		if(sb.charAt(sb.length()-1) != '*') {
			// add front gate onto end of site name
			sb.append(" -- ");
		}
		appendPart(sb, " ", buildStreetAddressString());
		return sb.toString();
	}
	
}
