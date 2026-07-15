package ca.bc.gov.ols.geocoder.status;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.rowreader.DateType;

public class SystemStatus {
	
	public final String version = GeocoderConfig.VERSION;
	public final String gitCommitId = GeocoderConfig.GIT_COMMIT_ID;
	public String dataProcessingTimestamp;
	public String roadNetworkTimestamp;

	public String startTimestamp = "";

	public Map<String, Integer> counts = new HashMap<>();
	
	//public SlowQueryList slowQueries = new SlowQueryList(10);
	
	public void setDates(Map<DateType, ZonedDateTime> dates) {
		if(dates != null) {
			dataProcessingTimestamp = String.valueOf(dates.get(DateType.PROCESSING_DATE));
			roadNetworkTimestamp = String.valueOf(dates.get(DateType.ITN_VINTAGE_DATE));
		}
	}

}
