/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.bootstrap.spi.BeansXml.EMPTY_BEANS_XML;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.config.SystemPropertiesConfiguration;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.XmlLogger;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.ScanningImpl;
import org.jboss.weld.util.Function;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Simple parser for beans.xml
 * <p/>
 * This class is NOT threadsafe, and should only be called in a single thread
 *
 * @author Pete Muir
 * @author Ales Justin
 */
public class BeansXmlParser {

    private static final InputSource[] EMPTY_INPUT_SOURCE_ARRAY = new InputSource[0];

    private Function<URL, BeansXml> URL_TO_BEANS_XML_FUNCTION = new Function<URL, BeansXml>() {
        @Override
        public BeansXml apply(URL url) {
            return parse(url);
        }
    };

    private static Function<BeanDeploymentArchive, BeansXml> BEAN_ARCHIVE_TO_BEANS_XML_FUNCTION = new Function<BeanDeploymentArchive, BeansXml>() {
        @Override
        public BeansXml apply(BeanDeploymentArchive archive) {
            if (archive == null) {
                return null;
            }
            return archive.getBeansXml();
        }
    };

    private static Function<BeansXml, BeansXml> BEANS_XML_IDENTITY_FUNCTION = new Function<BeansXml, BeansXml>() {
        @Override
        public BeansXml apply(BeansXml beansXml) {
            return beansXml;
        }
    };

    public BeansXml parse(final URL beansXml) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(!SystemPropertiesConfiguration.INSTANCE.isXmlValidationDisabled());
        factory.setNamespaceAware(true);
        if (beansXml == null) {
            throw XmlLogger.LOG.loadError("unknown", null);
        }
        SAXParser parser;
        try {
            parser = factory.newSAXParser();
        } catch (SAXException e) {
            throw XmlLogger.LOG.configurationError(e);
        } catch (ParserConfigurationException e) {
            throw XmlLogger.LOG.configurationError(e);
        }
        InputStream beansXmlInputStream = null;
        try {
            beansXmlInputStream = beansXml.openStream();
            InputSource source = new InputSource(beansXmlInputStream);
            if (source.getByteStream().available() == 0) {
                // The file is just acting as a marker file
                return EMPTY_BEANS_XML;
            }
            BeansXmlHandler handler = getHandler(beansXml);

            try {
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", loadXsds());
            } catch (IllegalArgumentException e) {
                // No op, we just don't validate the XML
            } catch (SAXNotRecognizedException e) {
                // No op, we just don't validate the XML
            } catch (SAXNotSupportedException e) {
                // No op, we just don't validate the XML
            }

            parser.parse(source, handler);

            return handler.createBeansXml();
        } catch (IOException e) {
            throw XmlLogger.LOG.loadError(beansXml, e);
        } catch (SAXException e) {
            throw XmlLogger.LOG.parsingError(beansXml, e);
        } finally {
            if (beansXmlInputStream != null) {
                try {
                    beansXmlInputStream.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public BeansXml parse(Iterable<URL> urls) {
        return parse(urls, false);
    }

    public BeansXml parse(Iterable<URL> urls, boolean removeDuplicates) {
        return merge(urls, URL_TO_BEANS_XML_FUNCTION, removeDuplicates);
    }

    private <T> BeansXml merge(Iterable<? extends T> items, Function<T, BeansXml> function, boolean removeDuplicates) {
        List<Metadata<String>> alternatives = new ArrayList<Metadata<String>>();
        List<Metadata<String>> alternativeStereotypes = new ArrayList<Metadata<String>>();
        List<Metadata<String>> decorators = new ArrayList<Metadata<String>>();
        List<Metadata<String>> interceptors = new ArrayList<Metadata<String>>();
        List<Metadata<Filter>> includes = new ArrayList<Metadata<Filter>>();
        List<Metadata<Filter>> excludes = new ArrayList<Metadata<Filter>>();
        URL beansXmlUrl = null;
        for (T item : items) {
            BeansXml beansXml = function.apply(item);
            addTo(alternatives, beansXml.getEnabledAlternativeClasses(), removeDuplicates);
            addTo(alternativeStereotypes, beansXml.getEnabledAlternativeStereotypes(), removeDuplicates);
            addTo(decorators, beansXml.getEnabledDecorators(), removeDuplicates);
            addTo(interceptors, beansXml.getEnabledInterceptors(), removeDuplicates);
            includes.addAll(beansXml.getScanning().getIncludes());
            excludes.addAll(beansXml.getScanning().getExcludes());
            /*
             * provided we are merging the content of multiple XML files, getBeansXml() returns an
             * InputStream representing the last one
             */
            beansXmlUrl = beansXml.getUrl();
        }
        return new BeansXmlImpl(alternatives, alternativeStereotypes, decorators, interceptors, new ScanningImpl(includes, excludes), beansXmlUrl, BeanDiscoveryMode.ALL, null);
    }

    private void addTo(List<Metadata<String>> list, List<Metadata<String>> listToAdd, boolean removeDuplicates) {
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

    private static InputSource[] loadXsds() {

        List<InputSource> xsds = new ArrayList<InputSource>();

        for (XmlSchema schema : XmlSchema.values()) {
            InputSource source = loadXsd(schema.getFileName(), schema.getClassLoader());
            if (source != null) {
                xsds.add(source);
            }
        }

        return xsds.toArray(EMPTY_INPUT_SOURCE_ARRAY);
    }


    private static InputSource loadXsd(String name, ClassLoader classLoader) {
        InputStream in = classLoader.getResourceAsStream(name);
        if (in == null) {
            return null;
        } else {
            return new InputSource(in);
        }
    }

    protected BeansXmlHandler getHandler(final URL beansXml) {
        return new BeansXmlHandler(beansXml);
    }

    public BeansXml mergeExisting(final Iterable<? extends BeanDeploymentArchive> beanArchives, final boolean removeDuplicates) {
        return merge(beanArchives, BEAN_ARCHIVE_TO_BEANS_XML_FUNCTION, removeDuplicates);
    }

    public BeansXml mergeExistingDescriptors(final Iterable<BeansXml> beanArchives, final boolean removeDuplicates) {
        return merge(beanArchives, BEANS_XML_IDENTITY_FUNCTION, removeDuplicates);
    }
}
