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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;

import au.com.bytecode.opencsv.CSVReader;
import ca.bc.gov.ols.config.CassandraConfigurationStore;
import ca.bc.gov.ols.rowreader.DatastaxResultSetRowReader;

public class CassandraGeocoderConfigurationStore extends CassandraConfigurationStore implements GeocoderConfigurationStore {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ CassandraGeocoderConfigurationStore.class.getCanonicalName());

	protected static final String APP_ID = "BGEO";
	
	public CassandraGeocoderConfigurationStore(Properties bootstrapConfig) {
		super(bootstrapConfig);
	}
	
	protected void validateKeyspace() {
		super.validateKeyspace();
		validateAbbreviationMappingsTable();
		validateUnitDesignatorsTable();
		validateLocalityMappingsTable();
	}

	@Override
	public Stream<AbbreviationMapping> getAbbrevMappings() {
		ResultSet rs = session.execute("SELECT abbreviated_form, long_form FROM " 
				+ keyspace + ".BGEO_ABBREVIATION_MAPPINGS ");
		return new DatastaxResultSetRowReader(rs).asStream(AbbreviationMapping::new);
	}

	@Override
	public void setAbbrevMapping(AbbreviationMapping abbrMap) {
		SimpleStatement statement = SimpleStatement.builder("INSERT INTO " + keyspace + ".BGEO_ABBREVIATION_MAPPINGS(abbreviated_form, long_form) VALUES (?, ?)")
				.addPositionalValues(abbrMap.getAbbreviatedForm(), abbrMap.getLongForm()).build();
		session.execute(statement);
	}

	@Override
	public void removeAbbrevMapping(AbbreviationMapping abbrMap) {
		SimpleStatement statement = SimpleStatement.builder("DELETE FROM " + keyspace + ".BGEO_ABBREVIATION_MAPPINGS WHERE abbreviated_form = ? AND long_form = ?") 
				.addPositionalValues(abbrMap.getAbbreviatedForm(), abbrMap.getLongForm()).build();
		session.execute(statement);
	}

	@Override
	public Stream<UnitDesignator> getUnitDesignators() {
		ResultSet rs = session.execute("SELECT canonical_form FROM " + keyspace + ".BGEO_UNIT_DESIGNATORS ");
		return new DatastaxResultSetRowReader(rs).asStream(UnitDesignator::new);
	}

	@Override
	public void addUnitDesignator(UnitDesignator ud) {
		SimpleStatement statement = SimpleStatement.builder("INSERT INTO " + keyspace + ".BGEO_UNIT_DESIGNATORS(canonical_form) VALUES (?)") 
				.addPositionalValues(ud.getCanonicalForm()).build();
		session.execute(statement);
	}

	@Override
	public void removeUnitDesignator(UnitDesignator ud) {
		SimpleStatement statement = SimpleStatement.builder("DELETE FROM " + keyspace + ".BGEO_UNIT_DESIGNATORS WHERE canonical_form = ?") 
				.addPositionalValues(ud.getCanonicalForm()).build();
		session.execute(statement);
	}

	@Override
	public Stream<LocalityMapping> getLocalityMappings() {
		ResultSet rs = session.execute("SELECT locality_id, input_string, confidence FROM " + keyspace + ".BGEO_LOCALITY_MAPPINGS ");
		return new DatastaxResultSetRowReader(rs).asStream(LocalityMapping::new);
	}

	@Override
	public void setLocalityMapping(LocalityMapping locMap) {
		SimpleStatement statement = SimpleStatement.builder("INSERT INTO " + keyspace + ".BGEO_LOCALITY_MAPPINGS(locality_id, input_string, confidence) VALUES (?, ?, ?)") 
				.addPositionalValues(locMap.getLocalityId(), locMap.getInputString(), locMap.getConfidence()).build();
		session.execute(statement);
	}

	@Override
	public void removeLocalityMapping(LocalityMapping locMap) {
		SimpleStatement statement = SimpleStatement.builder("DELETE FROM " + keyspace + ".BGEO_LOCALITY_MAPPINGS WHERE input_string = ? AND locality_id = ?") 
				.addPositionalValues(locMap.getInputString(), locMap.getLocalityId()).build();
		session.execute(statement);
	}

	void createAbbreviationMappingsTable() {
		logger.warn("Creating table " + keyspace + ".BGEO_ABBREVIATION_MAPPINGS");
		session.execute("CREATE TABLE " + keyspace + ".BGEO_ABBREVIATION_MAPPINGS("
				+ "abbreviated_form text, "
				+ "long_form text, "
				+ "PRIMARY KEY(abbreviated_form, long_form));");
		populateAbbreviationMappingsTable();
	}

	private void populateAbbreviationMappingsTable() {
		logger.warn("Populating table " + keyspace + ".BGEO_ABBREVIATION_MAPPINGS");
		InputStream in = CassandraGeocoderConfigurationStore.class.getClassLoader().getResourceAsStream("bgeo_abbreviation_mappings.csv");
		try (CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(in), Charset.forName("UTF-8")))) {
			String[] header = reader.readNext();
			if(header == null) {
				throw new RuntimeException("CSV file empty: bgeo_abbreviation_mappings.csv");
			}
			int abbrIdx = -1;
			int longIdx = -1;
			for(int i = 0; i < header.length; i++) {
				switch(header[i].trim().toLowerCase()) {
				case "abbreviated_form": 
					abbrIdx = i;
					break;
				case "long_form":
					longIdx = i;
					break;
				}
			}
			String [] row;
			PreparedStatement pStatement = session.prepare("INSERT INTO " 
					+ keyspace + ".BGEO_ABBREVIATION_MAPPINGS "
					+ "(ABBREVIATED_FORM, LONG_FORM) " 
					+ "VALUES (?, ?);");
			while((row = reader.readNext()) != null) {
				session.execute(pStatement.bind(row[abbrIdx], row[longIdx]));
			}
			reader.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	void validateAbbreviationMappingsTable() {
		logger.info("Validating table " + keyspace + ".BGEO_ABBREVIATION_MAPPINGS");
		Optional<KeyspaceMetadata> ks = session.getMetadata().getKeyspace(keyspace);
		Optional<TableMetadata> table = ks.get().getTable("BGEO_ABBREVIATION_MAPPINGS");
		if(table.isEmpty()) {
			logger.warn("Table " + keyspace + ".BGEO_ABBREVIATION_MAPPINGS does not exist");
			createAbbreviationMappingsTable();
		}
		// This is where we would check that the table has the right columns
		// and make any changes required by new versions		
	}

	void createUnitDesignatorsTable() {
		logger.warn("Creating table " + keyspace + ".BGEO_UNIT_DESIGNATORS");
		session.execute("CREATE TABLE " + keyspace + ".BGEO_UNIT_DESIGNATORS("
				+ "canonical_form text PRIMARY KEY);");	
		populateUnitDesignatorsTable();
	}

	private void populateUnitDesignatorsTable() {
		logger.warn("Populating table " + keyspace + ".BGEO_UNIT_DESIGNATORS");
		InputStream in = CassandraGeocoderConfigurationStore.class.getClassLoader().getResourceAsStream("bgeo_unit_designators.csv");
		try (CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(in), Charset.forName("UTF-8")))) {
			String[] header = reader.readNext();
			if(header == null) {
				throw new RuntimeException("CSV file empty: bgeo_unit_designators.csv");
			}
			int idx = -1;
			for(int i = 0; i < header.length; i++) {
				if("canonical_form".equals(header[i].trim().toLowerCase())) {
					idx = i;
				}
			}
			String [] row;
			PreparedStatement pStatement = session.prepare("INSERT INTO " 
					+ keyspace + ".BGEO_UNIT_DESIGNATORS "
					+ "(CANONICAL_FORM) " 
					+ "VALUES (?);");
			while((row = reader.readNext()) != null) {
				session.execute(pStatement.bind(row[idx]));
			}
			reader.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	void validateUnitDesignatorsTable() {
		logger.info("Validating table " + keyspace + ".BGEO_UNIT_DESIGNATORS");
		Optional<KeyspaceMetadata> ks = session.getMetadata().getKeyspace(keyspace);
		Optional<TableMetadata> table = ks.get().getTable("BGEO_UNIT_DESIGNATORS");
		if(table.isEmpty()) {
			logger.warn("Table " + keyspace + ".BGEO_UNIT_DESIGNATORS does not exist");
			createUnitDesignatorsTable();
		}
		// This is where we would check that the table has the right columns
		// and make any changes required by new versions
	}

	void createLocalityMappingsTable() {
		logger.warn("Creating table " + keyspace + ".BGEO_LOCALITY_MAPPINGS");
		session.execute("CREATE TABLE " + keyspace + ".BGEO_LOCALITY_MAPPINGS("
				+ "locality_id int, "
				+ "input_string text, "
				+ "confidence int, "
				+ "PRIMARY KEY(locality_id, input_string));");
		populateLocalityMappingsTable();
	}

	private void populateLocalityMappingsTable() {
		logger.warn("Populating table " + keyspace + ".BGEO_LOCALITY_MAPPINGS");
		InputStream in = CassandraGeocoderConfigurationStore.class.getClassLoader().getResourceAsStream("bgeo_locality_mappings.csv");
		try (CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(in)))) {
			String[] header = reader.readNext();
			if(header == null) {
				throw new RuntimeException("CSV file empty: bgeo_locality_mappings.csv");
			}
			int localityIdx = -1;
			int inputIdx = -1;
			int confidenceIdx = -1;
			for(int i = 0; i < header.length; i++) {
				switch(header[i].trim().toLowerCase()) {
				case "locality_id": 
					localityIdx = i;
					break;
				case "input_string":
					inputIdx = i;
					break;
				case "confidence":
					confidenceIdx = i;
					break;
				}
			}
			String [] row;
			PreparedStatement pStatement = session.prepare("INSERT INTO " 
					+ keyspace + ".BGEO_LOCALITY_MAPPINGS "
					+ "(LOCALITY_ID, INPUT_STRING, CONFIDENCE) " 
					+ "VALUES (?, ?, ?);");
			while((row = reader.readNext()) != null) {
				session.execute(pStatement.bind(Integer.parseInt(row[localityIdx]), 
						row[inputIdx], 
						Integer.parseInt(row[confidenceIdx])));
			}
			reader.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	void validateLocalityMappingsTable() {
		logger.info("Validating table " + keyspace + ".BGEO_LOCALITY_MAPPINGS");
		Optional<KeyspaceMetadata> ks = session.getMetadata().getKeyspace(keyspace);
		Optional<TableMetadata> table = ks.get().getTable("BGEO_LOCALITY_MAPPINGS");
		if(table.isEmpty()) {
			logger.warn("Table " + keyspace + ".BGEO_LOCALITY_MAPPINGS does not exist");
			createLocalityMappingsTable();
		}
		// This is where we would check that the table has the right columns
		// and make any changes required by new versions		
	}

	public void replaceWith(CassandraGeocoderConfigurationStore configStore) {
		super.replaceWith(configStore);
		
		// save BGEO_ABBREVIATION_MAPPINGS
		session.execute("TRUNCATE " + keyspace + ".BGEO_ABBREVIATION_MAPPINGS;");
		final PreparedStatement amPStatement = session.prepare("INSERT INTO " 
				+ keyspace + ".BGEO_ABBREVIATION_MAPPINGS "
				+ "(ABBREVIATED_FORM, LONG_FORM) " 
				+ "VALUES (?, ?);");
		configStore.getAbbrevMappings().map(abbrMap ->
				session.executeAsync(amPStatement.bind(abbrMap.getAbbreviatedForm(), 
					abbrMap.getLongForm())).toCompletableFuture())
				.map(CompletableFuture::join);

		// save BGEO_UNIT_DESIGNATORS
		session.execute("TRUNCATE " + keyspace + ".BGEO_UNIT_DESIGNATORS;");
		final PreparedStatement udPStatement = session.prepare("INSERT INTO " 
				+ keyspace + ".BGEO_UNIT_DESIGNATORS "
				+ "(CANONICAL_FORM) " 
				+ "VALUES (?);");
		configStore.getUnitDesignators().map(unit ->
			session.executeAsync(udPStatement.bind(unit.getCanonicalForm())).toCompletableFuture())
			.map(CompletableFuture::join);
		
		// save BGEO_LOCALITY_MAPPINGS
		session.execute("TRUNCATE " + keyspace + ".BGEO_LOCALITY_MAPPINGS;");
		final PreparedStatement lmPStatement = session.prepare("INSERT INTO " 
				+ keyspace + ".BGEO_LOCALITY_MAPPINGS "
				+ "(LOCALITY_ID, INPUT_STRING, CONFIDENCE) " 
				+ "VALUES (?, ?, ?);");
		configStore.getLocalityMappings().map(locMap ->
			session.executeAsync(lmPStatement.bind(locMap.getLocalityId(), locMap.getInputString(), 
					locMap.getConfidence())).toCompletableFuture())
			.map(CompletableFuture::join);
	}
	
}
