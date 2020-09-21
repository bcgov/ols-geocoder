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
package ca.bc.gov.ols.geocoder.api.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * MatchFault represents a fault found between the geocode query and a result. Note that MatchFault
 * is intended to be immutable but in order to support JAX-B for xml output, it can't truly be made
 * immutable.
 * 
 * @author chodgson
 * 
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class MatchFault {
	
	public enum MatchElement {
		OCCUPANT_NAME, SITE_NAME, UNIT_NUMBER, UNIT_NUMBER_SUFFIX, UNIT_DESIGNATOR,
		CIVIC_NUMBER, CIVIC_NUMBER_SUFFIX, STREET,
		STREET_NAME, STREET_TYPE, STREET_DIRECTION, STREET_QUALIFIER,
		LOCALITY, PROVINCE, POSTAL_ADDRESS_ELEMENT, UNRECOGNIZED, MAX_RESULTS, ADDRESS
	}
	
	private String text;
	private MatchElement element;
	private String fault;
	private int penalty = 0;
	
	public MatchFault() {
		
	}
	
	public MatchFault(String text, MatchElement element, String fault, int penalty) {
		this.text = text;
		this.element = element;
		this.fault = fault;
		this.penalty = penalty;
	}
	
	public String getText() {
		return text;
	}
	
	public MatchElement getElement() {
		return element;
	}
	
	public String getFault() {
		return fault;
	}
	
	public int getPenalty() {
		return penalty;
	}
	
	@Override
	public String toString() {
		return element + "." + fault + ":" + penalty;
	}
	
}
