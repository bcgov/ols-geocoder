package ca.bc.gov.siteloaderprep;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PhysicalStatus;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.rowreader.RowReader;
import ca.bc.gov.ols.rowreader.RowWriter;

public class OutputSite {
	Integer gId;
	String inputName;
	Integer siteId;
	UUID siteUuid;
	Integer parentSiteId;
	String siteName; 
	LocationDescriptor locationDescriptor;
	String unitDesignator; 
	String unitNumber; 
	String unitNumberSuffix; 
	PositionalAccuracy sitePositionalAccuracy; 
	String siteStatus; 
	LocalDate siteRetireDate; 
	Integer siteAlbersX; 
	Integer siteAlbersY; 
	String apType; 
	boolean isPrimary; 
	String narrativeLocation; 
	PositionalAccuracy accessPositionalAccuracy; 
	Integer civicNumber; 
	String civicNumberSuffix; 
	PhysicalStatus accessPointStatus; 
	LocalDate accessRetireDate;
	Integer accessAlbersX;
	Integer accessAlbersY;
	String fullAddress;
	Integer localityId; 
	Integer interimStreetNameId; 
	Integer rangeType; 
	LocalDate siteChangeDate;
	
	OutputSite(RowReader rr) {
		gId = rr.getInteger("GID");
		inputName = rr.getString("INPUT_NAME");
		siteId = rr.getInteger("SITE_ID");
		siteUuid = rr.getUuid("SITE_UUID");
		parentSiteId = rr.getInteger("PARENT_SITE_ID");
		siteName = rr.getString("SITE_NAME");
		locationDescriptor = LocationDescriptor.convert(rr.getString("LOCATION_DESCRIPTOR"));
		unitDesignator = rr.getString("UNIT_DESIGNATOR");
		unitNumber = rr.getString("UNIT_NUMBER");
		unitNumberSuffix = rr.getString("UNIT_NUMBER_SUFFIX");
		sitePositionalAccuracy = PositionalAccuracy.convert(rr.getString("SITE_POSITIONAL_ACCURACY"));
		siteStatus = rr.getString("SITE_STATUS");
		siteRetireDate = rr.getDate("SITE_RETIRE_DATE");
		siteAlbersX = rr.getInteger("SITE_ALBERS_X");
		siteAlbersY = rr.getInteger("SITE_ALBERS_Y");
		apType = rr.getString("AP_TYPE");
		isPrimary = Optional.ofNullable(rr.getBoolean("IS_PRIMARY_IND")).orElse(true);
		narrativeLocation = rr.getString("NARRATIVE_LOCATION");
		accessPositionalAccuracy = PositionalAccuracy.convert(rr.getString("ACCESS_POSITIONAL_ACCURACY"));
		civicNumber = rr.getInteger("CIVIC_NUMBER"); 
		civicNumberSuffix = rr.getString("CIVIC_NUMBER_SUFFIX"); 
		String apStatus = rr.getString("ACCESS_POINT_STATUS");
		accessPointStatus = apStatus == null ? null : PhysicalStatus.convert(apStatus);
		accessRetireDate = rr.getDate("ACCESS_RETIRE_DATE");
		accessAlbersX = rr.getInteger("ACCESS_ALBERS_X");
		accessAlbersY = rr.getInteger("ACCESS_ALBERS_Y");
		fullAddress = rr.getString("FULL_ADDRESS");
		localityId = rr.getInteger("LOCALITY_ID");
		interimStreetNameId = rr.getInteger("INTERIM_STREET_NAME_ID");
		rangeType = rr.getInteger("RANGE_TYPE");
		siteChangeDate = rr.getDate("SITE_CHANGE_DATE");
	}
	
