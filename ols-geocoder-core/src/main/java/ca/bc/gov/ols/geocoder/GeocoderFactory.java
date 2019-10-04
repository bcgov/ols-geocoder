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
package ca.bc.gov.ols.geocoder;

import java.util.Optional;
import java.util.Properties;

import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.api.GeometryReprojector;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

public class GeocoderFactory {
	final static Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ GeocoderFactory.class.getCanonicalName());
	
	private boolean dummyMode = false;
	
	private Properties bootstrapConfig = getBootstrapConfigFromEnvironment();
	private GeometryFactory geometryFactory;
	private GeometryReprojector geometryReprojector;
	
	public GeocoderFactory() {
	}
	
	public void setFeatures(String features) {
		bootstrapConfig.setProperty("features",features);
	}

	public void setCassandraContactPoint(String contactPoint) {
		bootstrapConfig.setProperty("OLS_CASSANDRA_CONTACT_POINT", contactPoint);
	}

	public void setCassandraLocalDatacenter(String datacenter) {
		bootstrapConfig.setProperty("OLS_CASSANDRA_LOCAL_DATACENTER", datacenter);
	}

	public void setCassandraKeyspace(String keyspace) {
		bootstrapConfig.setProperty("OLS_CASSANDRA_KEYSPACE", keyspace);
	}

	public void setCassandraReplicationFactor(String replFactor) {
		bootstrapConfig.setProperty("OLS_CASSANDRA_REPL_FACTOR", replFactor);
	}

	public void setConfigurationStore(String configStore) {
		bootstrapConfig.setProperty("OLS_GEOCODER_CONFIGURATION_STORE", configStore);
	}

	public void setFileConfigurationUrl(String fileConfigUrl) {
		bootstrapConfig.setProperty("OLS_FILE_CONFIGURATION_URL", fileConfigUrl);
	}

	public void setGeometryFactory(GeometryFactory gf) {
		geometryFactory = gf;
	}

	public void setGeometryReprojector(GeometryReprojector gr) {
		geometryReprojector = gr;
	}

	public void setDummyMode(String dummyMode) {
		if("TRUE".equalsIgnoreCase(dummyMode)) {
			this.dummyMode = true;
		}
	}
	
	public IGeocoder getGeocoder() {
		if(dummyMode) {
			logger.info("GeocoderFactory: create Dummy Geocoder.");
			return new DummyGeocoder(geometryFactory);
		}
		logger.info("GeocoderFactory: Creating new geocoder instance");
		return new Geocoder(new GeocoderDataStore(bootstrapConfig, geometryFactory, geometryReprojector));
	}
	
	public static Properties getBootstrapConfigFromEnvironment() {
		Properties bootstrapConfig = new Properties();
		Optional.ofNullable(System.getenv("OLS_FILE_CONFIGURATION_URL")).ifPresent(e -> bootstrapConfig.setProperty("OLS_FILE_CONFIGURATION_URL", e));
		bootstrapConfig.setProperty("OLS_CASSANDRA_CONTACT_POINT", Optional.ofNullable(System.getenv("OLS_CASSANDRA_CONTACT_POINT")).orElse("cassandra"));
		bootstrapConfig.setProperty("OLS_CASSANDRA_LOCAL_DATACENTER", Optional.ofNullable(System.getenv("OLS_CASSANDRA_LOCAL_DATACENTER")).orElse("datacenter1"));
		bootstrapConfig.setProperty("OLS_CASSANDRA_KEYSPACE", Optional.ofNullable(System.getenv("OLS_CASSANDRA_KEYSPACE")).orElse("bgeo"));
		bootstrapConfig.setProperty("OLS_CASSANDRA_REPL_FACTOR", Optional.ofNullable(System.getenv("OLS_CASSANDRA_REPL_FACTOR")).orElse("2"));
		bootstrapConfig.setProperty("OLS_GEOCODER_CONFIGURATION_STORE", Optional.ofNullable(System.getenv("OLS_GEOCODER_CONFIGURATION_STORE"))
				.orElse("ca.bc.gov.ols.geocoder.config.CassandraGeocoderConfigurationStore"));
		return bootstrapConfig;
	}
	
}
