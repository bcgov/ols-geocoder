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
package ca.bc.gov.ols.geocoder.datasources;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Stream;

import ca.bc.gov.ols.geocoder.config.AbbreviationMapping;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.config.LocalityMapping;
import ca.bc.gov.ols.geocoder.config.UnitDesignator;
import ca.bc.gov.ols.rowreader.DateType;
import ca.bc.gov.ols.rowreader.RowReader;

/**
 * The GeocoderDataSource encapsulates all of the database queries that need to be run. Anything
 * database-specific should be hidden under this interface. The GeocoderDataStore uses the
 * GeocoderDatasource to get information from the database.
 * 
 * @author chodgson
 * 
 */
public interface GeocoderDataSource {
	
	RowReader getIntersections();
	
	RowReader getStateProvTerrs();
	
	RowReader getStreetLocalityCentroids();
	
	RowReader getLocalities();

	RowReader getElectoralAreas();

	
	RowReader getStreetSegments();

	RowReader getStreetSegmentsPost();

	RowReader getStreetNames();
	
	RowReader getStreetNameOnSegments();
	
	RowReader getStreetTypes();
	
	RowReader getStreetDirs();
	
	RowReader getStreetQualifiers();
	
	RowReader getCombinedSites();

	RowReader getSid2Pids();

	RowReader getCombinedSitesPost();
	
	RowReader getBusinessCategories();
	
	RowReader getOccupants();
		
	Map<DateType, ZonedDateTime> getDates();
	
	GeocoderConfig getConfig();
	
	Stream<AbbreviationMapping> getAbbreviationMappings();
	
	Stream<LocalityMapping> getLocalityMappings();

	Stream<UnitDesignator> getUnitDesignators();

	void close();

}
