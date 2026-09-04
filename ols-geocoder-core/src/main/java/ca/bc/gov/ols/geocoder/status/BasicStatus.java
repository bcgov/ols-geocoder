package ca.bc.gov.ols.geocoder.status;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Service status information including version, timestamps, and data record counts.")
public record BasicStatus(
		@Schema(description = "The API version string.",
				example = "4.5.4")
		String version,

		@Schema(description = "The full Git commit hash of the deployed build.",
				example = "a1b2c3d4e5f6...")
		String gitCommitId,

		@Schema(description = "Timestamp of the last data processing run.",
				example = "2026-01-15T08:30:00Z")
		String dataProcessingTimestamp,

		@Schema(description = "Timestamp of the last road network update.",
				example = "2026-01-14T12:00:00Z")
		String roadNetworkTimestamp,

		@Schema(description = "Timestamp when the service was started.",
				example = "2026-01-15T08:00:00Z")
		String startTimestamp,

		@Schema(description = "Record counts for major data sets, keyed by data set name (e.g. 'roadNetwork', 'parcels', 'siteAddresses').")
		Map<String, Integer> counts) {

	public BasicStatus(SystemStatus status) {
		this(status.version, status.gitCommitId, status.dataProcessingTimestamp, 
				status.roadNetworkTimestamp, status.startTimestamp, status.counts);
	}
}