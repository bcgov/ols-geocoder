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
package ca.bc.gov.ols.geocoder.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("Prod")
public class AbstractSiteTest {

	static Stream<AbstractSite> sites() {
		return Stream.of(
				new Site(1, UUID.randomUUID(), null, null, null, null, null, null,
						null, null, null, null, null, null),
				new CompactSite(2, UUID.randomUUID(), null, null, null, null, null, null));
	}

	@ParameterizedTest
	@MethodSource("sites")
	void childlessSiteReturnsImmutableEmptyListAfterTrimming(AbstractSite site) {
		assertTrue(site.getChildren().isEmpty());
		site.trimToSize();
		List<ISite> children = site.getChildren();
		assertTrue(children.isEmpty());
		assertThrows(UnsupportedOperationException.class, () -> children.add(site));
	}

	@ParameterizedTest
	@MethodSource("sites")
	void siteRetainsImmutableChildrenAfterTrimming(AbstractSite site) {
		ISite child = new CompactSite(3, UUID.randomUUID(), null, site, null, null, null, null);
		site.addChild(child);
		site.trimToSize();
		List<ISite> children = site.getChildren();
		assertEquals(List.of(child), children);
		assertThrows(UnsupportedOperationException.class, () -> children.add(site));
	}
}