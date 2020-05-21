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
package ca.bc.gov.ols.geocoder.rest;

import java.util.List;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.api.data.ModifiableLocation;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.AffineTransformation;

public class LocationReprojector {
	private final Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX
			+ LocationReprojector.class.getCanonicalName());
	
	private MathTransform transform = null;
	int toSRSCode = 0;
	boolean isReversedAxes = false;
	
	public LocationReprojector(int fromSRSCode, int toSRSCode) {
		this.toSRSCode = toSRSCode;
		CoordinateReferenceSystem fromCRS = srsCodeToCRS(fromSRSCode);
		CoordinateReferenceSystem toCRS = srsCodeToCRS(toSRSCode);
		isReversedAxes = toCRS.getCoordinateSystem().getAxis(0).getDirection().absolute()
				.equals(AxisDirection.NORTH);
		try {
			transform = CRS.findMathTransform(fromCRS, toCRS);
		} catch(FactoryException e) {
			throw new RuntimeException("Unexpected error in coordinate reprojection.");
		}
	}
	
	public static CoordinateReferenceSystem srsCodeToCRS(int srsCode) {
		try {
			return CRS.decode("EPSG:" + srsCode);
		} catch(NoSuchAuthorityCodeException e) {
			throw new IllegalArgumentException("Invalid srsCode: \"" + srsCode + "\"");
		} catch(FactoryException e) {
			throw new RuntimeException("Unexpected error in coordinate reprojection.");
		}
	}
	
	public void reproject(ModifiableLocation loc) {
		try {
			if(loc.getLocation() != null) {
				Point p = (Point)JTS.transform(loc.getLocation(), transform);
				if(isReversedAxes) {
					p = flipAxes(p);
				}
				p.setSRID(toSRSCode);
				loc.setLocation(p);
			}
		} catch(TransformException te) {
			logger.info("Unexpected error in coordinate reprojection, likely due to user input error."
					+ te.getMessage());
		}
	}
	
	// TODO this entire method should probably be integrated with the geometryFactory
	// in the GeocoderDataStore class
	public void reprecision(ModifiableLocation loc) {
		Point p = loc.getLocation();
		if(toSRSCode == 4326) {
			PrecisionModel model = new PrecisionModel(10000000.0);
			GeometryFactory factory = new GeometryFactory(model);
			CoordinateArraySequence coords = new CoordinateArraySequence(
					new Coordinate[] {new Coordinate(model.makePrecise(p.getX()),
							model.makePrecise(p.getY()))});
			Point pnt = new Point(coords, factory);
			pnt.setSRID(toSRSCode);
			loc.setLocation(pnt);
		} else {
			PrecisionModel model = new PrecisionModel(1000.0);
			GeometryFactory factory = new GeometryFactory(model);
			CoordinateArraySequence coords = new CoordinateArraySequence(
					new Coordinate[] {new Coordinate(model.makePrecise(p.getX()),
							model.makePrecise(p.getY()))});
			Point pnt = new Point(coords, factory);
			pnt.setSRID(toSRSCode);
			loc.setLocation(pnt);
		}
	}
	
	public void reprecision(List<? extends ModifiableLocation> locs) {
		for(ModifiableLocation loc : locs) {
			reprecision(loc);
		}
	}
	
	public void reprecision(ModifiableLocation[] locs) {
		for(ModifiableLocation loc : locs) {
			reprecision(loc);
		}
	}
	
	public void reproject(List<? extends ModifiableLocation> locs) {
		for(ModifiableLocation loc : locs) {
			reproject(loc);
		}
	}
	
	public void reproject(ModifiableLocation[] locs) {
		for(ModifiableLocation loc : locs) {
			reproject(loc);
		}
	}
	
	public static <T extends Geometry> T reprojectGeom(T geom, int toSRSCode) {
		if(geom == null) {
			return null;
		}
		CoordinateReferenceSystem fromCRS = srsCodeToCRS(geom.getSRID());
		CoordinateReferenceSystem toCRS = srsCodeToCRS(toSRSCode);
		try {
			MathTransform transform = CRS.findMathTransform(fromCRS, toCRS);
			if(fromCRS.getCoordinateSystem().getAxis(0).getDirection().absolute()
					.equals(AxisDirection.NORTH)) {
				geom = flipAxes(geom);
			}
			@SuppressWarnings("unchecked")
			T newGeom = (T)JTS.transform(geom, transform);
			if(toCRS.getCoordinateSystem().getAxis(0).getDirection().absolute()
					.equals(AxisDirection.NORTH)) {
				newGeom = flipAxes(newGeom);
			}
			newGeom.setSRID(toSRSCode);
			return newGeom;
		} catch(FactoryException fe) {
			throw new RuntimeException("Unexpected error in coordinate reprojection.", fe);
		} catch(TransformException te) {
			throw new RuntimeException("Unexpected error in coordinate reprojection.", te);
		}
	}
	
	private static <T extends Geometry> T flipAxes(T geom) {
		AffineTransformation transform = new AffineTransformation(0, 1, 0, 1, 0, 0);
		@SuppressWarnings("unchecked")
		T newGeom = (T)transform.transform(geom);
		newGeom.setSRID(geom.getSRID());
		return newGeom;
	}
	
}