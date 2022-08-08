package ca.bc.gov.ols.geocoder.config;

import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import ca.bc.gov.ols.config.ConfigurationParameter;
import ca.bc.gov.ols.config.ConfigurationStore;

public class InMemoryGeocoderConfigurationStore implements GeocoderConfigurationStore {

    public InMemoryGeocoderConfigurationStore(Properties bootstrapConfig) {
        super();
    }

    @Override
    public Stream<ConfigurationParameter> getConfigParams() {
        ConfigurationParameter cp1 = new ConfigurationParameter("BGEO", "dataSource.className", "ca.bc.gov.ols.geocoder.datasources.TestDataSource");
        ConfigurationParameter cp2 = new ConfigurationParameter("BGEO", "baseSrsCode", "3005");
        ConfigurationParameter cp3 = new ConfigurationParameter("BGEO", "baseSrsBounds", "200000,300000,1900000,1800000");

        return Stream.of(cp1, cp2, cp3);
    }

    @Override
    public void setConfigParam(ConfigurationParameter param) {
        // not implemented
    }

    @Override
    public void removeConfigParam(ConfigurationParameter param) {
        // not implemented;
    }

    @Override
    public void replaceWith(ConfigurationStore configStore) {
        // not implemented
    }

    protected void writeConfigParams(List<ConfigurationParameter> configParams) {
        // not implemented
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public Stream<AbbreviationMapping> getAbbrevMappings() {
        return Stream.empty();
    }

    @Override
    public Stream<UnitDesignator> getUnitDesignators() {
        return Stream.empty();
    }

    @Override
    public Stream<LocalityMapping> getLocalityMappings() {
        return Stream.empty();
    }

    @Override
    public void setAbbrevMapping(AbbreviationMapping abbrMap) {
        // not implemented
    }

    @Override
    public void removeAbbrevMapping(AbbreviationMapping abbrMap) {
        // not implemented
    }

    protected void writeAbbrevMappings(List<AbbreviationMapping> abbrMaps) {
        // not implemented
    }

    @Override
    public void addUnitDesignator(UnitDesignator ud) {
        // not implemented
    }

    @Override
    public void removeUnitDesignator(UnitDesignator ud) {
        // not implemented
    }

    protected void writeUnitDesignators(List<UnitDesignator> uds) {
        // not implemented
    }

    @Override
    public void setLocalityMapping(LocalityMapping locMap) {
        // not implemented
    }

    @Override
    public void removeLocalityMapping(LocalityMapping locMap) {
        // not implemented
    }

    @Override
    public void replaceWith(GeocoderConfigurationStore configStore) {
        // not implemented
    }
}