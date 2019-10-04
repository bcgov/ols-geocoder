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

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PhysicalStatus;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

public class SiteFactory {

	public static ISite createSite(int id,
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
		if((siteName == null || siteName.isEmpty())
				&& (unitNumber == null || unitNumber.isEmpty())
				&& (unitNumberSuffix == null || unitNumberSuffix.isEmpty())
				&& (unitDesignator == null || unitDesignator.isEmpty())
				&& status == PhysicalStatus.ACTIVE
				&& retireDate.equals(GeocoderConfig.NOT_RETIRED_DATE)) {
			return new CompactSite(id, uuid, pids, parent, location, 
					locationPositionalAccuracy, locationDescriptor, changeDate);
		} else {
			return new Site(id, uuid, pids, parent, siteName, 
					unitNumber, unitNumberSuffix, unitDesignator, 
					location, locationPositionalAccuracy, locationDescriptor, 
					status, retireDate, changeDate);
		}
	}
}
