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
package ca.bc.gov.ols.geocoder.util;

import java.io.StringReader;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class GeometryAdapter extends XmlAdapter<String,Geometry> {

	@Override
	public Geometry unmarshal(String v) throws Exception {
		WKTReader reader = new WKTReader();
		try {
			return reader.read(new StringReader(v));
		} catch(ParseException pe) {
			return null;
		}
	}

	@Override
	public String marshal(Geometry v) throws Exception {
		WKTWriter writer = new WKTWriter();
		return writer.write(v);
	}

}
