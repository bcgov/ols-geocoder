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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PhysicalStatus;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

import org.locationtech.jts.geom.Point;

/**
 * A Site represents a known physical address. Note that the Site class intended to be immutable;
 * the location Point is technically mutable but must never be changed.
 * 
 * @author chodgson
 * 
 */
public class CompactSite extends AbstractSite implements ISite {
	
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ CompactSite.class.getCanonicalName());
	
	private AccessPoint primaryAccessPoint;
	private PositionalAccuracy locationPositionalAccuracy;
	private LocationDescriptor locationDescriptor;
	private LocalDate changeDate;
	
	public CompactSite(int id,
			UUID uuid,
			String pids,
			Object parent,
			Point location,
			PositionalAccuracy locationPositionalAccuracy,
			LocationDescriptor locationDescriptor,
			LocalDate changeDate) {
		super(id, uuid, pids, parent, location);
		this.locationPositionalAccuracy = locationPositionalAccuracy;
		this.locationDescriptor = locationDescriptor;
		this.changeDate = changeDate;
	}
	
	@Override
	public ISite getParent() {
		return (ISite)parent;
	}

	@Override
	public Integer getParentId() {
		return (Integer)parent;
	}

	@Override
	public String getSiteName() {
		return null;
	}
	
	@Override
	public String getUnitNumber() {
		return null;
	}
	
	@Override
	public String getUnitNumberSuffix() {
		return null;
	}
	
	@Override
	public String getUnitDesignator() {
		return null;
	}
	
	@Override
	public PositionalAccuracy getLocationPositionalAccuracy() {
		return locationPositionalAccuracy;
	}
	
	@Override
	public LocationDescriptor getLocationDescriptor() {
		return locationDescriptor;
	}

	@Override
	public PhysicalStatus getStatus() {
		return PhysicalStatus.ACTIVE;
	}

	@Override
	public LocalDate getRetireDate() {
		return GeocoderConfig.NOT_RETIRED_DATE;
	}

	@Override
	public LocalDate getChangeDate() {
		return changeDate;
	}

	@Override
	public AccessPoint getPrimaryAccessPoint() {
		if(primaryAccessPoint == null && parent != null) {
			return ((ISite)parent).getPrimaryAccessPoint();
		}
		return primaryAccessPoint;
	}
	
	@Override
	public void setPrimaryAccessPoint(AccessPoint ap) {
		if(this.primaryAccessPoint != null) {
			logger.error("Attempt to modify immutable Site.primaryAccesspoint");
		} else {
			this.primaryAccessPoint = ap;
		}
	}
		
	@Override
	public void resolveParent(TIntObjectHashMap<ISite> idToSite) {
		if(!isParentResolved()) {
			Integer parentId = (Integer)parent; 
			parent = idToSite.get(parentId);
			if(parent == null) {
				logger.warn("CONSTRAINT VIOLATION: Site id: " + id + " has missing parentId: " + parentId);
			} else {
				((CompactSite)parent).addChild(this);
			}
		}
	}
	
}

