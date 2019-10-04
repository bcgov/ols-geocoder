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
package ca.bc.gov.ols.geocoder.api.data;

import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.data.AccessPoint;
import ca.bc.gov.ols.geocoder.data.BlockFace;
import ca.bc.gov.ols.geocoder.data.CivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.ISite;
import ca.bc.gov.ols.geocoder.data.NonCivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.ResultCivicAccessPoint;
import ca.bc.gov.ols.geocoder.data.StreetName;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PhysicalStatus;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class SiteAddress extends GeocoderAddress {
	
	private String unitNumber;
	private String unitNumberSuffix;
	private String unitDesignator;
	private Integer civicNumber;
	private String civicNumberSuffix;
	private String streetName;
	private String streetType;
	private Boolean isStreetTypePrefix;
	private String streetDirection;
	private Boolean isStreetDirectionPrefix;
	private String streetQualifier;
	@XmlTransient
	private String parentSiteDescriptor;
	private String siteID; // note: this is the UUID
	private Integer SID; // note: this is the ID
	private String parentSiteID; // note: this is the UUID
	private Integer streetSegmentID; // aka blockID
	private String siteName;
	private String narrativeLocation;
	
	private boolean isPrimary = true;
	private PhysicalStatus siteStatus;
	private LocalDate siteRetireDate;
	private LocalDate siteChangeDate;
	
	@XmlTransient
	private Integer accessPointId;
	
	public SiteAddress() {
		
	}
	
	// copy constructor does a shallow copy except for the location point
	public SiteAddress(SiteAddress base) {
		super(base);
		unitNumber = base.unitNumber;
		unitNumberSuffix = base.unitNumberSuffix;
		unitDesignator = base.unitDesignator;
		civicNumber = base.civicNumber;
		civicNumberSuffix = base.civicNumberSuffix;
		streetName = base.streetName;
		streetType = base.streetType;
		isStreetTypePrefix = base.isStreetTypePrefix;
		streetDirection = base.streetDirection;
		isStreetDirectionPrefix = base.isStreetDirectionPrefix;
		streetQualifier = base.streetQualifier;
		parentSiteDescriptor = base.parentSiteDescriptor;
		siteID = base.siteID;
		SID = base.SID;
		parentSiteID = base.parentSiteID;
		streetSegmentID = base.streetSegmentID;
		siteName = base.siteName;
		narrativeLocation = base.narrativeLocation;
		isPrimary = base.isPrimary;
		siteStatus = base.siteStatus;
		siteRetireDate = base.siteRetireDate;
		siteChangeDate = base.siteChangeDate;
		accessPointId = base.accessPointId;	
	}
	
	public SiteAddress(ISite site, AccessPoint ap) {
		siteID = site.getUuid().toString();
		SID = site.getId();
		unitDesignator = site.getUnitDesignator();
		unitNumber = site.getUnitNumber();
		unitNumberSuffix = site.getUnitNumberSuffix();
		siteName = site.getSiteName();
		parentSiteDescriptor = site.getParentSiteDescriptor();
		if(ap == null) {
			ap = site.getPrimaryAccessPoint();
		}
		if(ap instanceof CivicAccessPoint) {
			CivicAccessPoint cap = (CivicAccessPoint)ap;
			civicNumber = cap.getCivicNumber();
			civicNumberSuffix = cap.getCivicNumberSuffix();
			BlockFace face = cap.getBlockFace();
			setStreetName(face.getSegment().getPrimaryStreetName());
			setLocality(face.getLocality());
			setElectoralArea(face.getElectoralArea());
			streetSegmentID = face.getSegment().getSegmentId();
		} else if(ap instanceof NonCivicAccessPoint){
			NonCivicAccessPoint ncap = (NonCivicAccessPoint)ap;
			if(ncap.getStreetSegment() != null) {
				setStreetName(ncap.getStreetSegment().getPrimaryStreetName());
				streetSegmentID = ncap.getStreetSegment().getSegmentId();
			}
			setLocality(ncap.getLocality());
			setElectoralArea(ncap.getElectoralArea());
		}
		setLocation(site.getLocation());
		setLocationDescriptor(site.getLocationDescriptor());
		setLocationPositionalAccuracy(site.getLocationPositionalAccuracy());
	}
	
	public String getUnitNumber() {
		return unitNumber;
	}
	
	public void setUnitNumber(String unitNumber) {
		addressString = null;
		this.unitNumber = unitNumber;
	}
	
	public String getUnitNumberSuffix() {
		return unitNumberSuffix;
	}
	
	public void setUnitNumberSuffix(String unitNumberSuffix) {
		addressString = null;
		this.unitNumberSuffix = unitNumberSuffix;
	}
	
	public String getUnitDesignator() {
		return unitDesignator;
	}
	
	public void setUnitDesignator(String unitDesignator) {
		addressString = null;
		this.unitDesignator = unitDesignator;
	}
	
	public Integer getCivicNumber() {
		return civicNumber;
	}
	
	public void setCivicNumber(Integer civicNumber) {
		addressString = null;
		this.civicNumber = civicNumber;
	}
	
	public String getCivicNumberSuffix() {
		return civicNumberSuffix;
	}
	
	public void setCivicNumberSuffix(String civicNumberSuffix) {
		addressString = null;
		this.civicNumberSuffix = civicNumberSuffix;
	}
	
	public String getStreetName() {
		return streetName;
	}
	
	public void setStreetName(StreetName streetName) {
		addressString = null;
		this.streetName = streetName.getBody();
		this.streetType = streetName.getType();
		this.streetDirection = streetName.getDir();
		this.streetQualifier = streetName.getQual();
		this.isStreetTypePrefix = streetName.getIsStreetTypePrefix();
		this.isStreetDirectionPrefix = streetName.getIsStreetDirPrefix();
	}
	
	public void setStreetName(String streetName) {
		addressString = null;
		this.streetName = streetName;
	}
	
	public String getStreetType() {
		return streetType;
	}
	
	public void setStreetType(String streetType) {
		addressString = null;
		this.streetType = streetType;
	}
	
	public Boolean isStreetTypePrefix() {
		return isStreetTypePrefix;
	}
	
	public void setStreetTypePrefix(Boolean isStreetTypePrefix) {
		addressString = null;
		this.isStreetTypePrefix = isStreetTypePrefix;
	}
	
	public String getStreetDirection() {
		return streetDirection;
	}
	
	public void setStreetDirection(String streetDirection) {
		addressString = null;
		this.streetDirection = streetDirection;
	}
	
	public Boolean isStreetDirectionPrefix() {
		return isStreetDirectionPrefix;
	}
	
	public void setStreetDirectionPrefix(Boolean isStreetDirectionPrefix) {
		addressString = null;
		this.isStreetDirectionPrefix = isStreetDirectionPrefix;
	}
	
	public String getStreetQualifier() {
		return streetQualifier;
	}
	
	public void setStreetQualifier(String streetQualifier) {
		addressString = null;
		this.streetQualifier = streetQualifier;
	}
	
	public void setParentSiteDescriptor(String parentSiteDescriptor) {
		this.parentSiteDescriptor = parentSiteDescriptor;
	}
	
	@XmlElement
	public String getFullSiteDescriptor() {
		StringBuilder sb = new StringBuilder(100);
		appendPart(sb, " ", unitDesignator);
		appendPart(sb, " ", unitNumber);
		if(unitNumberSuffix != null) {
			if(unitNumberSuffix.equals("1/2")) {
				appendPart(sb, " ", unitNumberSuffix);
			} else {
				appendPart(sb, "", unitNumberSuffix);
			}
		}
		appendPart(sb, " ", siteName);
		appendPart(sb, ", ", parentSiteDescriptor);
		return sb.toString();
	}
	
	/*
	 * Do not use; fullSiteDescriptor is a combination of other fields. This setter only exists to
	 * make JAXB happy.
	 */
	public void setFullSiteDescriptor(String fullSiteDescriptor) {
		// required for JAXB
	}
	
	/**
	 * Returns the sitename of this site, plus the fullSiteDescriptor of the parent site. Used to
	 * score the "full" sitename of a matched site against the parsed input siteName.
	 * 
	 * @return the sitename of this site, prepended to the fullSiteDescriptor of the parent site
	 */
	public String getFullSiteName() {
		StringBuilder sb = new StringBuilder();
		appendPart(sb, " ", siteName);
		appendPart(sb, ", ", parentSiteDescriptor);
		return sb.toString();
	}
	
	@XmlElement
	public String getAddressString() {
		if(addressString == null) {
			addressString = buildAddressString();
		}
		return addressString;
	}
	
	protected String buildStreetAddressString() {
		StringBuilder sb = new StringBuilder(1024);
		appendPart(sb, " ", GeocoderUtil.formatCivicNumber(civicNumber));
		if(civicNumberSuffix != null) {
			if(civicNumberSuffix.equals("1/2")) {
				appendPart(sb, " ", civicNumberSuffix);
			} else {
				appendPart(sb, "", civicNumberSuffix);
			}
		}
		if(Boolean.TRUE == isStreetDirectionPrefix) {
			appendPart(sb, " ", streetDirection);
		}
		if(Boolean.TRUE == isStreetTypePrefix) {
			appendPart(sb, " ", streetType);
		}
		appendPart(sb, " ", streetName);
		if(Boolean.TRUE != isStreetTypePrefix) {
			appendPart(sb, " ", streetType);
		}
		if(Boolean.TRUE != isStreetDirectionPrefix) {
			appendPart(sb, " ", streetDirection);
		}
		appendPart(sb, " ", streetQualifier);
		appendPart(sb, ", ", getLocalityName());
		appendPart(sb, ", ", getStateProvTerr());
		return sb.toString();
	}
	
	protected String buildAddressString() {
		StringBuilder sb = new StringBuilder(1024);
		appendPart(sb, " ", getFullSiteDescriptor());
		if(sb.length() > 0) {
			// add front gate onto end of site name
			sb.append(" -- ");
		}
		appendPart(sb, " ", buildStreetAddressString());
		return sb.toString();
	}
	
	protected void appendPart(StringBuilder sb, String preSeparator, Object part) {
		if(part != null && !"".equals(part)) {
			if(sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
				sb.append(preSeparator);
			}
			sb.append(part);
		}
	}
	
	/**
	 * Do not use; AddressString is a combination of other fields. This setter only exists to make
	 * JAXB happy.
	 */
	public void setAddressString() {
		// required for JAXB
	}
	
	public String getSiteID() {
		return siteID;
	}
	
	public void setSiteID(String siteID) {
		this.siteID = siteID;
	}

	public Integer getSID() {
		return SID;
	}
	
	public void setSID(Integer SID) {
		this.SID = SID;
	}

	public String getParentSiteID() {
		return parentSiteID;
	}
	
	public void setParentSiteUUID(String parentSiteID) {
		this.parentSiteID = parentSiteID;
	}
	
	public String getSiteName() {
		return siteName;
	}
	
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	
	public String getNarrativeLocation() {
		return narrativeLocation;
	}
	
	public void setNarrativeLocation(String narrativeLocation) {
		this.narrativeLocation = narrativeLocation;
	}
	
	public boolean isPrimary() {
		return isPrimary;
	}
	
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
	
	@XmlElement
	public int getSrsCode() {
		if(getLocation() == null) {
			return 0;
		}
		return getLocation().getSRID();
	}
	
	/*
	 * Do not use; srsCode is stored within the geometry itself This setter only exists to make JAXB
	 * happy.
	 */
	public void setSrsCode(int srsCode) {
		// required for JAXB
	}
	
	public PhysicalStatus getSiteStatus() {
		return siteStatus;
	}
	
	public void setSiteStatus(PhysicalStatus siteStatus) {
		this.siteStatus = siteStatus;
	}
	
	public LocalDate getSiteRetireDate() {
		return siteRetireDate;
	}
	
	public void setSiteRetireDate(LocalDate siteRetireDate) {
		this.siteRetireDate = siteRetireDate;
	}
	
	public LocalDate getSiteChangeDate() {
		return siteChangeDate;
	}
	
	public void setSiteChangeDate(LocalDate siteChangeDate) {
		this.siteChangeDate = siteChangeDate;
	}
	
	public Integer getStreetSegmentID() {
		return streetSegmentID;
	}
	
	public void setStreetSegmentID(Integer streetSegmentID) {
		this.streetSegmentID = streetSegmentID;
	}
	
	public Integer getAccessPointId() {
		return accessPointId;
	}
	
	public void setAccessPointId(Integer accessPointId) {
		this.accessPointId = accessPointId;
	}
	
	@Override
	public String toString() {
		return getAddressString();
	}
	
	public void resolveLocation(GeocoderDataStore datastore, ISite site, LocationDescriptor ld,
			int setBack) {
		AccessPoint ap = site.getPrimaryAccessPoint();
		if(ap instanceof CivicAccessPoint) {
			CivicAccessPoint cap = (CivicAccessPoint)ap;
			BlockFace face = ((CivicAccessPoint)ap).getBlockFace();
			resolveLocation(datastore, site, null, ld, new ResultCivicAccessPoint(cap,
					datastore.getInterpolator().applyOffset(cap.getPoint(), face, setBack),
					cap.getPositionalAccuracy(), false), face, false, 0);
			// site.setPrimaryAccessPoint(new CivicAccessPoint(cap,
			// datastore.getInterpolator().applyOffset(cap.getPoint(), face, setBack),
			// cap.getPositionalAccuracy()));
			return;
		} else {
			// must be a NonCivicAccessPoint
			// NonCivicAccessPoint ncap = (NonCivicAccessPoint)ap;
			if(LocationDescriptor.ACCESS_POINT.equals(ld)) {
				setLocation(ap.getPoint());
				setLocationDescriptor(LocationDescriptor.ACCESS_POINT);
				setLocationPositionalAccuracy(ap.getPositionalAccuracy());
			} else {
				// the query must be requesting one of the site points, we can only
				// return whatever it is that we have (rooftop, parcel, frontDoor, etc)
				setLocation(site.getLocation());
				setLocationDescriptor(site.getLocationDescriptor());
				setLocationPositionalAccuracy(site.getLocationPositionalAccuracy());
			}
		}
	}
	
	public void resolveLocation(GeocoderDataStore datastore, ISite site, Point parcelPoint,
			LocationDescriptor ld, AccessPoint ap, BlockFace face, boolean extrapolate,
			int setBack) {
		if(extrapolate && ((site != null && site.getLocation() != null) || parcelPoint != null)) {
			if(LocationDescriptor.ROUTING_POINT.equals(ld)) {
				setLocationDescriptor(LocationDescriptor.ROUTING_POINT);
				setBack = -1;
			} else {
				setLocationDescriptor(LocationDescriptor.ACCESS_POINT);
			}
			if(parcelPoint == null) {
				parcelPoint = site.getLocation();
			}
			Point extrPoint = datastore.getInterpolator().extrapolate(
					parcelPoint, face, setBack);
			setLocation(extrPoint);
			setLocationPositionalAccuracy(PositionalAccuracy.MEDIUM);
		} else if(LocationDescriptor.ACCESS_POINT.equals(ld)) {
			setLocation(ap.getPoint());
			setLocationDescriptor(LocationDescriptor.ACCESS_POINT);
			setLocationPositionalAccuracy(ap.getPositionalAccuracy());
		} else if(LocationDescriptor.ROUTING_POINT.equals(ld) && face != null) {
			// we need to calculate the routingPoint as interpolator.interpolate(...) will return
			// the accessPoint - offset -1 to find a point on centerline
			Point routingPoint = datastore.getInterpolator().extrapolate(
					ap.getPoint(), face, -1);
			setLocation(routingPoint);
			setLocationDescriptor(LocationDescriptor.ROUTING_POINT);
			setLocationPositionalAccuracy(ap.getPositionalAccuracy());
		} else if(site != null
				&& !LocationDescriptor.ACCESS_POINT.equals(ld)) {
			// the query must be requesting one of the site points, we can only
			// return whatever it is that we have (rooftop, parcel, frontDoor, etc)
			setLocation(site.getLocation());
			setLocationDescriptor(site.getLocationDescriptor());
			setLocationPositionalAccuracy(site.getLocationPositionalAccuracy());
		} else {
			// either the query was requesting the access point,
			// or a site point but we have no site so we fall back to the access point
			setLocation(ap.getPoint());
			setLocationDescriptor(LocationDescriptor.ACCESS_POINT);
			setLocationPositionalAccuracy(ap.getPositionalAccuracy());
		}
	}
	
}
