package ca.bc.gov.ols.geocoder.lucene;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;

public class LuceneGeocoder {
	private final static Logger logger = LoggerFactory.getLogger(GeocoderConfig.LOGGER_PREFIX + 
			LuceneGeocoder.class.getCanonicalName());
	
	private GeocoderDataStore ds;
	
	public LuceneGeocoder(GeocoderDataStore ds) {
		this.ds = ds;
	}
	
	public List<String> query(String queryString) {
		return ds.queryLuceneIndex(queryString);
	}
}
