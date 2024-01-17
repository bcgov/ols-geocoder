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
package ca.bc.gov.ols.admin;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.stream.JsonWriter;

import ca.bc.gov.ols.config.ConfigurationParameter;
import ca.bc.gov.ols.geocoder.config.AbbreviationMapping;
import ca.bc.gov.ols.geocoder.config.FileExportGeocoderConfigurationStore;
import ca.bc.gov.ols.geocoder.config.GeocoderConfigurationComparison;
import ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStore;
import ca.bc.gov.ols.geocoder.config.LocalityMapping;
import ca.bc.gov.ols.geocoder.config.UnitDesignator;

@RestController
public class AdminController {
	final static Logger logger = LoggerFactory.getLogger(
			AdminController.class.getCanonicalName());
	
	@Autowired
	private AdminApplication adminApp;
	
	
	@RequestMapping(value = "/export", produces = "application/json")
	public void doExport(HttpServletResponse response) throws IOException {
		GeocoderConfigurationStore configStore = adminApp.getConfigStore();
		
		// export all of the data in the database
		response.addHeader("Content-Type", "application/json");
		response.addHeader("Content-Disposition", "attachment; filename=ols_admin_config_export.json");
		response.setCharacterEncoding("UTF-8");
		JsonWriter jw = new JsonWriter(response.getWriter());
		jw.setIndent("  ");
		jw.beginObject();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		jw.name("exportDate").value(dateFormat.format(new Date()));
		
		// BGEO_CONFIGURATION_PARAMETERS
		jw.name("BGEO_CONFIGURATION_PARAMETERS");
		jw.beginObject();
		Stream<ConfigurationParameter> configParams = configStore.getConfigParams();
		jw.name("rows");
		jw.beginArray();
		int cpCount = 0;
		for (Iterator<ConfigurationParameter> it = configParams.iterator(); it.hasNext();) {
			cpCount++;
			ConfigurationParameter param = it.next();
			jw.beginObject();
			jw.name("app_id").value(param.getAppId());
			jw.name("config_param_name").value(param.getConfigParamName());
			jw.name("config_param_value").value(param.getConfigParamValue());
			jw.endObject();
		}
		jw.endArray();
		jw.name("rowCount").value(cpCount);
		jw.endObject();
		
		// BGEO_ABBREVIATION_MAPPINGS
		jw.name("BGEO_ABBREVIATION_MAPPINGS");
		jw.beginObject();
		Stream<AbbreviationMapping> abbrMaps = configStore.getAbbrevMappings();
		jw.name("rows");
		jw.beginArray();
		int amCount = 0;
		for (Iterator<AbbreviationMapping> it = abbrMaps.iterator(); it.hasNext();) {
			amCount++;
			AbbreviationMapping abbrMap = it.next();
			jw.beginObject();
			jw.name("abbreviated_form").value(abbrMap.getAbbreviatedForm());
			jw.name("long_form").value(abbrMap.getLongForm());
			jw.endObject();
		}
		jw.endArray();
		jw.name("rowCount").value(amCount);
		jw.endObject();
		
		// BGEO_UNIT_DESIGNATORS
		Stream<UnitDesignator> unitDesigs = configStore.getUnitDesignators();
		jw.name("BGEO_UNIT_DESIGNATORS");
		jw.beginObject();
		jw.name("rows");
		jw.beginArray();
		int udCount = 0;
		for (Iterator<UnitDesignator> it = unitDesigs.iterator(); it.hasNext();) {
			udCount++;
			UnitDesignator unitDesig = it.next();
			jw.beginObject();
			jw.name("canonical_form").value(unitDesig.getCanonicalForm());
			jw.endObject();
		}
		jw.endArray();
		jw.name("rowCount").value(udCount);
		jw.endObject();
		
		// BGEO_LOCALITY_MAPPINGS
		Stream<LocalityMapping> locMaps = configStore.getLocalityMappings();
		jw.name("BGEO_LOCALITY_MAPPINGS");
		jw.beginObject();
		jw.name("rows");
		jw.beginArray();
		int lmCount = 0;
		for (Iterator<LocalityMapping> it = locMaps.iterator(); it.hasNext();) {
			lmCount++;
			LocalityMapping locMap = it.next();
			jw.beginObject();
			jw.name("locality_id").value(locMap.getLocalityId());
			jw.name("input_string").value(locMap.getInputString());
			jw.name("confidence").value(locMap.getConfidence());
			jw.endObject();
		}
		jw.endArray();
		jw.name("rowCount").value(lmCount);
		jw.endObject();

		jw.endObject();
		jw.flush();
		jw.close();
	}
	
	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	public ModelAndView doValidate(@RequestParam("file") MultipartFile file) {
		GeocoderConfigurationStore exportConfig = new FileExportGeocoderConfigurationStore(file);
		GeocoderConfigurationStore localConfig = adminApp.getConfigStore();
		GeocoderConfigurationComparison comparison = new GeocoderConfigurationComparison(localConfig, exportConfig);
		ModelAndView modelAndView = new ModelAndView("view/validate", "exportConfig", exportConfig);
		modelAndView.addObject("comparison", comparison);
		return modelAndView;
	}

	@RequestMapping(value = "/import", method = RequestMethod.POST)
	public ModelAndView doImport(@RequestParam("file") MultipartFile file) {
		FileExportGeocoderConfigurationStore exportConfig = new FileExportGeocoderConfigurationStore(file);
		if(exportConfig.getErrors().isEmpty()) {
			adminApp.getConfigStore().replaceWith(exportConfig);
			return new ModelAndView("view/import", "errors", exportConfig.getErrors());
		}
		return new ModelAndView("view/import", "messages", exportConfig.getMessages());
	}
	
}
