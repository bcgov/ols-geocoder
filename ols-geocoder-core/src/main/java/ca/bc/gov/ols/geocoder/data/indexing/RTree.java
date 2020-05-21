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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.filters.Filter;

class IndexableEnvelope extends Envelope implements SpatiallyIndexable {

	private static final long serialVersionUID = 1L;

	public IndexableEnvelope() {
		super();
	}

	@Override
	public Envelope getEnvelope() {
		return this;
	}

	@Override
	public double distance(Point p) {
		return super.distance(p.getEnvelopeInternal());
	}
	
}
public class RTree<T extends SpatiallyIndexable> {

	private static final int PAGE_SIZE = 10;
	
	private SpatiallyIndexable[] nodes;

	public RTree(Collection<T> items) {
		int height = (int)Math.ceil(Math.log10(items.size())/Math.log10(PAGE_SIZE));
		int numBranches = (int)Math.round((Math.pow(PAGE_SIZE, height) - 1) / (PAGE_SIZE - 1));
		nodes = new SpatiallyIndexable[numBranches+items.size()];
		int i = 0;
		for(T item : items) {
			nodes[numBranches + i] = item;
			i++;
		}
		int left = numBranches;
		int right = nodes.length-1;
		while(left > 0) {
			medianSplit(left, right);
			right = left-1;
			left = (left-1) / PAGE_SIZE;
		}
	}

	// unfiltered query just passes an anonymous always-pass filter
	public List<T> search(Point query, int nResults, Integer maxDistance) {
		return search(query, nResults, maxDistance,
				new Filter<T>() {
					@Override
					public boolean pass(T item) {
						return true;
					}
				});
	}
	
	public List<T> search(Point query, int nResults, Integer maxDistance,
			Filter<? super T> filter) {
		return queueToOrderedList(searchInternal(query, nResults, maxDistance, filter));
	}
	
	@SuppressWarnings("unchecked")
	private Queue<PrioNode<T>> searchInternal(
			Point query, Integer nResults, Integer maxDistance, Filter<? super T> filter) {
		double maxDist;
		if(maxDistance == null) {
			maxDist = Double.POSITIVE_INFINITY;
		} else {
			maxDist = maxDistance;
		}
		
		final Queue<PrioNode<T>> results = new PriorityQueue<PrioNode<T>>(nResults+1,
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
		final PriorityQueue<PrioNode<Integer>> q = new PriorityQueue<PrioNode<Integer>>();
		q.add(new PrioNode<Integer>(nodes[0].distance(query), 0));
		while(!q.isEmpty()) {
			PrioNode<Integer> prioNode = q.poll();
			int node = prioNode.item;
			double dist = prioNode.priority;

			// if this node is at least as close as the max distance
			// AND either we don't have the max results yet 
			//	OR this node is closer than the current worst result
			if(dist <= maxDist && (results.size() < nResults || dist < results.peek().priority)) {
				if((node * PAGE_SIZE) + 1 < nodes.length) {
					// this is a branch node
					// add all children that are within the maxDist 
					for(int i = (node*PAGE_SIZE)+1; i <= (node+1)*PAGE_SIZE && i < nodes.length; i++) {
						if(nodes[i] != null) {
							dist = nodes[i].distance(query);
							if(dist <= maxDist) {
								q.add(new PrioNode<Integer>(dist, i));
							}
						}
					}
				} else {
					// must be a leaf node of Type T
					// if the item passes whatever filter parameters we have
					if(filter.pass((T)nodes[node])) {
						// keep the queue from outgrowing the result limit
						// by popping the farthest results off
						while(results.size() >= nResults) {
							results.poll();
						}
						results.offer(new PrioNode<T>(dist, (T)nodes[node]));
					}
				}
			}
		}
		return results;
	}
	
	private void medianSplit(int left, int right) {
		IndexableEnvelope union = merge_all_boxes(left, right);
		
		// if we have a finished page
		if(right - left < PAGE_SIZE ) {
			// put the union envelope at the parent node
			nodes[(left-1)/PAGE_SIZE] = union;
			return;
		}
		
		Comparator<SpatiallyIndexable> cmp;
		if( (union.getMaxX() - union.getMinX()) < (union.getMaxY() - union.getMinY()) ) {
			cmp = AxisSorter.Y;
		} else {
			cmp = AxisSorter.X;
		}
		
		int leftSize = (int) (Math.ceil( ((double)(right - left)) / (2 * PAGE_SIZE) ) * PAGE_SIZE);
		divide(left+leftSize, left, right, cmp);
		medianSplit(left, left + leftSize - 1);
		medianSplit(left + leftSize, right);
	}
	
	private IndexableEnvelope merge_all_boxes(int left, int right) {
		IndexableEnvelope union = new IndexableEnvelope();
		for(int i = left; i <= right; i++) {
			if(nodes[i] == null) break;
			union.expandToInclude(nodes[i].getEnvelope());	
		}
		return union;
	}
	
	private void divide(int k, int left, int right, Comparator<SpatiallyIndexable> cmp) {
		while( true ) {
			int pivotIndex = left + (int)(Math.floor(Math.random() * (right - left + 1)));
			int pivotNewIndex = partition(left, right, pivotIndex, cmp);
			if(k == pivotNewIndex) {
				return;
			} else if(k < pivotNewIndex) {
				right = pivotNewIndex - 1;
			} else {
				left = pivotNewIndex + 1;
			}
		}
	}
	
	private int partition(int left, int right, int pivotIndex, Comparator<SpatiallyIndexable> cmp ) {
		SpatiallyIndexable pivotItem = nodes[pivotIndex];
		// Move pivotItem to end
		SpatiallyIndexable tmp = pivotItem;
		nodes[pivotIndex] = nodes[right];
		nodes[right] = tmp;
		int storeIndex = left;
		for(int i = left; i < right; i++) {
			if( cmp.compare( nodes[i], pivotItem ) <= 0 ) {
				tmp = nodes[i];
				nodes[i] = nodes[storeIndex];
				nodes[storeIndex] = tmp;
				storeIndex++;
			}
		}
		// Move pivot to its final place
		tmp = nodes[storeIndex];
		nodes[storeIndex] = nodes[right];
		nodes[right] = tmp;
		return storeIndex;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		buildString(0, sb, "");
		return sb.toString();
	}
	
	private void buildString(int node, StringBuilder sb, String indent) {
		sb.append(indent + nodes[node].toString() + "\n");
		for(int child = (node * PAGE_SIZE) + 1; child <= (node + 1) * PAGE_SIZE; child++) {
			if(child >= nodes.length
					|| nodes[child] == null) {
				break;
			}
			buildString(child, sb, indent + "  ");
		}
	}
	
	private List<T> queueToOrderedList(Queue<PrioNode<T>> queue) {
		List<T> list = new ArrayList<T>(queue.size());
		while(!queue.isEmpty()) {
			list.add(queue.remove().item);
		}
		Collections.reverse(list);
		return list;
	}
	
}
