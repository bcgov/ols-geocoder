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

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An occupant address representation including site address details and occupant information such as name, contact details, and business category.")
public class OccupantAddress extends SiteAddress {

	@Schema(description = "The UUID of the occupant.")
	private String occupantId; // this is the UUID
	@Schema(description = "The name of the occupant (business or organization).")
	private String occupantName;
	@Schema(description = "A description of the occupant.")
	private String occupantDescription;
	@Schema(description = "An alternative address for the occupant.")
	private String occupantAliasAddress;
	@Schema(description = "The contact phone number.")
	private String contactPhone;
	@Schema(description = "The contact email address.")
	private String contactEmail;
	@Schema(description = "The contact fax number.")
	private String contactFax;
	@Schema(description = "The website URL of the occupant.")
	private String websiteUrl;
	@Schema(description = "The image URL of the occupant.")
	private String imageUrl;
	@Schema(description = "A list of keyword tags associated with the occupant.")
	private List<String> keywordList;
	@Schema(description = "The business category class.")
	private String businessCategoryClass;
	@Schema(description = "The business category description.")
	private String businessCategoryDescription;
	@Schema(description = "The NAICS code for the business.")
	private String naicsCode;
	@Schema(description = "The date the occupant information was last updated.")
	private LocalDate dateOccupantUpdated;
	@Schema(description = "The date the occupant was added.")
	private LocalDate dateOccupantAdded;
	//private String custodianId;
	//private String sourceDataId;
	@Schema(description = "A custom style name for mapping display.")
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
