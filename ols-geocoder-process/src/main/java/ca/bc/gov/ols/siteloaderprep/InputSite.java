package ca.bc.gov.ols.siteloaderprep;

import java.time.LocalDate;
import java.util.UUID;

import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.PhysicalStatus;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;

public class InputSite {
	int id;
	UUID uuid;
	Integer parentId;
	String addressString;
	String yourId;
	String siteName;
	String unitDesignator;
	String unitNumber;
	String unitNumberSuffix;
	Integer civicNumber;
	String civicNumberSuffix;
	String localityName;
	Integer localityId;
	String provinceCode;
	Integer gId;
	String inputName;
	Integer siteId;
	LocationDescriptor locationDescriptor;
	PositionalAccuracy positionalAccuracy;
	PhysicalStatus status;
	LocalDate retireDate;
	Integer albersX;
	Integer albersY;
	String apType;
	boolean isPrimary;
	String narrativeLocation;
	PositionalAccuracy accessPositionalAccuracy;
	PhysicalStatus accessPointStatus;
	LocalDate accessRetireDate;
	Integer accessAlbersX;
	Integer accessAlbersY;
	boolean isPseudoSite;
	String superFullSiteDescriptor;
	GeocodeResult result;

	// unique key string for de-duplicating addresses
	public String uniqueKey() {
		if(apType.equals("NCAP")) {
			return siteName + " " + albersX + " " + albersY + result.fullAddress;
		}
		if(superFullSiteDescriptor == null || superFullSiteDescriptor.isEmpty()) {
			return unitDesignator + " " + unitNumber + " " + unitNumberSuffix + "  " + civicNumber + " " + civicNumberSuffix + " " + result.fullAddress;
		}
		return siteName + unitDesignator + " " + unitNumber + " " + unitNumberSuffix + "  " + civicNumber + " " + civicNumberSuffix + " " + result.fullAddress + " " + superFullSiteDescriptor; 
	}
	
	// unique key string for de-duplicating addresses
	public String subsiteKey() {
		return civicNumber + " " + civicNumberSuffix + " " + result.fullAddress; 
	}

	public void forceSubsite() {
		accessAlbersX = null;
		accessAlbersY = null;
		accessPointStatus = null;
		accessPositionalAccuracy = null;
		accessRetireDate = null;
		apType = null;
	}
	
	public String fullAddress() {
		StringBuilder sb = new StringBuilder(1024);
		appendPart(sb, " ", unitDesignator);
		appendPart(sb, " ", unitNumber);
		appendPart(sb, " ", unitNumberSuffix);
		appendPart(sb, " ", GeocoderUtil.formatCivicNumber(civicNumber));
		if(civicNumberSuffix != null) {
			if(civicNumberSuffix.equals("1/2")) {
				appendPart(sb, " ", civicNumberSuffix);
			} else {
				appendPart(sb, "", civicNumberSuffix);
			}
		}
		if(result.name != null) {
			appendPart(sb, " ", result.name.toString());
		}
		appendPart(sb, " ", localityName);
		appendPart(sb, " ", provinceCode);
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
}
