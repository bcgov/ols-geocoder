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
package ca.bc.gov.ols.geocoder.config;

import java.util.stream.Stream;

import ca.bc.gov.ols.config.ConfigurationStore;

public interface GeocoderConfigurationStore extends ConfigurationStore {
	
	Stream<AbbreviationMapping> getAbbrevMappings();
	void setAbbrevMapping(AbbreviationMapping abbrMap);
	void removeAbbrevMapping(AbbreviationMapping abbrMap);
	
	Stream<UnitDesignator> getUnitDesignators();
	void addUnitDesignator(UnitDesignator ud);
	void removeUnitDesignator(UnitDesignator ud);

	Stream<LocalityMapping> getLocalityMappings();
	void setLocalityMapping(LocalityMapping locMap);
	void removeLocalityMapping(LocalityMapping locMap);
	
}
