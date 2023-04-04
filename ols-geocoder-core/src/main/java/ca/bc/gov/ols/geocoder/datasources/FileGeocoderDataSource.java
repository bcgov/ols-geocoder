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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.AbbreviationMapping;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.config.LocalityMapping;
import ca.bc.gov.ols.geocoder.config.UnitDesignator;
import ca.bc.gov.ols.rowreader.CollectionRowReader;
import ca.bc.gov.ols.rowreader.CsvRowReader;
import ca.bc.gov.ols.rowreader.DateType;
import ca.bc.gov.ols.rowreader.FlexObjListRowReader;
import ca.bc.gov.ols.rowreader.JsonRowReader;
import ca.bc.gov.ols.rowreader.RowReader;
import ca.bc.gov.ols.rowreader.TsvRowReader;
import gnu.trove.set.hash.THashSet;

public class FileGeocoderDataSource implements GeocoderDataSource {
	private final static Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX + 
			FileGeocoderDataSource.class.getCanonicalName());
	
	//private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	private GeocoderConfig config;
	private GeometryFactory geometryFactory;

	private Set<String> streetTypes;
	private Set<String> streetDirs;
	private Set<String> streetQuals;

	private Map<String,Map<String,String>> allDates = new HashMap<String,Map<String,String>>();

	public FileGeocoderDataSource(GeocoderConfig config, GeometryFactory geometryFactory) {
		logger.info(getClass().getName() + "() constructor called");
		this.config = config;
		this.geometryFactory = geometryFactory;
	}	
	
	@Override
	public RowReader getIntersections() {
		return getJsonRowReader("street_intersections");
	}
	
	@Override
	public RowReader getStateProvTerrs() {
		return getJsonRowReader("state_prov_terrs");
	}
	
	@Override
	public RowReader getStreetLocalityCentroids() {
		return getJsonRowReader("street_locality_centroids");
	}
	
	@Override
	public RowReader getLocalities() {
		return getJsonRowReader("localities");
	}

	@Override
	public RowReader getElectoralAreas() {
		return getJsonRowReader("electoral_areas");
	}

	@Override
	public Stream<LocalityMapping> getLocalityMappings() {
		return Stream.concat(config.getLocalityMappings(), getJsonRowReader("locality_mappings").asStream(LocalityMapping::new));
	}
	
	@Override
	public RowReader getStreetSegments() {
		return getJsonRowReader("street_segments");
	}

	@Override
	public RowReader getStreetSegmentsPost() {
		InputStream is = null;
		String name = "street_segments_geocoder";
		try {
			is = getInputStream("street_load_street_segments_geocoder.json");
		} catch(IOException ioe) {
			// fall back to the non-post if _geocoder doesn't exist yet
			try {
				is = getInputStream("street_load_street_segments.json");
				name = "street_segments";
			} catch(IOException ioe2) {
				logger.error("Error opening stream for file street_load_street_segments(_geocoder).json", ioe2);
				throw new RuntimeException(ioe2);
			}
		}
		return getJsonRowReader(name, is);
	}

	@Override
	public RowReader getStreetNames() {
		return getJsonRowReader("street_names");
	}
	
	@Override
	public RowReader getStreetNameOnSegments() {
		return getJsonRowReader("street_name_on_seg_xref");
	}
	
	@Override
	public Stream<AbbreviationMapping> getAbbreviationMappings() {
		return config.getAbbreviationMappings();
	}

	@Override
	public RowReader getStreetTypes() {
		if(streetTypes == null) {
			preReadStreetNames();
		}
		return new CollectionRowReader<String>(streetTypes, geometryFactory);
	}
	
	@Override
	public RowReader getStreetDirs() {
		if(streetDirs == null) {
			preReadStreetNames();
		}
		return new CollectionRowReader<String>(streetDirs, geometryFactory);
	}
	
	@Override
	public RowReader getStreetQualifiers() {
		if(streetQuals == null) {
			preReadStreetNames();
		}
		return new CollectionRowReader<String>(streetQuals, geometryFactory);
	}
	
	private void preReadStreetNames() {
		streetTypes = new THashSet<String>();
		streetDirs = new THashSet<String>();
		streetQuals = new THashSet<String>();
		RowReader rr = getStreetNames();
		while(rr.next()) {
			rr.getString("");
			String type = rr.getString("street_type");
			if(type != null && !type.isEmpty()) {
				streetTypes.add(type);
			}
			String dir = rr.getString("street_direction");
			if(dir != null && !dir.isEmpty()) {
				streetDirs.add(dir);
			}
			String qual = rr.getString("street_qualifier");
			if(qual != null && !qual.isEmpty()) {
				streetQuals.add(qual);
			}
		}
	}

	@Override
	public Stream<UnitDesignator> getUnitDesignators() {
		return config.getUnitDesignators();
	}
	
	@Override
	public RowReader getCombinedSites() {
		return getXsvRowReader("site_Hybrid");
	}

	@Override
	public RowReader getSid2Pids() {
		return getXsvRowReader("sid2pid");
	}

	@Override
	public RowReader getCombinedSitesPost() {
		return getXsvRowReader("site_Hybrid_geocoder");
	}

	@Override
	public RowReader getBusinessCategories() {
		return getXsvRowReader("gsr_business_categories");
	}

	@Override
	public RowReader getOccupants() {
		return getXsvRowReader("occupants_vw_Load");
	}	


	private RowReader getJsonRowReader(String name) {
		try {
			InputStream is = getInputStream("street_load_" + name + ".json");
			return getJsonRowReader(name, is);
		} catch(IOException ioe) {
			logger.error("Error opening stream for file {}.json", name, ioe);
			return new FlexObjListRowReader(Collections.emptyList());
		}
	}

	private RowReader getJsonRowReader(String name, InputStream is) {
			JsonRowReader jrr = new JsonRowReader(is, geometryFactory);
			allDates.put(name, jrr.getDates());
			return jrr;			
	}
	
	private RowReader getXsvRowReader(String name) {
		try {
			InputStream is = getInputStream(name + ".tsv");
			return new TsvRowReader(new BufferedInputStream(is), geometryFactory);
		} catch(IOException ioe) {
			try {
				logger.info("Unable to open stream for file {}.tsv, falling back to .csv", name);
				InputStream is = getInputStream(name + ".csv");
				return new CsvRowReader(new BufferedInputStream(is), geometryFactory);
			} catch(IOException ioe2) {
				logger.error("Error opening stream for file {}.csv, assuming no data; exception message: {}", name, ioe2.getMessage());
				return new FlexObjListRowReader(Collections.emptyList());
			}
		}
	}
	
	private InputStream getInputStream(String name) throws IOException {
		String fileUrlString = config.getDataSourceBaseFileUrl() + name;
		logger.info("Reading from file: " + fileUrlString);
		if(fileUrlString.startsWith("file:")) {
			return new FileInputStream(new File(fileUrlString.substring(5)));
		}
		URL fileUrl = new URL(fileUrlString);
		return fileUrl.openStream();		
	}
	
	@Override
	public GeocoderConfig getConfig() {
		return config;
	}
	
	@Override
	public void close() {
		logger.debug("FileGeocoderDataSource.close() called");
	}

	@Override
	public Map<DateType, ZonedDateTime> getDates() {
		Map<DateType, ZonedDateTime> dates = new EnumMap<DateType, ZonedDateTime>(DateType.class);
		boolean ok = true;
		for(Entry<String, Map<String, String>> dateSet : allDates.entrySet()) {
			String file = dateSet.getKey();
			Set<Entry<String, String>> dateEntries = dateSet.getValue().entrySet();
			if(dateEntries.isEmpty()) {
				ok = false;
				logger.error("No dates from file '" + file + "'.");
			}
			for(Entry<String, String> dateEntry : dateEntries) {
				String source = dateEntry.getKey();
				DateType sourceType = null;
				String dateStr = dateEntry.getValue();
				ZonedDateTime date = null;
				try {
					date = ZonedDateTime.parse(dateStr);
				} catch(DateTimeParseException pe) {
					logger.error("Invalid Date '" + dateStr + "' for source '" + source +"' from file '" + file + "'.");
				}
				try {
					sourceType = DateType.valueOf(source);
				} catch(IllegalArgumentException iae) {
					logger.error("Unexpected Date source '" + source +"' from file '" + file + "'.");
				}
				if(dates.get(sourceType) == null) {
					dates.put(sourceType, date);
				} else if(!dates.get(sourceType).equals(date)) {
					ok = false;
					logger.error("Date from file '" + file + "' for source '" + source + "' is not consistent with other files' dates for the same source.");
				}
			}
		}
		if(ok) {
			return dates;
		}
		return null;
	}

}
