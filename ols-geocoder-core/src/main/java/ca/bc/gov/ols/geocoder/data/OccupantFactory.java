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

public class OccupantFactory {
	
	public static IOccupant createOccupant(int id,
							UUID uuid,
							ISite site,
							String name,
							String description,
							String aliasAddress,
							String contactPhone,
							String contactEmail,
							String contactFax,
							String websiteUrl,
							String imageUrl,
							BusinessCategory businessCategory,
							LocalDate dateUpdated,
							LocalDate dateAdded,
							String customStyleName) {
		if((contactPhone == null || contactPhone.isEmpty())
				&& (contactEmail == null || contactEmail.isEmpty())
				&& (contactFax == null || contactFax.isEmpty())
				&& (websiteUrl == null || websiteUrl.isEmpty())
				&& (imageUrl == null || imageUrl.isEmpty())
				&& (customStyleName == null || customStyleName.isEmpty())) {
			return new CompactOccupant(id, uuid, site, name, description, aliasAddress, 
					businessCategory, dateUpdated, dateAdded);
		} else {
			return new Occupant(id, uuid, site, name, description, aliasAddress, 
					contactPhone, contactEmail, contactFax, websiteUrl, imageUrl, 
					businessCategory, dateUpdated, dateAdded, customStyleName);
		}
	}
	
}
