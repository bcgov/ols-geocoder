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

import java.lang.reflect.Constructor;

import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

public class GeocoderDataSourceFactory {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ GeocoderDataSourceFactory.class.getCanonicalName());
	
	public static GeocoderDataSource getGeocoderDataSource(GeocoderConfig config,
			GeometryFactory geometryFactory) {
		logger.info("GeocoderDataSourceFactory.getGeocoderDataSource() called");
		String dataSourceClassName = config.getDataSourceClassName();
		try {
			if(dataSourceClassName != null) {
				Class<?> cl = Class.forName(dataSourceClassName);
				Constructor<?> con = cl.getConstructor(new Class[] {GeocoderConfig.class,
						GeometryFactory.class});
				return (GeocoderDataSource)con.newInstance(config, geometryFactory);
			}
		} catch(ReflectiveOperationException roe) {
			throw new RuntimeException("Unable to load specified dataSource.class: "
					+ dataSourceClassName, roe);
		}
		throw new RuntimeException("No dataSource class specified in dataSource.class property");
	}
}
