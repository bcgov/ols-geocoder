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
package ca.bc.gov.ols.geocoder.dra;


import ca.bc.gov.ols.util.StringMapper;
import ca.bc.gov.ols.util.StringSet;

/**
 * The lexicon of well-known symbols used in Lesa address parsing.
 * 
 * Note: for more generality should be changed to use dynamic data.  
 * 
 * @author mbdavis
 *
 */
public class DraLexicon 
{
	public static final String FRACTION_1_2 = "1/2";
	public static final String WORD_ST = "ST";
	public static final String WORD_APT = "APT";
	
	/**
	 * The token values which are skipped while tokenizing the input
	 */
	public static String[] tokenSkipList = new String[] {
					"N/B", "S/B", "W/B", "E/B"
	};

	
	public static String[] blocks = new String[] {
		"BLOCK", "BLK"
	};

	public static String WORD_OF = "OF";
	public static String WORD_ALLEY = "ALLEY";

	public static StringSet numberSuffixSet = new StringSet(
			new String[] {
					"A", "B", "C", "D"
			}
	); 
		
	public static StringSet fractionSet = new StringSet(
			new String[] {
					"1/2"
			}
	); 
	public static StringSet allowedStreetTypesInName = new StringSet(
			new String[] {
					"PARK", "CIRCLE"
			}
	); 
		
	public static String[] city = new String[] {
		"AI","AU","BL","BU","CA","DU","EA","EW",
		"FF","FI","FL","FR","GH","HI","KC","KI",
		"KT","LC","LD","MC","MI","ML","MS","OR",
		"PC","PF","PY","RI","RU","RY","SM","SO",
		"SU","TA","UP","WI"	};
	
	public static StringMapper cityAbbrev = new StringMapper();
	
	static {
		cityAbbrev.putValues(city);
	}
		
	public static StringSet directionalWord = new StringSet(
			new String[] {
					"NORTH", "NORTHWEST","NORTHEAST","SOUTH","SOUTHWEST","SOUTHEAST","EAST","WEST"
			}
	); 

	public static StringMapper directionalAbbrev = new StringMapper();
	
	static {
		directionalAbbrev.put("N", "N");
		directionalAbbrev.put("NO", "N");
		directionalAbbrev.put("NORTH", "N");
		directionalAbbrev.put("NW", "NW");
		directionalAbbrev.put("N W", "NW");
		directionalAbbrev.put("NORTHWEST", "NW");
		directionalAbbrev.put("NE", "NE");
		directionalAbbrev.put("N E", "NE");
		directionalAbbrev.put("NORTHEAST", "NE");
		
		directionalAbbrev.put("S", "S");
		directionalAbbrev.put("SO", "S");
		directionalAbbrev.put("SOUTH", "S");
		
		directionalAbbrev.put("SW", "SW");
		directionalAbbrev.put("S W", "SW");
		directionalAbbrev.put("SOUTHWEST", "SW");
		
		directionalAbbrev.put("SE", "SE");
		directionalAbbrev.put("S E", "SE");
		directionalAbbrev.put("SOUTHEAST", "SE");
		
		directionalAbbrev.put("E", "E");
		directionalAbbrev.put("EA", "E");
		directionalAbbrev.put("EAST", "E");
		
		directionalAbbrev.put("W", "W");
		directionalAbbrev.put("WE", "W");
		directionalAbbrev.put("WEST", "W");
		
		// these appear to be very WA specific
		directionalAbbrev.put("KN", 	"KN");
		directionalAbbrev.put("KPN", 	"KN");
		
		directionalAbbrev.put("KS", 	"KS");
		directionalAbbrev.put("KPS", 	"KS");
		
		// no idea what this is - PC specific?
		directionalAbbrev.put("FI", 	"FI");
		
	}
	
	public static String STREET_TYPE_ST = "ST";
	public static String STREET_TYPE_CT = "CT";
	public static String STREET_TYPE_STCT = "STCT";
	public static String STREET_TYPE_STLP = "STLP";
	public static String STREET_TYPE_STPL = "STPL";
	public static String STREET_TYPE_AVCT = "AVCT";
	
	public static StringMapper streetTypeAbbrev = new StringMapper();
	
