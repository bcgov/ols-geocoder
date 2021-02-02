package ca.bc.gov.ols.geocoder.data.indexing;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class UniquePriorityQueue<T> {

	private final int max;
	private final Comparator<T> c;
	private final PriorityQueue<T> q;
	private final HashMap<T,T> m;
	
	public UniquePriorityQueue(int max, Comparator<T> c) {
		this.max = max;
		this.c = c;
		q = new PriorityQueue<T>(max, c);
		m = new HashMap<T,T>(max);
	}
	
	public void add(T item) {
		T existing = m.get(item);
		// if the item being added is a duplicate
		if(existing != null) {
			// if the new item has a higher score
			if(c.compare(existing, item) < 0) {
				// remove the old item
				q.remove(existing);
				m.remove(existing);
			} else {
				return;
			}
		}
		if(q.size() < max || c.compare(item, q.peek()) > 0) {
			q.add(item);
			m.put(item, item);
		}
		if (q.size() > max) {
			m.remove(q.poll());
		}
	}

	public void clear() {
		q.clear();
		m.clear();
	}
	
	public int size() {
		return q.size();
	}
	
	public boolean isEmpty() {
		return q.isEmpty();
	}
	
	public T poll() {
		T item = q.poll();
		if(item != null) {
			m.remove(item);
		}
		return item;
	}
}
