package ca.bc.gov.ols.siteloaderprep;

import java.util.List;

import ca.bc.gov.ols.geocoder.api.data.MatchFault;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;

public class GeocodeResult {
	String addressString;
	int sequenceNumber;
	String yourId;
	String fullAddress;
	int score;
	MatchPrecision matchPrecision;
	int precisionPoints;
	List<MatchFault> faults;
	RawStreetName name;
	Integer localityId;
	double executionTime;
}
