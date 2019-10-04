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
package ca.bc.gov.ols.geocoder.data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

import org.locationtech.jts.geom.Point;

public class Occupant implements IOccupant {

	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ Occupant.class.getCanonicalName());

	private final int id;
	private final UUID uuid;
	private final ISite site;
	private final String name;
	private final String description;
	private final String aliasAddress;
	private final String contactPhone;
	private final String contactEmail;
	private final String contactFax;
	private final String websiteUrl;
	private final String imageUrl;
	private List<String> keywords;
	private final BusinessCategory businessCategory;
	private final LocalDate dateUpdated;
	private final LocalDate dateAdded;
	//private final String custodianId;
	//private final String sourceDataId;
	private final String customStyleName;
	
	public Occupant(int id, UUID uuid, ISite site, String name, String description, 
			String aliasAddress, String contactPhone, String contactEmail, String contactFax, 
			String websiteUrl, String imageUrl, BusinessCategory businessCategory,
			LocalDate dateUpdated, LocalDate dateAdded, String customStyleName) {
		this.id = id;
		this.uuid = uuid;
		this.site = site;
		this.name = name;
		this.description = description;
		this.aliasAddress = aliasAddress;
		this.contactPhone = contactPhone;
		this.contactEmail = contactEmail;
		this.contactFax = contactFax;
		this.websiteUrl = websiteUrl;
		this.imageUrl = imageUrl;
		this.businessCategory = businessCategory;
		this.dateUpdated = dateUpdated;
		this.dateAdded = dateAdded;
		//this.custodianId = custodianId;
		//this.sourceDataId = sourceDataId;
		this.customStyleName = customStyleName;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getId()
	 */
	@Override
	public int getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getUuid()
	 */
	@Override
	public UUID getUuid() {
		return uuid;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getSite()
	 */
	@Override
	public ISite getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getAliasAddress()
	 */
	@Override
	public String getAliasAddress() {
		return aliasAddress;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getContactPhone()
	 */
	@Override
	public String getContactPhone() {
		return contactPhone;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getContactEmail()
	 */
	@Override
	public String getContactEmail() {
		return contactEmail;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getContactFax()
	 */
	@Override
	public String getContactFax() {
		return contactFax;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getWebsiteUrl()
	 */
	@Override
	public String getWebsiteUrl() {
		return websiteUrl;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getImageUrl()
	 */
	@Override
	public String getImageUrl() {
		return imageUrl;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#setKeywords(java.util.List)
	 */
	@Override
	public void setKeywords(List<String> keywords) {
		if(this.keywords != null) {
			logger.error("Keyword tags being reset on occupant uuid: " + uuid);
		}
		this.keywords = keywords;  
	}
	
	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getKeywords()
	 */
	@Override
	public List<String> getKeywords() {
		return keywords;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getBusinessCategory()
	 */
	@Override
	public BusinessCategory getBusinessCategory() {
		return businessCategory;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getDateUpdated()
	 */
	@Override
	public LocalDate getDateUpdated() {
		return dateUpdated;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getDateAdded()
	 */
	@Override
	public LocalDate getDateAdded() {
		return dateAdded;
	}

//	public String getCustodianId() {
//		return custodianId;
//	}
//
//	public String getSourceDataId() {
//		return sourceDataId;
//	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getCustomStyleName()
	 */
	@Override
	public String getCustomStyleName() {
		return customStyleName;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getLocation()
	 */
	@Override
	public Point getLocation() {
		return site.getLocation();
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.ols.data.IOccupant#getPrimaryAccessPoint()
	 */
	@Override
	public AccessPoint getPrimaryAccessPoint() {
		return site.getPrimaryAccessPoint();
	}


}
