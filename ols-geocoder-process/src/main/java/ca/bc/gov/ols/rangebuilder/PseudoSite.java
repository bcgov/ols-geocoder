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

import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.rowreader.RowReader;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Point;

public class PseudoSite extends AbstractRbSite {
	
	public PseudoSite() {
	}

	public PseudoSite(int civicNumber, RbStreetSegment segment, Point point, double measure) {
		setStreetSegmentId(segment.getSegmentId());
		setCivicNumber(civicNumber);
		setSiteX(point.getX());
		setSiteY(point.getY());
		setMeasure(measure);
	}

	@Override
	public UUID getSiteUuid() {
		return null;
	}

	@Override
	public void setSiteUuid(UUID siteUuid) {
	}

	@Override
	public int getParentSiteId() {
		return RowReader.NULL_INT_VALUE;
	}

	@Override
	public void setParentSiteId(int parentSiteId) {
	}

	@Override
	public String getSiteName() {
		return null;
	}

	@Override
	public void setSiteName(String siteName) {
	}

	@Override
	public LocationDescriptor getLocationDescriptor() {
		return null;
	}

	@Override
	public void setLocationDescriptor(LocationDescriptor locationDescriptor) {
	}

	@Override
	public String getUnitDesignator() {
		return null;
	}

	@Override
	public void setUnitDesignator(String unitDesignator) {
	}

	@Override
	public String getUnitNumber() {
		return null;
	}

	@Override
	public void setUnitNumber(String unitNumber) {
	}

	@Override
	public String getUnitNumberSuffix() {
		return null;
	}

	@Override
	public void setUnitNumberSuffix(String unitNumberSuffix) {
	}

	@Override
	public PositionalAccuracy getSitePositionalAccuracy() {
		return null;
	}

	@Override
	public void setSitePositionalAccuracy(
			PositionalAccuracy sitePositionalAccuracy) {
	}

	@Override
	public String getSiteStatus() {
		return null;
	}

	@Override
	public void setSiteStatus(String siteStatus) {
	}

	@Override
	public LocalDate getSiteChangeDate() {
		return null;
	}

	@Override
	public void setSiteChangeDate(LocalDate siteChangeDate) {
	}

	@Override
	public LocalDate getSiteRetireDate() {
		return null;
	}

	@Override
	public void setSiteRetireDate(LocalDate siteRetireDate) {
	}

	@Override
	public int getAccessPointId() {
		return RowReader.NULL_INT_VALUE;
	}

	@Override
	public void setAccessPointId(int accessPointId) {
	}

	@Override
	public String getApType() {
		return null;
	}

	@Override
	public void setApType(String apType) {
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public void setPrimary(boolean isPrimary) {
	}

	@Override
	public String getNarrativeLocation() {
		return null;
	}

	@Override
	public void setNarrativeLocation(String narrativeLocation) {
	}

	@Override
	public PositionalAccuracy getAccessPositionalAccuracy() {
		return null;
	}

	@Override
	public void setAccessPositionalAccuracy(
			PositionalAccuracy accessPositionalAccuracy) {
	}

	@Override
	public String getCivicNumberSuffix() {
		return null;
	}

	@Override
	public void setCivicNumberSuffix(String civicNumberSuffix) {
	}

	@Override
	public String getAccessPointStatus() {
		return null;
	}

	@Override
	public void setAccessPointStatus(String accessPointStatus) {
	}

	@Override
	public LocalDate getAccessRetireDate() {
		return null;
	}

	@Override
	public void setAccessRetireDate(LocalDate accessRetireDate) {
	}

	@Override
	public Point getAccessLocation() {
		return null;
	}

	@Override
	public void setAccessX(double accessX) {
	}

	@Override
	public void setAccessY(double accessY) {
	}

	@Override
	public String getPids() {
		return null;
	}

	@Override
	public void setPids(String pids) {
	}

	@Override
	public void setOriginalAP(boolean isOriginalAP) {
	}
	
	@Override
	public boolean isOriginalAP() {
		return false;
	}

	@Override
	public CoordinateSequence copy() {
		throw new RuntimeException("This coordinateSequence is not copyable");
	}

}