	static {
		streetTypeAbbrev.put("AV", 		"AV");	
		streetTypeAbbrev.put("AVE",	 	"AV");
		streetTypeAbbrev.put("AVENU", "AV");
		streetTypeAbbrev.put("AVENUE", "AV");
		
		streetTypeAbbrev.put("AC", "AVCT");
		streetTypeAbbrev.put("AVCT", "AVCT");
		
		streetTypeAbbrev.put("BV", 	 		"BLVD");
		streetTypeAbbrev.put("BL", 			"BLVD");
		streetTypeAbbrev.put("BLV", 		"BLVD");
		streetTypeAbbrev.put("BLVD", 		"BLVD");
		streetTypeAbbrev.put("BVD", 		"BLVD");
		streetTypeAbbrev.put("BOULEVARD","BLVD");
		
		streetTypeAbbrev.put("CI",			"CIR");
		streetTypeAbbrev.put("CIR",			"CIR");
		streetTypeAbbrev.put("CIRCLE",	"CIR");
		
		streetTypeAbbrev.put("CT",			"CT");
		streetTypeAbbrev.put("CRT", 		"CT");
		streetTypeAbbrev.put("COURT",		"CT");
		
		streetTypeAbbrev.put("CR",   		"CR");
		streetTypeAbbrev.put("CRES", 		"CR");
		streetTypeAbbrev.put("CRESCENT",	"CR");
		
		streetTypeAbbrev.put("DR", 			"DR");
		streetTypeAbbrev.put("DRIVE",		"DR");
		
		streetTypeAbbrev.put("HW",			"HWY");
		streetTypeAbbrev.put("HY",			"HWY");
		streetTypeAbbrev.put("HYW",			"HWY");
		streetTypeAbbrev.put("HWY",			"HWY");
		streetTypeAbbrev.put("HIGHWAY",	"HWY");
		
		streetTypeAbbrev.put("LN", 			"LN");
		streetTypeAbbrev.put("LANE",		"LN");
		
		streetTypeAbbrev.put("LP", 			"LP");
		streetTypeAbbrev.put("LOOP", 		"LP");
		
		streetTypeAbbrev.put("PK", 			"PK");
		streetTypeAbbrev.put("PARK", 		"PK");
		
		streetTypeAbbrev.put("PW", 			"PKWY");
		streetTypeAbbrev.put("PKW", 		"PKWY");
		streetTypeAbbrev.put("PKY", 		"PKWY");
		streetTypeAbbrev.put("PKWY",		"PKWY");
		
		streetTypeAbbrev.put("PL",			"PL");
		streetTypeAbbrev.put("PLACE",		"PL");
		
		streetTypeAbbrev.put("RD",			"RD");
		streetTypeAbbrev.put("ROAD", 		"RD");
		
		streetTypeAbbrev.put("SQ",			"SQ");
		streetTypeAbbrev.put("SQUARE",	"SQ");
		
		streetTypeAbbrev.put("ST",			"ST");
		streetTypeAbbrev.put("STREET",	"ST");
		
		streetTypeAbbrev.put("SC", 			"STCT");
		streetTypeAbbrev.put("STCT", 		"STCT");
		
		streetTypeAbbrev.put("SL", 			"STLP");
		
		streetTypeAbbrev.put("SP", 			"STPL");
		
		streetTypeAbbrev.put("TE", 			"TER");
		streetTypeAbbrev.put("TER", 		"TER");
		streetTypeAbbrev.put("TERRACE",	"TER");
		
		streetTypeAbbrev.put("TRL", 		"TRL");
		streetTypeAbbrev.put("TRAIL",		"TRL");
		
		streetTypeAbbrev.put("WY",			"WY");
		streetTypeAbbrev.put("WAY", 		"WY");
	}
	
	public static StringSet directionalSet = new StringSet(directionalAbbrev.keySet());

	public static StringSet streetTypeSet = new StringSet(streetTypeAbbrev.keySet());

	public static StringSet citySet = new StringSet(city);
	
	public static StringSet blockSet = new StringSet(blocks);

	public static StringMapper nameExpansion = new StringMapper();
	
	static {
		nameExpansion.put("KP", 	"KEY PENINSULA");
		nameExpansion.put("MLK", 	"MARTIN LUTHER KING");
		nameExpansion.put("PAC", 	"PACIFIC");
		nameExpansion.put("PKW", 	"PARKWAY");
		nameExpansion.put("TMB", 	"TACOMA MALL");
	}

	public static StringMapper nameFirstExpansion = new StringMapper();
	
	static {
		nameFirstExpansion.put("MLK", 	"MARTIN LUTHER KING");

		/*
		 // don't need to expand these now, because they will always be assigned to directional
		nameFirstExpansion.put("N", 	"NORTH");
		nameFirstExpansion.put("NO", 	"NORTH");
		nameFirstExpansion.put("S", 	"SOUTH");
		nameFirstExpansion.put("SO", 	"SOUTH");
		nameFirstExpansion.put("E", 	"EAST");
		nameFirstExpansion.put("EA", 	"EAST");
		nameFirstExpansion.put("W", 	"WEST");
		nameFirstExpansion.put("WE", 	"WEST");
		
		nameFirstExpansion.put("NW", 	"NORTHWEST");
		nameFirstExpansion.put("NE", 	"NORTHEAST");
		nameFirstExpansion.put("SW", 	"SOUTHWEST");
		nameFirstExpansion.put("SE", 	"SOUTHEAST");
		*/
		nameFirstExpansion.put("CT", 	"COURT");
		nameFirstExpansion.put("AVE", "AVENUE");
		nameFirstExpansion.put("LK", 	"LAKE");
		nameFirstExpansion.put("PR", 	"PRINCE");
	}

	public static StringMapper nameLastExpansion = new StringMapper();
	
	static {
		nameLastExpansion.put("LP", "LOOP");
		nameLastExpansion.put("LK", "LAKE");
		nameLastExpansion.put("LN", "LANE");
		
		nameLastExpansion.put("N", 	"NORTH");
		nameLastExpansion.put("NO", "NORTH");
		nameLastExpansion.put("S", 	"SOUTH");
		nameLastExpansion.put("SO", "SOUTH");
		nameLastExpansion.put("E", 	"EAST");
		nameLastExpansion.put("EA", "EAST");
		nameLastExpansion.put("W", 	"WEST");
		nameLastExpansion.put("WE", "WEST");
		
		nameLastExpansion.put("NW", "NORTHWEST");
		nameLastExpansion.put("NE", "NORTHEAST");
		nameLastExpansion.put("SW", "SOUTHWEST");
		nameLastExpansion.put("SE", "SOUTHEAST");
	}


}
