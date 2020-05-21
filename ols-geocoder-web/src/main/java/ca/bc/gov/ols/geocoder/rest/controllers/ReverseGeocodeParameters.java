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
package ca.bc.gov.ols.geocoder.rest.controllers;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import ca.bc.gov.ols.geocoder.api.GeometryReprojector;
import ca.bc.gov.ols.geocoder.api.SharedParameters;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.util.GeomParseUtil;

public class ReverseGeocodeParameters extends SharedParameters {
	
	private int minDegree = 2;
	private int maxDegree = 100;
	private double[] point;
	private Point pointPoint;
	private double[] bbox;
	private Polygon bboxPolygon;
	private Integer maxDistance;
	private boolean excludeUnits = false;
	private boolean onlyCivic = false;
	
	public int getMinDegree() {
		return minDegree;
	}
	
	public void setMinDegree(int minDegree) {
		this.minDegree = minDegree;
	}
	
	public int getMaxDegree() {
		return maxDegree;
	}
	
	public void setMaxDegree(int maxDegree) {
		this.maxDegree = maxDegree;
	}
	
	public Point getPoint() {
		return pointPoint;
	}
	
	public void setPoint(double[] point) {
		this.point = point;
	}
	
	public void setPointGeom(Point point) {
		this.pointPoint = point;
	}
	
	public Polygon getBbox() {
		return bboxPolygon;
	}
	
	public void setBbox(double[] bbox) {
		this.bbox = bbox;
	}
	
	public Integer getMaxDistance() {
		return maxDistance;
	}
	
	public void setMaxDistance(Integer maxDistance) {
		this.maxDistance = maxDistance;
	}
	
	public void setExcludeUnits(boolean excludeUnits) {
		this.excludeUnits = excludeUnits;
	}
	
	public boolean getExcludeUnits() {
		return excludeUnits;
	}
	
	public boolean isOnlyCivic() {
		return onlyCivic;
	}

	public void setOnlyCivic(boolean onlyCivic) {
		this.onlyCivic = onlyCivic;
	}

	public void resolveAndValidate(GeocoderConfig config, GeometryFactory gf, GeometryReprojector gr) {
		// convert any double[] into geometries in inputSRS projection
		// then reproject any geometries to the internal projection
		// note the incoming geomtries may either come in as double[]
		// or as geometry objects in the inputSRS projection
		// so we may not need to create them from double[] but still need to reproject
		if(point != null && point.length != 0) {
			if(point.length == 2) {
				pointPoint = gf.createPoint(new Coordinate(point[0], point[1]));
			} else {
				throw new IllegalArgumentException(
						"Parameter \"point\" must be in the format \"x,y\".");
			}
		}
		if(pointPoint != null) {
			pointPoint = gr.reproject(pointPoint, config.getBaseSrsCode());
		}
		if(bbox != null && bbox.length != 0) {
			if(bbox.length == 4) {
				bboxPolygon = GeomParseUtil.buildBbox(bbox, gf);
			} else {
				throw new IllegalArgumentException(
						"Parameter \"bbox\" must be in the format \"minx,miny,maxx,maxy\".");
			}
		}
		if(bboxPolygon != null) {
			bboxPolygon = gr.reproject(bboxPolygon, config.getBaseSrsCode());
		}		
	}

}
