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

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.data.enumTypes.LocalityType;

public class Locality {
	private final int id;
	private final String name;
	private final String qualifier; 
	private final LocalityType type;
	private final String electoralArea;
	private final StateProvTerr stateProvTerr;
	private final Point location;
	
	public Locality(int id, String name, String qualifier, LocalityType type, String electoralArea, StateProvTerr stateProvTerr,
			Point location) {
		this.id = id;
		this.name = name;
		this.qualifier = qualifier;
		this.type = type;
		this.electoralArea = electoralArea;
		this.stateProvTerr = stateProvTerr;
		this.location = location;
	}
	
	public int getId() {
		return id;
	}
	
	public String getFullyQualifiedName() {
		if(qualifier != null && !qualifier.isEmpty())
			return name + " " + qualifier;
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getQualifier() {
		return qualifier;
	}
	
	public LocalityType getType() {
		return type;
	}
	
	public String getElectoralArea() {
		return electoralArea;
	}
	
	public StateProvTerr getStateProvTerr() {
		return stateProvTerr;
	}
	
	public Point getLocation() {
		return location;
	}
	
	@Override
	public String toString() {
		return name + "(" + type + ")";
	}
	
}
