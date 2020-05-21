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
package ca.bc.gov.ols.geocoder.api.data;

public class AddressComponentMisspellings {
	private int siteNameMS = 0;
	private int unitNumberMS = 0;
	private int unitDesignatorMS = 0;
	private int[] streetNameMS;
	private int[] streetTypeMS;
	private int[] streetDirectionMS;
	private int[] streetQualifierMS;
	private int localityMS = 0;
	private int stateProvTerrMS = 0;
	
	public AddressComponentMisspellings() {
		this(1);
	}
	
	public AddressComponentMisspellings(int numStreets) {
		streetNameMS = new int[numStreets];
		streetTypeMS = new int[numStreets];
		streetDirectionMS = new int[numStreets];
		streetQualifierMS = new int[numStreets];
	}
	
	public int getSiteNameMS() {
		return siteNameMS;
	}
	
	public void setSiteNameMS(int siteNameMS) {
		this.siteNameMS = siteNameMS;
	}
	
	public int getUnitNumberMS() {
		return unitNumberMS;
	}
	
	public void setUnitNumberMS(int unitNumberMS) {
		this.unitNumberMS = unitNumberMS;
	}
	
	public int getUnitDesignatorMS() {
		return unitDesignatorMS;
	}
	
	public void setUnitDesignatorMS(int unitDesignatorMS) {
		this.unitDesignatorMS = unitDesignatorMS;
	}
	
	public int getStreetNameMS() {
		return streetNameMS[0];
	}
	
	public int getStreetNameMS(int num) {
		return this.streetNameMS[num];
	}
	
	public void setStreetNameMS(int streetNameMS) {
		this.streetNameMS[0] = streetNameMS;
	}
	
	public void setStreetNameMS(int num, int streetNameMS) {
		this.streetNameMS[num] = streetNameMS;
	}
	
	public int getStreetTypeMS() {
		return streetTypeMS[0];
	}
	
	public int getStreetTypeMS(int num) {
		return this.streetTypeMS[num];
	}
	
	public void setStreetTypeMS(int streetTypeMS) {
		this.streetTypeMS[0] = streetTypeMS;
	}
	
	public void setStreetTypeMS(int num, int streetTypeMS) {
		this.streetTypeMS[num] = streetTypeMS;
	}
	
	public int getStreetDirectionMS() {
		return streetDirectionMS[0];
	}
	
	public int getStreetDirectionMS(int num) {
		return this.streetDirectionMS[num];
	}
	
	public void setStreetDirectionMS(int streetDirectionMS) {
		this.streetDirectionMS[0] = streetDirectionMS;
	}
	
	public void setStreetDirectionMS(int num, int streetDirectionMS) {
		this.streetDirectionMS[num] = streetDirectionMS;
	}
	
	public int getStreetQualifierMS() {
		return streetQualifierMS[0];
	}
	
	public int getStreetQualifierMS(int num) {
		return streetQualifierMS[num];
	}
	
	public void setStreetQualifierMS(int streetQualifierMS) {
		this.streetQualifierMS[0] = streetQualifierMS;
	}
	
	public void setStreetQualifierMS(int num, int streetQualifierMS) {
		this.streetQualifierMS[num] = streetQualifierMS;
	}
	
	public int getLocalityMS() {
		return localityMS;
	}
	
	public void setLocalityMS(int localityMS) {
		this.localityMS = localityMS;
	}
	
	public int getStateProvTerrMS() {
		return stateProvTerrMS;
	}
	
	public void setStateProvTerrMS(int stateProvTerrMS) {
		this.stateProvTerrMS = stateProvTerrMS;
	}
	
}
