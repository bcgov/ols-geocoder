package ca.bc.gov.ols.streetprep;

import org.locationtech.jts.geom.Geometry;

import ca.bc.gov.ols.rowreader.RowReader;

public class CustomCity {
	String locBase;
	String inType;
	int inTypeId; 
	int inLocId;
	String outType; 
	int outTypeId;
	int outLocId; 
	Geometry geom;
	
	public CustomCity(RowReader rr) {
		locBase = rr.getString("LOC_BASE");
		inType = rr.getString("IN_TYP");
		inTypeId = rr.getInt("IN_TYP_ID");
		inLocId = rr.getInt("IN_LOC_ID");
		outType = rr.getString("OUT_TYP");
		outTypeId = rr.getInt("OUT_TYP_ID");
		outLocId = rr.getInt("OUT_LOC_ID");
		geom = rr.getGeometry();
	}
}
