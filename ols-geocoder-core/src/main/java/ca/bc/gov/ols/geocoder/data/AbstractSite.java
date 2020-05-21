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

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

import org.locationtech.jts.geom.Point;

public abstract class AbstractSite extends LocationBase implements ISite {
	static final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ AbstractSite.class.getCanonicalName());

	final int id;
	private final UUID uuid;
	private final String pids;

	protected Object parent;
	ArrayList<ISite> children;

	public AbstractSite(int id, UUID uuid, String pids, Object parent, Point location) {
		super(location);
		this.id = id;
		this.uuid = uuid;
		this.pids = pids;
		this.parent = parent;
		children = new ArrayList<ISite>();		
	}

	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public UUID getUuid() {
		return uuid;
	}
	
	@Override
	public String getPids() {
		return pids;
	}

	@Override
	public String getFullSiteDescriptor() {
		StringBuilder sb = new StringBuilder(100);
		appendPart(sb, " ", getUnitDesignator());
		appendPart(sb, " ", getUnitNumber());
		String unitNumberSuffix = getUnitNumberSuffix();
		if(unitNumberSuffix != null) {
			if(unitNumberSuffix.equals("1/2")) {
				appendPart(sb, " ", unitNumberSuffix);
			} else {
				appendPart(sb, "", unitNumberSuffix);
			}
		}
		appendPart(sb, ", ", getSiteName());
		String parentSD = getParentSiteDescriptor();
		appendPart(sb, ", ", parentSD);
		return sb.toString();
	}

	@Override
	public String getParentSiteDescriptor() {
		if(getParent() == null) {
			return null;
		}
		return ((ISite)parent).getFullSiteDescriptor();
	}

	@Override
	public String getFullSiteName() {
		StringBuilder sb = new StringBuilder();
		appendPart(sb, " ", getSiteName());
		appendPart(sb, ", ", getParentSiteDescriptor());
		return sb.toString();
	}

	private void appendPart(StringBuilder sb, String preSeparator, String part) {
		if(part != null && !part.isEmpty()) {
			if(sb.length() > 0) {
				sb.append(preSeparator);
			}
			sb.append(part);
		}
	}

	public void addChild(ISite child) {
		children.add(child);
	}

	@Override
	public void trimToSize() {
		if(children.size() == 0) {
			children = null;
		} else {
			children.trimToSize();
		}
	}

	@Override
	public ISite getParent() {
		return (ISite)parent;
	}

	@Override
	public Integer getParentId() {
		return (Integer)parent;
	}

	@Override
	public List<ISite> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	/**
	 * Recursively checks if this site is a descendant of the given site.
	 * 
	 * @param site a possible ancestor Site
	 * @return true if this site is a descendant of the given site
	 */
	@Override
	public boolean isDescendantOf(ISite site) {
		if(parent == site) {
			return true;
		}
		if(parent != null) {
			return ((ISite)parent).isDescendantOf(site);
		}
		return false;
	}

	@Override
	public List<ISite> findChildrenByUnitNumber(String unitNumber) {
		if(children == null || unitNumber == null || unitNumber.isEmpty()) {
			return Collections.emptyList();
		}
		ArrayList<ISite> matches = new ArrayList<ISite>();
		findChildrenByUnitNumber(unitNumber, matches);
		return matches;
	}

	public void findChildrenByUnitNumber(String unitNumber, ArrayList<ISite> matches) {
		if(children == null) {
			return;
		}
		for(ISite child : children) {
			if(unitNumber.equals(child.getUnitNumber())) {
				matches.add(child);
			}
			child.findChildrenByUnitNumber(unitNumber, matches);
		}
	}

	@Override
	public String toString() {
		String str = getFullSiteDescriptor();
		if(str != null) {
			return str;
		}
		return "Site:" + id;
	}

	@Override
	public void resolveParent(TIntObjectHashMap<ISite> idToSite) {
		if(!isParentResolved()) {
			Integer parentId = (Integer)parent; 
			parent = idToSite.get(parentId);
			if(parent == null) {
				logger.warn("CONSTRAINT VIOLATION: Site id: " + id + " has missing parentId: " + parentId);
			} else {
				((ISite)parent).addChild(this);
			}
		}
	}

	@Override
	public boolean isParentResolved() {
		if(parent instanceof Integer) {
			return false;
		}
		return true;
	}

}