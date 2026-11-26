package ca.bc.gov.ols.geocoder.rest.openapi;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.rest.GeocoderApplication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {CassandraAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@ComponentScan(
		basePackages = "ca.bc.gov.ols.geocoder.rest",
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = GeocoderApplication.class
		)
)
public class TestApplication {

	@Bean
	public IGeocoder geocoder() {
		IGeocoder mockGeocoder = mock(IGeocoder.class);
		GeocoderConfig mockConfig = mock(GeocoderConfig.class);
		when(mockConfig.getParcelKeysRequired()).thenReturn(false);
		when(mockGeocoder.getConfig()).thenReturn(mockConfig);
		return mockGeocoder;
	}
}
