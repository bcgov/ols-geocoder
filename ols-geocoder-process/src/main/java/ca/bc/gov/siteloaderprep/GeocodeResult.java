package ca.bc.gov.siteloaderprep;

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
	StreetNamePrep name;
	Integer localityId;
	double executionTime;
}
