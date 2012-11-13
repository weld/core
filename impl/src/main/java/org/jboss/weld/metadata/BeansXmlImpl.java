package org.jboss.weld.metadata;

import java.net.URL;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.EnabledClass;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;

public class BeansXmlImpl implements BeansXml {

    private final List<Metadata<EnabledClass>> enabledAlternatives;
    private final List<Metadata<EnabledClass>> enabledDecorators;
    private final List<Metadata<EnabledClass>> enabledInterceptors;
    private final Scanning scanning;
    private final URL url;

    public BeansXmlImpl(List<Metadata<EnabledClass>> enabledAlternatives, List<Metadata<EnabledClass>> enabledDecorators, List<Metadata<EnabledClass>> enabledInterceptors, Scanning scanning, URL url) {
        this.enabledAlternatives = enabledAlternatives;
        this.enabledDecorators = enabledDecorators;
        this.enabledInterceptors = enabledInterceptors;
        this.scanning = scanning;
        this.url = url;
    }

    public List<Metadata<EnabledClass>> getEnabledAlternatives() {
        return enabledAlternatives;
    }

    public List<Metadata<EnabledClass>> getEnabledDecorators() {
        return enabledDecorators;
    }

    public List<Metadata<EnabledClass>> getEnabledInterceptors() {
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
