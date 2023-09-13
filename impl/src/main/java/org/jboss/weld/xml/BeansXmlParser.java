/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.xml;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.config.SystemPropertiesConfiguration;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.ScanningImpl;

/**
 * Retained for backward compatibility with Arquillian and WildFly which incorrectly rely on Weld internals!
 *
 * <p>
 * Also contains various merging utils.
 * </p>
 *
 * @author Martin Kouba
 */
public class BeansXmlParser {

    private final BeansXmlValidator beansXmlValidator;
    // having this information here allows WFLY to create appropriate parser
    private final BeanDiscoveryMode emptyBeansXmlDiscoveryModeAll;

    public BeansXmlParser() {
        this(false);
    }

    public BeansXmlParser(boolean emptyBeansXmlDiscoveryModeAll) {
        beansXmlValidator = SystemPropertiesConfiguration.INSTANCE.isXmlValidationDisabled() ? null : new BeansXmlValidator();
        if (emptyBeansXmlDiscoveryModeAll) {
            this.emptyBeansXmlDiscoveryModeAll = BeanDiscoveryMode.ALL;
        } else {
            this.emptyBeansXmlDiscoveryModeAll = BeanDiscoveryMode.ANNOTATED;
        }
    }

    public BeansXml parse(final URL beansXml) {
        BeansXmlHandler handler = getHandler(beansXml);
        if (beansXmlValidator != null) {
            beansXmlValidator.validate(beansXml, handler);
        }
        return handler != null
                ? new BeansXmlStreamParser(beansXml, text -> handler.interpolate(text), emptyBeansXmlDiscoveryModeAll).parse()
                : new BeansXmlStreamParser(beansXml, emptyBeansXmlDiscoveryModeAll).parse();
    }

    public BeansXml parse(Iterable<URL> urls) {
        return parse(urls, false);
    }

    public BeansXml parse(Iterable<URL> urls, boolean removeDuplicates) {
        return merge(urls, this::parse, removeDuplicates);
    }

    protected BeansXmlHandler getHandler(final URL beansXml) {
        return null;
    }

    // Merging utils

    public static <T> BeansXml merge(Iterable<? extends T> items, Function<T, BeansXml> function, boolean removeDuplicates) {
        List<Metadata<String>> alternatives = new ArrayList<>();
        List<Metadata<String>> alternativeStereotypes = new ArrayList<>();
        List<Metadata<String>> decorators = new ArrayList<>();
        List<Metadata<String>> interceptors = new ArrayList<>();
        List<Metadata<Filter>> includes = new ArrayList<>();
        List<Metadata<Filter>> excludes = new ArrayList<>();
        boolean isTrimmed = false;
        URL beansXmlUrl = null;
        // capture all found discovery modes - if there is just one, use it; otherwise fallback to ALL
        Set<BeanDiscoveryMode> discoveryModesSet = new HashSet<>();
        for (T item : items) {
            BeansXml beansXml = function.apply(item);
            // if Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT is true, there doesn't need to be beans.xml
            if (beansXml != null) {
                addTo(alternatives, beansXml.getEnabledAlternativeClasses(), removeDuplicates);
                addTo(alternativeStereotypes, beansXml.getEnabledAlternativeStereotypes(), removeDuplicates);
                addTo(decorators, beansXml.getEnabledDecorators(), removeDuplicates);
                addTo(interceptors, beansXml.getEnabledInterceptors(), removeDuplicates);
                includes.addAll(beansXml.getScanning().getIncludes());
                excludes.addAll(beansXml.getScanning().getExcludes());
                isTrimmed = beansXml.isTrimmed();
                discoveryModesSet.add(beansXml.getBeanDiscoveryMode());
                /*
                 * provided we are merging the content of multiple XML files, getBeansXml() returns an InputStream representing
                 * the last one
                 */
                beansXmlUrl = beansXml.getUrl();
            }
        }

        return new BeansXmlImpl(alternatives, alternativeStereotypes, decorators, interceptors,
                new ScanningImpl(includes, excludes), beansXmlUrl,
                discoveryModesSet.size() == 1 ? discoveryModesSet.iterator().next() : BeanDiscoveryMode.ALL, null, isTrimmed);
    }

    private static void addTo(List<Metadata<String>> list, List<Metadata<String>> listToAdd, boolean removeDuplicates) {
        if (removeDuplicates) {
            List<Metadata<String>> filteredListToAdd = new ArrayList<>(listToAdd.size());
            for (Metadata<String> metadata : listToAdd) {
                if (!alreadyAdded(metadata, list)) {
                    filteredListToAdd.add(metadata);
                }
            }
            listToAdd = filteredListToAdd;
        }
        list.addAll(listToAdd);
    }

    private static boolean alreadyAdded(Metadata<String> metadata, List<Metadata<String>> list) {
        for (Metadata<String> existing : list) {
            if (existing.getValue().equals(metadata.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static BeansXml mergeExisting(final Iterable<? extends BeanDeploymentArchive> beanArchives,
            final boolean removeDuplicates) {
        return merge(beanArchives, bda -> bda.getBeansXml(), removeDuplicates);
    }

    public static BeansXml mergeExistingDescriptors(final Iterable<BeansXml> beanArchives, final boolean removeDuplicates) {
        return merge(beanArchives, Function.identity(), removeDuplicates);
    }

}
