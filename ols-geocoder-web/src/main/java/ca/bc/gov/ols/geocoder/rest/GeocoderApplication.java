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
package ca.bc.gov.ols.geocoder.rest;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import ca.bc.gov.ols.geocoder.Geocoder;
import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.GeometryReprojector;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

@SpringBootApplication
@EnableAutoConfiguration(exclude={CassandraAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
public class GeocoderApplication {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ GeocoderApplication.class.getCanonicalName());
	
	private IGeocoder geocoder;
	private GeometryReprojector reprojector = new GeotoolsGeometryReprojector();
	
	public static void main(String[] args) {
		SpringApplication.run(GeocoderApplication.class, args);
	}
	
	public GeocoderApplication() {
		logger.info("GeocoderApplication() constructor called");
		GeocoderFactory geocoderFactory = new GeocoderFactory();
		geocoderFactory.setGeometryReprojector(reprojector);
		geocoder = geocoderFactory.getGeocoder();
	}
	
	@Bean
	public IGeocoder geocoder() {
		return geocoder;
	}
	
	@PreDestroy
	public void preDestroy() {
		if(geocoder instanceof Geocoder) {
			((Geocoder)geocoder).close();
		}
	}
	
}
