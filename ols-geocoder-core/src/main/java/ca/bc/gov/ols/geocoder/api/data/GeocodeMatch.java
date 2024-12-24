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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.api.data.MatchFault.MatchElement;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocalityType;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;

@XmlRootElement
@XmlSeeAlso({AddressMatch.class, IntersectionMatch.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class GeocodeMatch implements ModifiableLocation {
	
	public static final Comparator<GeocodeMatch> SCORE_COMPARATOR = new Comparator<GeocodeMatch>() {
		@Override
		public int compare(GeocodeMatch m1, GeocodeMatch m2) {
			int diff = m2.score - m1.score;
			if(diff == 0) {
				diff = m2.precisionPoints - m1.precisionPoints;
			}
			if(diff == 0) {
				// note that this is reverse order because lower ordinal is better
				diff = m1.precision.ordinal() - m2.precision.ordinal();
			}
			if(diff == 0) {
				// note that this is reverse order because fewer faults is better
				diff = m1.faults.size() - m2.faults.size();
			}
			return diff;
		}
	};
	
	public static final Comparator<GeocodeMatch> ADDRESS_LOCATION_COMPARATOR = new Comparator<GeocodeMatch>() {
		/**
		 * Compares by point location if both matches have a point location (site matches don't load
		 * their point location until the last moment), otherwise falls back to comparing the full
		 * Address Strings. This comparator is used for identifying duplicates, the relative
		 * placement of non-dupes isn't important but is consistent.
		 * 
		 * @param m1 the first match to compare
		 * @param m2 the second match to compare
		 * @return 0 if m1 and m2 both represent the same match, < 0 if m1 is theoretically "lesser"
		 *         than m2, or > 0 if m1 is theoretically "greater" than m2
		 */
		@Override
		public int compare(GeocodeMatch m1, GeocodeMatch m2) {
			Point m1Loc = m1.getLocation();
			Point m2Loc = m2.getLocation();
			
			// if only one of the locations is null, it always comes first
			if(m1Loc == null && m2Loc != null) {
				return -1;
			}
			if(m1Loc != null && m2Loc == null) {
				return 1;
			}
			if(m1 != null && m2 != null) {
				int locComp = m1Loc.compareTo(m2Loc);
				if(locComp != 0) {
					return locComp;
				}
			}
			return m1.getAddressString().compareTo(m2.getAddressString());
		}
	};
	
	public static final Comparator<GeocodeMatch> ADDRESS_STRING_COMPARATOR = Comparator
	        .comparing(GeocodeMatch::getAddressString, Comparator.nullsFirst(String::compareToIgnoreCase))
			.thenComparingInt(match -> match.getAddressString().length());
    		

	
	@XmlElement(nillable = true)
	protected int score;
	
	@XmlElement(nillable = true)
	protected String yourId;
	
	@XmlElementWrapper
	@XmlElement(name = "fault")
	private List<MatchFault> faults = new ArrayList<MatchFault>();
	
	@XmlElement(nillable = true)
	protected MatchPrecision precision;
	
	@XmlElement(nillable = true)
	protected int precisionPoints;
	
	/**
	 * Required by JAXB
	 */
	public GeocodeMatch() {
	}
	
	public GeocodeMatch(MatchPrecision precision, int precisionPoints) {
		this.precision = precision;
		this.precisionPoints = precisionPoints;
		this.score = precisionPoints;
	}
	
	/**
	 * Copy constructor, used for copying exactMatch instances before returning them. 
	 * Meant to be used by children to implement copy()
	 * @param toCopy
	 */
	public GeocodeMatch(GeocodeMatch toCopy) {
		this.yourId = toCopy.yourId;
		this.score = toCopy.score;
		this.precision = toCopy.precision;
		this.precisionPoints = toCopy.precisionPoints;
		this.score = toCopy.precisionPoints;
	}

	/**
	 * Effectively a deep-enough clone method, used for copying exactMatch instances before returning them.
	 * ExactMatchLookup needs to clone the matches it returns because the output code reprojects the location.
	 */
	public abstract GeocodeMatch copy();

	@Override
	public int hashCode() {
		return Objects.hashCode(getLocation());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GeocodeMatch)) return false;
		GeocodeMatch m = (GeocodeMatch)o;
		return m.getAddressString().equals(getAddressString());
	}
	
	@Override
	public abstract Point getLocation();
	
	@Override
	public abstract void setLocation(Point location);
	
	public abstract String getAddressString();
	
	public abstract GeocoderAddress getAddress();
	
	public String getLocalityName() {
		return getAddress().getLocalityName();
	}
	
	public LocalityType getLocalityType() {
		return getAddress().getLocalityType();
	}
	
	public String getStateProvTerr() {
		return getAddress().getStateProvTerr();
	}
	
	public PositionalAccuracy getLocationPositionalAccuracy() {
		return getAddress().getLocationPositionalAccuracy();
	}
	
	public LocationDescriptor getLocationDescriptor() {
		return getAddress().getLocationDescriptor();
	}
	
	public void addFault(MatchFault fault) {
		if(fault == null) {
			return;
		}
		score -= fault.getPenalty();
		faults.add(fault);
	}
	
	public void addFaults(List<MatchFault> faults) {
		for(MatchFault f : faults) {
			addFault(f);
		}
	}
	
	public boolean containsFault(MatchFault.MatchElement element, String fault) {
		for(MatchFault f : faults) {
			if(f.getElement().equals(element) && f.getFault().equals(fault)) {
				return true;
			}
		}
		return false;
	}
	
	public int getScore() {
		return score;
	}
	
	public String getYourId() {
		return yourId;
	}
	
	public void setYourId(String yourId) {
		this.yourId = yourId;
	}
	
	public List<MatchFault> getFaults() {
		return faults;
	}
	
	public MatchPrecision getPrecision() {
		return precision;
	}
	
	public int getPrecisionPoints() {
		return precisionPoints;
	}
	
	public boolean isNearPerfect() {
		if(faults.size() == 0
				|| (faults.size() == 1
						&& faults.get(0).getElement() == MatchElement.PROVINCE
						&& faults.get(0).getFault().equals("missing"))) {
			return true;
		}
		return false;
	}
	
	public abstract void resolve(GeocoderDataStore ds);

}