	public void write(RowWriter rw) {
		Map<String,Object> row = new HashMap<String,Object>(50);
		row.put("GID", gId);
		row.put("INPUT_NAME", inputName);
		row.put("SITE_ID", siteId);
		row.put("SITE_UUID", siteUuid);
		row.put("PARENT_SITE_ID", parentSiteId); // TODO: parent site resolution 
		row.put("SITE_NAME", siteName); 
		row.put("LOCATION_DESCRIPTOR", locationDescriptor == null ? "" : locationDescriptor.toString()); 
		row.put("UNIT_DESIGNATOR", unitDesignator); 
		row.put("UNIT_NUMBER", unitNumber); 
		row.put("UNIT_NUMBER_SUFFIX", unitNumberSuffix); 
		row.put("SITE_POSITIONAL_ACCURACY", sitePositionalAccuracy); 
		row.put("SITE_STATUS", siteStatus); 
		row.put("SITE_RETIRE_DATE", siteRetireDate); 
		row.put("SITE_ALBERS_X", siteAlbersX); 
		row.put("SITE_ALBERS_Y", siteAlbersY); 
		row.put("AP_TYPE", apType); 
		row.put("IS_PRIMARY_IND", isPrimary); 
		row.put("NARRATIVE_LOCATION", narrativeLocation); 
		row.put("ACCESS_POSITIONAL_ACCURACY", accessPositionalAccuracy == null ? "" : accessPositionalAccuracy.toString()); 
		row.put("CIVIC_NUMBER", civicNumber); 
		row.put("CIVIC_NUMBER_SUFFIX", civicNumberSuffix); 
		row.put("ACCESS_POINT_STATUS", accessPointStatus == null ? "" : accessPointStatus.toDbValue()); 
		row.put("ACCESS_RETIRE_DATE", accessRetireDate); 
		row.put("ACCESS_ALBERS_X", accessAlbersX); 
		row.put("ACCESS_ALBERS_Y", accessAlbersY); 
		row.put("FULL_ADDRESS", fullAddress);
		row.put("LOCALITY_ID", localityId); 
		row.put("INTERIM_STREET_NAME_ID", interimStreetNameId); 
		row.put("RANGE_TYPE", rangeType); 
		row.put("SITE_CHANGE_DATE", siteChangeDate); 
		rw.writeRow(row);
	}
	
