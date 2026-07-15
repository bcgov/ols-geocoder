package ca.bc.gov.ols.geocoder.status;

import ca.bc.gov.ols.geocoder.api.GeocodeQuery;

public class QueryLog implements Comparable<QueryLog>{

	public final String addressString;
	public final long executionTimeNanos;
	
	public QueryLog(GeocodeQuery query) {
		addressString = query.getQueryAddress();
		executionTimeNanos = query.getExecutionTimeNanos();
	}

	public int compareTo(QueryLog other) {
		return Long.compare(executionTimeNanos, other.executionTimeNanos);
	}

}
