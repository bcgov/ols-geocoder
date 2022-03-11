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
package ca.bc.gov.ols.geocoder.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeocoderUtil {
	
	private static final Pattern NUMBER_WITH_ORDINAL_PATTERN = Pattern
			.compile("(?i)(\\d{1,8})(ST|TH|RD|ND|E|ER|RE|EME|ERE|IEME|IERE)");
	private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("[\\s,]+");
	
	public static boolean equalsIgnoreCaseNullSafe(String a, String b) {
		if(a == null && b == null) {
			return true;
		}
		if((a == null && b.equals(""))
				|| (b == null && a.equals(""))) {
			return true;
		}
		if(a == null || b == null) {
			return false;
		}
		return a.equalsIgnoreCase(b);
	}
	
	public static boolean equalsNullSafe(Integer a, Integer b) {
		if(a == null && b == null) {
			return true;
		}
		if(a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}
	
	public static String[] wordSplit(String in) {
		if(in == null || in.isEmpty()) {
			return new String[0];
		}
		return WORD_SPLIT_PATTERN.split(in);
	}
	
	public static Boolean charToBoolean(String in) {
		if(in == null) {
			return null;
		}
		if(in.equals("N")) {
			return Boolean.FALSE;
		} else if(in.equals("Y")) {
			return Boolean.TRUE;
		}
		return null;
	}
	
	public static String removeOrdinal(String str) {
		Matcher m = NUMBER_WITH_ORDINAL_PATTERN.matcher(str);
		if(m.matches()) {
			return m.group(1);
		}
		return str;
	}
	
	public static String nullSafeTrim(String string) {
		if(string == null) {
			return null;
		}
		return string.trim();
	}
	
	public static int parseCivicNumber(String string) {
		if(string.charAt(0) == '0' && string.length() > 1) {
			string = "-" + string.substring(1);
		}
		return Integer.parseInt(string);
	}
	
	public static String formatCivicNumber(Integer in) {
		if(in == null) {
			return null;
		}
		if(in < 0) {
			return "0" + Math.abs(in);
		}
		return "" + in;
	}
	
}
