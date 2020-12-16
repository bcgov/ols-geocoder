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
package ca.bc.gov.ols.geocoder.data.indexing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.ILocation;
import ca.bc.gov.ols.geocoder.filters.Filter;

public class KDTree<T extends ILocation> {
	private static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ KDTree.class.getCanonicalName());

	private T item = null;
	private Axis splitAxis;
	private KDTree<T> left = null;
	private KDTree<T> right = null;
	
	public KDTree(List<T> items) {
		this(items, Axis.X);
	}
	
	private KDTree(List<T> items, Axis axis) {
		if(items.size() == 0) {
			return;
		}
		// sorting the entire list is unnecessary
		// Collections.sort(items, new PointLocationComparator(axis));
		// int middle = items.size() >> 1;
		// faster approach is to partition the list using QuickSelect to find the median
		int middle = select(items, 0, items.size() - 1, items.size() >> 1,
				new PointLocationComparator(axis));
		this.item = items.get(middle);
		this.splitAxis = axis;
		if(middle > 0) {
			this.left = new KDTree<T>(items.subList(0, middle), axis.nextAxis());
		}
		if(items.size() > middle + 1) {
			this.right = new KDTree<T>(items.subList(middle + 1, items.size()), axis.nextAxis());
		}
	}
	
	// unfiltered query just passes an anonymous always-pass filter
	public List<T> search(Point query, int nResults, Integer maxDistance) {
		return queueToOrderedList(KDTree.search(this, query, nResults, maxDistance,
				new Filter<T>() {
					@Override
					public boolean pass(T item) {
						return true;
					}
				}));
	}
	
	public List<T> search(Point query, int nResults, Integer maxDistance,
			Filter<? super T> filter) {
		return queueToOrderedList(search(this, query, nResults, maxDistance, filter));
	}
	
	@Override
	public String toString() {
		return toString("");
	}
	
	private String toString(String indent) {
		return indent + item + " " + splitAxis
				+ (left != null || right != null ?
						"\n" + (left == null ? indent + "  null" : left.toString(indent + "  "))
								+ "\n" + (right == null ? indent + "  null"
										: right.toString(indent + "  ")) : "");
	}
	
	private static <T extends ILocation> Queue<PrioNode<T>> search(KDTree<T> tree,
			Point query, Integer nResults, Integer maxDistance, Filter<? super T> filter) {
		double maxDistSq;
		if(maxDistance == null) {
			maxDistSq = Double.POSITIVE_INFINITY;
		} else {
			maxDistSq = sq(maxDistance);
		}
		
		final Queue<PrioNode<T>> results = new PriorityQueue<PrioNode<T>>(nResults,
				new Comparator<PrioNode<T>>() {
					// Java's Priority queue is a min-heap, in that the
					// first item in the sort order is at the top of the heap
					// so this comparator puts the larger distances first,
					// essentially implementing a max-heap
					@Override
					public int compare(PrioNode<T> o1, PrioNode<T> o2) {
						return o1.priority == o2.priority ? 0
								: o1.priority > o2.priority ? -1
										: 1;
					}
				});
		if(tree.item == null) {
			return results;
		}
		final Deque<KDTree<T>> stack = new ArrayDeque<KDTree<T>>();
		stack.addLast(tree);
		while(!stack.isEmpty()) {
			tree = stack.removeLast();
			
			if(tree.left != null || tree.right != null) {
				// Guess nearest tree to query point
				KDTree<T> nearTree = tree.left, farTree = tree.right;
				if(PointLocationComparator.compareAxis(query, tree.item.getLocation(),
						tree.splitAxis) > 0) {
					nearTree = tree.right;
					farTree = tree.left;
				}
				
				// Only search far tree if our search sphere might
				// overlap with splitting plane
				double dist = sq(PointLocationComparator.diffAxis(query, tree.item.getLocation(),
						tree.splitAxis));
				if(farTree != null && dist <= maxDistSq && (results.size() < nResults
						|| dist <= results.peek().priority)) {
					stack.addLast(farTree);
				}
				
				// Always search the nearest branch
				if(nearTree != null) {
					stack.addLast(nearTree);
				}
			}
			final double dSq = distanceSq(query, tree.item.getLocation());
			// if this node is closer than the max distance
			// AND either we don't have the max results OR this node is closer than the current
			// worst result
			// AND the item passes whatever filter parameters we have
			if(dSq < maxDistSq && (results.size() < nResults || dSq < results.peek().priority)
					&& filter.pass(tree.item)) {
				while(results.size() >= nResults) {
					results.poll();
				}
				results.offer(new PrioNode<T>(dSq, tree.item));
			}
		}
		return results;
	}
	
	private static double distanceSq(Point p1, Point p2) {
		return sq(p1.getX() - p2.getX()) + sq(p1.getY() - p2.getY());
	}
	
	private static double sq(double n) {
		return n * n;
	}
	
//	private static <T extends ILocation> int select(
//			List<T> list, int left, int right, int n, Comparator<ILocation> comparator) {
//		// If the list contains only one element
//		if(left == right) {
//			return left;
//		}
//		// pick a random pivot
//		int pivotIndex = left + (int)(Math.floor(Math.random() * (right - left + 1)));
//		pivotIndex = partition(list, left, right, pivotIndex, comparator);
//		// The pivot is in its final sorted position
//		if(n == pivotIndex) {
//			return n;
//		} else if(n < pivotIndex) {
//			return select(list, left, pivotIndex - 1, n, comparator);
//		} else {
//			return select(list, pivotIndex + 1, right, n, comparator);
//		}
//	}
	
	private static <T extends ILocation> int select(
			List<T> list, int left, int right, int n, Comparator<ILocation> comparator) {
		while(true) {
			// If the list contains only one element
			if(left == right) {
				return left;
			}
			// pick a random pivot
			int pivotIndex = left + (int)(Math.floor(Math.random() * (right - left + 1)));
			pivotIndex = partition(list, left, right, pivotIndex, comparator);
			// The pivot is in its final sorted position
			if(n == pivotIndex) {
				return n;
			} else if(n < pivotIndex) {
				right = pivotIndex - 1;
			} else {
				left = pivotIndex + 1;
			}
		}
	}
	
	private static <T extends ILocation> int partition(
			List<T> list, int left, int right, int pivotIndex, Comparator<ILocation> comparator) {
		T pivotValue = list.get(pivotIndex);
		T temp = null;
		list.set(pivotIndex, list.get(right));
		list.set(right, pivotValue);
		int storeIndex = left;
		for(int i = left; i < right; i++) {
			if(comparator.compare(list.get(i), pivotValue) < 0) {
				temp = list.get(storeIndex);
				list.set(storeIndex, list.get(i));
				list.set(i, temp);
				storeIndex++;
			}
		}
		// Move pivot to its final place
		temp = list.get(storeIndex);
		list.set(storeIndex, list.get(right));
		list.set(right, temp);
		return storeIndex;
	}
	
	private static <T extends ILocation> List<T> queueToOrderedList(Queue<PrioNode<T>> queue) {
		List<T> list = new ArrayList<T>(queue.size());
		while(!queue.isEmpty()) {
			list.add(queue.remove().item);
		}
		Collections.reverse(list);
		return list;
	}
	
}
