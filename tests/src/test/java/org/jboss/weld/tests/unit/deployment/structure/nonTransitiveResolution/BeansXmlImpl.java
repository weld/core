package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

import com.google.common.base.Function;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.BeansXmlRecord;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;
import org.jboss.weld.metadata.MetadataImpl;

import java.net.URL;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static java.util.Collections.emptyList;
import static org.jboss.weld.bootstrap.spi.Scanning.EMPTY_SCANNING;

public class BeansXmlImpl implements BeansXml {

    private static class LegacyBeansXmlRecord implements BeansXmlRecord {

        private final String value;

        public LegacyBeansXmlRecord(String value) {
            this.value = value;
        }

        public Boolean isEnabled() {
            return null;
        }

        public Integer getPriority() {
            return null;
        }

        public String getValue() {
            return value;
        }

        public boolean isStereotype() {
            return false;
        }
    }

    private static class AddMetadataFunction<T> implements Function<String, Metadata<BeansXmlRecord>> {

        public Metadata<BeansXmlRecord> apply(String from) {
            return new MetadataImpl<BeansXmlRecord>(new LegacyBeansXmlRecord(from), "unknown");
        }

    }

    private final List<Metadata<BeansXmlRecord>> alternatives;
    private final List<Metadata<BeansXmlRecord>> decorators;
    private final List<Metadata<BeansXmlRecord>> interceptors;
    private final Scanning scanning;


    public BeansXmlImpl(List<String> alternativeClasses, List<String> decorators, List<String> interceptors) {
        if (alternativeClasses != null) {
            this.alternatives = transform(alternativeClasses, new AddMetadataFunction<String>());
        } else {
            this.alternatives = emptyList();
        }
        if (decorators != null) {
            this.decorators = transform(decorators, new AddMetadataFunction<String>());
        } else {
            this.decorators = emptyList();
        }
        if (interceptors != null) {
            this.interceptors = transform(interceptors, new AddMetadataFunction<String>());
        } else {
            this.interceptors = emptyList();
        }
        this.scanning = EMPTY_SCANNING;
    }

    public List<Metadata<BeansXmlRecord>> getEnabledAlternatives() {
        return alternatives;
    }

    public List<Metadata<BeansXmlRecord>> getEnabledDecorators() {
        return decorators;
    }

    public List<Metadata<BeansXmlRecord>> getEnabledInterceptors() {
        return interceptors;
    }

    public Scanning getScanning() {
        return scanning;
    }

    @Override
    public URL getUrl() {
        return null;
    }

}
