package ca.bc.gov.ols.geocoder.config;

import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import ca.bc.gov.ols.config.InMemoryConfigurationStore;

public class InMemoryGeocoderConfigurationStore extends InMemoryConfigurationStore implements GeocoderConfigurationStore {

    public InMemoryGeocoderConfigurationStore(Properties bootstrapConfig) {
        super();
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