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
package ca.bc.gov.ols.rangebuilder;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

public abstract class AbstractRbSite implements IRbSite, CoordinateSequence {

	private int siteId;
	private String inputName;
	private int civicNumber;
	private String fullAddress;
	private int streetSegmentId;
	private int localityId;
	private double siteX;
	private double siteY;
	private double measure;
	private int interimStreetNameId;

	public AbstractRbSite() {
		super();
	}
	
	@Override
	public int getSiteId() {
		return siteId;
	}

	@Override
	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	@Override
	public String getInputName() {
		return inputName;
	}

	@Override
	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	@Override
	public int getCivicNumber() {
		return civicNumber;
	}

	@Override
	public void setCivicNumber(int civicNumber) {
		this.civicNumber = civicNumber;
	}

	@Override
	public String getFullAddress() {
		return fullAddress;
	}

	@Override
	public void setFullAddress(String fullAddress) {
		this.fullAddress = fullAddress;
	}

	@Override
	public int getStreetSegmentId() {
		return streetSegmentId;
	}

	@Override
	public void setStreetSegmentId(int streetSegmentId) {
		this.streetSegmentId = streetSegmentId;
	}

	@Override
	public int getLocalityId() {
		return localityId;
	}

	@Override
	public void setLocalityId(int localityId) {
		this.localityId = localityId;
	}

	@Override
	public void setInterimStreetNameId(int interimStreetNameId) {
		this.interimStreetNameId = interimStreetNameId;
	}

	@Override
	public int getInterimStreetNameId() {
		return interimStreetNameId;
	}

	public Point getSiteLocation() {
		if(Double.isNaN(siteX) || Double.isNaN(siteY)) {
			return null;
		}
		return RangeBuilder.getGeometryFactory().createPoint(this);
	}

	public void setSiteX(double siteX) {
		this.siteX = siteX;
	}

	public void setSiteY(double siteY) {
		this.siteY = siteY;
	}

	public double getMeasure() {
		return measure;
	}
	
	public void setMeasure(double measure) {
		this.measure = measure;
	}

	// Coordinate Sequence Interface Requirements
	
	@Override
	public int getDimension() {
		return 2;
	}

	@Override
	public Coordinate getCoordinate(int i) {
		if(i != 0) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
		return new Coordinate(siteX, siteY);
	}

	@Override
	public Coordinate getCoordinateCopy(int i) {
		return new Coordinate(siteX, siteY);
	}

	@Override
	public void getCoordinate(int index, Coordinate coord) {
		if(index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		coord.x = siteX;
		coord.y = siteY;
	}

	@Override
	public double getX(int index) {
		if(index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return siteX;
	}

	@Override
	public double getY(int index) {
		if(index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return siteY;
	}

	@Override
	public double getOrdinate(int index, int ordinateIndex) {
		if(index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		if(ordinateIndex == 0) {
			return siteX;
		} else if(ordinateIndex == 1) {
			return siteY;
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
			siteX = value;
		} else if(ordinateIndex == 1) {
			siteY = value;
		};
		throw new ArrayIndexOutOfBoundsException(ordinateIndex);
	}

	@Override
	public Coordinate[] toCoordinateArray() {
		return new Coordinate[] {new Coordinate(siteX, siteY)};
	}

	@Override
	public Envelope expandEnvelope(Envelope env) {
		env.expandToInclude(siteX, siteY);
		return env;
	}
	
	public String toString() {
		return "Site(" + getCivicNumber() + ")"; 
	}
	
	public Object clone() {
		throw new RuntimeException("Don't clone this FFS!");
	}

}