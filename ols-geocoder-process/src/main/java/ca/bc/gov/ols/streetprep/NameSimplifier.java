package ca.bc.gov.ols.streetprep;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameSimplifier {

	private static Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+|\\.|'");
	
	public static String simplify(String name) {
		name = Normalizer.normalize(name, Normalizer.Form.NFD);
        Matcher matcher = pattern.matcher(name);
        if(matcher.find()){
        	name = matcher.replaceAll("");
        } 
        return name.toLowerCase();
	}

}
