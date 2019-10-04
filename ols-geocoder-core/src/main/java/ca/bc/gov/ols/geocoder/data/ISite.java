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

import gnu.trove.map.hash.TIntObjectHashMap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PhysicalStatus;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

public interface ISite extends ILocation {

	public abstract String getFullSiteDescriptor();

	public abstract String getParentSiteDescriptor();

	public abstract String getFullSiteName();

	public abstract int getId();

	public abstract UUID getUuid();

	public abstract boolean isParentResolved();

	public abstract ISite getParent();

	public abstract Integer getParentId();

	public abstract String getSiteName();

	public abstract String getUnitNumber();

	public abstract String getUnitNumberSuffix();

	public abstract String getUnitDesignator();

	public abstract PositionalAccuracy getLocationPositionalAccuracy();

	public abstract LocationDescriptor getLocationDescriptor();

	public abstract PhysicalStatus getStatus();

	public abstract LocalDate getRetireDate();

	public abstract LocalDate getChangeDate();
	
	public abstract String getPids();

	public abstract AccessPoint getPrimaryAccessPoint();

	public abstract void setPrimaryAccessPoint(AccessPoint ap);

	public abstract void trimToSize();

	/**
	 * Recursively checks if this site is a descendant of the given site.
	 * 
	 * @param site a possible ancestor Site
	 * @return true if this site is a descendant of the given site
	 */
	public abstract boolean isDescendantOf(ISite site);

	public abstract List<ISite> findChildrenByUnitNumber(String unitNumber);
	
	public abstract void findChildrenByUnitNumber(String unitNumber, ArrayList<ISite> matches);
	
	public abstract List<ISite> getChildren();

	public abstract void resolveParent(TIntObjectHashMap<ISite> idToSite);

	public void addChild(ISite child);


}