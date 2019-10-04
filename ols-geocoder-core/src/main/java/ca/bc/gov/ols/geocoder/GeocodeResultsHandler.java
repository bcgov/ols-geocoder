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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.AddressComponentMisspellings;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.IntersectionMatch;
import ca.bc.gov.ols.geocoder.api.data.MatchFault;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.data.StreetIntersection;
import ca.bc.gov.ols.geocoder.data.StreetName;
import ca.bc.gov.ols.geocoder.data.StreetNameBody;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.parser.ParseDerivation;
import ca.bc.gov.ols.geocoder.parser.ParseDerivationHandler;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;

public class GeocodeResultsHandler implements ParseDerivationHandler {
	private Pattern numberWithAffixPattern = Pattern
			.compile("(.*\\d+)([a-zA-Z])");
	
	private GeocodeQuery query;
	private Geocoder geocoder;
	private GeocoderDataStore datastore;
	private List<GeocodeMatch> matches;
	private int derivationCount = 0;
	
	public GeocodeResultsHandler(GeocodeQuery query, Geocoder geocoder) {
		this.query = query;
		this.geocoder = geocoder;
		this.datastore = geocoder.getDatastore();
		matches = new ArrayList<GeocodeMatch>();
	}
	
	@Override
	public boolean handleDerivation(ParseDerivation pd) {
		derivationCount++;
		// if the locality is garbage, skip this derivation
		// String locality = pd.getPart("locality");
		// if(locality != null && !locality.isEmpty() &&
		// datastore.getLocalities(locality).size() == 0) {
		// return true;
		// }
		int numIntersectionSeparators = pd.getPartCount("intersectionSeparator");
		if(numIntersectionSeparators > 0) {
			// intersection query
			StreetIntersectionAddress intAddr = new StreetIntersectionAddress();
			// match each streetname part
			StreetName[] streetNames = new StreetName[numIntersectionSeparators + 1];
			AddressComponentMisspellings misspellings = new AddressComponentMisspellings(
					numIntersectionSeparators + 1);
			StringBuilder intersectionNameBuilder = new StringBuilder();
			for(int i = 0; i <= numIntersectionSeparators; i++) {
				boolean streetTypePrefix = true;
				String streetType = pd.getPartBySeparator("streetPreType", i, "intersectionSeparator");
				int streetTypeMS = pd.getErrorBySeparator("streetPreType", i, "intersectionSeparator");
				if(streetType == null) {
					streetTypePrefix = false;
					streetType = pd.getPartBySeparator("streetPostType", i, "intersectionSeparator");				
					streetTypeMS = pd.getErrorBySeparator("streetPostType", i, "intersectionSeparator");
				}

				boolean streetDirectionPrefix = true;
				String streetDirection = pd.getPartBySeparator("streetPreDirection", i, "intersectionSeparator");
				int streetDirectionMS = pd.getErrorBySeparator("streetPreDirection", i, "intersectionSeparator");
				if(streetDirection == null) {
					streetDirectionPrefix = false;
					streetDirection = pd.getPartBySeparator("streetPostDirection", i, "intersectionSeparator");				
					streetDirectionMS = pd.getErrorBySeparator("streetPostDirection", i, "intersectionSeparator");
				}

				streetNames[i] = new StreetName(
						pd.getPartBySeparator("streetName", i, "intersectionSeparator"),
						streetType, streetDirection,
						pd.getPartBySeparator("streetQualifier", i, "intersectionSeparator"),
						streetTypePrefix, streetDirectionPrefix, null);
				misspellings.setStreetNameMS(i,
						pd.getErrorBySeparator("streetName", i, "intersectionSeparator"));
				misspellings.setStreetTypeMS(i, streetTypeMS);
				misspellings.setStreetDirectionMS(i,streetDirectionMS);
				misspellings.setStreetQualifierMS(i,
						pd.getErrorBySeparator("streetQualifier", i, "intersectionSeparator"));
				if(i > 0) {
					intersectionNameBuilder.append(" and ");
				}
				intersectionNameBuilder.append(streetNames[i].toString());
			}
			intAddr.setName(intersectionNameBuilder.toString());
			intAddr.setLocalityName(pd.getPart("locality"));
			misspellings.setLocalityMS(pd.getError("locality"));
			intAddr.setStateProvTerr(pd.getPart("stateProvTerr"));
			misspellings.setStateProvTerrMS(pd.getError("stateProvTerr"));
			String streetName1 = streetNames[0].getBody();
			Collection<MisspellingOf<StreetNameBody>> nameBodyMatches = datastore
					.getStreetNameBodies(streetName1);
			// System.out.println("numMatches:" + nameBodyMatches.size());
			for(MisspellingOf<StreetNameBody> nameBodyMatch : nameBodyMatches) {
				String streetName2 = streetNames[1].getBody();
				Set<StreetIntersection> intersections = nameBodyMatch.get().getIntersections(
						streetName2);
				// System.out.println(nameBody.getStreetNames() + " " + streetName2 + " "
				// + intersections.size());
				for(StreetIntersection intersection : intersections) {
					StreetIntersectionAddress address = new StreetIntersectionAddress(
							intersection);
					IntersectionMatch match = new IntersectionMatch(
							address, MatchPrecision.INTERSECTION,
							datastore.getConfig().getMatchPrecisionPoints(
									MatchPrecision.INTERSECTION));
					if(pd.getNonWords().size() > 0) {
						match.addFault(datastore.getConfig().getUnrecognizedMatchFault(pd.getNonWords()));
					}
					if(nameBodyMatch.getError() > 0) {
						match.addFault(datastore.getConfig().getMatchFault("STREET_NAME.partialMatch"));
					}
					geocoder.scoreIntersectionMatch(intAddr, streetNames, intersection, match, misspellings);
					if(query.pass(match)) {
						matches.add(match);
					}
				}
			}
			
		} else {
			// non-intersection query
			List<MatchFault> faults = new ArrayList<MatchFault>();
			OccupantAddress addr = new OccupantAddress();
			AddressComponentMisspellings misspellings = new AddressComponentMisspellings();
			
			String unitNumber = pd.getPart("unitNumber");
			String unitNumberSuffix = pd.getPart("unitNumberSuffix");
			if(unitNumber != null && !unitNumber.isEmpty()) {
				if((unitNumberSuffix != null && !unitNumberSuffix.isEmpty())) {
					addr.setUnitNumber(unitNumber);
					misspellings.setUnitNumberMS(pd.getError("unitNumber"));
					addr.setUnitNumberSuffix(pd.getPart("unitNumberSuffix"));
				} else {
					// try to extract a suffix from the number
					Matcher m = numberWithAffixPattern.matcher(unitNumber);
					if(m.matches()) {
						addr.setUnitNumber(m.group(1));
						addr.setUnitNumberSuffix(m.group(2));
					} else {
						addr.setUnitNumber(unitNumber);
					}
				}
			}
			String unitDesignator = pd.getPart("unitDesignator");
			String floor = pd.getPart("floor");
			if(unitDesignator != null) {
				addr.setUnitDesignator(pd.getPart("unitDesignator"));
				misspellings.setUnitDesignatorMS(pd.getError("unitDesignator"));
			} else if(floor != null) {
				addr.setUnitDesignator(pd.getPart("floor"));
				misspellings.setUnitDesignatorMS(pd.getError("floor"));
			}

			addr.setOccupantName(pd.getPart("occupantName"));
			addr.setSiteName(pd.getPart("siteName"));
			misspellings.setSiteNameMS(pd.getError("siteName"));
			
			String part = pd.getPart("civicNumberWithAttachedSuffix");
			if(part != null && !part.isEmpty()) {
				Matcher m = numberWithAffixPattern.matcher(part);
				if(m.matches()) {
					addr.setCivicNumber(GeocoderUtil.parseCivicNumber(m.group(1)));
					addr.setCivicNumberSuffix(m.group(2));
				}
			} else if(pd.getPart("civicNumber") != null) {
				addr.setCivicNumber(GeocoderUtil.parseCivicNumber(pd.getPart("civicNumber")));
				addr.setCivicNumberSuffix(pd.getPart("civicNumberSuffix"));
			}
			addr.setStreetName(pd.getPart("streetName"));
			misspellings.setStreetNameMS(pd.getError("streetName"));
			
			addr.setStreetTypePrefix(true);
			String streetType = pd.getPart("streetPreType");
			misspellings.setStreetTypeMS(pd.getError("streetPreType"));
			if(streetType == null) {
				addr.setStreetTypePrefix(false);
				streetType = pd.getPart("streetPostType");				
				misspellings.setStreetTypeMS(pd.getError("streetPostType"));
			}
			addr.setStreetType(streetType);

			addr.setStreetDirectionPrefix(true);
			String streetDirection = pd.getPart("streetPreDirection");
			misspellings.setStreetDirectionMS(pd.getError("streetPreDirection"));
			if(streetDirection == null) {
				addr.setStreetDirectionPrefix(false);
				streetDirection = pd.getPart("streetPostDirection");				
				misspellings.setStreetDirectionMS(pd.getError("streetPostDirection"));
			}
			addr.setStreetDirection(streetDirection);

			addr.setStreetQualifier(pd.getPart("streetQualifier"));
			misspellings.setStreetQualifierMS(pd.getError("streetQualifier"));
			addr.setLocalityName(pd.getPart("locality"));
			misspellings.setLocalityMS(pd.getError("locality"));
			addr.setStateProvTerr(pd.getPart("stateProvTerr"));
			misspellings.setStateProvTerrMS(pd.getError("stateProvTerr"));
			if(pd.getPart("postalJunk") != null) {
				faults.add(datastore.getConfig().getMatchFault(
						"POSTAL_ADDRESS_ELEMENT.notAllowed"));
			}
			if(pd.getNonWords().size() > 0) {
				faults.add(datastore.getConfig().getUnrecognizedMatchFault(pd.getNonWords()));
			}
			List<GeocodeMatch> results = geocoder.geocodeInternal(addr, query, faults, misspellings);
			for(GeocodeMatch match : results) {
				if(geocoder.canShortCircuit(query, match)) {
					// lets short-circuit outta here!
					matches.clear();
					matches.add(match);
					return false;
				}
			}
			matches.addAll(results);
		}
		return true;
	}
	
	public int getDerivationCount() {
		return derivationCount;
	}
	
	public List<GeocodeMatch> getMatches() {
		return matches;
	}
}
