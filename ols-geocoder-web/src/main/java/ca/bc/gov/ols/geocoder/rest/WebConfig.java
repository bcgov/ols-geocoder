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
package ca.bc.gov.ols.geocoder.rest;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.rest.bulk.CSVBulkResponseConverter;
import ca.bc.gov.ols.geocoder.rest.converters.BooleanConverter;
import ca.bc.gov.ols.geocoder.rest.converters.InterpolationConverter;
import ca.bc.gov.ols.geocoder.rest.converters.LocationDescriptorConverter;
import ca.bc.gov.ols.geocoder.rest.converters.MatchPrecisionConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.CsvOlsResponseConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.CsvStringConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.GmlOlsResponseConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.HtmlErrorMessageConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.HtmlOlsResponseConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.JsonOlsResponseConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.JsonStringListConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.JsonpOlsResponseConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.KmlErrorMessageConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.KmlOlsResponseConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.ShpOlsResponseConverter;
import ca.bc.gov.ols.geocoder.rest.messageconverters.XhtmlOlsResponseConverter;

@Configuration
@ComponentScan("ca.bc.gov.ols.rest")
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
	
	@Autowired
	private IGeocoder geocoder;
	
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
	
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	if(geocoder.getConfig().getParcelKeysRequired()) {
    		registry.addInterceptor(new ApiKeyInterceptor(geocoder.getConfig().getParcelKeys())).addPathPatterns("/parcels/**");
    	}
    }
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(csvOlsResponseConverter());
		converters.add(gmlOlsResponseConverter());
		converters.add(htmlOlsResponseConverter());
		converters.add(jsonOlsResponseConverter());
		converters.add(jsonpOlsResponseConverter());
		converters.add(kmlOlsResponseConverter());
		converters.add(shpOlsResponseConverter());
		converters.add(xhtmlOlsResponseConverter());
		converters.add(htmlErrorMessageConverter());
		converters.add(kmlErrorMessageConverter());
		converters.add(csvStringConverter());
		converters.add(csvBulkResponseConverter());
		converters.add(jsonStringListConverter());
		//super.configureMessageConverters(converters);
	}
	
	@Bean
	public CsvOlsResponseConverter csvOlsResponseConverter() {
		return new CsvOlsResponseConverter();
	}

	@Bean
	public GmlOlsResponseConverter gmlOlsResponseConverter() {
		return new GmlOlsResponseConverter();
	}

	@Bean
	public HtmlOlsResponseConverter htmlOlsResponseConverter() {
		return new HtmlOlsResponseConverter();
	}

	@Bean
	public JsonOlsResponseConverter jsonOlsResponseConverter() {
		return new JsonOlsResponseConverter();
	}

	@Bean
	public JsonpOlsResponseConverter jsonpOlsResponseConverter() {
		return new JsonpOlsResponseConverter();
	}
	
	@Bean
	public KmlOlsResponseConverter kmlOlsResponseConverter() {
		return new KmlOlsResponseConverter();
	}
	
	@Bean
	public ShpOlsResponseConverter shpOlsResponseConverter() {
		return new ShpOlsResponseConverter();
	}
	
	@Bean
	public XhtmlOlsResponseConverter xhtmlOlsResponseConverter() {
		return new XhtmlOlsResponseConverter();
	}
	
	@Bean
	public HtmlErrorMessageConverter htmlErrorMessageConverter() {
		return new HtmlErrorMessageConverter();
	}
	
	@Bean
	public KmlErrorMessageConverter kmlErrorMessageConverter() {
		return new KmlErrorMessageConverter();
	}
	
	@Bean
	public CsvStringConverter csvStringConverter() {
		return new CsvStringConverter();
	}
	
	@Bean
	public CSVBulkResponseConverter csvBulkResponseConverter() {
		return new CSVBulkResponseConverter();
	}
	
	@Bean
	public CommonsMultipartResolver multipartResolver() {
	    CommonsMultipartResolver resolver=new CommonsMultipartResolver();
	    resolver.setDefaultEncoding("utf-8");
	    return resolver;
	}

	@Bean
	public JsonStringListConverter jsonStringListConverter() {
		return new JsonStringListConverter();
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new BooleanConverter());
		registry.addConverter(new LocationDescriptorConverter());
		registry.addConverter(new MatchPrecisionConverter());
		registry.addConverter(new InterpolationConverter());
	}
	
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer
				.favorPathExtension(true)
				.favorParameter(false)
				.ignoreAcceptHeader(true)
				.useRegisteredExtensionsOnly(true)
				.defaultContentType(MediaType.APPLICATION_XHTML_XML)
				.mediaType("xhtml", MediaType.APPLICATION_XHTML_XML)
				.mediaType("html", MediaType.TEXT_HTML)
				.mediaType("csv",
						new MediaType("text", "csv", Charset.forName("UTF-8")))
				.mediaType("gml",
						new MediaType("application", "gml+xml", Charset.forName("UTF-8")))
				.mediaType("xml", MediaType.APPLICATION_XML)
				.mediaType("json", MediaType.APPLICATION_JSON)
				.mediaType("geojson", new MediaType("application", "vnd.geo+json",
						Charset.forName("UTF-8")))
				.mediaType("jsonp",
						new MediaType("application", "javascript", Charset.forName("UTF-8")))
				.mediaType("geojsonp",
						new MediaType("application", "javascript", Charset.forName("UTF-8")))
				.mediaType("shp", new MediaType("application", "zip", Charset.forName("UTF-8")))
				.mediaType("shpz", new MediaType("application", "zip", Charset.forName("UTF-8")))
				.mediaType("kml", new MediaType("application", "vnd.google-earth.kml+xml",
						Charset.forName("UTF-8")));
	}
	
}
