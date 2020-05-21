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
package ca.bc.gov.ols.geocoder.rest.messageconverters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.IntersectionMatch;
import ca.bc.gov.ols.geocoder.api.data.OccupantAddress;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.api.data.SiteAddress;
import ca.bc.gov.ols.geocoder.api.data.StreetIntersectionAddress;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.rest.LocationReprojector;
import ca.bc.gov.ols.geocoder.rest.OlsResponse;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;

import org.locationtech.jts.geom.Point;

@Component
public class ShpOlsResponseConverter extends AbstractHttpMessageConverter<OlsResponse> {
	
	private static final int BUFFER_SIZE = 2048;
	
	@Autowired
	private IGeocoder geocoder;
	
	public ShpOlsResponseConverter() {
		super(new MediaType("application", "zip",
				Charset.forName("UTF-8")));
	}
	
	@Override
	protected boolean supports(Class<?> clazz) {
		return OlsResponse.class.isAssignableFrom(clazz);
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}
	
	@Override
	protected OlsResponse readInternal(Class<? extends OlsResponse> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void writeInternal(OlsResponse response, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		GeocoderConfig config = geocoder.getDatastore().getConfig();
		OutputStream out = outputMessage.getBody();
		response.reproject(config.getBaseSrsCode(), response.getOutputSRS());
		
		// shapefile base name and feature type, different for different outputs
		String fileBase = "";
		SimpleFeatureType FEATURE_TYPE = null;
		
		if(response.getResponseObj() == null) {
			return; // empty file in case no result found
		}
		
		if(response.getResponseObj() instanceof SearchResults) {
			FEATURE_TYPE = createSearchResultsFeatureType(response.getOutputSRS(), 
					response.getExtraInfo("occupantQuery").equals("true"));
			fileBase = "geocodeResults";
		} else if(response.getResponseObj() instanceof SiteAddress) {
			FEATURE_TYPE = createSiteAddressFeatureType(response.getOutputSRS(),
					response.getResponseObj() instanceof OccupantAddress);
			fileBase = "SiteAddress";
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress) {
			FEATURE_TYPE = createStreetIntersectionAddressFeatureType(response.getOutputSRS());
			fileBase = "StreetIntersectionAddress";
		} else if(response.getResponseObj() instanceof SiteAddress[]) {
			FEATURE_TYPE = createSiteAddressFeatureType(response.getOutputSRS(),
					response.getExtraInfo("occupantQuery").equals("true"));
			fileBase = "SiteAddresses";
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress[]) {
			FEATURE_TYPE = createStreetIntersectionAddressFeatureType(response.getOutputSRS());
			fileBase = "StreetIntersectionAddresses";
		} else {
			// should never get here but this is handy for debugging if we do
			out.write(("SHP output not supported for " + response.getResponseObj().getClass()
					.getCanonicalName()).getBytes());
		}
		
		// make a temporary directory for the shapefile files
		File tempDir = createTempDir();
		File shpFile = new File(tempDir, fileBase + ".shp");
		
		// Use a datastore to write the featureCollection to the shapefile
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", shpFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		ShapefileDataStore outputShpDataStore = (ShapefileDataStore)dataStoreFactory
				.createNewDataStore(params);
		outputShpDataStore.createSchema(FEATURE_TYPE);
		String typeName = outputShpDataStore.getTypeNames()[0];
		SimpleFeatureSource outputShpFeatureSource = outputShpDataStore.getFeatureSource(typeName);
		
		SimpleFeatureStore outputShpFeatureStore = (SimpleFeatureStore)outputShpFeatureSource;
		
		if(response.getResponseObj() instanceof SearchResults) {
			writeSearchResultsToFeatureStore(
					(SearchResults)response.getResponseObj(), outputShpFeatureStore,
					response.getExtraInfo("occupantQuery").equals("true"));
		} else if(response.getResponseObj() instanceof SiteAddress) {
			writeSiteAddressesToFeatureStore(
					new SiteAddress[] {(SiteAddress)response.getResponseObj()},
					outputShpFeatureStore);
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress) {
			writeStreetIntersectionAddressesToFeatureStore(
					new StreetIntersectionAddress[] {
					(StreetIntersectionAddress)response.getResponseObj()},
					outputShpFeatureStore);
		} else if(response.getResponseObj() instanceof SiteAddress[]) {
			writeSiteAddressesToFeatureStore(
					(SiteAddress[])response.getResponseObj(), outputShpFeatureStore);
		} else if(response.getResponseObj() instanceof StreetIntersectionAddress[]) {
			writeStreetIntersectionAddressesToFeatureStore(
					(StreetIntersectionAddress[])response.getResponseObj(), outputShpFeatureStore);
		} else {
			// should never get here but this is handy for debugging if we do
			out.write(("SHP output not supported for " + response.getResponseObj().getClass()
					.getCanonicalName()).getBytes());
		}
		
		// zip up the shapefile files
		File[] files = new File[] {
				new File(tempDir, fileBase + ".shp"),
				new File(tempDir, fileBase + ".shx"),
				new File(tempDir, fileBase + ".dbf"),
				new File(tempDir, fileBase + ".prj")
		};
		zipFileArray(out, files);
		
		// clean up the temp dir
		deleteDir(tempDir);
	}
	
	private static void writeSearchResultsToFeatureStore(SearchResults results,
			FeatureStore<SimpleFeatureType, SimpleFeature> featureStore,
			boolean isOccupant) throws IOException {
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureStore.getSchema());
		int featureId = 1;
		for(GeocodeMatch match : results.getMatches()) {
			if(match instanceof AddressMatch) {
				SiteAddress addr = ((AddressMatch)match).getAddress();
				featureBuilder.add(addr.getAddressString());
				featureBuilder.add(""); // IntersectionName
				featureBuilder.add(match.getScore());
				featureBuilder.add(match.getPrecision());
				featureBuilder.add(match.getPrecisionPoints());
				featureBuilder.add(match.getFaults().toString());
				featureBuilder.add(addr.getSiteName());
				featureBuilder.add(addr.getUnitDesignator());
				featureBuilder.add(addr.getUnitNumber());
				featureBuilder.add(addr.getUnitNumberSuffix());
				featureBuilder.add(GeocoderUtil.formatCivicNumber(addr.getCivicNumber()));
				featureBuilder.add(addr.getCivicNumberSuffix());
				featureBuilder.add(addr.getStreetName());
				featureBuilder.add(addr.getStreetType());
				featureBuilder.add(addr.isStreetTypePrefix());
				featureBuilder.add(addr.getStreetDirection());
				featureBuilder.add(addr.isStreetDirectionPrefix());
				featureBuilder.add(addr.getStreetQualifier());
				featureBuilder.add(addr.getLocalityName());
				featureBuilder.add(addr.getLocalityType());
				featureBuilder.add(addr.getElectoralArea());
				featureBuilder.add(addr.getStateProvTerr());
				featureBuilder.add(addr.getLocation());
				featureBuilder.add(addr.getLocationPositionalAccuracy());
				featureBuilder.add(addr.getLocationDescriptor());
				featureBuilder.add(addr.getSiteID());
				featureBuilder.add(addr.getStreetSegmentID());
				featureBuilder.add(""); // IntersectionId
				featureBuilder.add(null); // degree
				featureBuilder.add(addr.getFullSiteDescriptor());
				featureBuilder.add(addr.getNarrativeLocation());// narrativeLocation as accessNotes
				featureBuilder.add(addr.getSiteStatus());
				featureBuilder.add(convertDate(addr.getSiteRetireDate()));
				featureBuilder.add(convertDate(addr.getSiteChangeDate()));
				featureBuilder.add(addr.isPrimary());
				featureBuilder.add(results.getExecutionTime());
				if(isOccupant) {
					if(addr instanceof OccupantAddress) {
						OccupantAddress occ = (OccupantAddress)addr;
						featureBuilder.add(occ.getOccupantName()); // Occupant name
						featureBuilder.add(occ.getOccupantId()); // Occupant ID
						featureBuilder.add(occ.getOccupantAliasAddress()); // Occupant Alias Address
						featureBuilder.add(occ.getOccupantDescription()); // Occupant Description
						featureBuilder.add(occ.getContactEmail()); // Contact Email
						featureBuilder.add(occ.getContactPhone()); // Contact Phone
						featureBuilder.add(occ.getContactFax()); // Contact Fax
						featureBuilder.add(occ.getWebsiteUrl()); // Website url
						featureBuilder.add(occ.getImageUrl()); // image url
						featureBuilder.add(occ.getKeywords()); // keywords
						featureBuilder.add(occ.getBusinessCategoryClass()); // business category class
						featureBuilder.add(occ.getBusinessCategoryDescription()); // business category description
						featureBuilder.add(occ.getNaicsCode()); // naics code
						featureBuilder.add(convertDate(occ.getDateOccupantUpdated())); // date occupant updated
						featureBuilder.add(convertDate(occ.getDateOccupantAdded())); // date occupant added
					} else {
						featureBuilder.add(""); // Occupant name
						featureBuilder.add(""); // Occupant ID
						featureBuilder.add(""); // Occupant Alias Address
						featureBuilder.add(""); // Occupant Description
						featureBuilder.add(""); // Contact Email
						featureBuilder.add(""); // Contact Phone
						featureBuilder.add(""); // Contact Fax
						featureBuilder.add(""); // Website url
						featureBuilder.add(""); // image url
						featureBuilder.add(""); // keywords
						featureBuilder.add(""); // business category class
						featureBuilder.add(""); // business category description
						featureBuilder.add(""); // naics code
						featureBuilder.add(null); // date occupant updated
						featureBuilder.add(null); // date occupant added
						featureBuilder.add(""); // custodian id
						featureBuilder.add(""); // source data id
						
					}
				}
			} else if(match instanceof IntersectionMatch) {
				StreetIntersectionAddress addr = ((IntersectionMatch)match).getAddress();
				featureBuilder.add(addr.getAddressString());
				featureBuilder.add(addr.getName());// intrsec name
				featureBuilder.add(match.getScore());
				featureBuilder.add(match.getPrecision());
				featureBuilder.add(match.getPrecisionPoints());
				featureBuilder.add(match.getFaults().toString());
				featureBuilder.add(""); // SiteName
				featureBuilder.add(""); // UnitDesignator
				featureBuilder.add(""); // UnitNumber
				featureBuilder.add(""); // UnitNumberSuffix
				featureBuilder.add(null); // CivicNumber
				featureBuilder.add(""); // CivicNumberSuffix
				featureBuilder.add(""); // StreetName
				featureBuilder.add(""); // StreetType
				featureBuilder.add(null); // isStreetTypePrefix
				featureBuilder.add(""); // StreetDirection
				featureBuilder.add(null); // isStreetDirectionPrefix
				featureBuilder.add(""); // getStreetQualifier
				featureBuilder.add(addr.getLocalityName());
				featureBuilder.add(addr.getLocalityType());
				featureBuilder.add(""); // ElectoralArea
				featureBuilder.add(addr.getStateProvTerr());
				featureBuilder.add(addr.getLocation());
				featureBuilder.add(""); // LocationPositionalAccuracy
				featureBuilder.add(""); // LocationDescriptor
				featureBuilder.add(""); // SiteID
				featureBuilder.add(null); // BlockID
				featureBuilder.add(addr.getID());
				featureBuilder.add(addr.getDegree());
				featureBuilder.add(""); // FullSiteDescriptor
				featureBuilder.add(""); // narrativeLocation as accessNotes
				featureBuilder.add(""); // SiteStatus
				featureBuilder.add(null); // SiteRetireDate
				featureBuilder.add(null); // ChangeDate
				featureBuilder.add(null); // isPrimary
				featureBuilder.add(results.getExecutionTime());
				if(isOccupant) {
					featureBuilder.add(""); // Occupant name
					featureBuilder.add(""); // Occupant ID
					featureBuilder.add(""); // Occupant Alias Address
					featureBuilder.add(""); // Occupant Description
					featureBuilder.add(""); // Contact Email
					featureBuilder.add(""); // Contact Phone
					featureBuilder.add(""); // Contact Fax
					featureBuilder.add(""); // Website url
					featureBuilder.add(""); // image url
					featureBuilder.add(""); // keywords
					featureBuilder.add(""); // business category class
					featureBuilder.add(""); // business category description
					featureBuilder.add(""); // naics code
					featureBuilder.add(null); // date occupant updated
					featureBuilder.add(null); // date occupant added
					featureBuilder.add(""); // custodian id
					featureBuilder.add(""); // source data id
				}
			}
			SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(featureId++));
			// Replace all nulls with empty strings to avoid geotools sending bad ascii in place of
			// null to shp file.
			List<Object> vals = new ArrayList<Object>(feature.getAttributes());
			
			for(int i = 1; i < vals.size(); i++) {
				if(vals.get(i) != null) {
					continue;
				} else {
					feature.setAttribute(i, "");
				}
			}
			
			// we have to write each feature in its own transaction to ensure the order is
			// maintained
			Transaction transaction = new DefaultTransaction("create");
			featureStore.setTransaction(transaction);
			featureStore.addFeatures(DataUtilities.collection(feature));
			transaction.commit();
			transaction.close();
		}
	}

	private static void writeSiteAddressesToFeatureStore(SiteAddress[] addrs,
			FeatureStore<SimpleFeatureType, SimpleFeature> featureStore) throws IOException {
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureStore.getSchema());
		int featureId = 1;
		for(SiteAddress addr : addrs) {
			featureBuilder.add(addr.getAddressString());
			featureBuilder.add(addr.getSiteName());
			featureBuilder.add(addr.getUnitDesignator());
			featureBuilder.add(addr.getUnitNumber());
			featureBuilder.add(addr.getUnitNumberSuffix());
			featureBuilder.add(GeocoderUtil.formatCivicNumber(addr.getCivicNumber()));
			featureBuilder.add(addr.getCivicNumberSuffix());
			featureBuilder.add(addr.getStreetName());
			featureBuilder.add(addr.getStreetType());
			featureBuilder.add(addr.isStreetTypePrefix());
			featureBuilder.add(addr.getStreetDirection());
			featureBuilder.add(addr.isStreetDirectionPrefix());
			featureBuilder.add(addr.getStreetQualifier());
			featureBuilder.add(addr.getLocalityName());
			featureBuilder.add(addr.getLocalityType());
			featureBuilder.add(addr.getElectoralArea());
			featureBuilder.add(addr.getStateProvTerr());
			featureBuilder.add(addr.getLocation());
			featureBuilder.add(addr.getLocationPositionalAccuracy());
			featureBuilder.add(addr.getLocationDescriptor());
			featureBuilder.add(addr.getSiteID());
			featureBuilder.add(addr.getStreetSegmentID());
			featureBuilder.add(addr.getFullSiteDescriptor());
			featureBuilder.add(addr.getNarrativeLocation());// narrativeLocation as accessNotes
			featureBuilder.add(addr.getSiteStatus());
			featureBuilder.add(convertDate(addr.getSiteRetireDate()));
			featureBuilder.add(convertDate(addr.getSiteChangeDate()));
			featureBuilder.add(addr.isPrimary());
			if(addr instanceof OccupantAddress) {
				OccupantAddress occ = (OccupantAddress)addr;
				featureBuilder.add(occ.getOccupantName()); // Occupant name
				featureBuilder.add(occ.getOccupantId()); // Occupant ID
				featureBuilder.add(occ.getOccupantAliasAddress()); // Occupant Alias Address
				featureBuilder.add(occ.getOccupantDescription()); // Occupant Description
				featureBuilder.add(occ.getContactEmail()); // Contact Email
				featureBuilder.add(occ.getContactPhone()); // Contact Phone
				featureBuilder.add(occ.getContactFax()); // Contact Fax
				featureBuilder.add(occ.getWebsiteUrl()); // Website url
				featureBuilder.add(occ.getImageUrl()); // image url
				featureBuilder.add(occ.getKeywords()); // keywords
				featureBuilder.add(occ.getBusinessCategoryClass()); // business category class
				featureBuilder.add(occ.getBusinessCategoryDescription()); // business category description
				featureBuilder.add(occ.getNaicsCode()); // naics code
				featureBuilder.add(convertDate(occ.getDateOccupantUpdated())); // date occupant updated
				featureBuilder.add(convertDate(occ.getDateOccupantAdded())); // date occupant added
			}
			
			SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(featureId++));
			// we have to write each feature in its own transaction to ensure the order is
			// maintained
			Transaction transaction = new DefaultTransaction("create");
			featureStore.setTransaction(transaction);
			featureStore.addFeatures(DataUtilities.collection(feature));
			transaction.commit();
			transaction.close();
		}
	}
	
	private static void writeStreetIntersectionAddressesToFeatureStore(
			StreetIntersectionAddress[] addrs,
			FeatureStore<SimpleFeatureType, SimpleFeature> featureStore) throws IOException {
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureStore.getSchema());
		int featureId = 1;
		for(StreetIntersectionAddress addr : addrs) {
			
			featureBuilder.add(addr.getAddressString());
			featureBuilder.add(addr.getName());
			featureBuilder.add(addr.getLocalityName());
			featureBuilder.add(addr.getLocalityType());
			featureBuilder.add(addr.getStateProvTerr());
			featureBuilder.add(addr.getLocation());
			featureBuilder.add(addr.getLocationPositionalAccuracy());
			featureBuilder.add(addr.getLocationDescriptor());
			featureBuilder.add(addr.getID());
			featureBuilder.add(addr.getDegree());
			
			SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(featureId++));
			// we have to write each feature in its own transaction to ensure the order is
			// maintained
			Transaction transaction = new DefaultTransaction("create");
			featureStore.setTransaction(transaction);
			featureStore.addFeatures(DataUtilities.collection(feature));
			transaction.commit();
			transaction.close();
		}
	}
	
	private static void zipFileArray(OutputStream out, File[] files) throws FileNotFoundException,
			IOException {
		ZipOutputStream zippedOut = new ZipOutputStream(out);
		zippedOut.setMethod(ZipOutputStream.DEFLATED);
		for(File file : files) {
			byte data[] = new byte[BUFFER_SIZE];
			FileInputStream fi = new FileInputStream(file);
			BufferedInputStream origin = new BufferedInputStream(fi, BUFFER_SIZE);
			ZipEntry entry = new ZipEntry(file.getName());
			zippedOut.putNextEntry(entry);
			int count;
			while((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
				zippedOut.write(data, 0, count);
			}
			origin.close();
		}
		zippedOut.finish();
	}
	
	private static SimpleFeatureType createSearchResultsFeatureType(int srsCode, boolean isOccupant) {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("SearchResults");
		builder.setCRS(LocationReprojector.srsCodeToCRS(srsCode));
		builder.add("ADDRES_STR", String.class);
		builder.add("INTRSCNAME", String.class);
		builder.length(3).add("SCORE", Integer.class);
		builder.length(25).add("MATCHPRECN", String.class);
		builder.length(3).add("PRCNPOINTS", Integer.class);
		builder.add("FAULTS", String.class);
		builder.add("SITENAME", String.class);
		builder.length(25).add("UNITDSGNTR", String.class);
		builder.length(25).add("UNIT_NUMBR", String.class);
		builder.length(25).add("UNITNUMSFX", String.class);
		builder.length(8).add("CIVIC_NUM", String.class);
		builder.length(25).add("CIVNUMSFX", String.class);
		builder.length(50).add("STREETNAME", String.class);
		builder.length(25).add("STREETTYPE", String.class);
		builder.add("IS_STRTPRX", Boolean.class);
		builder.length(2).add("STREETDIR", String.class);
		builder.add("IS_STRDPRX", Boolean.class);
		builder.length(25).add("STREETQUAL", String.class);
		builder.length(50).add("LOCAL_NAME", String.class);
		builder.length(50).add("LOCAL_TYPE", String.class);
		builder.length(50).add("ELCTRLAREA", String.class);
		builder.length(50).add("PROVINCE", String.class);
		builder.add("LOCATION", Point.class);
		builder.length(25).add("LOCNPOSACC", String.class);
		builder.length(25).add("LOCATNDESC", String.class);
		builder.length(36).add("SITEID", String.class);
		builder.add("BLOCKID", Integer.class);
		builder.length(36).add("INTRSCTNID", String.class);
		builder.add("DEGREE", Integer.class);
		builder.add("FULSITEDSC", String.class);
		builder.add("ACCESSNOTE", String.class);
		builder.add("SITESTATUS", String.class);
		builder.add("SITERETDAT", Date.class);
		builder.add("CHANGEDATE", Date.class);
		builder.add("ISOFFICIAL", Boolean.class);
		builder.length(9).add("EXECUTIONT", Float.class);
		if(isOccupant) {
			builder.add("OCCUP_NAME", String.class);
			builder.length(36).add("OCCUPANTID", String.class);
			builder.add("OCCALIADDR", String.class);
			builder.add("OCCUP_DESC", String.class);
			builder.add("CONTACTEML", String.class);
			builder.add("CONTACTPHN", String.class);
			builder.add("CONTACTFAX", String.class);
			builder.add("WEBSITEURL", String.class);
			builder.add("IMAGE_URL", String.class);
			builder.add("KEYWORDS", String.class);
			builder.add("BUSCATCLAS", String.class);
			builder.add("BUSCATDESC", String.class);
			builder.add("NAICS_CODE", String.class);
			builder.add("DATEOCCUPD", Date.class);
			builder.add("DATEOCCADD", Date.class);
		}
		final SimpleFeatureType SEARCH_RESULTS_TYPE = builder.buildFeatureType();
		
		return SEARCH_RESULTS_TYPE;
	}

	
	private static SimpleFeatureType createSiteAddressFeatureType(int srsCode, boolean isOccupant) {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("SiteAddress");
		builder.setCRS(LocationReprojector.srsCodeToCRS(srsCode));
		builder.add("ADDRES_STR", String.class);
		// builder.add("INTRSCNAME", String.class);
		// builder.add("SCORE", Integer.class);
		// builder.add("MATCHPRECN", String.class);
		// builder.add("PRCNPOINTS", Integer.class);
		// builder.add("FAULTS", String.class);
		builder.add("SITENAME", String.class);
		builder.length(25).add("UNITDSGNTR", String.class);
		builder.length(25).add("UNIT_NUMBR", String.class);
		builder.length(25).add("UNITNUMSFX", String.class);
		builder.length(8).add("CIVIC_NUM", String.class);
		builder.length(25).add("CIVNUMSFX", String.class);
		builder.length(50).add("STREETNAME", String.class);
		builder.length(25).add("STREETTYPE", String.class);
		builder.add("IS_STRTPRX", Boolean.class);
		builder.length(1).add("STREETDIR", String.class);
		builder.add("IS_STRDPRX", Boolean.class);
		builder.length(25).add("STREETQUAL", String.class);
		builder.length(50).add("LOCAL_NAME", String.class);
		builder.length(50).add("LOCAL_TYPE", String.class);
		builder.length(50).add("ELCTRLAREA", String.class);
		builder.length(2).add("PROVINCE", String.class);
		builder.add("LOCATION", Point.class);
		builder.length(25).add("LOCNPOSACC", String.class);
		builder.length(25).add("LOCATNDESC", String.class);
		builder.length(36).add("SITEID", String.class);
		builder.add("BLOCKID", Integer.class);
		// builder.add("INTRSCTNID", String.class);
		// builder.add("DEGREE", Integer.class);
		builder.add("FULSITEDSC", String.class);
		builder.add("ACCESSNOTE", String.class);
		builder.add("SITESTATUS", String.class);
		builder.add("SITERETDAT", Date.class);
		builder.add("CHANGEDATE", Date.class);
		builder.add("ISOFFICIAL", Boolean.class);
		if(isOccupant) {
			builder.add("OCCUP_NAME", String.class);
			builder.length(36).add("OCCUPANTID", String.class);
			builder.add("OCCALIADDR", String.class);
			builder.add("OCCUP_DESC", String.class);
			builder.add("CONTACTEML", String.class);
			builder.add("CONTACTPHN", String.class);
			builder.add("CONTACTFAX", String.class);
			builder.add("WEBSITEURL", String.class);
			builder.add("IMAGE_URL", String.class);
			builder.add("KEYWORDS", String.class);
			builder.add("BUSCATCLAS", String.class);
			builder.add("BUSCATDESC", String.class);
			builder.add("NAICS_CODE", String.class);
			builder.add("DATEOCCUPD", Date.class);
			builder.add("DATEOCCADD", Date.class);
		}
		final SimpleFeatureType SITE_ADDRESS_TYPE = builder.buildFeatureType();
		
		return SITE_ADDRESS_TYPE;
	}
	
	private static SimpleFeatureType createStreetIntersectionAddressFeatureType(int srsCode) {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("StreetintersectionAddress");
		builder.setCRS(LocationReprojector.srsCodeToCRS(srsCode));
		
		builder.add("ADDRESSSTR", String.class);
		builder.add("INTRSCNAME", String.class);
		builder.length(50).add("LOCAL_NAME", String.class);
		builder.length(50).add("LOCAL_TYPE", String.class);
		builder.length(2).add("PROVINCE", String.class);
		builder.add("LOCATION", Point.class);
		builder.length(25).add("LOCNPOSACC", String.class);
		builder.add("LOCATNDESC", String.class);
		builder.length(36).add("INTRSCTNID", String.class);
		builder.add("DEGREE", Integer.class);
		
		final SimpleFeatureType STREET_INTERSECTION_ADDRESS_TYPE = builder.buildFeatureType();
		
		return STREET_INTERSECTION_ADDRESS_TYPE;
	}

	private static Date convertDate(LocalDate date) {
		if(date == null) return null;
		return new Date(date.getLong(ChronoField.EPOCH_DAY)*24*60*60*1000);
	}
	
	private static final int TEMP_DIR_ATTEMPTS = 10000;
	
	public static File createTempDir() {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = System.currentTimeMillis() + "-";
		
		for(int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
			File tempDir = new File(baseDir, baseName + counter);
			if(tempDir.mkdir()) {
				return tempDir;
			}
		}
		throw new IllegalStateException("Failed to create directory in: "
				+ System.getProperty("java.io.tmpdir") + " within "
				+ TEMP_DIR_ATTEMPTS + " attempts (tried "
				+ baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
	}
	
	// Deletes all files and subdirectories under dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns false.
	public static boolean deleteDir(File dir) {
		if(dir.isDirectory()) {
			String[] children = dir.list();
			for(int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if(!success) {
					return false;
				}
			}
		}
		
		// The directory is now empty so delete it
		return dir.delete();
	}
}
