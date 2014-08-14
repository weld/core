package org.jboss.weld.metadata;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;

/**
 * Utility class to merge beans.xml files.
 * @author Stefan Großmann
 */
public class BeansXmlMergeUtil {
    public BeansXml merge(final Iterable<BeansXml> beansXmls) {
        return merge(beansXmls, false);
    }

    public BeansXml merge(final Iterable<BeansXml> beansXmls, final boolean removeDuplicates) {
        final List<Metadata<String>> alternatives = new ArrayList<Metadata<String>>();
        final List<Metadata<String>> alternativeStereotypes = new ArrayList<Metadata<String>>();
        final List<Metadata<String>> decorators = new ArrayList<Metadata<String>>();
        final List<Metadata<String>> interceptors = new ArrayList<Metadata<String>>();
        final List<Metadata<Filter>> includes = new ArrayList<Metadata<Filter>>();
        final List<Metadata<Filter>> excludes = new ArrayList<Metadata<Filter>>();

        URL beansXmlUrl = null;
        for (BeansXml beansXml : beansXmls) {
            addTo(alternatives, beansXml.getEnabledAlternativeClasses(), removeDuplicates);
            addTo(alternativeStereotypes, beansXml.getEnabledAlternativeStereotypes(), removeDuplicates);
            addTo(decorators, beansXml.getEnabledDecorators(), removeDuplicates);
            addTo(interceptors, beansXml.getEnabledInterceptors(), removeDuplicates);
            includes.addAll(beansXml.getScanning().getIncludes());
            excludes.addAll(beansXml.getScanning().getExcludes());

            if (beansXml.getUrl() != null) {
                /**
                 * provided we are merging the content of multiple XML files, getBeansXml() returns an InputStream representing the last one
                 */
                beansXmlUrl = beansXml.getUrl();
            }
        }

        return new BeansXmlImpl(alternatives, alternativeStereotypes, decorators, interceptors, new ScanningImpl(includes, excludes), beansXmlUrl,
                BeanDiscoveryMode.ALL, null);
    }

    private void addTo(List<Metadata<String>> list, List<Metadata<String>> listToAdd, final boolean removeDuplicates) {
        if (removeDuplicates) {
            List<Metadata<String>> filteredListToAdd = new ArrayList<Metadata<String>>(listToAdd.size());
            for (Metadata<String> metadata : listToAdd) {
                if (!alreadyAdded(metadata, list)) {
                    filteredListToAdd.add(metadata);
                }
            }
            listToAdd = filteredListToAdd;
        }
        list.addAll(listToAdd);
    }

    private boolean alreadyAdded(Metadata<String> metadata, List<Metadata<String>> list) {
        for (Metadata<String> existing : list) {
            if (existing.getValue().equals(metadata.getValue())) {
                return true;
            }
        }
        return false;
    }
}
