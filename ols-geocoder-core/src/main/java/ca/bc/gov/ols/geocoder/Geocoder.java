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

import gnu.trove.set.hash.THashSet;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.AddressComponentMisspellings;
import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.IntersectionMatch;
import ca.bc.gov.ols.geocoder.api.data.MatchFault;
import ca.bc.gov.ols.geocoder.api.data.MatchFault.MatchElement;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.AccessPoint;
import ca.bc.gov.ols.geocoder.data.BlockFace;
import ca.bc.gov.ols.geocoder.data.CivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.ILocation;
import ca.bc.gov.ols.geocoder.data.IOccupant;
import ca.bc.gov.ols.geocoder.data.ISite;
import ca.bc.gov.ols.geocoder.data.Locality;
import ca.bc.gov.ols.geocoder.data.LocalityMapTarget;
import ca.bc.gov.ols.geocoder.data.NonCivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.ResultCivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.StateProvTerr;
import ca.bc.gov.ols.geocoder.data.StreetIntersection;
import ca.bc.gov.ols.geocoder.data.StreetName;
import ca.bc.gov.ols.geocoder.data.StreetSegment;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.Word;
import ca.bc.gov.ols.geocoder.data.indexing.WordClass;
import ca.bc.gov.ols.geocoder.dra.DraLexicalRules;
import ca.bc.gov.ols.geocoder.lexer.Lexer;
import ca.bc.gov.ols.geocoder.parser.AddressParser;
import ca.bc.gov.ols.geocoder.parser.generator.AddressParserGenerator;
import ca.bc.gov.ols.geocoder.parser.generator.RuleChoice;
import ca.bc.gov.ols.geocoder.parser.generator.RuleOperator;
import ca.bc.gov.ols.geocoder.parser.generator.RuleSequence;
import ca.bc.gov.ols.geocoder.parser.generator.RuleTerm;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;
import ca.bc.gov.ols.rowreader.DateType;
import ca.bc.gov.ols.util.PairedList;
import ca.bc.gov.ols.util.PairedListEntry;
import ca.bc.gov.ols.util.StringUtils;

import org.locationtech.jts.geom.Coordinate;

/**
 * The Geocoder takes GeocodeQueries and uses the GeocoderDataStore to return GeocodeResults.
 * 
 * @author chodgson
 * 
 */
