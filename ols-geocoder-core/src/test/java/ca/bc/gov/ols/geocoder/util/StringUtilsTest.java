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
package ca.bc.gov.ols.geocoder.util;

import ca.bc.gov.ols.junitFlags.ProdTest;
import ca.bc.gov.ols.util.StringUtils;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(ProdTest.class)
public class StringUtilsTest extends TestCase {

	public void testOSADistance() {
		assertEquals(0, StringUtils.OSADistance("foo", "foo"));
		assertEquals(1, StringUtils.OSADistance("foo", "fob"));
		assertEquals(1, StringUtils.OSADistance("foo", "foob"));
		assertEquals(1, StringUtils.OSADistance("foo", "fo"));
		assertEquals(1, StringUtils.OSADistance("foo", "ofo"));
		assertEquals(2, StringUtils.OSADistance("foo", "fooba"));
		assertEquals(2, StringUtils.OSADistance("foo", "foba"));
		assertEquals(2, StringUtils.OSADistance("foo", "fba"));
		assertEquals(3, StringUtils.OSADistance("foo", "bar"));
		assertEquals(3, StringUtils.OSADistance("foo", "foobar"));
		assertEquals(3, StringUtils.OSADistance("foo", ""));
		assertEquals(3, StringUtils.OSADistance("", "bar"));
	}
	
}
