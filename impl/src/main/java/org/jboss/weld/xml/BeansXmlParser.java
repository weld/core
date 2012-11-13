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
import static org.jboss.weld.logging.messages.XmlMessage.CONFIGURATION_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.LOAD_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.PARSING_ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.BeanManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.BeansXmlRecord;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.ScanningImpl;
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
    private static boolean disableValidating;

    static {
        try {
            disableValidating = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    return Boolean.getBoolean("org.jboss.weld.xml.disableValidating");
                }
            });
        } catch (Throwable ignored) {
        }
    }

    public BeansXml parse(final URL beansXml) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(disableValidating == false);
        factory.setNamespaceAware(true);
        if (beansXml == null) {
            throw new IllegalStateException(LOAD_ERROR, "unknown");
        }
        SAXParser parser;
        try {
            parser = factory.newSAXParser();
        } catch (SAXException e) {
            throw new IllegalStateException(CONFIGURATION_ERROR, e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(CONFIGURATION_ERROR, e);
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
            throw new IllegalStateException(LOAD_ERROR, e, beansXml);
        } catch (SAXException e) {
            throw new IllegalStateException(PARSING_ERROR, e, beansXml);
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
        List<Metadata<BeansXmlRecord>> alternatives = new ArrayList<Metadata<BeansXmlRecord>>();
        List<Metadata<BeansXmlRecord>> decorators = new ArrayList<Metadata<BeansXmlRecord>>();
        List<Metadata<BeansXmlRecord>> interceptors = new ArrayList<Metadata<BeansXmlRecord>>();
        List<Metadata<Filter>> includes = new ArrayList<Metadata<Filter>>();
        List<Metadata<Filter>> excludes = new ArrayList<Metadata<Filter>>();
        URL beansXmlUrl = null;
        for (URL url : urls) {
            BeansXml beansXml = parse(url);
            addTo(alternatives, beansXml.getEnabledAlternatives(), removeDuplicates);
            addTo(decorators, beansXml.getEnabledDecorators(), removeDuplicates);
            addTo(interceptors, beansXml.getEnabledInterceptors(), removeDuplicates);
            includes.addAll(beansXml.getScanning().getIncludes());
            excludes.addAll(beansXml.getScanning().getExcludes());
            /*
             * provided we are merging the content of multiple XML files, getBeansXml() returns an
             * InputStream representing the last one
             */
            beansXmlUrl = url;
        }
        return new BeansXmlImpl(alternatives, decorators, interceptors, new ScanningImpl(includes, excludes), beansXmlUrl);
    }

    private void addTo(List<Metadata<BeansXmlRecord>> list, List<Metadata<BeansXmlRecord>> listToAdd, boolean removeDuplicates) {
        if (removeDuplicates) {
            List<Metadata<BeansXmlRecord>> filteredListToAdd = new ArrayList<Metadata<BeansXmlRecord>>(listToAdd.size());
            for (Metadata<BeansXmlRecord> metadata : listToAdd) {
                if (!alreadyAdded(metadata, list)) {
                    filteredListToAdd.add(metadata);
                }
            }
            listToAdd = filteredListToAdd;
        }
        list.addAll(listToAdd);
    }

    private boolean alreadyAdded(Metadata<BeansXmlRecord> metadata, List<Metadata<BeansXmlRecord>> list) {
        for (Metadata<BeansXmlRecord> existing : list) {
            if (existing.getValue().equals(metadata.getValue())) {
                return true;
            }
        }
        return false;
    }

    private static InputSource[] loadXsds() {
        List<InputSource> xsds = new ArrayList<InputSource>();
        // The Weld xsd
        InputSource weldXsd = loadXsd("beans_1_1.xsd", BeansXmlParser.class.getClassLoader());
        // The CDI Xsd
        InputSource cdiXsd = loadXsd("beans_1_0.xsd", BeanManager.class.getClassLoader());
        if (weldXsd != null) {
            xsds.add(weldXsd);
        }
        if (cdiXsd != null) {
            xsds.add(cdiXsd);
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
}
