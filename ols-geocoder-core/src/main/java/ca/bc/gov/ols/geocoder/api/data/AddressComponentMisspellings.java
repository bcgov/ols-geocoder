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

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.ols.geocoder.data.indexing.MisspellingOf;
import ca.bc.gov.ols.geocoder.data.indexing.Word;

public class AddressComponentMisspellings {
	// each is a list of the misspellings for each word of each component
	// the length of the list is at most the number of words in the component 
	private List<MisspellingOf<Word>> siteNameMS = null;
	private List<MisspellingOf<Word>> unitNumberMS = null;
	private List<MisspellingOf<Word>> unitDesignatorMS = null;
	// these are lists of lists, one for each street name in an intersection
	private List<List<MisspellingOf<Word>>> streetNameMS;
	private List<List<MisspellingOf<Word>>> streetTypeMS;
	private List<List<MisspellingOf<Word>>> streetDirectionMS;
	private List<List<MisspellingOf<Word>>> streetQualifierMS;
	
	private List<MisspellingOf<Word>> localityMS = null;
	private List<MisspellingOf<Word>> stateProvTerrMS = null;
	
	public AddressComponentMisspellings() {
		this(1);
	}
	
	public AddressComponentMisspellings(int numStreets) {
		streetNameMS = makeList(numStreets);
		streetTypeMS = makeList(numStreets);
		streetDirectionMS = makeList(numStreets);
		streetQualifierMS = makeList(numStreets);
	}
	
	private List<List<MisspellingOf<Word>>> makeList(int num) {
		ArrayList<List<MisspellingOf<Word>>> list = new ArrayList<List<MisspellingOf<Word>>>(num);
		for(int i = 0; i < num; i++) {
			list.add(null);
		}
		return list;
	}
	
	public List<MisspellingOf<Word>> getSiteNameMS() {
		return siteNameMS;
	}

	public String getSiteNameMSString() {
		return combineMispellings(siteNameMS);
	}

	public int getSiteNameMSError() {
		return sumErrors(siteNameMS);
	}

	public void setSiteNameMS(List<MisspellingOf<Word>> siteNameMS) {
		this.siteNameMS = siteNameMS;
	}
	
	public List<MisspellingOf<Word>> getUnitNumberMS() {
		return unitNumberMS;
	}

	public String getUnitNumberMSString() {
		return combineMispellings(unitNumberMS);
	}

	public int getUnitNumberMSError() {
		return sumErrors(unitNumberMS);
	}

	public void setUnitNumberMS(List<MisspellingOf<Word>> unitNumberMS) {
		this.unitNumberMS = unitNumberMS;
	}
	
	public List<MisspellingOf<Word>> getUnitDesignatorMS() {
		return unitDesignatorMS;
	}

	public String getUnitDesignatorMSString() {
		return combineMispellings(unitDesignatorMS);
	}

	public int getUnitDesignatorMSError() {
		return sumErrors(unitDesignatorMS);
	}

	public void setUnitDesignatorMS(List<MisspellingOf<Word>> unitDesignatorMS) {
		this.unitDesignatorMS = unitDesignatorMS;
	}
	
	public List<MisspellingOf<Word>> getStreetNameMS() {
		return streetNameMS.get(0);
	}

	public String getStreetNameMSString() {
		return combineMispellings(streetNameMS.get(0));
	}
	
	public int getStreetNameMSError() {
		return sumErrors(streetNameMS.get(0));
	}

	public List<MisspellingOf<Word>> getStreetNameMS(int num) {
		return streetNameMS.get(num);
	}

	public String getStreetNameMSString(int num) {
		return combineMispellings(streetNameMS.get(num));
	}

	public int getStreetNameMSError(int num) {
		return sumErrors(streetNameMS.get(num));
	}

	public void setStreetNameMS(List<MisspellingOf<Word>> streetNameMS) {
		this.streetNameMS.set(0, streetNameMS);
	}
	
	public void setStreetNameMS(int num, List<MisspellingOf<Word>> streetNameMS) {
		this.streetNameMS.set(num, streetNameMS);
	}
	
	public List<MisspellingOf<Word>> getStreetTypeMS() {
		return streetTypeMS.get(0);
	}

	public String getStreetTypeMSString() {
		return combineMispellings(streetTypeMS.get(0));
	}

	public int getStreetTypeMSError() {
		return sumErrors(streetTypeMS.get(0));
	}

	public List<MisspellingOf<Word>> getStreetTypeMS(int num) {
		return streetTypeMS.get(num);
	}

	public String getStreetTypeMSString(int num) {
		return combineMispellings(streetTypeMS.get(num));
	}

	public int getStreetTypeMSError(int num) {
		return sumErrors(streetTypeMS.get(num));
	}

	public void setStreetTypeMS(List<MisspellingOf<Word>> streetTypeMS) {
		this.streetTypeMS.set(0, streetTypeMS);
	}
	
	public void setStreetTypeMS(int num, List<MisspellingOf<Word>> streetTypeMS) {
		this.streetTypeMS.set(num, streetTypeMS);
	}
	
	public List<MisspellingOf<Word>> getStreetDirectionMS() {
		return streetDirectionMS.get(0);
	}

