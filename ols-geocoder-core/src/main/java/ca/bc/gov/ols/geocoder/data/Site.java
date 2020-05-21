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
public class Site extends AbstractSite {
	static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ Site.class.getCanonicalName());
	
	String siteName;
	String unitNumber;
	String unitNumberSuffix;
	String unitDesignator;
	private AccessPoint primaryAccessPoint;
	private PositionalAccuracy locationPositionalAccuracy;
	private LocationDescriptor locationDescriptor;
	private PhysicalStatus status;
	private LocalDate retireDate;
	private LocalDate changeDate;
	
	public Site(int id,
			UUID uuid,
			String pids,
			Object parent,
			String siteName,
			String unitNumber,
			String unitNumberSuffix,
			String unitDesignator,
			Point location,
			PositionalAccuracy locationPositionalAccuracy,
			LocationDescriptor locationDescriptor,
			PhysicalStatus status,
			LocalDate retireDate,
			LocalDate changeDate) {
		super(id, uuid, pids, parent, location);
		this.siteName = siteName;
		this.unitNumber = unitNumber;
		this.unitNumberSuffix = unitNumberSuffix;
		this.unitDesignator = unitDesignator;
		this.locationPositionalAccuracy = locationPositionalAccuracy;
		this.locationDescriptor = locationDescriptor;
		this.status = status;
		this.retireDate = retireDate;
		this.changeDate = changeDate;
	}
	
	@Override
	public String getSiteName() {
		return siteName;
	}
	
	@Override
	public String getUnitNumber() {
		return unitNumber;
	}
	
	@Override
	public String getUnitNumberSuffix() {
		return unitNumberSuffix;
	}
	
	@Override
	public String getUnitDesignator() {
		return unitDesignator;
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
		return status;
	}

	@Override
	public LocalDate getRetireDate() {
		return retireDate;
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
	
}

