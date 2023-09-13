package org.jboss.weld.metadata;

import java.net.URL;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;

public class BeansXmlImpl implements BeansXml {

    private final List<Metadata<String>> enabledAlternatives;
    private final List<Metadata<String>> enabledAlternativeStereotypes;
    private final List<Metadata<String>> enabledDecorators;
    private final List<Metadata<String>> enabledInterceptors;
    private final Scanning scanning;
    private final URL url;
    private final BeanDiscoveryMode discoveryMode;
    private final String version;
    private final boolean isTrimmed;

    public BeansXmlImpl(List<Metadata<String>> enabledAlternatives, List<Metadata<String>> enabledAlternativeStereotypes,
            List<Metadata<String>> enabledDecorators, List<Metadata<String>> enabledInterceptors, Scanning scanning, URL url,
            BeanDiscoveryMode discoveryMode,
            String version, boolean isTrimmed) {
        this.enabledAlternatives = enabledAlternatives;
        this.enabledAlternativeStereotypes = enabledAlternativeStereotypes;
        this.enabledDecorators = enabledDecorators;
        this.enabledInterceptors = enabledInterceptors;
        this.scanning = scanning == null ? Scanning.EMPTY_SCANNING : scanning;
        this.url = url;
        this.discoveryMode = discoveryMode;
        this.version = version;
        this.isTrimmed = isTrimmed;
    }

    @Override
    public List<Metadata<String>> getEnabledAlternativeClasses() {
        return enabledAlternatives;
    }

    @Override
    public List<Metadata<String>> getEnabledAlternativeStereotypes() {
        return enabledAlternativeStereotypes;
    }

    @Override
    public List<Metadata<String>> getEnabledDecorators() {
        return enabledDecorators;
    }

    @Override
    public List<Metadata<String>> getEnabledInterceptors() {
        return enabledInterceptors;
    }

    @Override
    public Scanning getScanning() {
        return scanning;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public BeanDiscoveryMode getBeanDiscoveryMode() {
        return discoveryMode;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isTrimmed() {
        return isTrimmed;
    }
}