public class Geocoder implements IGeocoder {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ Geocoder.class.getCanonicalName());
	
	private GeocoderDataStore datastore;
	private AddressParser parser;
	//private DateFormatter dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private Lexer lexer;
	private SiteAddress fallbackSiteAddress;
	
	public Geocoder(GeocoderDataStore datastore) {
		this.datastore = datastore;
		lexer = new Lexer(new DraLexicalRules(), datastore.getWordMap());
		parser = createParser(lexer);
		fallbackSiteAddress = geocodeFallbackAddress(datastore.getConfig().getFallbackAddress());
	}
	
	private SiteAddress geocodeFallbackAddress(String fallbackAddress) {
		if(fallbackAddress != null && !fallbackAddress.isEmpty()) {
			GeocodeQuery query = new GeocodeQuery(fallbackAddress);
			query.setMaxResults(1);
			SearchResults results = geocode(query);
			if(results.getBestMatch().getAddress() instanceof SiteAddress) {
				return (SiteAddress)results.getBestMatch().getAddress();
			}
		}
		return getNoMatch();
	}
	
	// returns a cloned copy of the fallback address
	// to prevent overwriting the location coordinates when reprojecting for output
	private SiteAddress getFallbackAddress() {
		return new SiteAddress(fallbackSiteAddress);
	}
	
	private SiteAddress getNoMatch() {
		SiteAddress site = new SiteAddress();
		site.setLocalityName("NO MATCH FOUND");
		site.setLocation(datastore.getConfig().getBaseSrsBounds().getCentroid());
		return site;
	}
	
	@Override
	public GeocoderConfig getConfig() {
		return datastore.getConfig();
	}
	
	@PreDestroy
	public void close() {
		if(datastore != null) {
			datastore.close();
		}
		datastore = null;
		lexer = null;
		parser = null;
	}
	
	/**
	 * Primary geocode method for unstructured queries. Uses the parser to build multiple possible
	 * structured queries, and passes these to the internal geocoding function.
	 * 
	 * @param query The input address query and its parameters
	 * @return the results of the geocoding
	 */
	@Override
	public SearchResults geocode(GeocodeQuery query) {
		query.startTimer();
		logger.debug(query.toString());
		List<GeocodeMatch> matches;

		// bracket the number of results: 0 < maxResults <= resultsLimit
		int resultsLimit = getDatastore().getConfig().getResultsLimit();
		if(query.getMaxResults() == null || query.getMaxResults() < 0) {
			query.setMaxResults(1);
		} else if(query.getMaxResults() == 0 || query.getMaxResults() > resultsLimit) {
			query.setMaxResults(resultsLimit);
		}

		if(query.getAddressString() != null && !query.getAddressString().isEmpty()) {
			matches = new ArrayList<GeocodeMatch>();
			if(query.getExactSpelling()) {
				matches = datastore.lookupExact(query);
			} else {
				GeocodeResultsHandler handler = new GeocodeResultsHandler(query, this);
				parser.parse(query.getAddressString(), query.getAutoComplete(), handler);
				logger.debug("Number of derivations: {}", handler.getDerivationCount());
				matches = handler.getMatches();
			}
		} else {
			// go through all of the components and lex them
			// filter the lex results to words of the correct type for the specified field
			// combine the results into one list
			List<List<MisspellingOf<Word>>> addressWords = new ArrayList<List<MisspellingOf<Word>>>();
			
			
			// TODO need to accept invalid unit designators and somehow pass the UNIT_DESIGNATOR.notMatched all the way up
			addressWords.addAll(lexer.lexField(query.getUnitDesignator(),
					EnumSet.of(WordClass.UNIT_DESIGNATOR)));
			addressWords.addAll(lexer.lexField(query.getUnitNumber(),
					EnumSet.of(WordClass.NUMBER, WordClass.LETTER)));
			addressWords.addAll(lexer.lexField(query.getUnitNumberSuffix(),
					EnumSet.of(WordClass.SUFFIX, WordClass.LETTER)));
			List<List<MisspellingOf<Word>>> siteWords = lexer.lexField(query.getSiteName(),
					EnumSet.of(WordClass.NAME));
			if(siteWords.size() > 0) {
				addressWords.addAll(siteWords);
				addressWords.addAll(lexer.lexField("--", EnumSet.of(WordClass.FRONT_GATE)));
			}
			addressWords.addAll(lexer.lexField(
					query.getCivicNumber() == null ? "" : query.getCivicNumber().toString(),
					EnumSet.of(WordClass.NUMBER)));
			addressWords.addAll(lexer.lexField(query.getCivicNumberSuffix(),
					EnumSet.of(WordClass.SUFFIX, WordClass.LETTER)));
			addressWords.addAll(lexer.lexField(query.getStreetName(),
					EnumSet.of(WordClass.STREET_NAME_BODY)));
			addressWords.addAll(lexer.lexField(query.getStreetType(),
					EnumSet.of(WordClass.STREET_TYPE)));
			addressWords.addAll(lexer.lexField(query.getStreetDirection(),
					EnumSet.of(WordClass.STREET_DIRECTIONAL)));
			addressWords.addAll(lexer.lexField(query.getStreetQualifier(),
					EnumSet.of(WordClass.STREET_QUALIFIER)));
			addressWords.addAll(lexer.lexField(query.getLocalityName(),
					EnumSet.of(WordClass.LOCALITY_NAME)));
			addressWords.addAll(lexer.lexField(query.getStateProvTerr(),
					EnumSet.of(WordClass.STATE_PROV_TERR)));
			
			GeocodeResultsHandler handler = new GeocodeResultsHandler(query, this);
			parser.parse(addressWords, new ArrayList<String>(), handler);
			logger.debug("Number of derivations: {}", handler.getDerivationCount());
			matches = handler.getMatches();
		}
		
		logger.debug("Initial matches.size(): {}", matches.size());
		categorizeMatches(matches);
		
		// if we have no matches, add the fallback match
		if(matches.isEmpty()) {
			// if nothing matched we return a low-scoring stateProvTerr level match
			SiteAddress matchAddress = getFallbackAddress();
			if(matchAddress == null) {
				matchAddress = getNoMatch();
			}
			// if we have a spatial filter, try to put the point inside it
			if(query.getBbox() != null) {
				matchAddress.setLocation(query.getBbox().getCentroid());
			} else if(query.getCentre() != null && query.getMaxDistance() > 0) {
				matchAddress.setLocation(query.getCentre());
			} else if(query.getLocalities() != null && query.getLocalities().size() > 0) {
				LocalityMapTarget lm = datastore.getBestLocalityMapping(query.getLocalities().get(0));
				if(lm != null) {
					matchAddress.setLocation(lm.getLocality().getLocation());
				}
			}
			matchAddress.setLocationPositionalAccuracy(PositionalAccuracy.COARSE);
			AddressMatch match = new AddressMatch(
					matchAddress, MatchPrecision.PROVINCE,
					datastore.getConfig().getMatchPrecisionPoints(
							MatchPrecision.PROVINCE));
			// only add the match if it passes the query filters
			if(query.pass(match)) {
				matches.add(match);
			}
		}

		if(query.isFuzzyMatch() && query.getAddressString() != null && !query.getAddressString().isEmpty()) {
			// When fuzzy matching is enabled, we need to sort results more intelligently
			// than just by fuzzy score alone. We prioritize:
			// 1. Exact locality matches (no penalty) over prefix matches (with penalty)
			// 2. LOCALITY precision matches for simple word queries without numbers
			// 3. Fuzzy score within each priority group (case-insensitive)
			final String queryStr = query.getAddressString();
			final String normalizedInput = queryStr.toLowerCase();
			matches.sort(
				Comparator
					// First, prioritize matches without locality partialMatch faults (exact matches)
					.comparing((GeocodeMatch match) -> {
						for(MatchFault fault : match.getFaults()) {
							if(fault.getElement() == MatchFault.MatchElement.LOCALITY 
								&& fault.getFault().equals("partialMatch")
								&& fault.getPenalty() > 0) {
								return 1; // Deprioritize prefix matches
							}
						}
						return 0; // Prioritize exact matches
					})
					// Second, for locality-only queries, prioritize LOCALITY precision matches
					.thenComparing((GeocodeMatch match) -> {
						// Check if this is likely a locality-only query (simple word(s), no numbers)
						boolean likelyLocalityQuery = !queryStr.matches(".*\\d+.*");
						// If it's a locality query and this is a LOCALITY match, boost it
						if (likelyLocalityQuery && match.getPrecision() == MatchPrecision.LOCALITY) {
							return 0; // Higher priority
						}
						return 1; // Lower priority
					})
					// Then sort by fuzzy score (higher is better, case-insensitive)
					.thenComparing((GeocodeMatch match) ->
						FuzzySearch.ratio(normalizedInput, match.getAddressString().toLowerCase()),
						Comparator.reverseOrder()
					)
			);
			// limit to maxResults
			matches = matches.subList(0, Math.min(query.getMaxResults(), matches.size()));
		}


//		logger.debug("matches.size() before duplicate filter: {}", matches.size());
//		
//		// filter out any duplicate results, keep the one with the highest score
//		// sort by AddressString,Location first
//		Collections.sort(matches, GeocodeMatch.ADDRESS_LOCATION_COMPARATOR);
//		
//		List<GeocodeMatch> newMatches = new ArrayList<GeocodeMatch>();
//		for(int i = 0; i < matches.size();) {
//			GeocodeMatch a = matches.get(i);
//			// loop over matches following match[i] aka. "a"
//			int next = 1;
//			while(i + next < matches.size()) {
//				GeocodeMatch b = matches.get(i + next);
//				if(((a.getLocation() == null && b.getLocation() == null)
//						|| (a.getLocation() != null && b.getLocation() != null
//						&& a.getLocation().getX() == b.getLocation().getX()
//						&& a.getLocation().getY() == b.getLocation().getY()))
//						&& a.getAddressString().equals(b.getAddressString())) {
//					if(b.getScore() > a.getScore()) {
//						// if b is better, move a up to b
//						i = i + next;
//						a = b;
//						next = 1;
//					} else {
//						next++;
//					}
//				} else {
//					// we've run out of duplicates for "a"
//					break;
//				}
//			}
//			i = i + next;
//			newMatches.add(a);
//		}
//		matches = newMatches;
//		
//		logger.debug("matches.size() after duplicate filter: {}", matches.size());
		
		/*
		 * // filter by centre/maxDistance spatial filter if(query.getCentre() != null &&
		 * query.getMaxDistance() != 0) { newMatches = new ArrayList<GeocodeMatch>(); for(int i = 0;
		 * i < matches.size(); i++) { GeocodeMatch match = matches.get(i);
		 * if(match.getLocation().distance(query.getCentre()) < query.getMaxDistance()) {
		 * newMatches.add(match); } } matches = newMatches; }
		 * 
		 * // filter by bbox spatial filter if(query.getBbox() != null) { newMatches = new
		 * ArrayList<GeocodeMatch>(); for(int i = 0; i < matches.size(); i++) { GeocodeMatch match =
		 * matches.get(i); if(query.getBbox().contains(match.getLocation())) {
		 * newMatches.add(match); } } matches = newMatches; }
		 * 
		 * logger.debug("matches.size() after spatial filters: {}", matches.size());
		 */
		// sort the list by score
		//Collections.sort(matches, GeocodeMatch.SCORE_COMPARATOR);
		
		// filter the list down to the maxResults limit from the query
		List<GeocodeMatch> limitedMatches = matches;
		if(query.getMaxResults() > 0) {
			if(matches.size() > query.getMaxResults()) {
				if(matches.get(0).getScore() == matches.get(query.getMaxResults()).getScore()) {
					matches.get(0).addFault(
							datastore.getConfig().getMatchFault(null,
									MatchElement.MAX_RESULTS, "too_low_to_include_all_best_matches"));
					if(query.getParcelPoint() != null) {
						// sort all equally-top-scored results by their distance from specified
						// parcelPoint
						int numEqualMatches = query.getMaxResults();
						while(matches.size() > numEqualMatches + 1
								&& matches.get(0).getScore() == matches.get(numEqualMatches + 1)
										.getScore()) {
							numEqualMatches++;
						}
						List<GeocodeMatch> equalMatches = matches.subList(0, numEqualMatches);
						Collections.sort(equalMatches,
								new ILocation.DistanceComparator(query.getParcelPoint()));
					}
				}
				limitedMatches = matches
						.subList(0, Math.min(query.getMaxResults(), matches.size()));
			}
		}
		
		for(GeocodeMatch gm : limitedMatches) {
			gm.setYourId(query.getYourId());
			gm.resolve(datastore);
		}
		query.stopTimer();
		return new SearchResults(query, limitedMatches, datastore.getDate(DateType.PROCESSING_DATE));
	}
	
	private void categorizeMatches(List<GeocodeMatch> matches) {
		if(logger.isDebugEnabled()) {
			EnumMap<MatchPrecision, Integer> counts = new EnumMap<MatchPrecision, Integer>(
					MatchPrecision.class);
			for(GeocodeMatch match : matches) {
				MatchPrecision prec = match.getPrecision();
				Integer count = counts.get(prec);
				if(count == null) {
					count = 0;
				}
				counts.put(prec, count + 1);
			}
			logger.debug("Counts by match precision: {}", counts.toString());
		}
	}
	
	/**
	 * Internal geocode method used by both other structured and unstructured queries.
	 * 
	 * @param query The input address query and its parameters
	 * @return a list of address matches
	 */
	List<GeocodeMatch> geocodeInternal(OccupantAddress address, GeocodeQuery query,
			List<MatchFault> incomingFaults, AddressComponentMisspellings misspellings) {
		List<GeocodeMatch> matches = new ArrayList<GeocodeMatch>();
		// AddressMatch parentMatch = null;
		String[] siteNameWords = GeocoderUtil.wordSplit(
				address.getFullSiteName());
		Set<ISite> possibleSites = datastore.getSitesByName(siteNameWords);
		
		String[] occNameWords = GeocoderUtil.wordSplit(
				address.getOccupantName());
		Set<IOccupant> possibleOccupants = Collections.emptySet();
		if(query.getIncludeOccupants()) {
			possibleOccupants = datastore.getOccupantsByName(occNameWords);
		}

		if((address.getStreetName() != null && !address.getStreetName().isEmpty())
				&& (address.getCivicNumber() != null || 
						(possibleSites.isEmpty() && possibleOccupants.isEmpty()))) {
			// this is a civic address query
			String streetName = address.getStreetName();
			List<MisspellingOf<StreetName>> nameMatches = datastore.getStreetNames(streetName,
					address.getStreetType(), address.getStreetDirection(),
					address.getStreetQualifier());
			for(MisspellingOf<StreetName> nameMatch : nameMatches) {
				StreetName name = nameMatch.get();
				List<BlockFace> faces = null;
				if(address.getCivicNumber() != null) {
					// look for matching blocks on those street names
					faces = name.getBlocks(address.getCivicNumber());
					faces = prioritizeBlocks(faces, address.getLocalityName());
					for(BlockFace face : faces) {
						// look for a matching Access Point on the block, or
						// interpolate the location
						ResultCivicAccessPoint ap = datastore.getInterpolator().interpolate(face, new CivicAccessPoint(
								address.getCivicNumber(),
								address.getCivicNumberSuffix()), query);
						// if we get a null AP back then probably interpolation=none
						// and there were no exact matches
						if(ap == null) {
							continue;
						}
						if(ap.isInterpolated()) {
							// we have an interpolated match
							SiteAddress matchAddress = new SiteAddress();
							MatchPrecision precision = MatchPrecision.BLOCK;
							matchAddress.setStreetName(face.getSegment().getPrimaryStreetName());
							matchAddress.setCivicNumber(address.getCivicNumber());
							matchAddress.setLocality(face.getLocality());
							matchAddress.setElectoralArea(face.getElectoralArea());
							matchAddress.setStreetSegmentID(face.getSegment().getSegmentId());
							matchAddress.setStateProvTerr(face.getLocality().getStateProvTerr()
									.getName());
							
							matchAddress.resolveLocation(datastore, null, query.getParcelPoint(),
									query.getLocationDescriptor(),
									ap, face, query.isExtrapolate(), query.getSetBack());
							
							AddressMatch match = new AddressMatch(matchAddress,
									precision, datastore.getConfig().getMatchPrecisionPoints(
											precision));
							match.setSegment(face.getSegment());
							
							match.addFaults(incomingFaults);
							if(nameMatch.getError() > 0) {
								match.addFault(datastore.getConfig().getMatchFault(nameMatch.getMisspelling(), MatchElement.STREET_NAME, "partialMatch"));
							}
							score(address, match, name, misspellings, query);
							globalScoring(match, misspellings);
							if(query.pass(match)) {
								if(canShortCircuit(query, match)) {
									// lets short-circuit outta here!
									matches.clear();
									matches.add(match);
									return matches;
								}
								matches.add(match);
							}
						} else {
							// we have a site or occupant match
							// if our input address has a sitename part, look it up
							List<ISite> descendantSites = new ArrayList<ISite>();
							descendantSites.add(ap.getSite());
							if(possibleSites != null) {
								for(ISite possibleSite : possibleSites) {
									if(possibleSite.isDescendantOf(ap.getSite())) {
										descendantSites.add(possibleSite);
									}
								}
							}
							// if our input address has a unit number part, look it up
							if(address.getUnitNumber() != null) {
								descendantSites.addAll(ap.getSite().findChildrenByUnitNumber(
										address.getUnitNumber()));
							}
							// score all possible sites
							for(ISite site : descendantSites) {
								SiteAddress matchAddress = new SiteAddress();
								matchAddress.setSiteID(site.getUuid().toString());
								matchAddress.setSID(site.getId());
								matchAddress.setUnitDesignator(site.getUnitDesignator());
								matchAddress.setUnitNumber(site.getUnitNumber());
								matchAddress.setUnitNumberSuffix(site.getUnitNumberSuffix());
								matchAddress.setSiteName(site.getSiteName());
								matchAddress.setParentSiteDescriptor(site.getParentSiteDescriptor());
								matchAddress.setStreetName(face.getSegment().getPrimaryStreetName());
								matchAddress.setCivicNumber(address.getCivicNumber());
								matchAddress.setCivicNumberSuffix(address.getCivicNumberSuffix());
								matchAddress.setLocality(face.getLocality());
								matchAddress.setElectoralArea(face.getElectoralArea());
								
								matchAddress.setStreetSegmentID(
										face.getSegment().getSegmentId());
								matchAddress.setStateProvTerr(
										face.getLocality().getStateProvTerr().getName());
								
								matchAddress.resolveLocation(datastore, site,
										query.getParcelPoint(),
										query.getLocationDescriptor(), ap, face,
										query.isExtrapolate(), query.getSetBack());
								
								// determine the precision level of the match
								MatchPrecision precision = MatchPrecision.CIVIC_NUMBER;
								if((matchAddress.getUnitDesignator() != null && !matchAddress.getUnitDesignator().isEmpty())
										|| (matchAddress.getUnitNumber() != null && matchAddress.getUnitNumber().isEmpty())) {
									precision = MatchPrecision.UNIT;
								} else if(matchAddress.getSiteName() != null) {
									precision = MatchPrecision.SITE;
								}
								AddressMatch match = new AddressMatch(matchAddress,
										precision, datastore.getConfig()
												.getMatchPrecisionPoints(
														precision));
								match.setAccessPoint(ap);
								match.setSite(site);
								match.setSegment(face.getSegment());
								
								match.addFaults(incomingFaults);
								score(address, match, name, misspellings, query);
								if(nameMatch.getError() > 0) {
									match.addFault(datastore.getConfig().getMatchFault(nameMatch.getMisspelling(), MatchElement.STREET_NAME, "partialMatch"));
								}
								globalScoring(match, misspellings);
								if(query.pass(match)) {
									if(canShortCircuit(query, match)) {
										// lets short-circuit outta here!
										matches.clear();
										matches.add(match);
										return matches;
									}
									matches.add(match);
								}
							}
							if(query.getIncludeOccupants()) {
								List<IOccupant> descendantOccupants = new ArrayList<IOccupant>();
								if(possibleOccupants != null) {
									for(IOccupant possibleOccupant : possibleOccupants) {
										if(possibleOccupant.getSite() == ap.getSite()
												|| possibleOccupant.getSite().isDescendantOf(ap.getSite())) {
											descendantOccupants.add(possibleOccupant);
										}
									}
								}
							
								// score all possible occupants
								for(IOccupant occ : descendantOccupants) {
									OccupantAddress matchAddress = new OccupantAddress(occ, null);
	//								matchAddress.setSiteID(site.getUuid());
	//								matchAddress.setUnitDesignator(site.getUnitDesignator());
	//								matchAddress.setUnitNumber(site.getUnitNumber());
	//								matchAddress.setUnitNumberSuffix(site.getUnitNumberSuffix());
	//								matchAddress.setSiteName(site.getSiteName());
	//								matchAddress
	//										.setParentSiteDescriptor(site.getParentSiteDescriptor());
	//								matchAddress.setStreetName(face
	//										.getPrimaryStreetName());
	//								matchAddress.setCivicNumber(address.getCivicNumber());
	//								matchAddress.setLocality(face.getLocality());
	//								matchAddress.setStreetSegmentID(face.getSegment()
	//										.getSegmentId());
	//								matchAddress
	//										.setStateProvTerr(face.getLocality().getStateProvTerr()
	//												.getName());
									
									matchAddress.resolveLocation(datastore, occ.getSite(),
											query.getParcelPoint(),
											query.getLocationDescriptor(), ap, face,
											query.isExtrapolate(), query.getSetBack());
									
									// determine the precision level of the match
									MatchPrecision precision = MatchPrecision.OCCUPANT;
									AddressMatch match = new AddressMatch(matchAddress,
											precision, datastore.getConfig()
													.getMatchPrecisionPoints(precision));
									match.setAccessPoint(ap);
									//match.setSiteId(site.getId());
									match.setSegment(face.getSegment());
									
									match.addFaults(incomingFaults);
									if(nameMatch.getError() > 0) {
										match.addFault(datastore.getConfig().getMatchFault(nameMatch.getMisspelling(), MatchElement.STREET_NAME, "partialMatch"));
									}
									score(address, match, name, misspellings, query);
									globalScoring(match,misspellings);
									if(query.pass(match)) {
										if(canShortCircuit(query, match)) {
											// lets short-circuit outta here!
											matches.clear();
											matches.add(match);
											return matches;
										}
										matches.add(match);
									}
								}
							}
						}
					}
				}
				// add the streetname itself
				PairedList<Locality, double[]> localityPoints = name.getLocalityCentroids();
				for(PairedListEntry<Locality, double[]> entry : localityPoints) {
					// locality aliases are handled by scoring
					SiteAddress matchAddress = new SiteAddress();
					matchAddress.setStreetName(name);
					matchAddress.setLocality(entry.getLeft());
					
					matchAddress.setLocation(GeocoderDataStore.getGeometryFactory().createPoint(
							new Coordinate(entry.getRight()[0], entry.getRight()[1])));
					matchAddress.setStateProvTerr(entry.getLeft().getStateProvTerr().getName());
					matchAddress.setLocationPositionalAccuracy(PositionalAccuracy.COARSE);
					matchAddress.setLocationDescriptor(LocationDescriptor.STREET_POINT);
					AddressMatch match = new AddressMatch(
							matchAddress, MatchPrecision.STREET,
							datastore.getConfig().getMatchPrecisionPoints(
									MatchPrecision.STREET));
					match.addFaults(incomingFaults);
					if(nameMatch.getError() > 0) {
						match.addFault(datastore.getConfig().getMatchFault(nameMatch.getMisspelling(), MatchElement.STREET_NAME, "partialMatch"));
					}
					score(address, match, name, misspellings, query);
					globalScoring(match, misspellings);
					if(query.pass(match)) {
						matches.add(match);
					}
				}
			}
			// if no street names were matched but there is a locality name
			if(nameMatches.isEmpty() && address.getLocalityName() != null) {
				// check if the locality at least matches
				LocalityMapTarget lm = datastore.getBestLocalityMapping(address.getLocalityName());
				if(lm != null) {
					SiteAddress matchAddress = new SiteAddress(lm.getLocality());
					AddressMatch match = new AddressMatch(
							matchAddress, MatchPrecision.LOCALITY,
							datastore.getConfig().getMatchPrecisionPoints(
									MatchPrecision.LOCALITY));
					match.addFaults(incomingFaults);
					match.addFault(datastore.getConfig().getMatchFault(streetName, MatchElement.STREET_NAME, "notMatched"));
					scoreLocalityMatch(address, lm, match, misspellings, query);
					globalScoring(match, misspellings);
					if(query.pass(match)) {
						matches.add(match);
					}
				}
			}
		} else if(address.getOccupantName() != null) {
			// score all possible occupants
			for(IOccupant occ : possibleOccupants) {
				MatchPrecision precision = MatchPrecision.OCCUPANT;
				OccupantAddress matchAddress = new OccupantAddress(occ, null);
				AccessPoint ap = occ.getSite().getPrimaryAccessPoint();
				
				if(ap instanceof CivicAccessPoint) {
					CivicAccessPoint cap = (CivicAccessPoint)ap;
					matchAddress.resolveLocation(datastore, occ.getSite(), query.getParcelPoint(),
							query.getLocationDescriptor(),
							cap, cap.getBlockFace(), query.isExtrapolate(), query.getSetBack());
					
					AddressMatch match = new AddressMatch(matchAddress,
							precision, datastore.getConfig()
									.getMatchPrecisionPoints(precision));
					match.setAccessPoint(ap);
					//match.setSiteId(site.getId());
					match.setSegment(cap.getBlockFace().getSegment());
					
					match.addFaults(incomingFaults);
					scoreSiteNameMatch(address, match, misspellings, query);
					globalScoring(match, misspellings);
					if(query.pass(match)) {
						if(canShortCircuit(query, match)) {
							// lets short-circuit outta here!
							matches.clear();
							matches.add(match);
							return matches;
						}
						matches.add(match);
					}
				} else if(ap instanceof NonCivicAccessPoint) {
					resolveLocation(query, (NonCivicAccessPoint)ap, occ.getSite(), matchAddress);
					
					AddressMatch match = new AddressMatch(matchAddress,
							precision, datastore.getConfig()
									.getMatchPrecisionPoints(precision));
					match.setAccessPoint(ap);
					//match.setSiteId(site.getId());
					// we're arbitrarily picking the left side
					// in fact it's possible that there are no blockfaces if the segment has no
					// addressing
					match.setSegment(((NonCivicAccessPoint)ap).getStreetSegment());
					
					match.addFaults(incomingFaults);
					// TODO possibly need different scoring for NCAPs
					//score(address, match, null, misspellings, query);
					scoreSiteNameMatch(address, match, misspellings, query);
					globalScoring(match, misspellings);
					// we don't want to accept unit-number only matches
					if(match.containsFault(MatchFault.MatchElement.SITE_NAME, "notMatched") ||
							match.containsFault(MatchFault.MatchElement.SITE_NAME, "missing")) {
						continue;
					}
					if(query.pass(match)) {
						if(canShortCircuit(query, match)) {
							// lets short-circuit outta here!
							matches.clear();
							matches.add(match);
							return matches;
						}
						matches.add(match);
					}
				}
			}
		} else if(address.getSiteName() != null) {
			// this is a site/occupant name query
			// only do the site name lookup if there is a valid locality, or no locality
			if(address.getLocalityName() == null
					|| datastore.getLocalities(address.getLocalityName()).size() > 0) {
				if(possibleSites.isEmpty() && possibleOccupants.isEmpty()) {
					if(address.getLocalityName() != null) {
						// check if the locality at least matches
						LocalityMapTarget lm = datastore
								.getBestLocalityMapping(address.getLocalityName());
						if(lm != null) {
							SiteAddress matchAddress = new SiteAddress(lm.getLocality());
							AddressMatch match = new AddressMatch(
									matchAddress, MatchPrecision.LOCALITY,
									datastore.getConfig().getMatchPrecisionPoints(
											MatchPrecision.LOCALITY));
							match.addFaults(incomingFaults);
							// match.addFault(datastore.getConfig().getMatchFault("SITE_NAME.notMatched"));
							scoreLocalityMatch(address, lm, match, misspellings, query);
							globalScoring(match, misspellings);
							if(query.pass(match)) {
								matches.add(match);
							}
						}
					}
				} else {
					for(ISite site : possibleSites) {
						MatchPrecision precision = MatchPrecision.SITE;
						SiteAddress matchAddress = new SiteAddress(site, null);
						AccessPoint ap = site.getPrimaryAccessPoint();
						if(ap instanceof CivicAccessPoint) {
							CivicAccessPoint cap = (CivicAccessPoint)ap;
							matchAddress.resolveLocation(datastore, site, query.getParcelPoint(),
									query.getLocationDescriptor(),
									cap, cap.getBlockFace(), query.isExtrapolate(), query.getSetBack());
							
							AddressMatch match = new AddressMatch(matchAddress,
									precision, datastore.getConfig()
											.getMatchPrecisionPoints(precision));
							match.setAccessPoint(ap);
							match.setSite(site);
							match.setSegment(cap.getBlockFace().getSegment());
							
							match.addFaults(incomingFaults);
							scoreSiteNameMatch(address, match, misspellings, query);
							// we don't want to accept unit-number only matches
							if(match.containsFault(MatchFault.MatchElement.SITE_NAME, "notMatched") ||
									match.containsFault(MatchFault.MatchElement.SITE_NAME, "missing")) {
								continue;
							}
							globalScoring(match, misspellings);
							if(query.pass(match)) {
								if(canShortCircuit(query, match)) {
									// lets short-circuit outta here!
									matches.clear();
									matches.add(match);
									return matches;
								}
								matches.add(match);
							}
						} else if(ap instanceof NonCivicAccessPoint) {
							resolveLocation(query, (NonCivicAccessPoint)ap, site, matchAddress);
							
							AddressMatch match = new AddressMatch(matchAddress,
									precision, datastore.getConfig()
											.getMatchPrecisionPoints(precision));
							match.setAccessPoint(ap);
							match.setSite(site);
							StreetSegment seg = ((NonCivicAccessPoint)ap).getStreetSegment();
							if(seg != null) {
								// we're arbitrarily picking the left side
								match.setSegment(seg);
							}
							
							match.addFaults(incomingFaults);
							// TODO possibly need different scoring for NCAPs
							//score(address, match, null, misspellings, query);
							scoreSiteNameMatch(address, match, misspellings, query);
							// we don't want to accept unit-number only matches
							if(match.containsFault(MatchFault.MatchElement.SITE_NAME, "notMatched") ||
									match.containsFault(MatchFault.MatchElement.SITE_NAME, "missing")) {
								continue;
							}
							globalScoring(match, misspellings);
							if(query.pass(match)) {
								if(canShortCircuit(query, match)) {
									// lets short-circuit outta here!
									matches.clear();
									matches.add(match);
									return matches;
								}
								matches.add(match);
							}
						}
					}
				}
			}
		} else if(address.getLocalityName() != null) {
			// this is a locality-only query
			List<MisspellingOf<LocalityMapTarget>> lms = datastore.getLocalities(address.getLocalityName());
			for(MisspellingOf<LocalityMapTarget> mlm : lms) {
				LocalityMapTarget lm = mlm.get();
				SiteAddress matchAddress = new SiteAddress(lm.getLocality());
				AddressMatch match = new AddressMatch(
						matchAddress, MatchPrecision.LOCALITY,
						datastore.getConfig().getMatchPrecisionPoints(
								MatchPrecision.LOCALITY));
				match.addFaults(incomingFaults);
				// if there is an error it means this is a prefix match
				if(mlm.getError() > 0 ) {
					// check if the prefix is a perfect match for the base locality name (without qualifier)
					if(address.getLocalityName().equalsIgnoreCase(lm.getLocality().getName())) {
						// add a no-penalty fault in this case
						match.addFault(new MatchFault(address.getLocalityName(), MatchElement.LOCALITY, "partialMatch", 0));
					} else {
						// usual penalty in this case
						match.addFault(datastore.getConfig().getMatchFault(address.getLocalityName(), MatchElement.LOCALITY, "partialMatch"));
					}
				}
				scoreLocalityMatch(address, lm, match, misspellings, query);
				globalScoring(match, misspellings);
				if(query.pass(match)) {
					matches.add(match);
				}
			}
		} else if(address.getStateProvTerr() != null) {
			// this is a state/prov/terr-only query
			StateProvTerr spt = datastore.getStateProvTerr(address.getStateProvTerr());
			if(spt != null) {
				SiteAddress matchAddress = new SiteAddress();
				matchAddress.setStateProvTerr(spt.getName());
				matchAddress.setLocation(spt.getLocation());
				matchAddress.setLocationPositionalAccuracy(PositionalAccuracy.COARSE);
				matchAddress.setLocationDescriptor(LocationDescriptor.PROVINCE_POINT);
				AddressMatch match = new AddressMatch(
						matchAddress, MatchPrecision.PROVINCE,
						datastore.getConfig().getMatchPrecisionPoints(
								MatchPrecision.PROVINCE));
				match.addFaults(incomingFaults);
				scoreStateProvTerr(address, match, misspellings);
				globalScoring(match, misspellings);
				if(query.pass(match)) {
					matches.add(match);
				}
			}
		}
		return matches;
	}
	
	private void resolveLocation(GeocodeQuery query, NonCivicAccessPoint ap,
			ISite site, SiteAddress matchAddress) {
		// for a non-civic access point,
		// if you ask for the routing point, you get the access point
		if(site != null
				&& !(LocationDescriptor.ACCESS_POINT.equals(query.getLocationDescriptor())
				|| LocationDescriptor.ROUTING_POINT.equals(query.getLocationDescriptor()))) {
			// the query must be requesting one of the site points, we can only
			// return whatever it is that we have (rooftop, parcel, frontDoor, etc)
			matchAddress.setLocation(site.getLocation());
			matchAddress.setLocationDescriptor(site.getLocationDescriptor());
			matchAddress.setLocationPositionalAccuracy(site.getLocationPositionalAccuracy());
		} else {
			// either the query was requesting the access point or routing point,
			// or a site point but we have no site so we fall back to the access point
			matchAddress.setLocation(ap.getPoint());
			matchAddress.setLocationDescriptor(LocationDescriptor.ACCESS_POINT);
			matchAddress.setLocationPositionalAccuracy(ap.getPositionalAccuracy());
		}
	}
	
	public boolean canShortCircuit(GeocodeQuery query, GeocodeMatch match) {
		if(1 == query.getMaxResults() && match.isNearPerfect()) {
			// lets short-circuit outta here!
			logger.debug("short circuiting");
			return true;
		}
		return false;
	}
	
	private List<BlockFace> prioritizeBlocks(List<BlockFace> faces, String localityName) {
		if(faces.isEmpty() || localityName == null) {
			return faces;
		}
		
		List<MisspellingOf<LocalityMapTarget>> lms = datastore.getLocalities(localityName);
		if(lms.isEmpty()) {
			return faces;
		}
		List<BlockFace> newFaces = new ArrayList<BlockFace>();
		for(int i = 0; i < faces.size(); i++) {
//			if(faces.get(i).getLocality().getName().equalsIgnoreCase("Vancouver")) {
//				System.out.println("foo");
//			}
			for(MisspellingOf<LocalityMapTarget> mlm : lms) {
				LocalityMapTarget lm = mlm.get();
				if(lm.getLocality().equals(faces.get(i).getLocality())) {
					if(lm.getConfidence() < 100) {
						newFaces.add(faces.get(i));
					} else {
						newFaces.add(0,faces.get(i));
					}
					break;
				}
			}
		}
		return newFaces;
	}
	
	private void scoreLocalityMatch(OccupantAddress input, LocalityMapTarget lm, AddressMatch match,
			AddressComponentMisspellings misspellings, GeocodeQuery query) {
		scoreUnit(input, match, misspellings, query);
		if(!input.getLocalityName().equalsIgnoreCase(match.getAddress().getLocalityName())) {
			if(lm.getConfidence() < 100) {
				// the penalty for an alias is variable based on
				// the confidence of the alias
				match.addFault(datastore.getConfig().getLocalityAliasFault(input.getLocalityName(),
						lm.getConfidence()));
			}
		}
		if(misspellings.getLocalityMSError() > 0) {
			match.addFault(datastore.getConfig().getMatchFault(misspellings.getLocalityMSString(), MatchElement.LOCALITY, "spelledWrong"));
		}
		scoreStateProvTerr(input, match, misspellings);
	}
	
	private void scoreSiteName(OccupantAddress input, AddressMatch match,
			AddressComponentMisspellings misspellings, GeocodeQuery query) {
		String matchFullSiteName = match.getAddress().getFullSiteName();
		if(match.getPrecision().equals(MatchPrecision.OCCUPANT)) {
			String[] inputWords = datastore.mapWordsArray(input.getOccupantName(), true);

			String[] occMatchWords = datastore.mapWordsArray(
					((OccupantAddress)match.getAddress()).getOccupantName().toUpperCase(), true);
			Set<String> occMatchWordSet = new THashSet<String>(Arrays.asList(occMatchWords));
			
			Set<String> leftoverInputWordSet = new THashSet<String>(Arrays.asList(inputWords));

			Set<String> intersectingOccWordSet = new THashSet<String>(Arrays.asList(inputWords));
			intersectingOccWordSet.retainAll(occMatchWordSet);
			leftoverInputWordSet.removeAll(intersectingOccWordSet);
			int leftoverWordCount = leftoverInputWordSet.size();
			
			// if the site and occupant names are identical then ignore the site name
			if(!matchFullSiteName.equalsIgnoreCase(((OccupantAddress)match.getAddress()).getOccupantName())) {
				Set<String> intersectingSiteWordSet = new THashSet<String>(Arrays.asList(inputWords));
				String[] siteMatchWords = datastore.mapWordsArray(
						matchFullSiteName.toUpperCase(), true);
				Set<String> siteMatchWordSet = new THashSet<String>(Arrays.asList(siteMatchWords));
				intersectingSiteWordSet.retainAll(siteMatchWordSet);
				leftoverInputWordSet.removeAll(intersectingSiteWordSet);
				leftoverWordCount = leftoverInputWordSet.size();
				
				if(intersectingSiteWordSet.size() == 0) {
					if(!matchFullSiteName.isEmpty()) {
						match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.SITE_NAME, "missing"));
					}
				} else if(!intersectingSiteWordSet.equals(siteMatchWordSet)) {
					// 	partial match
					double siteDiceDistance = 1 - 
							((double)(2 * intersectingSiteWordSet.size())
							/ (double)(intersectingSiteWordSet.size() + leftoverWordCount 
									+ siteMatchWordSet.size()));
					leftoverWordCount = 0;
					match.addFault(datastore.getConfig().getSiteNamePartialMatchFault(String.join(" ", intersectingSiteWordSet), siteDiceDistance));
				}	
			}

			// there has to be at least a partial occupant name match, or we wouldn't have matched it
			double occDiceDistance = 1 - 
					((double)(2 * intersectingOccWordSet.size())
					/ (double)(intersectingOccWordSet.size() + leftoverWordCount 
							+ occMatchWordSet.size()));
			if(occDiceDistance > 0) {
				match.addFault(datastore.getConfig().getOccupantNamePartialMatchFault(String.join(" ", intersectingOccWordSet), occDiceDistance));
			}
		} else {
			if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getSiteName(),
					matchFullSiteName)) {
				if(input.getSiteName() == null || input.getSiteName().isEmpty()) {
					match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.SITE_NAME, "missing"));
				} else {
					String[] inputWords = GeocoderUtil.wordSplit(input.getSiteName());
					Set<String> inputWordsSet = new THashSet<String>(Arrays.asList(inputWords));
					
					String[] matchWords = datastore.mapWordsArray(
							matchFullSiteName.toUpperCase(), true);
					Set<String> matchWordsSet = new THashSet<String>(Arrays.asList(matchWords));
					
					Set<String> intersectingWords = new THashSet<String>(Arrays.asList(inputWords));
					intersectingWords.retainAll(Arrays.asList(matchWords));
					double diceDistance = 1 - ((double)(2 * intersectingWords.size())
							/ (double)(inputWordsSet.size() + matchWordsSet.size()));
					if(diceDistance == 1) {
						// no intersecting words at all
						match.addFault(datastore.getConfig().getMatchFault(input.getSiteName(), MatchElement.SITE_NAME, "notMatched"));
						if((matchFullSiteName == null || matchFullSiteName.isEmpty()) && query.isEcho()) {
							match.getAddress().setSiteName(input.getSiteName());
						}
					} else if(diceDistance > 0) {
						// some intersecting words
						match.addFault(datastore.getConfig().getSiteNamePartialMatchFault(input.getSiteName(), diceDistance));
					}
				}
			}
		}
		if(misspellings.getSiteNameMSError() > 0) {
			match.addFault(datastore.getConfig().getMatchFault(misspellings.getSiteNameMSString(), MatchElement.SITE_NAME, "spelledWrong"));
		}
	}
	
	private void scoreSiteNameMatch(OccupantAddress input, AddressMatch match,
			AddressComponentMisspellings misspellings, GeocodeQuery query) {
		scoreUnit(input, match, misspellings, query);
		
		if((input.getStreetName() == null || input.getStreetName().isEmpty())
				&& (input.getStreetDirection() == null ||input.getStreetDirection().isEmpty())
				&& (input.getStreetType() == null ||input.getStreetType().isEmpty())
				&& (input.getStreetQualifier() == null || input.getStreetQualifier().isEmpty())
				&& (input.getCivicNumberSuffix() == null || input.getCivicNumberSuffix().isEmpty())
				&& (input.getLocalityName() == null || input.getLocalityName().isEmpty())
				&& (input.getStateProvTerr() == null || input.getStateProvTerr().isEmpty())
				&& input.getCivicNumber() == null) {
			match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.ADDRESS, "missing"));
			return;
		}
		
		// if the match has a segment, and there was a street name in the input
		if(match.getSegment() != null) {
			StreetName inputSn = new StreetName(input.getStreetName(), input.getStreetType(), 
					input.getStreetDirection(), input.getStreetQualifier(), 
					input.isStreetTypePrefix(), input.isStreetDirectionPrefix(), null);
			List<MatchFault> faults = new ArrayList<MatchFault>();
			int penalty = compareStreetNames(inputSn, match.getSegment().getPrimaryStreetName(), faults);
			if(penalty < 0 && match.getSegment().getAliasNames().size() > 0) {
				// try the aliases
				for(Object aliasName : match.getSegment().getAliasNames()) {
					List<MatchFault> aliasFaults = new ArrayList<MatchFault>();
					int aliasPenalty = compareStreetNames(inputSn, (StreetName)aliasName, aliasFaults);
					if(aliasPenalty >= 0 && (aliasPenalty <= penalty || penalty < 0)) {
						if("hwy".equalsIgnoreCase(match.getSegment().getPrimaryStreetName().getType())
								|| "hwy".equalsIgnoreCase(((StreetName)aliasName).getType())) {
							aliasFaults.add(datastore.getConfig().getMatchFault(inputSn.toString(), MatchElement.STREET_NAME, "isHighwayAlias"));
						} else {
							aliasFaults.add(datastore.getConfig().getMatchFault(inputSn.toString(), MatchElement.STREET_NAME, "isAlias"));
						}
						faults = aliasFaults;
						penalty = aliasPenalty;
					}
				}
			}
			if(penalty >= 0) {
				match.addFaults(faults);
			} else {
				match.addFault(datastore.getConfig().getMatchFault(inputSn.toString(), MatchElement.STREET_NAME, "notMatched"));
			}
		} else if(input.getStreetName() != null && !input.getStreetName().isEmpty()) {
			match.addFault(datastore.getConfig().getMatchFault(input.getStreetName(), MatchElement.STREET_NAME, "notMatched"));
		}
		
		if(!GeocoderUtil.equalsNullSafe(input.getCivicNumber(), match.getAddress().getCivicNumber())) {
			if(input.getCivicNumber() == null) {
				match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.CIVIC_NUMBER, "missing"));
			} else {
				match.addFault(datastore.getConfig().getMatchFault(input.getCivicNumber().toString(), MatchElement.CIVIC_NUMBER, "notMatched"));
			}
		}

		if(input.getLocalityName() == null || input.getLocalityName().isEmpty()) {
			match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.LOCALITY, "missing"));
		} else {
			if(misspellings.getLocalityMSError() > 0) {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getLocalityMSString(), MatchElement.LOCALITY, "spelledWrong"));
			}
			// check to see if the input locality name maps to the locality
			// of the match
			boolean matched = false;
			Collection<MisspellingOf<LocalityMapTarget>> lms = datastore.getLocalities(input.getLocalityName());
			// String mappedLocalityName = datastore.mapWords(match.getAddress().getLocalityName());
			for(MisspellingOf<LocalityMapTarget> mlm : lms) {
				LocalityMapTarget lm = mlm.get();
				if(lm.getLocality().getFullyQualifiedName().equals(match.getAddress().getLocalityName())) {
					if(lm.getConfidence() < 100) {
						// the penalty for an alias is variable based on the
						// confidence of the alias
						match.addFault(datastore.getConfig().getLocalityAliasFault(input.getLocalityName(), lm.getConfidence()));
					}
					if(mlm.getError() > 0) {
						match.addFault(datastore.getConfig().getMatchFault(input.getLocalityName(), MatchElement.LOCALITY, "partialMatch"));
					}
					matched = true;
					break;
				}
			}
			if(!matched) {
				// no mapping matches
				match.addFault(datastore.getConfig().getMatchFault(input.getLocalityName(), MatchElement.LOCALITY, "notMatched"));
			}
		}
		
		scoreStateProvTerr(input, match, misspellings);
	}
	
	/**
	 * Checks for differences between the input address and the potential match and adds MatchFaults
	 * as appropriate.
	 * 
	 * @param input the input query Address
	 * @param match the potential AddressMatch
	 */
	private void score(OccupantAddress input, AddressMatch match, StreetName aliasStreetName,
			AddressComponentMisspellings misspellings, GeocodeQuery query) {
		// if(match.getPrecision() != MatchPrecision.STREET) {
		scoreUnit(input, match, misspellings, query);
		// }
		
		if(input.getLocalityName() == null || input.getLocalityName().isEmpty()) {
			match.addFault(datastore.getConfig().getMatchFault(input.getLocalityName(), MatchElement.LOCALITY, "missing"));
		} else {
			if(misspellings.getLocalityMSError() > 0) {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getLocalityMSString(), MatchElement.LOCALITY, "spelledWrong"));
			}
			// check to see if the input locality name maps to the locality
			// of the match
			boolean matched = false;
			Collection<MisspellingOf<LocalityMapTarget>> lms = datastore.getLocalities(input.getLocalityName());
			// String mappedLocalityName = datastore.mapWords(match.getAddress().getLocalityName());
			for(MisspellingOf<LocalityMapTarget> mlm : lms) {
				LocalityMapTarget lm = mlm.get();
				if(lm.getLocality().getFullyQualifiedName().equals(match.getAddress().getLocalityName())) {
					if(lm.getConfidence() < 100) {
						// the penalty for an alias is variable based on the
						// confidence of the alias
						match.addFault(datastore.getConfig().getLocalityAliasFault(input.getLocalityName(),
								lm.getConfidence()));
					}
					if(mlm.getError() > 0) {
						match.addFault(datastore.getConfig().getMatchFault(input.getLocalityName(), MatchElement.LOCALITY, "partialMatch"));
						if(lm.getConfidence() < 100) {
							match.addFault(datastore.getConfig().getMatchFault(input.getLocalityName(), MatchElement.LOCALITY, "partialMatchToAlias"));
						}
					}
					matched = true;
					break;
				}
			}
			if(!matched) {
				// no mapping matches
				match.addFault(datastore.getConfig().getMatchFault(input.getLocalityName(), MatchElement.LOCALITY, "notMatched"));
			}
		}
		
		// if this match references a segment (ie. at least block level)
		//if((match.getSegment()) != null) {
		if(match.getAddress().getStreetName() != null) {
			if(misspellings.getStreetNameMSError() > 0) {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetNameMSString(), MatchElement.STREET_NAME, "spelledWrong"));
			}
			
			if(input.getStreetName() == null || input.getStreetName().isEmpty()) {
				// there was no streetname in the input, so penalize
				match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.STREET_NAME, "missing"));
			} else {
				StreetName inputSn = new StreetName(input.getStreetName(), input.getStreetType(), 
						input.getStreetDirection(), input.getStreetQualifier(), 
						input.isStreetTypePrefix(), input.isStreetDirectionPrefix(), null);
				if(aliasStreetName == null) {
					List<MatchFault> faults = new ArrayList<MatchFault>();
					int penalty = compareStreetNames(inputSn, match.getSegment().getPrimaryStreetName(), faults);
					if(penalty < 0 && match.getSegment().getAliasNames().size() > 0) {
						// try the aliases
						for(Object aliasName : match.getSegment().getAliasNames()) {
							List<MatchFault> aliasFaults = new ArrayList<MatchFault>();
							int aliasPenalty = compareStreetNames(inputSn, (StreetName)aliasName, aliasFaults);
							if(aliasPenalty >= 0 && (aliasPenalty <= penalty || penalty < 0)) {
								if("hwy".equalsIgnoreCase(match.getSegment().getPrimaryStreetName().getType())
										|| "hwy".equalsIgnoreCase(((StreetName)aliasName).getType())) {
									aliasFaults.add(datastore.getConfig().getMatchFault(inputSn.toString(), MatchElement.STREET_NAME, "isHighwayAlias"));
								} else {
									aliasFaults.add(datastore.getConfig().getMatchFault(inputSn.toString(), MatchElement.STREET_NAME, "isAlias"));
								}
								faults = aliasFaults;
								penalty = aliasPenalty;
							}
						}
					}
					if(penalty >= 0) {
						match.addFaults(faults);
					} else {
						match.addFault(datastore.getConfig().getMatchFault(inputSn.toString(), MatchElement.STREET_NAME, "notMatched"));
					}
				} else if(match.getSegment() != null && match.getSegment().getPrimaryStreetName() != aliasStreetName) {
					// it must be an alias
					if("hwy".equalsIgnoreCase(match.getSegment().getPrimaryStreetName().getType())
							|| "hwy".equalsIgnoreCase(aliasStreetName.getType())) {
						match.addFault(datastore.getConfig().getMatchFault(inputSn.toString(), MatchElement.STREET_NAME, "isHighwayAlias"));
					} else {
						match.addFault(datastore.getConfig().getMatchFault(inputSn.toString(), MatchElement.STREET_NAME, "isAlias"));
					}
				}
			}
		}
		
		String matchStreetDirection = match.getAddress().getStreetDirection();
		Boolean matchStreetDirIsPrefix = match.getAddress().isStreetDirectionPrefix();
		if(aliasStreetName != null) {
			matchStreetDirection = aliasStreetName.getDir();
			matchStreetDirIsPrefix = aliasStreetName.getIsStreetDirPrefix();
		}
		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getStreetDirection(), matchStreetDirection)) {
			if(input.getStreetDirection() == null || input.getStreetDirection().isEmpty()) {
				match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.STREET_DIRECTION, "missing"));
			} else {
				if((match.getSegment() != null && "hwy".equalsIgnoreCase(match.getSegment().getPrimaryStreetName().getType()))
						|| "hwy".equalsIgnoreCase(match.getAddress().getStreetType())
						|| (aliasStreetName != null && "hwy".equalsIgnoreCase(aliasStreetName.getType()))) {
					match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetDirectionMSString(), MatchElement.STREET_DIRECTION, "notMatchedInHighway"));
				} else {
					match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetDirectionMSString(), MatchElement.STREET_DIRECTION, "notMatched"));
				}
			}
		}
		if(misspellings.getStreetDirectionMSError() > 0) {
			match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetDirectionMSString(), MatchElement.STREET_DIRECTION, "spelledWrong"));
		}
		if(matchStreetDirIsPrefix != null) {
			if(input.isStreetDirectionPrefix() && !matchStreetDirIsPrefix) {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetDirectionMSString(), MatchElement.STREET_DIRECTION, "notPrefix"));
			} else if(!input.isStreetDirectionPrefix() && matchStreetDirIsPrefix) {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetDirectionMSString(), MatchElement.STREET_DIRECTION, "notSuffix"));
			}
		}

		String matchStreetType = match.getAddress().getStreetType();
		Boolean matchStreetTypeIsPrefix = match.getAddress().isStreetTypePrefix();
		if(aliasStreetName != null) {
			matchStreetType = aliasStreetName.getType();
			matchStreetTypeIsPrefix = aliasStreetName.getIsStreetTypePrefix();
		}
		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getStreetType(), matchStreetType)) {
			if(input.getStreetType() == null || input.getStreetType().isEmpty()) {
				match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.STREET_TYPE, "missing"));
			} else {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetTypeMSString(), MatchElement.STREET_TYPE, "notMatched"));
			}
		}
		if(misspellings.getStreetTypeMSError() > 0) {
			match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetTypeMSString(), MatchElement.STREET_TYPE, "spelledWrong"));
		}
		if(matchStreetTypeIsPrefix != null) {
			if(input.isStreetTypePrefix() && !matchStreetTypeIsPrefix) {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetTypeMSString(), MatchElement.STREET_TYPE, "notPrefix"));
			} else if(!input.isStreetTypePrefix() && matchStreetTypeIsPrefix) {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetTypeMSString(), MatchElement.STREET_TYPE, "notSuffix"));
			}
		}
		
		String matchStreetQualifier = match.getAddress().getStreetQualifier();
		if(aliasStreetName != null) {
			matchStreetQualifier = aliasStreetName.getQual();
		}
		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getStreetQualifier(), matchStreetQualifier)) {
			if(input.getStreetQualifier() == null || input.getStreetQualifier().isEmpty()) {
				match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.STREET_QUALIFIER, "missing"));
			} else {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetQualifierMSString(), MatchElement.STREET_QUALIFIER, "notMatched"));
			}
		}
		if(misspellings.getStreetQualifierMSError() > 0) {
			match.addFault(datastore.getConfig().getMatchFault(misspellings.getStreetQualifierMSString(), MatchElement.STREET_QUALIFIER, "spelledWrong"));
		}
		
		if(!GeocoderUtil
				.equalsNullSafe(input.getCivicNumber(), match.getAddress().getCivicNumber())) {
			if(input.getCivicNumber() == null) {
				match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.CIVIC_NUMBER, "missing"));
			} else {
				match.addFault(datastore.getConfig().getMatchFault(input.getCivicNumber().toString(), MatchElement.CIVIC_NUMBER, "notInAnyBlock"));
			}
		}
		
		// score the civic number suffix
		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getCivicNumberSuffix(),
				match.getAddress().getCivicNumberSuffix())) {
			if(input.getCivicNumberSuffix() == null || input.getCivicNumberSuffix().isEmpty()) {
				match.addFault(datastore.getConfig().getMatchFault(input.getCivicNumberSuffix(), MatchElement.CIVIC_NUMBER_SUFFIX, "missing"));
			} else {
				match.addFault(datastore.getConfig().getMatchFault(input.getCivicNumberSuffix(),MatchElement.CIVIC_NUMBER_SUFFIX, "notMatched"));
				if(query.isEcho()) {
					match.getAddress().setCivicNumberSuffix(input.getCivicNumberSuffix());
				}
			}
		}
		
		scoreStateProvTerr(input, match, misspellings);
	}
	
	private void scoreStateProvTerr(SiteAddress input, AddressMatch match,
			AddressComponentMisspellings misspellings) {
		if(input.getStateProvTerr() == null || input.getStateProvTerr().isEmpty()) {
			match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.PROVINCE, "missing"));
		} else {
			if(misspellings.getStateProvTerrMSError() > 0) {
				match.addFault(datastore.getConfig().getMatchFault(misspellings.getStateProvTerrMSString(), MatchElement.PROVINCE, "spelledWrong"));
			}
			if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getStateProvTerr(),
					datastore.mapWords(match.getAddress().getStateProvTerr()))) {
				match.addFault(datastore.getConfig().getMatchFault(input.getStateProvTerr(), MatchElement.PROVINCE, "notMatched"));
			}
		}
	}
	
	private void scoreUnit(OccupantAddress input, AddressMatch match,
			AddressComponentMisspellings misspellings, GeocodeQuery query
			) {
		SiteAddress matchAddress = match.getAddress();
		// score site name (must be done before unit details are echoed)
		scoreSiteName(input, match, misspellings, query);
		
		// score unitDesignator
		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getUnitDesignator(),
				matchAddress.getUnitDesignator())
				|| (input.getUnitDesignator() == null && input.getUnitNumber() != null)) {
			if(input.getUnitDesignator() == null || input.getUnitDesignator().isEmpty()) {
				match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.UNIT_DESIGNATOR, "missing"));
			} else {
				if(matchAddress.getUnitDesignator() == null
						|| matchAddress.getUnitDesignator().isEmpty()) {
					match.addFault(datastore.getConfig()
							.getMatchFault(input.getUnitDesignator(), MatchElement.UNIT_DESIGNATOR, "notMatched"));
					if(query.isEcho()) {
						matchAddress.setUnitDesignator(input.getUnitDesignator().toUpperCase());
					}
				} else {
					match.addFault(datastore.getConfig().getMatchFault(input.getUnitDesignator(), MatchElement.UNIT_DESIGNATOR, "isAlias"));
				}
			}
		}
		if(misspellings.getUnitDesignatorMSError() > 0) {
			match.addFault(datastore.getConfig().getMatchFault(misspellings.getUnitDesignatorMSString(), MatchElement.UNIT_DESIGNATOR, "spelledWrong"));
		}
		// score unitNumber and suffix
		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getUnitNumber(), matchAddress.getUnitNumber())
				|| !GeocoderUtil.equalsIgnoreCaseNullSafe(input.getUnitNumberSuffix(), matchAddress.getUnitNumberSuffix())) {
			if(input.getUnitNumber() == null || input.getUnitNumber().isEmpty()) {
				match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.UNIT_NUMBER, "missing"));
			} else {
				StringBuilder sb = new StringBuilder();
				if(input.getUnitNumber() != null) {
					sb.append(input.getUnitNumber());
				}
				if(input.getUnitNumberSuffix() != null) {
					sb.append(input.getUnitNumberSuffix());
				}
				match.addFault(datastore.getConfig().getMatchFault(sb.toString(), MatchElement.UNIT_NUMBER, "notMatched"));
				if(query.isEcho()) {
					matchAddress.setUnitNumber(input.getUnitNumber());
					matchAddress.setUnitNumberSuffix(input.getUnitNumberSuffix());
				}
			}
		}
		if(misspellings.getUnitNumberMSError() > 0) {
			match.addFault(datastore.getConfig().getMatchFault(misspellings.getUnitNumberMSString(), MatchElement.UNIT_NUMBER, "spelledWrong"));
		}
		// add the default unitDesignator if we don't have one but do have a unit number
		if(matchAddress.getUnitDesignator() == null && matchAddress.getUnitNumber() != null) {
			matchAddress.setUnitDesignator(datastore.getConfig().getDefaultUnitDesignator());
		}
		// score the unit number suffix
