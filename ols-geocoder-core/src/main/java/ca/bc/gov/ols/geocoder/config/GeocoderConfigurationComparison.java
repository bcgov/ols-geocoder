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

import java.util.List;
import java.util.stream.Collectors;

import ca.bc.gov.ols.config.ConfigDifference;
import ca.bc.gov.ols.config.ConfigurationComparison;

public class GeocoderConfigurationComparison extends ConfigurationComparison {
	
	private int curAbbrevMappingCount = 0;
	private int otherAbbrevMappingCount = 0;
	private List<ConfigDifference<AbbreviationMapping>> abbrevMappingDiffs;

	private int curUnitDesignatorCount = 0;
	private int otherUnitDesignatorCount = 0;
	private List<ConfigDifference<UnitDesignator>> unitDesignatorDiffs;

	private int curLocalityMappingCount = 0;
	private int otherLocalityMappingCount = 0;
	private List<ConfigDifference<LocalityMapping>> localityMappingDiffs;
	
	public GeocoderConfigurationComparison(GeocoderConfigurationStore curConfig, GeocoderConfigurationStore otherConfig) {
		super(curConfig, otherConfig);
		
		List<AbbreviationMapping> curAbbrevMappings = curConfig.getAbbrevMappings().collect(Collectors.toList());
		curAbbrevMappingCount = curAbbrevMappings.size();
		List<AbbreviationMapping> otherAbbrevMappings = otherConfig.getAbbrevMappings().collect(Collectors.toList());
		otherAbbrevMappingCount = otherAbbrevMappings.size();
		abbrevMappingDiffs = diffLists(curAbbrevMappings, otherAbbrevMappings);
		
		List<UnitDesignator> curUnitDesignators = curConfig.getUnitDesignators().collect(Collectors.toList());
		curUnitDesignatorCount = curUnitDesignators.size();
		List<UnitDesignator> otherUnitDesignators = otherConfig.getUnitDesignators().collect(Collectors.toList());
		otherUnitDesignatorCount = otherUnitDesignators.size();
		unitDesignatorDiffs = diffLists(curUnitDesignators, otherUnitDesignators);
		
		List<LocalityMapping> curLocalityMappings = curConfig.getLocalityMappings().collect(Collectors.toList());
		curLocalityMappingCount = curLocalityMappings.size();
		List<LocalityMapping> otherLocalityMappings = otherConfig.getLocalityMappings().collect(Collectors.toList());
		otherLocalityMappingCount = otherLocalityMappings.size();
		localityMappingDiffs = diffLists(curLocalityMappings, otherLocalityMappings);
	}
	
	public int getCurAbbrevMappingCount() {
		return curAbbrevMappingCount;
	}

	public int getOtherAbbrevMappingCount() {
		return otherAbbrevMappingCount;
	}

	public List<ConfigDifference<AbbreviationMapping>> getAbbrevMappingDiffs() {
		return abbrevMappingDiffs;
	}

	public int getCurUnitDesignatorCount() {
		return curUnitDesignatorCount;
	}

	public int getOtherUnitDesignatorCount() {
		return otherUnitDesignatorCount;
	}

	public List<ConfigDifference<UnitDesignator>> getUnitDesignatorDiffs() {
		return unitDesignatorDiffs;
	}

	public int getCurLocalityMappingCount() {
		return curLocalityMappingCount;
	}

	public int getOtherLocalityMappingCount() {
		return otherLocalityMappingCount;
	}

	public List<ConfigDifference<LocalityMapping>> getLocalityMappingDiffs() {
		return localityMappingDiffs;
	}


}
