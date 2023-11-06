package ca.bc.gov.ols.siteloaderprep;

public class RawStreetName {
	public int id;
	public String body; // store the punctuated body here
	public String type;
	public String dir;
	public String qual;
	public boolean typeIsPrefix;
	public boolean dirIsPrefix;
	
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
