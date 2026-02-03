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
package ca.bc.gov.ols.geocoder.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.Interpolation;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.filters.Filter;
import ca.bc.gov.ols.geocoder.filters.MultiFilter;
import ca.bc.gov.ols.util.GeomParseUtil;

/**
 * GeocodeQuery stores the parameters of a geocode query.
 * 
 * It also provides some common methods for converting input into useful parameter values.
 * 
 * @author chodgson@refractions.net
 * 
 */
public class GeocodeQuery extends SharedParameters{
	
	private String addressString;
	private String siteName;
	private String unitNumber;
	private String unitNumberSuffix;
	private String unitDesignator;
	private String civicNumber;
	private String civicNumberSuffix;
	private String streetName;
	private String streetType;
	private String streetDirection;
	private String streetQualifier;
	private String localityName;
	private String stateProvTerr;
	
	private int minScore = 0;
	private EnumSet<MatchPrecision> matchPrecision = null;
	private EnumSet<MatchPrecision> matchPrecisionNot = null;
	private EnumSet<MatchPrecision> matchPrecisionFilter = null;
	private List<String> localities = null;
	private List<String> notLocalities = null;
	private double[] centre;
	private Point centrePoint;
	private int maxDistance;
	private double[] bbox;
	private Polygon bboxPolygon;
	private double[] parcelPoint;
	private Point parcelPointPoint;
	private String yourId;
	private Interpolation interpolation = Interpolation.ADAPTIVE;
	private boolean echo = true;
	private boolean extrapolate = false;
	private long executionTime = 0;
	private Filter<GeocodeMatch> filter = null;
	private boolean includeOccupants = false;
	private boolean autoComplete = false;
	private boolean exactSpelling = false;
	private boolean fuzzyMatch = false;

	private boolean onlyAsciiNames = false;

	public GeocodeQuery() {
		setMaxResults(1);
	}
	
	public GeocodeQuery(String addressString) {
		this.addressString = addressString;
	}
	
	public String getAddressString() {
		return addressString;
	}
	
	public String getQueryAddress() {
		if(addressString != null) {
			return addressString;
		}
		StringBuilder sb = new StringBuilder(1024);
		appendPart(sb, " ", unitDesignator);
		appendPart(sb, " ", unitNumber);
		if(unitNumberSuffix != null) {
			if(unitNumberSuffix.equals("1/2")) {
				appendPart(sb, " ", unitNumberSuffix);
			} else {
				appendPart(sb, "", unitNumberSuffix);
			}
		}
		appendPart(sb, " ", siteName);
		if(sb.length() > 0) {
			// add front gate onto end of site name
			sb.append(" -- ");
		}
		appendPart(sb, " ", civicNumber);
		if(civicNumberSuffix != null) {
			if(civicNumberSuffix.equals("1/2")) {
				appendPart(sb, " ", civicNumberSuffix);
			} else {
				appendPart(sb, "", civicNumberSuffix);
			}
		}
		appendPart(sb, " ", streetName);
		appendPart(sb, " ", streetType);
		appendPart(sb, " ", streetDirection);
		appendPart(sb, " ", streetQualifier);
		appendPart(sb, ", ", localityName);
		appendPart(sb, ", ", stateProvTerr);
		return sb.toString();
	}
	
	private void appendPart(StringBuilder sb, String preSeparator, Object part) {
		if(part != null && !"".equals(part)) {
			if(sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
				sb.append(preSeparator);
			}
			sb.append(part);
		}
	}
	
	public void setAddressString(String addressString) {
		this.addressString = addressString;
	}
	
	public String getSiteName() {
		return siteName;
	}
	
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	
	public String getUnitNumber() {
		return unitNumber;
	}
	
	public void setUnitNumber(String unitNumber) {
		this.unitNumber = unitNumber;
	}
	
	public String getUnitNumberSuffix() {
		return unitNumberSuffix;
	}
	
	public void setUnitNumberSuffix(String unitNumberSuffix) {
		this.unitNumberSuffix = unitNumberSuffix;
	}
	
	public String getUnitDesignator() {
		return unitDesignator;
	}
	
