package ca.bc.gov.ols.geocoder.data.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
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
	
	public List<GeocodeMatch> query(GeocodeQuery query){
		if(!sorted) build();
		String addressString = query.getAddressString();
		List<GeocodeMatch> results = new ArrayList<GeocodeMatch>(query.getMaxResults() + 1);
		int pos = Collections.binarySearch(table, new StringOnlyGeocodeMatch(addressString), GeocodeMatch.ADDRESS_STRING_COMPARATOR);
		pos = Math.abs(pos) - 1;
		for(int i = pos; results.size() < query.getNumPrelimResults(); i++) {
			GeocodeMatch match = table.get(i);
			if(match.getAddressString().substring(0, addressString.length()).equalsIgnoreCase(addressString)) {
				if(query.pass(match)) {
					results.add(match.copy()); // need to clone the matches so the reference data doesn't get polluted by later steps
				}
			} else {
				break;
			}
		}
		return results;

	}

}