	public String getStreetDirectionMSString() {
		return combineMispellings(streetDirectionMS.get(0));
	}

	public int getStreetDirectionMSError() {
		return sumErrors(streetDirectionMS.get(0));
	}

	public List<MisspellingOf<Word>> getStreetDirectionMS(int num) {
		return streetDirectionMS.get(num);
	}
	
	public String getStreetDirectionMSString(int num) {
		return combineMispellings(streetDirectionMS.get(num));
	}

	public int getStreetDirectionMSError(int num) {
		return sumErrors(streetDirectionMS.get(num));
	}

	public void setStreetDirectionMS(List<MisspellingOf<Word>> streetDirectionMS) {
		this.streetDirectionMS.set(0, streetDirectionMS);
	}
	
	public void setStreetDirectionMS(int num, List<MisspellingOf<Word>> streetDirectionMS) {
		this.streetDirectionMS.set(num, streetDirectionMS);
	}
	
	public List<MisspellingOf<Word>> getStreetQualifierMS() {
		return streetQualifierMS.get(0);
	}

	public String getStreetQualifierMSString() {
		return combineMispellings(streetQualifierMS.get(0));
	}

	public int getStreetQualifierMSError() {
		return sumErrors(streetQualifierMS.get(0));
	}

	public List<MisspellingOf<Word>> getStreetQualifierMS(int num) {
		return streetQualifierMS.get(num);
	}

	public String getStreetQualifierMSString(int num) {
		return combineMispellings(streetQualifierMS.get(num));
	}

	public int getStreetQualifierMSError(int num) {
		return sumErrors(streetQualifierMS.get(num));
	}

	public void setStreetQualifierMS(List<MisspellingOf<Word>> streetQualifierMS) {
		this.streetQualifierMS.set(0, streetQualifierMS);
	}
	
	public void setStreetQualifierMS(int num, List<MisspellingOf<Word>> streetQualifierMS) {
		this.streetQualifierMS.set(num, streetQualifierMS);
	}
	
	public List<MisspellingOf<Word>> getLocalityMS() {
		return localityMS;
	}
	
	public String getLocalityMSString() {
		return combineMispellings(localityMS);
	}
	
	public int getLocalityMSError() {
		return sumErrors(localityMS);
	}
	
	public void setLocalityMS(List<MisspellingOf<Word>> localityMS) {
		this.localityMS = localityMS;
	}
	
	public List<MisspellingOf<Word>> getStateProvTerrMS() {
		return stateProvTerrMS;
	}

	public String getStateProvTerrMSString() {
		return combineMispellings(stateProvTerrMS);
	}

	public int getStateProvTerrMSError() {
		return sumErrors(stateProvTerrMS);
	}

	public void setStateProvTerrMS(List<MisspellingOf<Word>> stateProvTerrMS) {
		this.stateProvTerrMS = stateProvTerrMS;
	}
	
	public String wasAutoCompleted() {
		String misspelling = wasAutoCompleted(siteNameMS);
		if(misspelling != null) {
			return misspelling;
		}
		misspelling = wasAutoCompleted(unitNumberMS);
		if(misspelling != null) {
			return misspelling;
		}
		misspelling = wasAutoCompleted(unitDesignatorMS);
		if(misspelling != null) {
			return misspelling;
		}
		misspelling = wasListAutoCompleted(streetNameMS);
		if(misspelling != null) {
			return misspelling;
		}
		misspelling = wasListAutoCompleted(streetTypeMS);
		if(misspelling != null) {
			return misspelling;
		}
		misspelling = wasListAutoCompleted(streetDirectionMS);
		if(misspelling != null) {
			return misspelling;
		}
		misspelling = wasListAutoCompleted(streetQualifierMS);
		if(misspelling != null) {
			return misspelling;
		}
		misspelling = wasAutoCompleted(localityMS);
		if(misspelling != null) {
			return misspelling;
		}
		misspelling = wasAutoCompleted(stateProvTerrMS);
		if(misspelling != null) {
			return misspelling;
		}

		return null;
	}

	private String wasListAutoCompleted(List<List<MisspellingOf<Word>>> listOfLists) {
		for(List<MisspellingOf<Word>> list: listOfLists) {
			String misspelling = wasAutoCompleted(list);
			if(misspelling != null) return misspelling;
		}
		return null;
	}

	private String wasAutoCompleted(List<MisspellingOf<Word>> list) {
		if(list == null) return null;
		for(MisspellingOf<Word> ms: list) {
			if(ms.getError() < 0) return ms.getMisspelling();
		}
		return null;
	}
	
	private static int sumErrors(List<MisspellingOf<Word>> misspellingList) {
		int error = 0;
		if(misspellingList != null) {
			for(MisspellingOf<Word> mw : misspellingList) {
				error += mw.getError();
			}
		}
		return error;
	}

	private static String combineMispellings(List<MisspellingOf<Word>> misspellingList) {
		String misspelling = "";
		if(misspellingList != null) {
			for(MisspellingOf<Word> mw : misspellingList) {
				misspelling += misspelling.isEmpty() ? mw.getMisspelling() : " " + mw.getMisspelling();
			}
		}
		return misspelling;
	}


}
