package org.jboss.weld.metadata;

import java.net.URL;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.BeansXmlRecord;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;

public class BeansXmlImpl implements BeansXml {

    private final List<Metadata<BeansXmlRecord>> enabledAlternatives;
    private final List<Metadata<BeansXmlRecord>> enabledDecorators;
    private final List<Metadata<BeansXmlRecord>> enabledInterceptors;
    private final Scanning scanning;
    private final URL url;

    public BeansXmlImpl(List<Metadata<BeansXmlRecord>> enabledAlternatives, List<Metadata<BeansXmlRecord>> enabledDecorators, List<Metadata<BeansXmlRecord>> enabledInterceptors, Scanning scanning, URL url) {
        this.enabledAlternatives = enabledAlternatives;
        this.enabledDecorators = enabledDecorators;
        this.enabledInterceptors = enabledInterceptors;
        this.scanning = scanning;
        this.url = url;
    }

    public List<Metadata<BeansXmlRecord>> getEnabledAlternatives() {
        return enabledAlternatives;
    }

    public List<Metadata<BeansXmlRecord>> getEnabledDecorators() {
        return enabledDecorators;
    }

    public List<Metadata<BeansXmlRecord>> getEnabledInterceptors() {
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
