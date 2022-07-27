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
package ca.bc.gov.ols.geocoder.test;

import java.util.Properties;

import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.junitFlags.DevTest;
import org.junit.experimental.categories.Category;

@Category(DevTest.class)
public abstract class TestCase extends junit.framework.TestCase {
	
	protected static IGeocoder gc;
	
	protected static IGeocoder getTestGeocoder() {
		Properties props = new Properties();
		props.setProperty("dataSource.class", "ca.bc.gov.ols.datasources.TestDataSource");
		props.setProperty("baseSrsCode", "3005");
		props.setProperty("baseSrsBounds", "200000,300000,1900000,1800000");
		return new GeocoderFactory().getGeocoder();
	}
	
	public static void assertGreater(double expected, double value) {
		if(!(value > expected)) {
			fail("Expected value to be greater than: " + expected + " but was: " + value);
		}
	}
	
	public static void assertGE(double expected, double value) {
		if(!(value >= expected)) {
			fail("Expected value to be greater than or equal to: " + expected + " but was: "
					+ value);
		}
	}
	
	public static void assertLess(double expected, double value) {
		if(!(value < expected)) {
			fail("Expected value to be less than: " + expected + " but was: " + value);
		}
	}
	
	public static void assertLE(double expected, double value) {
		if(!(value <= expected)) {
			fail("Expected value to be less than or equal to: " + expected + " but was: " + value);
		}
	}
	
}
