package ca.bc.gov.siteloaderprep;

public class StreetNamePrep {
	int id;
	String body; // store the punctuated body here
	String type;
	String dir;
	String qual;
	Boolean typeIsPrefix;
	Boolean dirIsPrefix;
	
	@Override
	public String toString() {
		if(body == null) return null;
		return (dir != null && dirIsPrefix ? dir + " " : "")
				+ (type != null && typeIsPrefix ? type + " " : "")
				+ body
				+ (type != null && !typeIsPrefix ? " " + type : "")
				+ (dir != null && !dirIsPrefix ? " " + dir : "")
				+ (qual != null ? " " + qual : "");
	}
}
