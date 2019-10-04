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
import java.util.Comparator;
import java.util.UUID;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

public interface IRbSite {

	public static final Comparator<IRbSite> MEASURE_COMPARATOR = new Comparator<IRbSite>() {
		@Override
		public int compare(IRbSite site1, IRbSite site2) {
			double diff = site1.getMeasure() - site2.getMeasure();
			if(diff < 0) {
				return -1;
			} else if(diff > 0) {
				return 1;
			} else {
				return 0;
			}
		}
	};
	
	public static final Comparator<IRbSite> ADDRESS_COMPARATOR = new Comparator<IRbSite>() {
		@Override
		public int compare(IRbSite site1, IRbSite site2) {
			return site1.getCivicNumber() - site2.getCivicNumber();
		}
	};


	public abstract int getSiteId();

	public abstract void setSiteId(int siteId);

	public abstract String getInputName();

	public abstract void setInputName(String inputName);

	public abstract UUID getSiteUuid();

	public abstract void setSiteUuid(UUID siteUuid);

	public abstract int getParentSiteId();

	public abstract void setParentSiteId(int parentSiteId);

	public abstract String getSiteName();

	public abstract void setSiteName(String siteName);

	public abstract LocationDescriptor getLocationDescriptor();

	public abstract void setLocationDescriptor(
			LocationDescriptor locationDescriptor);

	public abstract String getUnitDesignator();

	public abstract void setUnitDesignator(String unitDesignator);

	public abstract String getUnitNumber();

	public abstract void setUnitNumber(String unitNumber);

	public abstract String getUnitNumberSuffix();

	public abstract void setUnitNumberSuffix(String unitNumberSuffix);

	public abstract PositionalAccuracy getSitePositionalAccuracy();

	public abstract void setSitePositionalAccuracy(
			PositionalAccuracy sitePositionalAccuracy);

	public abstract String getSiteStatus();

	public abstract void setSiteStatus(String siteStatus);

	public abstract LocalDate getSiteChangeDate();

	public abstract void setSiteChangeDate(LocalDate siteRetireDate);

	public abstract LocalDate getSiteRetireDate();

	public abstract void setSiteRetireDate(LocalDate siteRetireDate);

	public abstract Point getSiteLocation();

	public abstract void setSiteX(double siteX);
	
	public abstract void setSiteY(double siteY);

	public abstract int getAccessPointId();

	public abstract void setAccessPointId(int accessPointId);

	public abstract String getApType();

	public abstract void setApType(String apType);

	public abstract boolean isPrimary();

	public abstract void setPrimary(boolean isPrimary);

	public abstract String getNarrativeLocation();

	public abstract void setNarrativeLocation(String narrativeLocation);

	public abstract PositionalAccuracy getAccessPositionalAccuracy();

	public abstract void setAccessPositionalAccuracy(
			PositionalAccuracy accessPositionalAccuracy);

	public abstract int getCivicNumber();

	public abstract void setCivicNumber(int civicNumber);

	public abstract String getCivicNumberSuffix();

	public abstract void setCivicNumberSuffix(String civicNumberSuffix);

	public abstract String getAccessPointStatus();

	public abstract void setAccessPointStatus(String accessPointStatus);

	public abstract LocalDate getAccessRetireDate();

	public abstract void setAccessRetireDate(LocalDate accessRetireDate);

	public abstract Point getAccessLocation();

	public abstract void setOriginalAP(boolean isOriginalAP);
	
	public abstract boolean isOriginalAP();

	public abstract void setAccessX(double accessX);
	
	public abstract void setAccessY(double accessY);

	public abstract String getFullAddress();

	public abstract void setFullAddress(String fullAddress);

	public abstract int getStreetSegmentId();

	public abstract void setStreetSegmentId(int blockID);

	public abstract String getPids();

	public abstract void setPids(String pids);

	public abstract int getLocalityId();

	public abstract void setLocalityId(int localityID);

	public abstract int getInterimStreetNameId();

	public abstract void setInterimStreetNameId(int interimStreetNameId);

	public abstract double getMeasure();

	public void setMeasure(double measure);

}