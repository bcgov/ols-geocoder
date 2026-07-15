package ca.bc.gov.ols.geocoder.status;

import java.util.Map;

public record BasicStatus(
		String version,
		String gitCommitId,
		String dataProcessingTimestamp,
		String roadNetworkTimestamp,
		String startTimestamp,
		Map<String, Integer> counts) {

	public BasicStatus(SystemStatus status) {
		this(status.version, status.gitCommitId, status.dataProcessingTimestamp, 
				status.roadNetworkTimestamp, status.startTimestamp, status.counts);
	}
}