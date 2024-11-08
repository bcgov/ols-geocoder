package ca.bc.gov.ols.geocoder.data.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.StringOnlyGeocodeMatch;

public class ExactMatchLookup {

	private ArrayList<GeocodeMatch> table;
	private boolean sorted = false;
	
	public ExactMatchLookup(int initialSize) {
		table = new ArrayList<GeocodeMatch>(initialSize);
	}

	public void build() {
		table.trimToSize();
		Collections.sort(table, GeocodeMatch.ADDRESS_STRING_COMPARATOR);
		sorted = true;
	}

	public void add(AddressMatch match) {
		table.add(match);
		sorted = false;
	}
	
	public List<GeocodeMatch> query(String addressString, int maxResults){
		if(!sorted) build();
		List<GeocodeMatch> results = new ArrayList<GeocodeMatch>(maxResults);
		int pos = Collections.binarySearch(table, new StringOnlyGeocodeMatch(addressString), GeocodeMatch.ADDRESS_STRING_COMPARATOR);
		pos = Math.abs(pos) - 1;
		for(int i = pos; i < pos + maxResults && i < table.size(); i++) {
			results.add(table.get(i).copy()); // TODO probably need to clone the matches so the don't get polluted by later steps
		}
		return results;

	}

}
