package org.jboss.weld.metadata;

import java.net.URL;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;

public class BeansXmlImpl implements BeansXml {

    private final List<Metadata<String>> enabledAlternativeClasses;
    private final List<Metadata<String>> enabledAlternativeStereotypes;
    private final List<Metadata<String>> enabledDecorators;
    private final List<Metadata<String>> enabledInterceptors;
    private final Scanning scanning;
    private final URL url;

    public BeansXmlImpl(List<Metadata<String>> enabledAlternativeClasses, List<Metadata<String>> enabledAlternativeStereotypes, List<Metadata<String>> enabledDecorators, List<Metadata<String>> enabledInterceptors, Scanning scanning, URL url) {
        this.enabledAlternativeClasses = enabledAlternativeClasses;
        this.enabledAlternativeStereotypes = enabledAlternativeStereotypes;
        this.enabledDecorators = enabledDecorators;
        this.enabledInterceptors = enabledInterceptors;
        this.scanning = scanning;
        this.url = url;
    }

    public List<Metadata<String>> getEnabledAlternativeClasses() {
        return enabledAlternativeClasses;
    }

    public List<Metadata<String>> getEnabledAlternativeStereotypes() {
        return enabledAlternativeStereotypes;
    }

    public List<Metadata<String>> getEnabledDecorators() {
        return enabledDecorators;
    }

    public List<Metadata<String>> getEnabledInterceptors() {
        return enabledInterceptors;
    }

    public Scanning getScanning() {
        return scanning;
    }

    @Override
    public URL getUrl() {
        return url;
    }

}
