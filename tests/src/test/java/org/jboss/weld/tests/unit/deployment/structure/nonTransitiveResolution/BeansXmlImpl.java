package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

import com.google.common.base.Function;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;
import org.jboss.weld.metadata.MetadataImpl;

import java.net.URL;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static java.util.Collections.emptyList;
import static org.jboss.weld.bootstrap.spi.Scanning.EMPTY_SCANNING;

public class BeansXmlImpl implements BeansXml {

    private static class AddMetadataFunction<T> implements Function<String, Metadata<String>> {

        public Metadata<String> apply(String from) {
            return new MetadataImpl<String>(from, "unknown");
        }

    }

    private final List<Metadata<String>> alternativeClasses;
    private final List<Metadata<String>> alternativeStereotypes;
    private final List<Metadata<String>> decorators;
    private final List<Metadata<String>> interceptors;
    private final Scanning scanning;


    public BeansXmlImpl(List<String> alternativeClasses, List<String> alternativeStereotypes, List<String> decorators, List<String> interceptors) {
        if (alternativeClasses != null) {
            this.alternativeClasses = transform(alternativeClasses, new AddMetadataFunction<String>());
        } else {
            this.alternativeClasses = emptyList();
        }
        if (alternativeStereotypes != null) {
            this.alternativeStereotypes = transform(alternativeStereotypes, new AddMetadataFunction<String>());
        } else {
            this.alternativeStereotypes = emptyList();
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

    public List<Metadata<String>> getEnabledAlternativeClasses() {
        return alternativeClasses;
    }

    public List<Metadata<String>> getEnabledAlternativeStereotypes() {
        return alternativeStereotypes;
    }

    public List<Metadata<String>> getEnabledDecorators() {
        return decorators;
    }

    public List<Metadata<String>> getEnabledInterceptors() {
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
