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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.config.FileConfigurationStore;
import ca.bc.gov.ols.rowreader.CsvRowReader;
import ca.bc.gov.ols.rowreader.RowReader;
import ca.bc.gov.ols.rowreader.RowWriter;
import ca.bc.gov.ols.rowreader.XsvRowWriter;

public class FileGeocoderConfigurationStore extends FileConfigurationStore implements GeocoderConfigurationStore {
	private final static Logger logger = LoggerFactory.getLogger( 
			FileGeocoderConfigurationStore.class.getCanonicalName());

	protected static final String ABBREVIATION_MAPPINGS_FILENAME = "bgeo_abbreviation_mappings.csv";
	protected static final String UNIT_DESIGNATORS_FILENAME = "bgeo_unit_designators.csv";
	protected static final String LOCALITY_MAPPINGS_FILENAME = "bgeo_locality_mappings.csv";

	protected FileGeocoderConfigurationStore() {
	}

	public FileGeocoderConfigurationStore(Properties bootstrapConfig) {
		super(bootstrapConfig);
	}
	
	protected void validate() {
		super.validate();
		GeocoderConfigurationStore defaults = new DefaultGeocoderConfigurationStore();
		if(!fileExists(ABBREVIATION_MAPPINGS_FILENAME)) {
			//createAbbreviationMappings();
			writeAbbrevMappings(defaults.getAbbrevMappings().collect(Collectors.toList()));			
		} 
		if(!fileExists(UNIT_DESIGNATORS_FILENAME)) {
			//createAbbreviationMappings();
			writeUnitDesignators(defaults.getUnitDesignators().collect(Collectors.toList()));			
		} 
		if(!fileExists(LOCALITY_MAPPINGS_FILENAME)) {
			//createLocalityMappings();
			writeLocalityMappings(defaults.getLocalityMappings().collect(Collectors.toList()));			
		} 
	}

	@Override
	public Stream<AbbreviationMapping> getAbbrevMappings() {
		InputStream in = getInputStream(ABBREVIATION_MAPPINGS_FILENAME);
		RowReader rr = new CsvRowReader(in,null);
		return rr.asStream(AbbreviationMapping::new);
	}

	@Override
	public void setAbbrevMapping(AbbreviationMapping abbrMap) {
		List<AbbreviationMapping> ams = getAbbrevMappings().collect(Collectors.toList());
		if(!ams.contains(abbrMap)) {
			ams.add(abbrMap);
		}
		writeAbbrevMappings(ams);
	}

	@Override
	public void removeAbbrevMapping(AbbreviationMapping abbrMap) {
		List<AbbreviationMapping> ams = getAbbrevMappings().collect(Collectors.toList());
		ams.remove(abbrMap);
		writeAbbrevMappings(ams);
	}

	protected void writeAbbrevMappings(List<AbbreviationMapping> abbrMaps) {
		OutputStream out = getOutputStream(ABBREVIATION_MAPPINGS_FILENAME);
		RowWriter rw = new XsvRowWriter(out, ',', List.of("abbreviated_form", "long_form"),true);
		HashMap<String, String> row = new HashMap<String,String>();
		for(AbbreviationMapping am : abbrMaps) {
			row.put("abbreviated_form", am.getAbbreviatedForm());
			row.put("long_form", am.getLongForm());
			rw.writeRow(row);
		}
		rw.close();
	}
	
	@Override
	public Stream<UnitDesignator> getUnitDesignators() {
		InputStream in = getInputStream(UNIT_DESIGNATORS_FILENAME);
		RowReader rr = new CsvRowReader(in, null);
		return rr.asStream(UnitDesignator::new);
	}

	@Override
	public void addUnitDesignator(UnitDesignator ud) {
		List<UnitDesignator> uds = getUnitDesignators().collect(Collectors.toList());
		if(!uds.contains(ud)) {
			uds.add(ud);
		}
		writeUnitDesignators(uds);
	}

	@Override
	public void removeUnitDesignator(UnitDesignator ud) {
		List<UnitDesignator> uds = getUnitDesignators().collect(Collectors.toList());
		uds.remove(ud);
		writeUnitDesignators(uds);
	}

	protected void writeUnitDesignators(List<UnitDesignator> uds) {
		OutputStream out = getOutputStream(UNIT_DESIGNATORS_FILENAME);
		RowWriter rw = new XsvRowWriter(out, ',', List.of("canonical_form"),true);
		HashMap<String, String> row = new HashMap<String,String>();
		for(UnitDesignator ud : uds) {
			row.put("canonical_form", ud.getCanonicalForm());
			rw.writeRow(row);
		}
		rw.close();
	}

	@Override
	public Stream<LocalityMapping> getLocalityMappings() {
		InputStream in = getInputStream(LOCALITY_MAPPINGS_FILENAME);
		RowReader rr = new CsvRowReader(in, null);
		return rr.asStream(LocalityMapping::new);
	}

	@Override
	public void setLocalityMapping(LocalityMapping locMap) {
		List<LocalityMapping> locMaps = getLocalityMappings().collect(Collectors.toList());
		locMaps.stream().filter(lm -> lm.sameMappingAs(locMap))
			.findFirst().ifPresentOrElse(lm -> lm.setConfidence(locMap.getConfidence()), () -> locMaps.add(locMap));	
		writeLocalityMappings(locMaps);	
	}

	@Override
	public void removeLocalityMapping(LocalityMapping locMap) {
		List<LocalityMapping> locMaps = getLocalityMappings().collect(Collectors.toList());
		locMaps.remove(locMap);
		writeLocalityMappings(locMaps);
	}

	private void writeLocalityMappings(List<LocalityMapping> locMaps) {
		OutputStream out = getOutputStream(LOCALITY_MAPPINGS_FILENAME);
		RowWriter rw = new XsvRowWriter(out, ',', List.of("locality_id", "input_string", "confidence"), true);
		HashMap<String, Object> row = new HashMap<String, Object>();
		for(LocalityMapping lm : locMaps) {
			row.put("locality_id", lm.getLocalityId());
			row.put("input_string", lm.getInputString());
			row.put("confidence", lm.getConfidence());
			rw.writeRow(row);
		}
		rw.close();
	}

	@Override
	public void replaceWith(GeocoderConfigurationStore configStore) {
		super.replaceWith(configStore);
		if(configStore instanceof GeocoderConfigurationStore) {			
			writeAbbrevMappings(((GeocoderConfigurationStore)configStore).getAbbrevMappings().collect(Collectors.toList()));			
			writeUnitDesignators(((GeocoderConfigurationStore)configStore).getUnitDesignators().collect(Collectors.toList()));
			writeLocalityMappings(((GeocoderConfigurationStore)configStore).getLocalityMappings().collect(Collectors.toList()));
		}
	}
	
}
