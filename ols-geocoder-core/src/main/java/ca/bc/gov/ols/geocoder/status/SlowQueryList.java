package ca.bc.gov.ols.geocoder.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;

import ca.bc.gov.ols.geocoder.api.GeocodeQuery;

public class SlowQueryList {
	private PriorityQueue<QueryLog> q;
	private int size = 10;
	
	public SlowQueryList(int size) {
		this.size = size;
		q = new PriorityQueue<QueryLog>(size + 1);
	}
	
	public synchronized void offer(GeocodeQuery query) {
		if(q.size() == size && query.getExecutionTimeNanos() > q.peek().executionTimeNanos) {
            q.poll(); // remove least slow query, new one is slower
		}
        if (q.size() < size) {
            q.add(new QueryLog(query)); // we removed one or are not full, so add
        }
	}
	
	public synchronized QueryLog[] get() {
		QueryLog[] logs = (QueryLog[]) q.toArray(new QueryLog[q.size()]);
		Arrays.sort(logs, Collections.reverseOrder());
		return logs;
	}
}