	public String compare(OutputSite o) {
		StringBuilder sb = new StringBuilder();
		if(!Objects.equals(gId, o.gId)) {
			sb.append("gId: " + gId + " | " + o.gId + " ");
		}
		if(!Objects.equals(inputName, o.inputName)) {
			sb.append("inputName: " + inputName + " | " + o.inputName + " ");
		}
		if(!Objects.equals(siteId, o.siteId)) {
			sb.append("siteId: " + siteId + " | " + o.siteId + " ");
		}
		// don't compare siteUuid;
		// don't compare generated parentSiteIds
		if(!Objects.equals(parentSiteId, o.parentSiteId) 
				&& (parentSiteId == null || parentSiteId < SiteLoaderPrep.FIRST_GENERATED_PARENT_ID
					|| o.parentSiteId == null || o.parentSiteId < SiteLoaderPrep.FIRST_GENERATED_PARENT_ID)) {
			sb.append("parentSiteId: " + parentSiteId + " | " + o.parentSiteId + " ");
		}
		if(!Objects.equals(siteName, o.siteName)) {
			sb.append("siteName: " + siteName + " | " + o.siteName + " ");
		}
		if(!Objects.equals(locationDescriptor, o.locationDescriptor)) {
			sb.append("locationDescriptor: " + locationDescriptor + " | " + o.locationDescriptor + " ");
		}
		if(!Objects.equals(unitDesignator, o.unitDesignator)) {
			sb.append("unitDesignator: " + unitDesignator + " | " + o.unitDesignator + " ");
		}
		if(!Objects.equals(unitNumber, o.unitNumber)) {
			sb.append("unitNumber: " + unitNumber + " | " + o.unitNumber + " ");
		}
		if(!Objects.equals(unitNumberSuffix, o.unitNumberSuffix)) {
			sb.append("unitNumberSuffix: " + unitNumberSuffix + " | " + o.unitNumberSuffix + " ");
		}
		if(!Objects.equals(sitePositionalAccuracy, o.sitePositionalAccuracy)) {
			sb.append("sitePositionalAccuracy: " + sitePositionalAccuracy + " | " + o.sitePositionalAccuracy + " ");
		}
		if(!Objects.equals(siteStatus, o.siteStatus)) {
			sb.append("siteStatus: " + siteStatus + " | " + o.siteStatus + " ");
		}
		if(!Objects.equals(siteRetireDate, o.siteRetireDate)) {
			sb.append("siteRetireDate: " + siteRetireDate + " | " + o.siteRetireDate + " ");
		}
		if(!Objects.equals(siteAlbersX, o.siteAlbersX)) {
			sb.append("siteAlbersX: " + siteAlbersX + " | " + o.siteAlbersX + " ");
		}
		if(!Objects.equals(siteAlbersY, o.siteAlbersY)) {
			sb.append("siteAlbersY: " + siteAlbersY + " | " + o.siteAlbersY + " ");
		}
		if(!Objects.equals(apType, o.apType)) {
			sb.append("apType: " + apType + " | " + o.apType + " ");
		}
		if(!Objects.equals(isPrimary, o.isPrimary)) {
			sb.append("isPrimary: " + isPrimary + " | " + o.isPrimary + " ");
		}
		if(!Objects.equals(narrativeLocation, o.narrativeLocation)) {
			sb.append("narrativeLocation: " + narrativeLocation + " | " + o.narrativeLocation + " ");
		}
		if(!Objects.equals(accessPositionalAccuracy, o.accessPositionalAccuracy)) {
			sb.append("accessPositionalAccuracy: " + accessPositionalAccuracy + " | " + o.accessPositionalAccuracy + " ");
		}
		if(!Objects.equals(civicNumber, o.civicNumber)) {
			sb.append("civicNumber: " + civicNumber + " | " + o.civicNumber + " ");
		}
		if(!Objects.equals(civicNumberSuffix, o.civicNumberSuffix)) {
			sb.append("civicNumberSuffix: " + civicNumberSuffix + " | " + o.civicNumberSuffix + " ");
		}
		if(!Objects.equals(accessPointStatus, o.accessPointStatus)) {
			sb.append("accessPointStatus: " + accessPointStatus + " | " + o.accessPointStatus + " ");
		}
		if(!Objects.equals(accessRetireDate, o.accessRetireDate)) {
			sb.append("accessRetireDate: " + accessRetireDate + " | " + o.accessRetireDate + " ");
		}
		if(!Objects.equals(accessAlbersX, o.accessAlbersX)) {
			sb.append("accessAlbersX: " + accessAlbersX + " | " + o.accessAlbersX + " ");
		}
		if(!Objects.equals(accessAlbersY, o.accessAlbersY)) {
			sb.append("accessAlbersY: " + accessAlbersY + " | " + o.accessAlbersY + " ");
		}
		// don't compare fullAddress, it is just an assembly of the other parts already being compared
		//if(!GeocoderUtil.equalsIgnoreCaseNullSafe(fullAddress, o.fullAddress)) {
		//	sb.append("fullAddress: " + fullAddress + " | " + o.fullAddress + " ");
		//}
		if(!Objects.equals(localityId, o.localityId)) {
			sb.append("localityId: " + localityId + " | " + o.localityId + " ");
		}
		if(!Objects.equals(interimStreetNameId, o.interimStreetNameId)) {
			sb.append("interimStreetNameId: " + interimStreetNameId + " | " + o.interimStreetNameId + " ");
		}
		if(!Objects.equals(rangeType, o.rangeType)) {
			sb.append("rangeType: " + rangeType + " | " + o.rangeType + " ");
		}
		// don't compare siteChangeDate
		return sb.toString();
	}
}
