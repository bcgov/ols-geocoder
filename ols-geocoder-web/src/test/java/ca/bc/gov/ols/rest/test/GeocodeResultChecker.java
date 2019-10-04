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
package ca.bc.gov.ols.rest.test;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.test.web.servlet.MvcResult;

public class GeocodeResultChecker {

	Object contentObj;
	
	public static GeocodeResultChecker check(MvcResult result) {
		return new GeocodeResultChecker(result);
	}
	
	public GeocodeResultChecker(MvcResult result) {
		try {
			contentObj = JSONValue.parse(result.getResponse().getContentAsString());
		} catch (UnsupportedEncodingException e) {
			fail(e.toString());
		}
	}

	public GeocodeResultChecker(JSONObject obj) {
		contentObj = obj;
	}

	public GeocodeResultChecker getFirst() {
		if(contentObj instanceof JSONArray) {
			return new GeocodeResultChecker((JSONObject)(((JSONArray)contentObj).get(0)));
		} else if(contentObj instanceof JSONObject
				&& ((JSONObject)contentObj).get("features") instanceof JSONArray) {
			return new GeocodeResultChecker((JSONObject)((JSONArray)((JSONObject)contentObj).get("features")).get(0));
		}
		fail("not an array; cannot 'getFirst()'");
		return null;
	}
	
	public GeocodeResultChecker property(String property, Object value) {
		if(contentObj instanceof JSONObject) {
			JSONObject obj =((JSONObject)((JSONObject)contentObj).get("properties"));
			if(obj == null) {
				fail("not a valid object; try 'getFirst()'?");
			}
			if(obj.containsKey(property)) {
				Object propVal = obj.get(property);
				if(!propVal.equals(value)) {
					fail("value of '" + property + "' is '" + propVal.toString() 
							+ " instead of expected value '" + value + "'");
				}
			} else {
				fail("Property '" + property + "' does not exist");
			}
		} else {
			fail("result is not a JSONObject, it is a '" + contentObj.getClass().getName() + "'");
		}
		return this;
	}
	
}
