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
package ca.bc.gov.ols.geocoder.data.indexing;

public enum WordClass {
	UNIT_DESIGNATOR,
	UNIT_NUMBER_WORD,
	NAME,
	OCCUPANT_SEPARATOR,
	FRONT_GATE,
	STREET_TYPE,
	STREET_NAME_BODY,
	STREET_DIRECTIONAL,
	STREET_QUALIFIER,
	LOCALITY_NAME,
	AND,
	FLOOR,
	NUMBER,
	NUMBER_WITH_SUFFIX,
	ORDINAL,
	STATE_PROV_TERR,
	SUFFIX,
	POSTAL_ADDRESS_ELEMENT,
	UNRECOGNIZED
}