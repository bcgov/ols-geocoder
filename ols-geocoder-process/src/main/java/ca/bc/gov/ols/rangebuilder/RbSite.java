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
package ca.bc.gov.ols.rangebuilder;

import java.time.LocalDate;
import java.util.UUID;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

public class RbSite extends AbstractRbSite {
	private UUID siteUuid;
	private int parentSiteId;
	private String siteName;
	private LocationDescriptor locationDescriptor;
	private String unitDesignator;
	private String unitNumber;
	private String unitNumberSuffix; 
	private PositionalAccuracy sitePositionalAccuracy;
	private String siteStatus;
	private LocalDate siteChangeDate;
	private LocalDate siteRetireDate;
	private int accessPointId;
	private String apType;
	private boolean isPrimary;
	private String narrativeLocation;
	private PositionalAccuracy accessPositionalAccuracy;
	private String civicNumberSuffix;
	private String accessPointStatus;
	private LocalDate accessRetireDate;
	private String pids;
	private double accessX;
	private double accessY;
	private boolean originalAP;
	

	@Override
	public UUID getSiteUuid() {
		return siteUuid;
	}

	@Override
	public void setSiteUuid(UUID siteUuid) {
		this.siteUuid = siteUuid;
	}

	@Override
	public int getParentSiteId() {
		return parentSiteId;
	}

	@Override
	public void setParentSiteId(int parentSiteId) {
		this.parentSiteId = parentSiteId;
	}

	@Override
	public String getSiteName() {
		return siteName;
	}

	@Override
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	@Override
	public LocationDescriptor getLocationDescriptor() {
		return locationDescriptor;
	}

	@Override
	public void setLocationDescriptor(LocationDescriptor locationDescriptor) {
		this.locationDescriptor = locationDescriptor;
	}

	@Override
	public String getUnitDesignator() {
		return unitDesignator;
	}

	@Override
	public void setUnitDesignator(String unitDesignator) {
		this.unitDesignator = unitDesignator;
	}

	@Override
	public String getUnitNumber() {
		return unitNumber;
	}

	@Override
	public void setUnitNumber(String unitNumber) {
		this.unitNumber = unitNumber;
	}

	@Override
	public String getUnitNumberSuffix() {
		return unitNumberSuffix;
	}

	@Override
	public void setUnitNumberSuffix(String unitNumberSuffix) {
		this.unitNumberSuffix = unitNumberSuffix;
	}

	@Override
	public PositionalAccuracy getSitePositionalAccuracy() {
		return sitePositionalAccuracy;
	}

	@Override
	public void setSitePositionalAccuracy(PositionalAccuracy sitePositionalAccuracy) {
		this.sitePositionalAccuracy = sitePositionalAccuracy;
	}

	@Override
	public String getSiteStatus() {
		return siteStatus;
	}

	@Override
	public void setSiteStatus(String siteStatus) {
		this.siteStatus = siteStatus;
	}

	@Override
	public LocalDate getSiteChangeDate() {
		return siteChangeDate;
	}

	@Override
	public void setSiteChangeDate(LocalDate siteChangeDate) {
		this.siteChangeDate = siteChangeDate;
	}

	@Override
	public LocalDate getSiteRetireDate() {
		return siteRetireDate;
	}

	@Override
	public void setSiteRetireDate(LocalDate siteRetireDate) {
		this.siteRetireDate = siteRetireDate;
	}

	@Override
	public int getAccessPointId() {
		return accessPointId;
	}

	@Override
	public void setAccessPointId(int accessPointId) {
		this.accessPointId = accessPointId;
	}

	@Override
	public String getApType() {
		return apType;
	}

	@Override
	public void setApType(String apType) {
		this.apType = apType;
	}

	@Override
	public boolean isPrimary() {
		return isPrimary;
	}

	@Override
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	@Override
	public String getNarrativeLocation() {
		return narrativeLocation;
	}

	@Override
	public void setNarrativeLocation(String narrativeLocation) {
		this.narrativeLocation = narrativeLocation;
	}

	@Override
	public PositionalAccuracy getAccessPositionalAccuracy() {
		return accessPositionalAccuracy;
	}

	@Override
	public void setAccessPositionalAccuracy(
			PositionalAccuracy accessPositionalAccuracy) {
		this.accessPositionalAccuracy = accessPositionalAccuracy;
	}

	@Override
	public String getCivicNumberSuffix() {
		return civicNumberSuffix;
	}

	@Override
	public void setCivicNumberSuffix(String civicNumberSuffix) {
		this.civicNumberSuffix = civicNumberSuffix;
	}

	@Override
	public String getAccessPointStatus() {
		return accessPointStatus;
	}

	@Override
	public void setAccessPointStatus(String accessPointStatus) {
		this.accessPointStatus = accessPointStatus;
	}

	@Override
	public LocalDate getAccessRetireDate() {
		return accessRetireDate;
	}

	@Override
	public void setAccessRetireDate(LocalDate accessRetireDate) {
		this.accessRetireDate = accessRetireDate;
	}

	@Override
	public Point getAccessLocation() {
		if(Double.isNaN(accessX) || Double.isNaN(accessY)) {
			return null;
		}
		return RangeBuilder.getGeometryFactory().createPoint(new Coordinate(accessX, accessY));
	}

	@Override
	public void setAccessX(double accessX) {
		this.accessX = accessX;
	}

	@Override
	public void setAccessY(double accessY) {
		this.accessY = accessY;
	}

	@Override
	public String getPids() {
		return pids;
	}

	@Override
	public void setPids(String pids) {
		this.pids = pids;
	}

	public void setOriginalAP(boolean isOriginalAP) {
		originalAP = isOriginalAP;
	}

	public boolean isOriginalAP() {
		return originalAP;
	}

	@Override
	public CoordinateSequence copy() {
		throw new RuntimeException("This coordinateSequence is not copyable");
	}

}
