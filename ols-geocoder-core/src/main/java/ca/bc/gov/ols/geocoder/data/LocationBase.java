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
package ca.bc.gov.ols.geocoder.data;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;

public class LocationBase implements ILocation, CoordinateSequence {

	private double x;
	private double y;

	LocationBase(Point location) {
		if(location != null) {
			x = location.getCoordinate().x;
			y = location.getCoordinate().y;
		}
	}	

	// ILocation
	
	@Override
	public Point getLocation() {
		return GeocoderDataStore.getGeometryFactory().createPoint(this);
	}
	
	// Coordinate Sequence

	@Override
	public int getDimension() {
		return 2;
	}

	@Override
	public Coordinate getCoordinate(int i) {
		if(i != 0) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
		return new Coordinate(x, y);
	}

	@Override
	public Coordinate getCoordinateCopy(int i) {
		return new Coordinate(x, y);
	}

	@Override
	public void getCoordinate(int index, Coordinate coord) {
		if(index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		coord.x = x;
		coord.y = y;
	}

	@Override
	public double getX(int index) {
		if(index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return x;
	}

	@Override
	public double getY(int index) {
		if(index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return y;
	}

	@Override
	public double getOrdinate(int index, int ordinateIndex) {
		if(index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		if(ordinateIndex == 0) {
			return x;
		} else if(ordinateIndex == 1) {
			return y;
		};
		throw new ArrayIndexOutOfBoundsException(ordinateIndex);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void setOrdinate(int index, int ordinateIndex, double value) {
		if(index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		if(ordinateIndex == 0) {
			x = value;
		} else if(ordinateIndex == 1) {
			y = value;
		};
		throw new ArrayIndexOutOfBoundsException(ordinateIndex);
	}

	@Override
	public Coordinate[] toCoordinateArray() {
		return new Coordinate[] {new Coordinate(x, y)};
	}

	@Override
	public Envelope expandEnvelope(Envelope env) {
		env.expandToInclude(x, y);
		return env;
	}
	
	@Override
	public Object clone() {
		throw new RuntimeException("This coordinate sequence cannot be cloned!");
	}

	@Override
	public CoordinateSequence copy() {
		throw new RuntimeException("This coordinate sequence cannot be copied!");
	}

}
