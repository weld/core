package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;


import static java.util.Collections.emptyList;
import static org.jboss.weld.bootstrap.spi.Scanning.EMPTY_SCANNING;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;
import org.jboss.weld.bootstrap.spi.helpers.MetadataImpl;
import org.jboss.weld.util.Function;

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
        AddMetadataFunction<String> function = new AddMetadataFunction<String>();
        if (alternativeClasses != null) {
            this.alternativeClasses = new ArrayList<>();
            for (String alternativeClass : alternativeClasses) {
                this.alternativeClasses.add(function.apply(alternativeClass));
            }
        } else {
            this.alternativeClasses = emptyList();
        }
        if (alternativeStereotypes != null) {
            this.alternativeStereotypes = new ArrayList<>();
            for (String alternativeStereotypeClass : alternativeStereotypes) {
                this.alternativeStereotypes.add(function.apply(alternativeStereotypeClass));
            }
        } else {
            this.alternativeStereotypes = emptyList();
        }
        if (decorators != null) {
            this.decorators = new ArrayList<>();
            for (String decorator : decorators) {
                this.decorators.add(function.apply(decorator));
            }
        } else {
            this.decorators = emptyList();
        }
        if (interceptors != null) {
            this.interceptors = new ArrayList<>();
            for (String interceptor : interceptors) {
                this.interceptors.add(function.apply(interceptor));
            }
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

    @Override
    public BeanDiscoveryMode getBeanDiscoveryMode() {
        return BeanDiscoveryMode.ALL;
    }

    @Override
    public String getVersion() {
        return null;
    }

}
