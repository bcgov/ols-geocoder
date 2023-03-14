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

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.bc.gov.ols.geocoder.lexer.CleanRule;
import ca.bc.gov.ols.geocoder.lexer.JoinRule;
import ca.bc.gov.ols.geocoder.lexer.LexicalRules;

/**
 * Lexical restructuring rules for DRA
 * 
 * @author chodgson
 * 
 */

public class DraLexicalRules extends LexicalRules
{
	public static final String RE_WORD = "[^0-9]+|[\\w]{9,}";
	
	public static final String RE_AND = "AND";
	
	public static final String RE_NUMBER = "\\d{1,8}";

	public static final String RE_LETTER = "[A-Z]";

	//public static final String RE_NUMBER_WITH_SUFFIX = "\\d{1,8}[a-zA-Z]";
	
	//public static final String RE_NUMBER_WITH_OPTIONAL_SUFFIX = "\\d{1,8}([a-zA-Z])?";
	
	public static final String RE_ORDINAL = "(?i)(ST|TH|RD|ND|E|ER|RE|EME|ERE|IEME|IERE)";
	
	// unit numbers can be a single letter,
	// or an optional letter followed by some numbers followed by an optional letter
	//public static final String RE_UNIT_NUMBER = "[a-zA-Z0-9]?\\d{0,8}((?<=\\d)[a-zA-Z])?";
	
	public static final String RE_DIRECTIONAL = "N|NW|NE|S|SE|SW|E|W";
	
	public static final String RE_PROVINCE = "BC|AB|YT|SK|MB|ON|QC|NB|NS|NL|NT|NU|PE";
	
	public static final String RE_SUFFIX = "[A-Z]|1/2";
	
	public static final String POSTAL_ADDRESS_ELEMENT = "/PJ";
	
	public static final String FRONT_GATE = "/FG";

	public static final String OCCUPANT_SEPARATOR = "/OS";

	private Pattern[] postalPatterns;
	
	private static CleanRule[] cleanRules = new CleanRule[] {
			// remove diacritical marks
			new CleanRule("\\p{InCombiningDiacriticalMarks}+", ""),
			// replace ligatures with individual characters
			// (note this is only a small subset of ligatures based on experience)
			new CleanRule("æ", "ae"),
			new CleanRule("Æ", "AE"),
			new CleanRule("Œ", "OE"),
			new CleanRule("œ", "oe"),
			new CleanRule("½", " 1/2"),
			// replace o: with just o - it is a native language character
			new CleanRule("o:", "o"),
			// replace ʔ with 7
			new CleanRule("ʔ", "7"),
			// remove periods and apostrophes between letters, squish letters together
			new CleanRule("(?<=[a-zA-Z]|^)[.'`’](?=[a-zA-Z]|$)", ""),
			// replace "c/o" with "careof"
			new CleanRule("\\bc\\/o\\b", "careof"),
			// remove slashes between letters, leave a space between letters
			new CleanRule("(?<=[a-zA-Z]|^)[\\/\\\\](?=[a-zA-Z]|$)", " "),
			// change & into "and"
			new CleanRule("&", " and "),
			// change -- into "/FG"
			new CleanRule("(?<=[^-]|^)--(?=[^-]|$)", " " + FRONT_GATE + " "),
			// change ** into "/OS"
			new CleanRule("(?<=[^\\*]|^)\\*\\*(?=[^\\*]|$)", " " + OCCUPANT_SEPARATOR + " "),
			// replace invalid characters with spaces (including apostrophes, periods, dashes)
			new CleanRule("[^a-zA-Z0-9/]", " "),
			// remove ordinals eg. 1st, 2nd, 3rd 4-9th
			new CleanRule("1[sS][tT]\\b", "1"),
			new CleanRule("2[nN][dD]\\b", "2"),
			new CleanRule("3[rR][dD]\\b", "3"),
			new CleanRule("([0-9])[tT][hH]\\b", "$1"),
			// insert spaces between sequences of letters and numbers eg. 100A
			new CleanRule("\\b([a-zA-Z]+)(\\d+)\\b", "$1 $2"),
			// insert spaces between numbers and trailing single letters (we can't put a space in "7Eten"!)
			new CleanRule("\\b(\\d+)([a-zA-Z])\\b", "$1 $2"),
			// reduce all whitespace to single spaces
			new CleanRule("\\s+", " "),
	};
	
	public DraLexicalRules() {
		
		setJoinRules(new JoinRule[] {
				JoinRule.createJoin("(?i)(B)RITISH", "(?i)(C)OLUMBIA"),
				JoinRule.createJoin("(?i)(C)OLUMBIE", "(?i)(B)RITANIQUE"),
				JoinRule.createJoin("(?i)(C)", "(?i)(B)")
		});
		
		// setup patterns for handling postal junk
		postalPatterns = new Pattern[] {
				// postal code eg: V9K 1X9
				Pattern.compile(
						"\\b[ABCEGHJ-NPRSTVXY][0-9][ABCEGHJ-NPRSTV-Z]\\s*[0-9][ABCEGHJ-NPRSTV-Z][0-9]\\b",
						Pattern.CASE_INSENSITIVE),
				// Postal Box eg: PO BOX ##
				Pattern.compile("\\b(P\\s*O\\s*)?BOX\\s*[0-9]+\\b(?!.*\\/FG)",Pattern.CASE_INSENSITIVE),
				// Mailbag/Rural Routes/Mail Route/SS eg: MAILBAG ##, RR ## 
				Pattern.compile("\\b(((MAIL)?BAG)|LCD|COMP|RR|MR|SS|RURAL\\s+ROUTE)\\s*[0-9]+\\b(?!.*\\/FG)", Pattern.CASE_INSENSITIVE),
				// General Delivery Station eg: GD STN ABC
				Pattern.compile("\\b(POSTAL\\s+)?STN\\s+[^\\s]+\\b(?!.*\\/FG)", Pattern.CASE_INSENSITIVE),
				// General Delivery
				Pattern.compile("\\b(GD|GENERAL\\s+DELIVERY)\\b", Pattern.CASE_INSENSITIVE),
				// care/of
				Pattern.compile("\\bC\\/O\\b", Pattern.CASE_INSENSITIVE),
				Pattern.compile("\\bcareof\\b", Pattern.CASE_INSENSITIVE),
				// RPO/SPO
				Pattern.compile("\\b[RS]PO\\b", Pattern.CASE_INSENSITIVE)
		};
		
	}
	
	@Override
	public String cleanSentence(String sentence) {
		return clean(sentence);
	}
	
	public static String clean(String sentence) {
		sentence = Normalizer.normalize(sentence, Normalizer.Form.NFD);
		for(CleanRule rule : cleanRules) {
			sentence = rule.clean(sentence);
		}
		return sentence.trim();
	}
	
	@Override
	public String runSpecialRules(String sentence) {
		// here we handle postal junk; we remove any and all postal-related junk, and
		// put a special "postal junk" flag on the end of the string where it is easy to find
		boolean foundPostalJunk = false;
		for(Pattern pp : postalPatterns) {
			Matcher matcher = pp.matcher(sentence);
			if(matcher.find()) {
				sentence = matcher.replaceAll("");
				foundPostalJunk = true;
			}
		}
		if(foundPostalJunk) {
			sentence += " " + POSTAL_ADDRESS_ELEMENT;
		}
		return sentence;
	}
}