//		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getUnitNumberSuffix(),
//				matchAddress.getUnitNumberSuffix())) {
//			if(input.getUnitNumberSuffix() == null || input.getUnitNumberSuffix().isEmpty()) {
//				match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.UNIT_NUMBER_SUFFIX, "missing"));
//			} else {
//				match.addFault(datastore.getConfig().getMatchFault(input.getUnitNumberSuffix(), MatchElement.UNIT_NUMBER_SUFFIX, "notMatched"));
//				if(query.isEcho()) {
//					matchAddress.setUnitNumberSuffix(input.getUnitNumberSuffix());
//				}
//			}
//		}
	}
	
	/**
	 * This version of score() is for IntersectionMatches. Checks for differences between the input
	 * intersection address and the potential match and adds MatchFaults as appropriate.
	 * 
	 * @param input the input query Address
	 * @param streetNames
	 * @param intersection
	 * @param match the potential AddressMatch
	 */
	void scoreIntersectionMatch(StreetIntersectionAddress input,
			StreetName[] streetNames, StreetIntersection intersection,
			IntersectionMatch match, AddressComponentMisspellings misspellings) {
		// reduce two identical streetnames down to one name
		if(streetNames.length == 2 && streetNames[0].nameEquals(streetNames[1])) {
			streetNames = new StreetName[] {streetNames[0]};
		}
		// score every input streetName against every streetName at the
		// intersection
		@SuppressWarnings("unchecked")
		List<StreetNameMatch>[] streetNameMatches = new List[streetNames.length];
		for(int i = 0; i < streetNames.length; i++) {
			StreetName inputSn = streetNames[i];
			streetNameMatches[i] = new ArrayList<StreetNameMatch>();
			for(StreetName matchSn : intersection.getStreetNames()) {
				List<MatchFault> faults = new ArrayList<MatchFault>();
				int penalty = compareStreetNames(inputSn, matchSn, faults);
				if(penalty >= 0) {
					// add a fault if the name is an alias
					if(intersection.getAliasStreetNames().contains(matchSn)) {
						faults.add(datastore.getConfig().getMatchFault(inputSn.toString(), MatchElement.STREET_NAME, "isAlias"));
					}
					// add faults for misspellings
					if(misspellings.getStreetNameMSError(i) > 0) {
						faults.add(datastore.getConfig().getMatchFault(misspellings.getStreetNameMSString(i), MatchElement.STREET_NAME, "spelledWrong"));
					}
					if(misspellings.getStreetTypeMSError(i) > 0) {
						faults.add(datastore.getConfig().getMatchFault(misspellings.getStreetTypeMSString(i), MatchElement.STREET_TYPE, "spelledWrong"));
					}
					if(misspellings.getStreetDirectionMSError(i) > 0) {
						faults.add(datastore.getConfig().getMatchFault(misspellings.getStreetDirectionMSString(i), MatchElement.STREET_DIRECTION, "spelledWrong"));
					}
					if(misspellings.getStreetQualifierMSError(i) > 0) {
						faults.add(datastore.getConfig().getMatchFault(misspellings.getStreetQualifierMSString(i), MatchElement.STREET_QUALIFIER, "spelledWrong"));
					}
					
					streetNameMatches[i].add(new StreetNameMatch(matchSn, penalty, faults));
				}
			}
		}
		// sort the possible matches for each streetName, lowest penalty first
		for(List<StreetNameMatch> matchList : streetNameMatches) {
			Collections.sort(matchList, new Comparator<StreetNameMatch>() {
				// Sorts lowest penalty first.
				@Override
				public int compare(StreetNameMatch o1, StreetNameMatch o2) {
					return o1.penalty - o2.penalty;
				}
			});
		}
		// now figure out which combination of matches has the best score
		// without allowing the same name to be used for more than one match
		// this is necessary to handle cases where multiple input street names
		// match to the same street name at the intersection
		// always prefer to match something for every name, if possible
		StreetNameMatchCombination bestCombo = findBestStreetNameMatchCombo(streetNameMatches,
				new StreetNameMatch[streetNameMatches.length]);
		for(StreetNameMatch nameMatch : bestCombo.selections) {
			match.addFaults(nameMatch.faults);
		}
		for(int missingNames = 0; missingNames < (intersection.getPrimaryStreetNames().size() - streetNames.length); missingNames++) {
			// TODO: figure out which names didn't match
			match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.STREET_NAME, "notMatched"));
		}
		if(input.getLocalityName() == null || input.getLocalityName().isEmpty()) {
			match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.LOCALITY, "missing"));
		} else {
			// check to see if the input locality name maps to the locality
			// of the match
			MisspellingOf<LocalityMapTarget> bestMlm = null;
			Collection<MisspellingOf<LocalityMapTarget>> lms = datastore.getLocalities(input.getLocalityName());
			for(MisspellingOf<LocalityMapTarget> mlm : lms) {
				LocalityMapTarget lm = mlm.get();
				// search for the BEST alias, amongst all possible
				for(Locality loc : intersection.getLocalities()) {
					if(lm.getLocality().equals(loc) && (bestMlm == null || lm.getConfidence() > bestMlm.get().getConfidence())) {
						bestMlm = mlm;
					}
				}
			}
			if(bestMlm != null) {
				if(bestMlm.get().getConfidence() < 100) {
					// the penalty for an alias is variable based on
					// the confidence of the alias
					match.addFault(datastore.getConfig().getLocalityAliasFault(input.getLocalityName(), bestMlm.get().getConfidence()));
				}
				if(bestMlm.getError() > 0) {
					match.addFault(datastore.getConfig().getMatchFault(input.getLocalityName(), MatchElement.LOCALITY, "partialMatch"));
				}
			} else {
				// no locality matches at all
				match.addFault(datastore.getConfig().getMatchFault(input.getLocalityName(), MatchElement.LOCALITY, "notMatched"));
			}
		}
		
		if(input.getStateProvTerr() == null || input.getStateProvTerr().isEmpty()) {
			match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.PROVINCE, "missing"));
		} else if(!GeocoderUtil.equalsIgnoreCaseNullSafe(input.getStateProvTerr(), match
				.getAddress().getStateProvTerr())) {
			match.addFault(datastore.getConfig().getMatchFault(input.getStateProvTerr(), MatchElement.PROVINCE, "notMatched"));
		}
	}
	
	private int compareStreetNames(StreetName inputSn, StreetName matchSn, List<MatchFault> faults) {
		// first match the name body
		if(inputSn.getBody() == null || matchSn.getBody() == null) {
			return -1;
		}
		boolean bodyMatched = false;
		if(inputSn.getBody().equalsIgnoreCase(datastore.mapWords(matchSn.getBody()))) {
			bodyMatched = true;
		} else if(StringUtils.OSADistanceWithLimit(inputSn.getBody(),
				matchSn.getBody(), 1) <= 1) {
			faults.add(datastore.getConfig().getMatchFault(inputSn.getBody(), MatchElement.STREET_NAME, "spelledWrong"));
			bodyMatched = true;
		}
		
		// name body doesn't match, so this isn't a match.
		if(!bodyMatched) {
			return -1;
		}
		// name body matched, now lets check the direction, type and qualifier
		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(inputSn.getDir(), matchSn.getDir())) {
			if(inputSn.getDir() == null || inputSn.getDir().isEmpty()) {
				faults.add(datastore.getConfig().getMatchFault(null, MatchElement.STREET_DIRECTION, "missing"));
			} else {
				if("hwy".equalsIgnoreCase(matchSn.getType())) {
					faults.add(datastore.getConfig().getMatchFault(inputSn.getDir(), MatchElement.STREET_DIRECTION, "notMatchedInHighway"));
				} else {
					faults.add(datastore.getConfig().getMatchFault(inputSn.getDir(), MatchElement.STREET_DIRECTION, "notMatched"));
				}
			}
		}
		if(matchSn.getIsStreetDirPrefix() != null) {
			if(inputSn.getIsStreetDirPrefix() && !matchSn.getIsStreetDirPrefix()) {
				faults.add(datastore.getConfig().getMatchFault(inputSn.getDir(), MatchElement.STREET_DIRECTION, "notPrefix"));
			} else if(!inputSn.getIsStreetDirPrefix() && matchSn.getIsStreetDirPrefix()) {
				faults.add(datastore.getConfig().getMatchFault(inputSn.getDir(), MatchElement.STREET_DIRECTION, "notSuffix"));
			}
		}

		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(inputSn.getType(), matchSn.getType())) {
			if(inputSn.getType() == null || inputSn.getType().isEmpty()) {
				faults.add(datastore.getConfig().getMatchFault(inputSn.getType(), MatchElement.STREET_TYPE, "missing"));
			} else {
				faults.add(datastore.getConfig().getMatchFault(inputSn.getType(), MatchElement.STREET_TYPE, "notMatched"));
			}
		}
		if(matchSn.getIsStreetTypePrefix() != null) {
			if(inputSn.getIsStreetTypePrefix() && !matchSn.getIsStreetTypePrefix()) {
				faults.add(datastore.getConfig().getMatchFault(inputSn.getType(), MatchElement.STREET_TYPE, "notPrefix"));
			} else if(!inputSn.getIsStreetTypePrefix() && matchSn.getIsStreetTypePrefix()) {
				faults.add(datastore.getConfig().getMatchFault(inputSn.getType(), MatchElement.STREET_TYPE, "notSuffix"));
			}
		}

		if(!GeocoderUtil.equalsIgnoreCaseNullSafe(inputSn.getQual(),
				matchSn.getQual())) {
			if(inputSn.getQual() == null || inputSn.getQual().isEmpty()) {
				faults.add(datastore.getConfig().getMatchFault(null, MatchElement.STREET_QUALIFIER, "missing"));
			} else {
				faults.add(datastore.getConfig().getMatchFault(inputSn.getQual(), MatchElement.STREET_QUALIFIER, "notMatched"));
			}
		}
		int penalty = 0;
		for(MatchFault f : faults) {
			penalty += f.getPenalty();
		}
		return penalty;
	}
	
	private StreetNameMatchCombination findBestStreetNameMatchCombo(
			List<StreetNameMatch>[] streetNameMatches, StreetNameMatch[] selections) {
		// loop over all of the streetNames that have yet to have a match
		// selected
		StreetNameMatchCombination bestCombo = new StreetNameMatchCombination(
				null, Integer.MAX_VALUE);
		for(int i = 0; i < selections.length; i++) {
			if(selections[i] == null) {
				// pick a match for this name, that has not already been used
				for(StreetNameMatch possibleMatch : streetNameMatches[i]) {
					// check that it isn't in use
					boolean used = false;
					for(StreetNameMatch previousMatch : selections) {
						if(previousMatch != null
								&& possibleMatch.name == previousMatch.name) {
							used = true;
							break;
						}
					}
					if(!used) {
						selections[i] = possibleMatch;
						break;
					}
				}
				// if we didn't find a selection
				if(selections[i] == null) {
					// TODO: figure out which name didn't match
					// No unused name for this one, assign it a "no-match"
					selections[i] = new StreetNameMatch(datastore.getConfig().getMatchFault(
							null, MatchElement.STREET_NAME, "notMatched"));
				}
				StreetNameMatchCombination combo = findBestStreetNameMatchCombo(streetNameMatches,
						selections);
				if(combo.totalPenalty < bestCombo.totalPenalty) {
					bestCombo = combo;
				}
				// remove the selected match before trying another
				selections[i] = null;
			}
		}
		if(bestCombo.selections != null) {
			return bestCombo;
		}
		// if we get here, all selections have been made, this is the base case
		// for the recursion
		// calculate the score for the selections
		int totalPenalty = 0;
		for(StreetNameMatch match : selections) {
			totalPenalty += match.penalty;
		}
		return new StreetNameMatchCombination(selections.clone(), totalPenalty);
	}
	
	private void globalScoring(GeocodeMatch match, AddressComponentMisspellings misspellings) {
		// check for autoComplete
		String misspelling = misspellings.wasAutoCompleted();
		if(misspelling != null) {
			match.addFault(datastore.getConfig().getMatchFault(misspelling, MatchElement.ADDRESS, "autoCompleted"));
		}
		// check for too many faults
		int faultCount = 0;
		for(MatchFault fault : match.getFaults()) {
			if(fault.getPenalty() > 0) {
				faultCount++;
			}
		}
		if(faultCount >= 5) {
			match.addFault(datastore.getConfig().getMatchFault(null, MatchElement.FAULTS, "tooMany"));
		}
	}
	
	private AddressParser createParser(Lexer lexer) {
		AddressParserGenerator parserGen = new AddressParserGenerator();
		
		// overall rule, strips postal junk off the end first
		parserGen.addRule(new RuleSequence("addressWithPossibleJunk", false,
				new RuleTerm[] {
						new RuleTerm("address"),
						new RuleTerm("postalJunk", RuleOperator.OPTION)}));
		
		// our main rule - accepts several types of addresses
		parserGen.addRule(new RuleChoice("address", false,
				new RuleTerm[] {
						new RuleTerm("civicAddress"),
						new RuleTerm("occSepCivicAddress"),
						new RuleTerm("nonCivicAddress"),
						new RuleTerm("occSepNonCivicAddress"),
						new RuleTerm("streetAddress"),
						new RuleTerm("localityAddress"),
						new RuleTerm("stateProvTerr"),
						new RuleTerm("intersectionAddress")}));
		
		parserGen.addRule(new RuleChoice("civicAddress", false,
				new RuleTerm[] {
						new RuleTerm("civicAddressGarbageFirst"),
						new RuleTerm("civicAddressUnitFirst"),
						new RuleTerm("civicAddressUnitFirstNoSiteName"),
						new RuleTerm("civicAddressUnitAfterSiteName"),
						new RuleTerm("civicAddressUnitAfterStreetName")}));

		parserGen.addRule(new RuleSequence("occSepCivicAddress", false,
				new RuleTerm[] {
						new RuleTerm("occupantName"),
						new RuleTerm("OCCUPANT_SEPARATOR"),
						new RuleTerm("civicAddress")}));

		// civic address with optional garbage first, then unit, no site name or front gate
		parserGen.addRule(new RuleSequence("civicAddressGarbageFirst", false,
				new RuleTerm[] {
						new RuleTerm("initialGarbage", "garbageWord", RuleOperator.STAR),
						new RuleTerm("unitDescription", RuleOperator.OPTION),
						new RuleTerm("civicNumberDescription"),
						new RuleTerm("streetDescription"),
						new RuleTerm("localityTail")}));

		// civic address with unit first, then site name and front gate
		parserGen.addRule(new RuleSequence("civicAddressUnitFirst", false,
				new RuleTerm[] {
						new RuleTerm("unitDescription"),
						new RuleTerm("siteNameWithFrontGate"),
						new RuleTerm("civicNumberDescription"),
						new RuleTerm("streetDescription"),
						new RuleTerm("localityTail")}));
		
		// civic address with unit and front gate first, no site name
		parserGen.addRule(new RuleSequence("civicAddressUnitFirstNoSiteName", false,
				new RuleTerm[] {
						new RuleTerm("unitDescription"),
						new RuleTerm("FRONT_GATE"),
						new RuleTerm("civicNumberDescription"),
						new RuleTerm("streetDescription"),
						new RuleTerm("locality", RuleOperator.OPTION),
						new RuleTerm("stateProvTerr", RuleOperator.OPTION)}));

		// civic address with site name, then unit and front gate 
		parserGen.addRule(new RuleSequence("civicAddressUnitAfterSiteName", false,
				new RuleTerm[] {
						new RuleTerm("siteName"),
						new RuleTerm("unitDescription"),
						new RuleTerm("FRONT_GATE"),
						new RuleTerm("civicNumberDescription"),
						new RuleTerm("streetDescription"),
						new RuleTerm("locality", RuleOperator.OPTION),
						new RuleTerm("stateProvTerr", RuleOperator.OPTION)}));

		// civic address with the unit number optionally after the street name/type
		parserGen.addRule(new RuleSequence("civicAddressUnitAfterStreetName", false,
				new RuleTerm[] {
						new RuleTerm("siteNameWithFrontGate", RuleOperator.OPTION),
						new RuleTerm("civicNumberDescription"),
						new RuleTerm("streetDescription"),
						new RuleTerm("unitDescriptionWithDesignator", RuleOperator.OPTION),
						new RuleTerm("localityTail")}));
		
		parserGen.addRule(new RuleSequence("nonCivicAddress", false,
				new RuleTerm[] {
						new RuleTerm("unitDescription", RuleOperator.OPTION),
						new RuleTerm("siteNameWithFrontGate"),
						new RuleTerm("streetDescription", RuleOperator.OPTION),
						new RuleTerm("optionalLocalityTailNoGarbage")}));
		
		parserGen.addRule(new RuleSequence("occSepNonCivicAddress", false,
				new RuleTerm[] {
						new RuleTerm("occupantName"),
						new RuleTerm("OCCUPANT_SEPARATOR"),
						new RuleTerm("unitDescription", RuleOperator.OPTION),
						new RuleTerm("siteNameWithFrontGate", RuleOperator.OPTION),
						new RuleTerm("streetDescription", RuleOperator.OPTION),
						new RuleTerm("optionalLocalityTailNoGarbage")}));

		parserGen.addRule(new RuleSequence("streetAddress", false,
				new RuleTerm[] {
						new RuleTerm("streetDescription"),
						new RuleTerm("localityTail"),}));

		parserGen.addRule(new RuleChoice("optionalLocalityTailNoGarbage", false,
				new RuleTerm[] {
						new RuleTerm("localityTailNoGarbage"),
						new RuleTerm("stateProvTerr", RuleOperator.OPTION)}));

		parserGen.addRule(new RuleChoice("localityTail", false,
				new RuleTerm[] {
						new RuleTerm("localityTailNoGarbage"),
						new RuleTerm("localityTailWithGarbage"),
						new RuleTerm("stateProvTerr", RuleOperator.OPTION)}));

		parserGen.addRule(new RuleSequence("localityTailNoGarbage", false,
				new RuleTerm[] {
						new RuleTerm("locality"),
						new RuleTerm("sptTail")}));

		parserGen.addRule(new RuleSequence("localityTailWithGarbage", false,
				new RuleTerm[] {
						new RuleTerm("localityGarbage", "garbage"),
						new RuleTerm("locality"),
						new RuleTerm("sptTail")}));
		
		parserGen.addRule(new RuleChoice("sptTail", false,
				new RuleTerm[] {
						new RuleTerm("stateProvTerr", RuleOperator.OPTION),
						new RuleTerm("sptTailWithGarbage")}));

		parserGen.addRule(new RuleSequence("sptTailWithGarbage", false,
				new RuleTerm[] {
						new RuleTerm("provinceGarbage", "garbage"),
						new RuleTerm("stateProvTerr")}));

		parserGen.addRule(new RuleSequence("localityAddress", false,
				new RuleTerm[] {
						new RuleTerm("localityInitialGarbage", "garbage", RuleOperator.OPTION),
						new RuleTerm("locality"),
						new RuleTerm("stateProvTerr", RuleOperator.OPTION)}));		

		parserGen.addRule(new RuleSequence("intersectionAddress", false,
				new RuleTerm[] {
						new RuleTerm("streetDescription"),
						new RuleTerm("intersectionDescription"),
						new RuleTerm("intersectionDescription", RuleOperator.STAR),
						new RuleTerm("locality", RuleOperator.OPTION),
						new RuleTerm("stateProvTerr", RuleOperator.OPTION)}));
		
		parserGen.addRule(new RuleSequence("intersectionDescription", false,
				new RuleTerm[] {
						new RuleTerm("intersectionSeparator"),
						new RuleTerm("streetDescription")}));
		
		parserGen.addRule(new RuleChoice("unitDescription", false,
				new RuleTerm[] {
						new RuleTerm("unitDescriptionWithDesignator"),
						new RuleTerm("unitNumberDescription")}));
		
		parserGen.addRule(new RuleChoice("unitDescriptionWithDesignator", false,
				new RuleTerm[] {
						new RuleTerm("unitDescriptionWithDesignatorFirst"),
						new RuleTerm("unitDescriptionWithDesignatorLast")}));
		
		parserGen.addRule(new RuleSequence("unitDescriptionWithDesignatorFirst", false,
				new RuleTerm[] {
						new RuleTerm("unitDesignator"),
						new RuleTerm("unitNumberDescription", RuleOperator.OPTION)}));
		
		parserGen.addRule(new RuleSequence("unitDescriptionWithDesignatorLast", false,
				new RuleTerm[] {
						new RuleTerm("unitNumberDescription"),
						new RuleTerm("floor")}));
		
		parserGen.addRule(new RuleSequence("unitNumberDescription", false,
				new RuleTerm[] {
						new RuleTerm("unitNumber"),
						new RuleTerm("unitNumberSuffix", RuleOperator.OPTION)}));

		parserGen.addRule(new RuleChoice("unitNumber", true,
				new RuleTerm[] {
						new RuleTerm("unitNumberLetterFirst"),
						new RuleTerm("NUMBER")}));

		parserGen.addRule(new RuleSequence("unitNumberLetterFirst", false,
				new RuleTerm[] {
						new RuleTerm("LETTER"),
						new RuleTerm("NUMBER", RuleOperator.OPTION)}));

		parserGen.addRule(new RuleSequence("civicNumberDescription", false,
				new RuleTerm[] {
						new RuleTerm("civicNumber"),
						new RuleTerm("civicNumberSuffix", RuleOperator.OPTION)}));
		
		parserGen.addRule(new RuleSequence("streetDescription", false,
				new RuleTerm[] {
						new RuleTerm("streetDescriptionWithOutQualifier"),
						new RuleTerm("streetQualifier", RuleOperator.OPTION)}));
		
		parserGen.addRule(new RuleChoice("streetDescriptionWithOutQualifier", false,
				new RuleTerm[] {
						new RuleTerm("streetDesc1"),
						new RuleTerm("streetDesc2"),
						new RuleTerm("streetDesc3"),
						new RuleTerm("streetDesc4"),
						new RuleTerm("streetDesc5"),
						new RuleTerm("streetDesc6")}));
		
		parserGen.addRule(new RuleSequence("streetDesc1", false,
				new RuleTerm[] {
						new RuleTerm("streetName"),
						new RuleTerm("streetPostType", RuleOperator.OPTION),
						new RuleTerm("streetPostDirection", RuleOperator.OPTION)}));
		
		parserGen.addRule(new RuleSequence("streetDesc2", false,
				new RuleTerm[] {
						new RuleTerm("streetName"),
						new RuleTerm("streetPostDirection"),
						new RuleTerm("streetPostType")}));
		
		parserGen.addRule(new RuleSequence("streetDesc3", false,
				new RuleTerm[] {
						new RuleTerm("streetPreType"),
						new RuleTerm("streetName"),
						new RuleTerm("streetPostDirection", RuleOperator.OPTION)}));
		
		parserGen.addRule(new RuleSequence("streetDesc4", false,
				new RuleTerm[] {
						new RuleTerm("streetPreType"),
						new RuleTerm("streetPreDirection"),
						new RuleTerm("streetName")}));
		
		parserGen.addRule(new RuleSequence("streetDesc5", false,
				new RuleTerm[] {
						new RuleTerm("streetPreDirection"),
						new RuleTerm("streetPreType"),
						new RuleTerm("streetName")}));
		
		parserGen.addRule(new RuleSequence("streetDesc6", false,
				new RuleTerm[] {
						new RuleTerm("streetPreDirection"),
						new RuleTerm("streetName"),
						new RuleTerm("streetPostType", RuleOperator.OPTION)}));
		
		// Base Symbols - refer to wordClasses
		parserGen.addRule(new RuleTerm("unitDesignator", "UNIT_DESIGNATOR"));
		
		parserGen.addRule(new RuleTerm("unitNumberSuffix", "LETTER"));
		parserGen.addRule(new RuleTerm("civicNumber", "NUMBER"));
		parserGen.addRule(new RuleTerm("civicNumberSuffix", "SUFFIX"));
		parserGen.addRule(new RuleTerm("streetPreType", "STREET_TYPE"));
		parserGen.addRule(new RuleTerm("streetPostType", "STREET_TYPE"));
		parserGen.addRule(new RuleSequence("streetPreDirection", true,
				new RuleTerm[] {
						new RuleTerm("STREET_DIRECTIONAL"),
						new RuleTerm("STREET_DIRECTIONAL", RuleOperator.OPTION)}));
		parserGen.addRule(new RuleSequence("streetPostDirection", true,
				new RuleTerm[] {
						new RuleTerm("STREET_DIRECTIONAL"),
						new RuleTerm("STREET_DIRECTIONAL", RuleOperator.OPTION)}));
		
		parserGen.addRule(new RuleTerm("streetQualifier", "STREET_QUALIFIER"));
		
		parserGen.addRule(new RuleTerm("intersectionSeparator", "AND"));
		parserGen.addRule(new RuleTerm("floor", "FLOOR"));
		
		parserGen.addRule(new RuleChoice("streetName", true,
				new RuleTerm[] {
						new RuleTerm("streetNameWords"),
						new RuleTerm("streetNameNumberAndOrdinal")}));
		
		parserGen.addRule(new RuleSequence("streetNameWords", false,
				new RuleTerm[] {
						new RuleTerm("STREET_NAME_BODY"),
						new RuleTerm("STREET_NAME_BODY", RuleOperator.STAR)}));

		parserGen.addRule(new RuleSequence("streetNameNumberAndOrdinal", false,
				new RuleTerm[] {
						new RuleTerm("NUMBER"),
						new RuleTerm("ordinal", "ORDINAL")})); // by giving this term a label it escapes the "streetName" label and is ignored

		parserGen.addRule(new RuleSequence("locality", true,
				new RuleTerm[] {
						new RuleTerm("LOCALITY_NAME"),
						new RuleTerm("LOCALITY_NAME", RuleOperator.STAR)}));
		
		parserGen.addRule(new RuleSequence("siteNameWithFrontGate", false,
				new RuleTerm[] {
						new RuleTerm("siteName"),
						new RuleTerm("FRONT_GATE")}));
		
		parserGen.addRule(new RuleSequence("siteName", true,
				new RuleTerm[] {
						new RuleTerm("NAME"),
						new RuleTerm("NAME", RuleOperator.STAR)}));

		parserGen.addRule(new RuleSequence("occupantName", true,
				new RuleTerm[] {
						new RuleTerm("NAME"),
						new RuleTerm("NAME", RuleOperator.STAR)}));

		parserGen.addRule(new RuleSequence("garbage", false,
				new RuleTerm[] {
						new RuleTerm("UNRECOGNIZED"),
						new RuleTerm("garbageWord", RuleOperator.STAR)}));
		
		parserGen.addRule(new RuleChoice("garbageWord", false,
				new RuleTerm[] {
						new RuleTerm("AND"),
						new RuleTerm("UNRECOGNIZED")}));
		
		parserGen.addRule(new RuleTerm("stateProvTerr", "STATE_PROV_TERR"));
		parserGen.addRule(new RuleTerm("postalJunk", "POSTAL_ADDRESS_ELEMENT"));
		
		parserGen.setLexer(lexer);
		return parserGen.getParser();
	}
	
	@Override
	public GeocoderDataStore getDatastore() {
		return datastore;
	}
	
	/**
	 * This should probably be private but made public so it's easier to test
	 * 
	 * @return the AddressParser for the geocoder
	 */
	public AddressParser getParser() {
		return parser;
	}
		
}

class StreetNameMatch {
	StreetName name;
	int penalty;
	List<MatchFault> faults;
	
	StreetNameMatch(StreetName name, int penalty, List<MatchFault> faults) {
		this.name = name;
		this.penalty = penalty;
		this.faults = faults;
	}
	
	/**
	 * For creating a "No-Match"
	 */
	StreetNameMatch(MatchFault fault) {
		name = null;
		faults = new ArrayList<MatchFault>(1);
		faults.add(fault);
		penalty = fault.getPenalty();
	}
	
	@Override
	public String toString() {
		return name.toString() + "(-" + penalty + ")";
	}
}

class StreetNameMatchCombination {
	StreetNameMatch[] selections;
	int totalPenalty;
	
	StreetNameMatchCombination(StreetNameMatch[] selections, int totalPenalty) {
		this.selections = selections;
		this.totalPenalty = totalPenalty;
	}
}