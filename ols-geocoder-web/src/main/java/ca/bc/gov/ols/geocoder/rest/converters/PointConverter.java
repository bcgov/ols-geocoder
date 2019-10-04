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
package ca.bc.gov.ols.geocoder.rest.converters;

import org.springframework.core.convert.converter.Converter;

import ca.bc.gov.ols.util.GeomParseUtil;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class PointConverter implements Converter<String, Point> {
	
	private GeometryFactory gf;
	
	public PointConverter(GeometryFactory gf) {
		this.gf = gf;
	}
	
	@Override
	public Point convert(String in) {
		try {
			double[] coords = GeomParseUtil.parseDoubleArray(in);
			return gf.createPoint(new Coordinate(coords[0], coords[1]));
		} catch(NumberFormatException nfe) {
			throw new IllegalArgumentException(
					"Parameter must be in the format \"xmin,ymin,xmax,ymax\".");
		}
	}
	
}
