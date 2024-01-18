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
package ca.bc.gov.ols.geocoder.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.multipart.MultipartFile;

import com.google.gson.stream.JsonReader;

import ca.bc.gov.ols.config.ConfigurationParameter;
import ca.bc.gov.ols.config.FileExportConfigurationStore;

public class FileExportGeocoderConfigurationStore extends FileExportConfigurationStore implements GeocoderConfigurationStore {

	protected List<AbbreviationMapping> abbrevMappings = new ArrayList<AbbreviationMapping>();
	protected int abbrevMappingCount = 0;
	protected List<UnitDesignator> unitDesignators = new ArrayList<UnitDesignator>();
	protected int unitDesignatorCount = 0;
	protected List<LocalityMapping> localityMappings = new ArrayList<LocalityMapping>();
	protected int localityMappingCount = 0;

	public FileExportGeocoderConfigurationStore(MultipartFile file) {
		// fileName = file.getOriginalFilename();
		try {
			JsonReader jsonReader = new JsonReader(
					new InputStreamReader(file.getInputStream(), Charset.forName("UTF-8")));
			jsonReader.beginObject();
			while(jsonReader.hasNext()) {
				switch(jsonReader.nextName()) {
				case "exportDate":
					exportDate = jsonReader.nextString();
					break;
				case "BGEO_CONFIGURATION_PARAMETERS":
					jsonReader.beginObject();
					while(jsonReader.hasNext()) {
						switch(jsonReader.nextName()) {
							case "rows":
								jsonReader.beginArray();
								while(jsonReader.hasNext()) {
									configParams.add(new ConfigurationParameter(jsonReader, messages));
								}
								jsonReader.endArray();
								break;
							case "rowCount":
								configParamCount = jsonReader.nextInt();
								break;
							default:
								messages.add("Unexpected key/value: " + jsonReader.getPath() 
										+ " = " + jsonReader.nextString());
						}
					}
					jsonReader.endObject();
					break;
				case "BGEO_ABBREVIATION_MAPPINGS":
					jsonReader.beginObject();
					while(jsonReader.hasNext()) {
						switch(jsonReader.nextName()) {
							case "rows":
								jsonReader.beginArray();
								while(jsonReader.hasNext()) {
									abbrevMappings.add(new AbbreviationMapping(jsonReader, messages));
								}
								jsonReader.endArray();
								break;
							case "rowCount":
								abbrevMappingCount = jsonReader.nextInt();
								break;
							default:
								messages.add("Unexpected key/value: " + jsonReader.getPath() 
										+ " = " + jsonReader.nextString());
						}
					}
					jsonReader.endObject();
					break;
				case "BGEO_UNIT_DESIGNATORS":
					jsonReader.beginObject();
					while(jsonReader.hasNext()) {
						switch(jsonReader.nextName()) {
							case "rows":
								jsonReader.beginArray();
								while(jsonReader.hasNext()) {
									unitDesignators.add(new UnitDesignator(jsonReader, messages));
								}
								jsonReader.endArray();
								break;
							case "rowCount":
								unitDesignatorCount = jsonReader.nextInt();
								break;
							default:
								messages.add("Unexpected key/value: " + jsonReader.getPath() 
										+ " = " + jsonReader.nextString());
						}
					}
					jsonReader.endObject();
					break;
				case "BGEO_LOCALITY_MAPPINGS":
					jsonReader.beginObject();
					while(jsonReader.hasNext()) {
						switch(jsonReader.nextName()) {
							case "rows":
								jsonReader.beginArray();
								while(jsonReader.hasNext()) {
									localityMappings.add(new LocalityMapping(jsonReader, messages));
								}
								jsonReader.endArray();
								break;
							case "rowCount":
								localityMappingCount = jsonReader.nextInt();
								break;
							default:
								messages.add("Unexpected key/value: " + jsonReader.getPath() 
										+ " = " + jsonReader.nextString());
						}
					}
					jsonReader.endObject();
					break;
				default:
					messages.add("Unexpected key/value: " + jsonReader.getPath() 
							+ " = " + jsonReader.nextString());
				}
			}
			validate();
		} catch(IOException ioe) {
			errors.add("IOException was thrown while reading configuration: " + ioe.toString());
		} catch(IllegalStateException ise) {
			errors.add("Invalid JSON input; error message was: " + ise.toString());
		}
	}

	protected void validate() {
		super.validate();
		if(abbrevMappingCount != abbrevMappings.size()) {
			errors.add("BGEO_ABBREVIATION_MAPPINGS count: " 
					+ abbrevMappings.size() + " does not match expected count " + abbrevMappingCount);
		}
		if(unitDesignatorCount != unitDesignators.size()) {
			errors.add("BGEO_UNIT_DESIGNATORS count: " 
					+ unitDesignators.size() + " does not match expected count " + unitDesignatorCount);
		}
		if(localityMappingCount != localityMappings.size()) {
			errors.add("BGEO_LOCALITY_MAPPINGS count: " 
					+ localityMappings.size() + " does not match expected count " + localityMappingCount);
		}		
	}


	@Override
	public Stream<AbbreviationMapping> getAbbrevMappings() {
		return abbrevMappings.stream();
	}

	public int getAbbrevMappingCount() {
		return abbrevMappingCount;
	}
	
	@Override
	public void setAbbrevMapping(AbbreviationMapping abbrMap) {
		if(!abbrevMappings.contains(abbrMap)) {
			abbrevMappings.add(abbrMap);
		}		
	}

	@Override
	public void removeAbbrevMapping(AbbreviationMapping abbrMap) {
		abbrevMappings.remove(abbrMap);
	}

	@Override
	public Stream<UnitDesignator> getUnitDesignators() {
		return unitDesignators.stream();
	}
	
	public int getUnitDesignatorCount() {
		return unitDesignatorCount;
	}

	@Override
	public void addUnitDesignator(UnitDesignator ud) {
		if(!unitDesignators.contains(ud)) {
			unitDesignators.add(ud);
		}		
	}

	@Override
	public void removeUnitDesignator(UnitDesignator ud) {
		unitDesignators.remove(ud);
	}

	@Override
	public Stream<LocalityMapping> getLocalityMappings() {
		return localityMappings.stream();
	}

	public int getLocalityMappingCount() {
		return localityMappingCount;
	}
	
	@Override
	public void setLocalityMapping(LocalityMapping locMap) {
		localityMappings.stream().filter(lm -> lm.getInputString().equals(locMap.getInputString()) 
						&& lm.getLocalityId() == locMap.getLocalityId())
				.findFirst().ifPresent(lm -> lm.setConfidence(locMap.getConfidence()));
		
	}

	@Override
	public void removeLocalityMapping(LocalityMapping locMap) {
		localityMappings.remove(locMap);
	}

	public void replaceWith(GeocoderConfigurationStore configStore) {
		super.replaceWith(configStore);
		unitDesignators = configStore.getUnitDesignators().collect(Collectors.toList());
		abbrevMappings = configStore.getAbbrevMappings().collect(Collectors.toList());
		localityMappings = configStore.getLocalityMappings().collect(Collectors.toList());
		
	}

}
