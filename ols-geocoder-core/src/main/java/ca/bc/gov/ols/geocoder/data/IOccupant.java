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

import org.locationtech.jts.geom.Point;

public interface IOccupant extends ILocation {

	public abstract int getId();

	public abstract UUID getUuid();

	public abstract ISite getSite();

	public abstract String getName();

	public abstract String getDescription();

	public abstract String getAliasAddress();

	public abstract String getContactPhone();

	public abstract String getContactEmail();

	public abstract String getContactFax();

	public abstract String getWebsiteUrl();

	public abstract String getImageUrl();

	public abstract void setKeywords(List<String> keywords);

	public abstract List<String> getKeywords();

	public abstract BusinessCategory getBusinessCategory();

	public abstract LocalDate getDateUpdated();

	public abstract LocalDate getDateAdded();

	public abstract String getCustomStyleName();

	public abstract Point getLocation();

	public abstract AccessPoint getPrimaryAccessPoint();

}