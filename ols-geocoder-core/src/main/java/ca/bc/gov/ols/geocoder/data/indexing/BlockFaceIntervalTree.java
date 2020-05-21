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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ca.bc.gov.ols.geocoder.data.BlockFace;

/**
 * BlockFaceIntervalTree is a implementation of a Centered Interval Tree specifically for handling
 * block face address intervals. It is intended to be used to index all of the block faces for a
 * given street name. Because it is unlikely that there is significant overlapping between block
 * ranges, the list of faces overlapping the center of the interval is not sorted. Extra care has
 * been taken to minimize memory usage.
 * 
 * @author chodgson
 * 
 */
public class BlockFaceIntervalTree implements Iterable<BlockFace> {
	
	/* The value that this portion of the tree is centered on */
	private int center;
	
	/*
	 * Intervals overlapping the center are just put into a list here. Due to the structure of
	 * addresses we don't expect there to be very many so sorting them (as per standard Interval
	 * tree) is probably overkill.
	 */
	private BlockFace[] overlaps;
	
	/* Intervals entirely to the left of center go here */
	private BlockFaceIntervalTree left;
	
	/* Intervals entirely to the right of center go here */
	private BlockFaceIntervalTree right;
	
	/**
	 * Construct a new BlockFaceIntervalTree from the given list of faces.
	 * 
	 * @param faces the faces to put into the tree.
	 */
	public BlockFaceIntervalTree(List<BlockFace> faces) {
		int min = faces.get(0).getMin();
		int max = faces.get(0).getMax();
		for(BlockFace f : faces) {
			if(f.getMin() < min) {
				min = f.getMin();
			}
			if(f.getMax() > max) {
				max = f.getMax();
			}
		}
		center = (max + min) / 2;
		List<BlockFace> leftList = new ArrayList<BlockFace>();
		List<BlockFace> rightList = new ArrayList<BlockFace>();
		List<BlockFace> overlapsList = new ArrayList<BlockFace>();
		for(BlockFace f : faces) {
			if(f.getMax() < center) {
				leftList.add(f);
			} else if(f.getMin() > center) {
				rightList.add(f);
			} else {
				overlapsList.add(f);
			}
		}
		if(overlapsList.size() > 0) {
			overlaps = overlapsList.toArray(new BlockFace[0]);
		}
		if(leftList.size() > 0) {
			left = new BlockFaceIntervalTree(leftList);
		}
		if(rightList.size() > 0) {
			right = new BlockFaceIntervalTree(rightList);
		}
	}
	
	/**
	 * Get a list of the faces which contain the given address number.
	 * 
	 * @param addr the address number to query for
	 * @return a list of all faces in the tree which contain the given address number.
	 */
	public List<BlockFace> query(int addr) {
		List<BlockFace> result = new ArrayList<BlockFace>();
		if(overlaps != null) {
			for(BlockFace f : overlaps) {
				if(f.contains(addr)) {
					result.add(f);
				}
			}
		}
		
		if(left != null) {
			result.addAll(left.query(addr));
		}
		if(right != null) {
			result.addAll(right.query(addr));
		}
		
		return result;
	}
	
	/**
	 * Returns an iterator which performs an in-order (left-center-right) traversal of this
	 * BlockFaceIntervalTree, returning all of the BlockFaces in the tree, one at a time. The order
	 * of the center-overlapping BlockFaces is indeterminate - in general the ordering of the
	 * BlockFaces returned by this iterator should not be depended upon - it is intended to be used
	 * for "brute force" searching for BlockFaces using aspects other than the address ranges.
	 */
	@Override
	public Iterator<BlockFace> iterator() {
		return new Iterator<BlockFace>() {
			
			private LinkedList<BlockFaceIntervalTree> stack = new LinkedList<BlockFaceIntervalTree>();
			private BlockFaceIntervalTree node;
			private int overlapsIndex = -1;
			private BlockFace next;
			
			{
				node = BlockFaceIntervalTree.this;
				getNext();
			}
			
			private void getNext() {
				// if we are looping over the list of faces in a node and have reached the end
				if(overlapsIndex > 0 && overlapsIndex >= node.overlaps.length) {
					// go to the right node next
					node = node.right;
					overlapsIndex = -1;
				}
				// while we have not found a list of faces
				// and either there is a node on the stack or we have one in hand
				while(overlapsIndex < 0 && (!stack.isEmpty() || node != null)) {
					// if we have a node in hand
					if(node != null) {
						// push it and go left
						stack.push(node);
						node = node.left;
					} else {
						// no node in hand, pop one off the stack
						node = stack.pop();
						// if it has a list
						if(node.overlaps != null) {
							// we'll iterator over it starting at 0
							overlapsIndex = 0;
						} else {
							// no list, so go right
							node = node.right;
						}
					}
				}
				// if we didn't find a list to iterate over
				if(overlapsIndex == -1) {
					// we're done traversing the tree
					next = null;
				} else {
					// we do have a list, return the next one and increment
					next = node.overlaps[overlapsIndex++];
				}
			}
			
			@Override
			public boolean hasNext() {
				return next != null;
			}
			
			@Override
			public BlockFace next() {
				BlockFace face = next;
				getNext();
				return face;
			}
			
			@Override
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	@Override
	public String toString() {
		return toString("");
	}
	
	private String toString(String indent) {
		StringBuffer buf = new StringBuffer();
		buf.append(indent + "Center: " + center + " (" + (overlaps == null ? "0" : overlaps.length)
				+ ")\n");
		buf.append(indent + "Left: "
				+ (left == null ? "empty" : "\n" + left.toString(indent + "  ")) + "\n");
		buf.append(indent + "Right: "
				+ (right == null ? "empty" : "\n" + right.toString(indent + "  ")));
		return buf.toString();
	}
	
}
