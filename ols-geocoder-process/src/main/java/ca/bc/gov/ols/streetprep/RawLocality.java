package ca.bc.gov.ols.streetprep;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.geocoder.data.enumTypes.LocalityType;
import gnu.trove.map.TIntObjectMap;

public class RawLocality {
	public int id;
	public String name;
	public String disambiguator;
	public String source;
	public LocalityType type;
	public int stateProvTerrId;
	public Integer electoralAreaId;
	public String descriptor;
	public Integer containingLocalityId;
	public Geometry geom;
	public Point point;
	
	public String getFullyQualifiedName(TIntObjectMap<RawLocality> localityMap) {
		if("BCGNIS".equals(source)) {
			// we add "near" for IRs and "in" for other gnis localities
			String descriptor = " in ";
			if(type.equals(LocalityType.INDIAN_RESERVE)) {
				descriptor = " near ";
			}
			String cName = localityMap.get(containingLocalityId).name;
			if(disambiguator != null) {
				return name + " (" + disambiguator +")" + descriptor + cName;
			}			
			return name + descriptor + cName;
		}
		return name;
	}
}
