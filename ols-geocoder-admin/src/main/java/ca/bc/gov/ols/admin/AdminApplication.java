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
package ca.bc.gov.ols.admin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PreDestroy;

import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStore;
import ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStoreFactory;
import ca.bc.gov.ols.rowreader.JsonRowReader;
import ca.bc.gov.ols.rowreader.RowReader;
import gnu.trove.map.hash.TIntObjectHashMap;

@SpringBootApplication
@EnableAutoConfiguration(exclude={CassandraAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
public class AdminApplication {
	final static Logger logger = LoggerFactory.getLogger(
			AdminApplication.class.getCanonicalName());

	private static AdminApplication singleton;
	
	private GeocoderConfigurationStore configStore;
	private TIntObjectHashMap<String> localityIdMap;
	private int[] sortedLocalityIds;
	private long localityIdMapTimestamp;
 	
	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}
	   
	public AdminApplication() {
		logger.info("AdminApplication() constructor called");
		configStore = GeocoderConfigurationStoreFactory.getConfigurationStore(GeocoderFactory.getBootstrapConfigFromEnvironment());
		singleton = this;
	}

	public static AdminApplication adminApplication() {
		if(singleton == null) {
			singleton = new AdminApplication();
		}
		return singleton;
	}

	public GeocoderConfigurationStore getConfigStore() {
		return configStore;
	}
		
	@PreDestroy
    public void preDestroy() {
        configStore.close();
    }
	
	public synchronized TIntObjectHashMap<String> getLocalityIdMap() {
		// if the localityIdMap has not been loaded, or is more than an hour old
		if(localityIdMap == null || localityIdMapTimestamp < System.currentTimeMillis() - (60*60*1000)) {
			// reload it
			try {
				localityIdMap = new TIntObjectDefaultHashMap();
				List<Locality> localities = new ArrayList<Locality>();
				String baseFileUrl = configStore.getConfigParams().filter(cp -> "dataSource.baseFileUrl".equals(cp.getConfigParamName())).findAny().orElseThrow().getConfigParamValue();
				URL fileUrl = new URL(baseFileUrl + "street_load_localities.json");
				logger.info("Reading from file: " + fileUrl);
				RowReader rr = new JsonRowReader(new InputStreamReader(fileUrl.openStream(),StandardCharsets.UTF_8), new GeometryFactory());
				while(rr.next()) {
					int id = rr.getInt("locality_id");
					// ids >= 10000 are from GNIS, we never want to map to these
					if(id >= 10000) {
						continue;
					}
					String name = rr.getString("locality_name");
					localityIdMap.put(id, name);
					localities.add(new Locality(id, name));
				}
				Collections.sort(localities);
				sortedLocalityIds = new int[localities.size()];
				for(int i=0; i < localities.size(); i++) {
					sortedLocalityIds[i] = localities.get(i).id;
				}
				localityIdMapTimestamp = System.currentTimeMillis();
				rr.close();
			} catch(IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		return localityIdMap;
	}
	
	public synchronized int[] getSortedLocalityIds() {
		return sortedLocalityIds;
	}
}

class Locality implements Comparable<Locality>{
	public final int id;
	public final String name;
	
	Locality(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public int compareTo(Locality l) {
		return name.compareTo(l.name);
	}
	
}