	public void setUnitDesignator(String unitDesignator) {
		this.unitDesignator = unitDesignator;
	}
	
	public String getCivicNumber() {
		return civicNumber;
	}
	
	public void setCivicNumber(String civicNumber) {
		this.civicNumber = civicNumber;
	}
	
	public String getCivicNumberSuffix() {
		return civicNumberSuffix;
	}
	
	public void setCivicNumberSuffix(String civicNumberSuffix) {
		this.civicNumberSuffix = civicNumberSuffix;
	}
	
	public String getStreetName() {
		return streetName;
	}
	
	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}
	
	public String getStreetType() {
		return streetType;
	}
	
	public void setStreetType(String streetType) {
		this.streetType = streetType;
	}
	
	public String getStreetDirection() {
		return streetDirection;
	}
	
	public void setStreetDirection(String streetDirection) {
		this.streetDirection = streetDirection;
	}
	
	public String getStreetQualifier() {
		return streetQualifier;
	}
	
	public void setStreetQualifier(String streetQualifier) {
		this.streetQualifier = streetQualifier;
	}
	
	public String getLocalityName() {
		return localityName;
	}
	
	public void setLocalityName(String localityName) {
		this.localityName = localityName;
	}
	
	public String getStateProvTerr() {
		return stateProvTerr;
	}
	
	public void setStateProvTerr(String stateProvTerr) {
		this.stateProvTerr = stateProvTerr;
	}
	
	public void setProvince(String province) {
		this.stateProvTerr = province;
	}

	public void setProvinceCode(String provinceCode) {
		this.stateProvTerr = provinceCode;
	}
	
	public void setInterpolation(Interpolation interpolation) {
		this.interpolation = interpolation;
	}
	
	public Interpolation getInterpolation() {
		return interpolation;
	}
	
	public String getYourId() {
		return yourId;
	}
	
	public void setYourId(String yourId) {
		this.yourId = yourId;
	}
	
	public int getMinScore() {
		return minScore;
	}
	
	public void setMinScore(int minScore) {
		// Clamp
		if(minScore < 0) {
			minScore = 0;
		}
		if(minScore > 100) {
			minScore = 100;
		}
		this.minScore = minScore;
	}
	
	private EnumSet<MatchPrecision> buildMatchPrecisionFilter() {
		if((matchPrecision == null || matchPrecision.isEmpty())
				&& (matchPrecisionNot == null || matchPrecisionNot.isEmpty())) {
			return null;
		}
		EnumSet<MatchPrecision> matchPrecisionFilter;
		if(matchPrecision == null || matchPrecision.isEmpty()) {
			matchPrecisionFilter = EnumSet.allOf(MatchPrecision.class);
		} else {
			matchPrecisionFilter = EnumSet.noneOf(MatchPrecision.class);
			matchPrecisionFilter.addAll(matchPrecision);
		}
		if(matchPrecisionNot != null) {
			matchPrecisionFilter.removeAll(matchPrecisionNot);
		}
		return matchPrecisionFilter;
	}
	
	public EnumSet<MatchPrecision> getMatchPrecision() {
		return matchPrecision;
	}

	public void setMatchPrecision(EnumSet<MatchPrecision> matchPrecision) {
		this.matchPrecision = matchPrecision;
	}
	
	public EnumSet<MatchPrecision> getMatchPrecisionNot() {
		return matchPrecisionNot;
	}

	public void setMatchPrecisionNot(EnumSet<MatchPrecision> matchPrecisionNot) {
		this.matchPrecisionNot = matchPrecisionNot;
	}
	
	public List<String> getLocalities() {
		return localities;
	}
	
	public void setLocalities(List<String> localities) {
		this.localities = new ArrayList<String>(localities.size());
		for(String locality : localities) {
			this.localities.add(locality.toLowerCase());
		}
	}
	
	public List<String> getNotLocalities() {
		return notLocalities;
	}
	
	public void setNotLocalities(List<String> notLocalities) {
		this.notLocalities = new ArrayList<String>(notLocalities.size());
		for(String locality : notLocalities) {
			this.notLocalities.add(locality.toLowerCase());
		}
	}
	
	public Point getCentre() {
		return centrePoint;
	}
	
	public void setCentre(double[] centre) {
		this.centre = centre;
	}
	
	public void setCenter(double[] center) {
		if(this.centre == null) {
			this.centre = center;
		}
	}
	
	public int getMaxDistance() {
		return maxDistance;
	}
	
	public void setMaxDistance(int maxDistance) {
		if(maxDistance < 0) {
			maxDistance = 0;
		}
		this.maxDistance = maxDistance;
	}
	
	public Polygon getBbox() {
		return bboxPolygon;
	}
	
	public void setBbox(double[] bbox) {
		this.bbox = bbox;
	}
	
	public Point getParcelPoint() {
		return parcelPointPoint;
	}
	
	public void setParcelPoint(double[] parcelPoint) {
		this.parcelPoint = parcelPoint;
	}
	
	public void setParcelPointGeom(Point parcelPointPoint) {
		this.parcelPointPoint = parcelPointPoint;
	}
	
	public boolean isEcho() {
		return echo;
	}
	
	public void setEcho(boolean echo) {
		this.echo = echo;
	}
	
	public boolean isExtrapolate() {
		return extrapolate;
	}
	
	public void setExtrapolate(boolean extrapolate) {
		this.extrapolate = extrapolate;
	}
	
	public void setIncludeOccupants(boolean includeOccupants) {
		this.includeOccupants = includeOccupants;
	}

	public boolean getIncludeOccupants() {
		return includeOccupants;
	}
	
	public boolean getAutoComplete() {
		return autoComplete;
	}
	
	public void setAutoComplete(boolean autoComplete) {
		this.autoComplete = autoComplete;
	}

	public boolean isFuzzyMatch() {
		return fuzzyMatch;
	}

	public void setFuzzyMatch(boolean fuzzyMatch) {
		this.fuzzyMatch = fuzzyMatch;
	}

	public boolean getExactSpelling() {
		return exactSpelling;
	}
	
	public void setExactSpelling(boolean exactSpelling) {
		this.exactSpelling = exactSpelling;
	}

	public int getNumPrelimResults() {
		if(fuzzyMatch) {
			return 100;
		}
		return getMaxResults() + 1;
	}

	public boolean getOnlyAsciiNames() {
		return onlyAsciiNames;
	}

	public void setOnlyAsciiNames(boolean onlyAsciiNames) {
		this.onlyAsciiNames = onlyAsciiNames;
	}


	public boolean pass(GeocodeMatch match) {
		if(filter == null) {
			filter = buildFilter();
		}
		return filter.pass(match);
	}
	
	@SuppressWarnings("unchecked")
	private Filter<GeocodeMatch> buildFilter() {
		List<Filter<GeocodeMatch>> filters = new ArrayList<Filter<GeocodeMatch>>();
		filters.add(new Filter<GeocodeMatch>() {
			@Override
			public boolean pass(GeocodeMatch match) {
				if(match.getScore() >= minScore) {
					return true;
				}
				return false;
			}
		});
		if(matchPrecisionFilter != null) {
			filters.add(new Filter<GeocodeMatch>() {
				@Override
				public boolean pass(GeocodeMatch match) {
					if(matchPrecisionFilter.contains(match.getPrecision())) {
						return true;
					}
					return false;
				}
			});
		}
		if(tags != null && !tags.isEmpty()) {
			filters.add(new Filter<GeocodeMatch>() {
				@Override
				public boolean pass(GeocodeMatch match) {
					if(match instanceof AddressMatch 
							&& ((AddressMatch)match).getAddress() instanceof OccupantAddress
							&& ((OccupantAddress)(((AddressMatch)match).getAddress())).getKeywordList()
									.containsAll(Arrays.asList(tags.toLowerCase().split(";")))
							) {
						return true;
					}
					return false;
				}
			});
		}
		if(localities != null) {
			filters.add(new Filter<GeocodeMatch>() {
				@Override
				public boolean pass(GeocodeMatch match) {
					if(match.getLocalityName() != null && localities.contains(match.getLocalityName().toLowerCase())) {
						return true;
					}
					return false;
				}
			});
		}
		if(notLocalities != null) {
			filters.add(new Filter<GeocodeMatch>() {
				@Override
				public boolean pass(GeocodeMatch match) {
					if(match.getLocalityName() == null || !notLocalities.contains(match.getLocalityName().toLowerCase())) {
						return true;
					}
					return false;
				}
			});
		}
		if(centrePoint != null && maxDistance != 0) {
			filters.add(new Filter<GeocodeMatch>() {
				@Override
				public boolean pass(GeocodeMatch match) {
					if(match.getLocation().distance(centrePoint) <= maxDistance) {
						return true;
					}
					return false;
				}
			});
		}
		if(bboxPolygon != null) {
			filters.add(new Filter<GeocodeMatch>() {
				@Override
				public boolean pass(GeocodeMatch match) {
					if(bboxPolygon.contains(match.getLocation())) {
						return true;
					}
					return false;
				}
			});
		}
		return new MultiFilter<GeocodeMatch>(filters.toArray(new Filter[filters.size()]));
	}
	
	public void resolveAndValidate(GeocoderConfig config, GeometryFactory gf, GeometryReprojector gr) {
		// convert any double[] into geometries in inputSRS projection
		// then reproject any geometries to the internal projection
		// note the incoming geomtries may either come in as double[]
		// or as geometry objects in the inputSRS projection
		// so we may not need to create them from double[] but still need to reproject
		if(parcelPoint != null) {
			if(parcelPoint.length == 2) {
				parcelPointPoint = gf.createPoint(new Coordinate(parcelPoint[0], parcelPoint[1]));
			} else {
				throw new IllegalArgumentException("Parameter must be in the format \"x,y\".");
			}
		}
		if(parcelPointPoint != null) {
			parcelPointPoint = gr.reproject(parcelPointPoint, config.getBaseSrsCode());
		}
		if(centre != null) {
			if(centre.length == 2) {
				centrePoint = gf.createPoint(new Coordinate(centre[0], centre[1]));
			} else {
				throw new IllegalArgumentException("Parameter must be in the format \"x,y\".");
			}
		}
		if(centrePoint != null) {
			centrePoint = gr.reproject(centrePoint, config.getBaseSrsCode());
		}
		if(bbox != null) {
			if(bbox.length == 4) {
				bboxPolygon = GeomParseUtil.buildBbox(bbox, gf);
			} else {
				throw new IllegalArgumentException("Parameter must be in the format \"x,y\".");
			}
		}
		if(bboxPolygon != null) {
			bboxPolygon = gr.reproject(bboxPolygon, config.getBaseSrsCode());
		}
		
		// validate parameters
		if((bbox != null && (centre != null || maxDistance > 0))
				|| (centre != null && maxDistance <= 0)
				|| (centre == null && maxDistance > 0)) {
			throw new IllegalArgumentException(
					"You may specify only one spatial filter, either bbox or centre and maxDistance");
		}
		
		// build the matchPrecision filter based on the matchPrecision/not parameters
		matchPrecisionFilter = buildMatchPrecisionFilter();
	}
	
	@Override
	public String toString() {
		return "GeocodeQuery: " + getQueryAddress()
				+ " setBack=" + getSetBack()
				+ " minScore=" + minScore
				+ " maxResults=" + getMaxResults()
				+ " matchPrecisionFilter=" + matchPrecisionFilter
				+ " localitiesFilter=" + localities
				+ " notLocalitiesFilter=" + notLocalities
				+ " yourId=" + yourId
				+ " interpolation=" + interpolation
				+ " echo=" + echo
				+ " locationDescriptor=" + locationDescriptor
				+ " extrapolate=" + extrapolate;
	}
	
	public void startTimer() {
		executionTime = System.nanoTime();
	}
	
	public void stopTimer() {
		executionTime = System.nanoTime() - executionTime;
	}
	
	public long getExecutionTimeNanos() {
		return executionTime;
	}
	
}
