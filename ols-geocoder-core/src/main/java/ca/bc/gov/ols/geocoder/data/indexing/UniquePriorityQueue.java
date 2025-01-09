package ca.bc.gov.ols.geocoder.data.indexing;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * The UniquePriorityQueue combines a HashMap with a PriorityQueue to
 * keep track of geocoding results ordered by score, while preventing duplicates
 * (keeping the highest scoring version of any equal results).
 * 
 * Also has a max count to prevent using too much memory for results 
 * that will never be returned (low scores fall off the end of the queue).
 * 
 * The Comparator is used for the prioritization/ordering (by "score").
 * 
 * @param <T> Intended to be used with GeocodeResult, but in theory any class with a valid hash function and a comparator can be used  
 */
public class UniquePriorityQueue<T> {

	private final int max;
	private final Comparator<T> c;
	private final PriorityQueue<T> q;
	private final HashMap<T,T> m;
	
	/**
	 * Creates a new UniquePriorityQueue with the give maximum size and comparator.
	 * 
	 * @param max the maximum number of items to store in the queue 
	 * @param c The comparator to use for the prioritization/ordering
	 */
	public UniquePriorityQueue(int max, Comparator<T> c) {
		this.max = max;
		this.c = c;
		q = new PriorityQueue<T>(max, c);
		m = new HashMap<T,T>(max);
	}
	
	/**
	 * Adds an item to the queue. If an "equal()" item had already been added, only
	 * the highest valued item (according to the comparator) is kept.
	 * 
	 * @param item the item to add to the queue
	 */
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

	/**
	 * Clears the queue of all items.
	 */
	public void clear() {
		q.clear();
		m.clear();
	}
	
	/**
	 * @return The number of items in the queue
	 */
	public int size() {
		return q.size();
	}
	
	/**
	 * @return True if the queue is empty, false otherwise
	 */
	public boolean isEmpty() {
		return q.isEmpty();
	}
	
	/**
	 * Pops the lowest scored item (according to the comparator) off of the front of the queue.
	 * 
	 * @return the lowest scoring item in the queue, or null if the queue is empty
	 */
	public T poll() {
		T item = q.poll();
		if(item != null) {
			m.remove(item);
		}
		return item;
	}
}
