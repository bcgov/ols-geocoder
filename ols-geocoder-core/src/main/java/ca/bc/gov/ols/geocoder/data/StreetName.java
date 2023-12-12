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
package ca.bc.gov.ols.geocoder.data;

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.ols.geocoder.data.indexing.BlockFaceIntervalTree;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;
import ca.bc.gov.ols.util.ArrayPairedList;
import ca.bc.gov.ols.util.PairedList;

/**
 * StreetName represents the complete name of a street in the geocoder data using three components:
 * body, type, and direction.
 * 
 * In addition, it holds a BlockFaceInterval tree of all the BlockFaces which have this street name,
 * in order to provide a quick way to lookup the correct segment for a given street name and civic
 * address number.
 * 
 * @author chodgson
 * 
 */
public class StreetName {
	private final String body; // store the punctuated body here
	private final String type;
	private final String dir;
	private final String qual;
	private final Boolean typeIsPrefix;
	private final Boolean dirIsPrefix;
	private PairedList<Locality, double[]> localityCentroids;
	private final BlockFaceIntervalTree blocks;
	
	public StreetName(String body, String type, String dir, String qual,
			boolean typeIsPrefix, boolean dirIsPrefix, BlockFaceIntervalTree blocks) {
		this.body = body;
		this.type = type;
		this.dir = dir;
		this.qual = qual;
		this.typeIsPrefix = typeIsPrefix;
		this.dirIsPrefix = dirIsPrefix;
		this.blocks = blocks;
		localityCentroids = new ArrayPairedList<Locality, double[]>();
	}
	
	@Override
	public String toString() {
		return (dir != null && dirIsPrefix ? dir + " " : "")
				+ (type != null && typeIsPrefix ? type + " " : "")
				+ body
				+ (type != null && !typeIsPrefix ? " " + type : "")
				+ (dir != null && !dirIsPrefix ? " " + dir : "")
				+ (qual != null ? " " + qual : "");
	}
	
	public List<BlockFace> getBlocks(int addr) {
		if(blocks != null) {
			return blocks.query(addr);
		}
		return new ArrayList<BlockFace>(0);
	}
	
	public String getBody() {
		return body;
	}
	
	public String getType() {
		return type;
	}
	
	public String getDir() {
		return dir;
	}
	
	public String getQual() {
		return qual;
	}
	
	public void addLocalityCentroid(Locality locality, double[] coords) {
		localityCentroids.add(locality, coords);
	}
	
	public PairedList<Locality, double[]> getLocalityCentroids() {
		return localityCentroids;
	}
	
	public Boolean getIsStreetTypePrefix() {
		return typeIsPrefix;
	}
	
	public Boolean getIsStreetDirPrefix() {
		return dirIsPrefix;
	}
	
	public void trimToSize() {
		localityCentroids.trimToSize();
	}
	
	public boolean nameEquals(StreetName otherName) {
		if(GeocoderUtil.equalsIgnoreCaseNullSafe(body, otherName.body)
				&& GeocoderUtil.equalsIgnoreCaseNullSafe(type, otherName.type)
				&& GeocoderUtil.equalsIgnoreCaseNullSafe(dir, otherName.dir)
				&& GeocoderUtil.equalsIgnoreCaseNullSafe(qual, otherName.qual)) {
			return true;
		}
		return false;
	}
	
}
