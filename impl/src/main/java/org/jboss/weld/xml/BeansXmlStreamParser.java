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

import static java.util.Collections.emptyList;
import static org.jboss.weld.bootstrap.spi.BeansXml.EMPTY_BEANS_XML;
import static org.jboss.weld.bootstrap.spi.Scanning.EMPTY_SCANNING;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.SystemPropertyActivation;
import org.jboss.weld.logging.XmlLogger;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.ClassAvailableActivationImpl;
import org.jboss.weld.metadata.FilterImpl;
import org.jboss.weld.metadata.ScanningImpl;
import org.jboss.weld.metadata.SystemPropertyActivationImpl;
import org.jboss.weld.metadata.WeldFilterImpl;
import org.jboss.weld.util.collections.ImmutableSet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Simple yet efficient parser for beans.xml. This class is not thread safe and instances cannot be reused.
 *
 * @author Martin Kouba
 */
public class BeansXmlStreamParser {

    public static final String JAVAEE_LEGACY_URI = "http://java.sun.com/xml/ns/javaee";
    public static final String JAVAEE_URI = "http://xmlns.jcp.org/xml/ns/javaee";
    public static final String JAKARTAEE_URI = "https://jakarta.ee/xml/ns/jakartaee";
    public static final Set<String> JAVAEE_URIS = ImmutableSet.of(JAVAEE_LEGACY_URI, JAVAEE_URI, JAKARTAEE_URI);

    public static final String WELD_URI = "http://jboss.org/schema/weld/beans";
    public static final Set<String> SCANNING_URIS = ImmutableSet.of(WELD_URI, JAVAEE_URI, JAVAEE_LEGACY_URI, JAKARTAEE_URI);

    private static final String VERSION_ATTRIBUTE_NAME = "version";
    private static final String BEAN_DISCOVERY_MODE_ATTRIBUTE_NAME = "bean-discovery-mode";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String VALUE_ATTRIBUTE_NAME = "value";
    private static final String PATTERN_ATTRIBUTE_NAME = "pattern";

    private static final String IF_CLASS_AVAILABLE = "if-class-available";
    private static final String IF_CLASS_NOT_AVAILABLE = "if-class-not-available";
    private static final String IF_SYSTEM_PROPERTY = "if-system-property";

    private static final String CLASS = "class";
    private static final String STEREOTYPE = "stereotype";
    private static final String INCLUDE = "include";
    private static final String EXCLUDE = "exclude";
    private static final String TRIM = "trim";
    private static final String BEANS = "beans";
    private static final String ALTERNATIVES = "alternatives";
    private static final String INTERCEPTORS = "interceptors";
    private static final String DECORATORS = "decorators";
    private static final String SCAN = "scan";

    private List<Metadata<String>> enabledInterceptors = null;
    private List<Metadata<String>> enabledDecorators = null;
    private List<Metadata<String>> selectedAlternatives = null;
    private List<Metadata<String>> selectedAlternativeStereotypes = null;
    private List<Metadata<Filter>> includes = null;
    private List<Metadata<Filter>> excludes = null;
    private BeanDiscoveryMode discoveryMode = BeanDiscoveryMode.ANNOTATED;
    private String version;
    private boolean isTrimmed;

    private final URL beansXml;

    private final Function<String, String> interpolator;

    private final BeanDiscoveryMode emptyBeansXmlDiscoveryMode;

    /**
     *
     * @param beansXml
     */
    public BeansXmlStreamParser(URL beansXml) {
        this(beansXml, Function.identity(), BeanDiscoveryMode.ANNOTATED);
    }

    /**
     *
     * @param beansXml
     * @param interpolator
     */
    public BeansXmlStreamParser(URL beansXml, Function<String, String> interpolator) {
        this(beansXml, interpolator, BeanDiscoveryMode.ANNOTATED);
    }

    public BeansXmlStreamParser(URL beansXml, BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        this(beansXml, Function.identity(), emptyBeansXmlDiscoveryMode);
    }

    public BeansXmlStreamParser(URL beansXml, Function<String, String> interpolator,
            BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        this.beansXml = beansXml;
        this.interpolator = interpolator;
        this.emptyBeansXmlDiscoveryMode = emptyBeansXmlDiscoveryMode;
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "False positive, see https://github.com/spotbugs/spotbugs/issues/259")
    public BeansXml parse() {
        if (beansXml == null) {
            throw XmlLogger.LOG.loadError("unknown", null);
        }
        try (InputStream in = beansXml.openStream()) {
            if (in.available() == 0) {
                // The file is just acting as a marker file
                // if the legacy treatment is on, we use discovery mode as specified, otherwise we default to annotated mode
                if (emptyBeansXmlDiscoveryMode.equals(BeanDiscoveryMode.ANNOTATED)) {
                    return EMPTY_BEANS_XML;
                } else {
                    return new BeansXmlImpl(emptyList(), emptyList(), emptyList(), emptyList(), EMPTY_SCANNING,
                            null, emptyBeansXmlDiscoveryMode, null, false);
                }
            }
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader reader = factory.createXMLEventReader(in);

            StartElement element = nextStartElement(reader, BEANS, JAVAEE_URIS);
            if (element != null) {
                parseBeans(element);
                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    if (isEnd(event, BEANS)) {
                        break;
                    } else if (isStartElement(event, ALTERNATIVES)) {
                        parseAlternatives(reader, event);
                    } else if (isStartElement(event, INTERCEPTORS)) {
                        parseInterceptors(reader, event);
                    } else if (isStartElement(event, DECORATORS)) {
                        parseDecorators(reader, event);
                    } else if (isStartElement(event, SCAN, SCANNING_URIS)) {
                        parseScan(reader, event);
                    } else if (isStartElement(event, TRIM)) {
                        isTrimmed = true;
                    }
                }
            }
            reader.close();

        } catch (IOException e) {
            throw XmlLogger.LOG.loadError(beansXml, e);
        } catch (XMLStreamException e) {
            throw XmlLogger.LOG.parsingError(beansXml, e);
        }
        return new BeansXmlImpl(orEmpty(selectedAlternatives), orEmpty(selectedAlternativeStereotypes),
                orEmpty(enabledDecorators),
                orEmpty(enabledInterceptors), new ScanningImpl(orEmpty(includes), orEmpty(excludes)), beansXml, discoveryMode,
                version, isTrimmed);
    }

    private StartElement nextStartElement(XMLEventReader reader, String localName, Set<String> namespaces)
            throws XMLStreamException {
        StartElement startElement = nextStartElement(reader);
        if (startElement != null && localName.equals(startElement.getName().getLocalPart())
                && isInNamespace(startElement.getName(), namespaces)) {
            return startElement;
        }
        return null;
    }

    private StartElement nextStartElement(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                return event.asStartElement();
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private void parseBeans(StartElement element) {
        Iterator attributes = element.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            if (isLocalName(attribute.getName(), VERSION_ATTRIBUTE_NAME)) {
                version = attribute.getValue();
            } else if (isLocalName(attribute.getName(), BEAN_DISCOVERY_MODE_ATTRIBUTE_NAME)) {
                discoveryMode = parseDiscoveryMode(interpolate(attribute.getValue()).trim().toUpperCase());
            }
        }
    }

    private boolean isLocalName(QName name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        return value.equals(name.getLocalPart());
    }

    private void parseAlternatives(XMLEventReader reader, XMLEvent event) throws XMLStreamException {
        if (selectedAlternatives != null) {
            throw XmlLogger.LOG.multipleAlternatives(beansXml + "@" + event.asStartElement().getLocation().getLineNumber());
        }
        selectedAlternatives = new LinkedList<>();
        selectedAlternativeStereotypes = new LinkedList<>();
        while (reader.hasNext()) {
            event = reader.nextEvent();
            if (isEnd(event, ALTERNATIVES)) {
                return;
            } else if (event.isStartElement()) {
                StartElement element = (StartElement) event;
                if (isStartElement(element, CLASS)) {
                    selectedAlternatives
                            .add(new XmlMetadata<String>(element.getName().toString(), getTrimmedElementText(reader), beansXml,
                                    element.getLocation().getLineNumber()));
                } else if (isStartElement(element, STEREOTYPE)) {
                    selectedAlternativeStereotypes
                            .add(new XmlMetadata<String>(element.getName().toString(), getTrimmedElementText(reader), beansXml,
                                    element.getLocation().getLineNumber()));
                }
            }
        }
    }

    private void parseInterceptors(XMLEventReader reader, XMLEvent event) throws XMLStreamException {
        if (enabledInterceptors != null) {
            throw XmlLogger.LOG.multipleInterceptors(beansXml + "@" + event.asStartElement().getLocation().getLineNumber());
        }
        enabledInterceptors = new LinkedList<>();
        while (reader.hasNext()) {
            event = reader.nextEvent();
            if (isEnd(event, INTERCEPTORS)) {
                return;
            } else if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                if (isStartElement(element, CLASS)) {
                    enabledInterceptors
                            .add(new XmlMetadata<String>(element.getName().toString(), getTrimmedElementText(reader), beansXml,
                                    element.getLocation().getLineNumber()));
                }
            }
        }
    }

    private void parseDecorators(XMLEventReader reader, XMLEvent event) throws XMLStreamException {
        if (enabledDecorators != null) {
            throw XmlLogger.LOG.multipleDecorators(beansXml + "@" + event.asStartElement().getLocation().getLineNumber());
        }
        enabledDecorators = new LinkedList<>();
        while (reader.hasNext()) {
            event = reader.nextEvent();
            if (isEnd(event, DECORATORS)) {
                return;
            } else if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                if (isStartElement(element, CLASS)) {
                    enabledDecorators
                            .add(new XmlMetadata<String>(element.getName().toString(), getTrimmedElementText(reader), beansXml,
                                    element.getLocation().getLineNumber()));
                }

            }
        }
    }

    private void parseScan(XMLEventReader reader, XMLEvent event) throws XMLStreamException {
        if (excludes != null) {
            throw XmlLogger.LOG.multipleScanning(beansXml + "@" + event.asStartElement().getLocation().getLineNumber());
        }
        excludes = new LinkedList<>();
        includes = new LinkedList<>();
        while (reader.hasNext()) {
            event = reader.nextEvent();
            if (isEnd(event, SCAN, SCANNING_URIS)) {
                return;
            } else if (event.isStartElement()) {
                StartElement element = (StartElement) event;
                if (isStartElement(element, EXCLUDE, SCANNING_URIS)) {
                    handleFilter(element, reader, excludes::add);
                } else if (isStartElement(element, INCLUDE, SCANNING_URIS)) {
                    handleFilter(element, reader, includes::add);
                }
            }
        }
    }

    private void handleFilter(StartElement filterElement, XMLEventReader reader, Consumer<XmlMetadata<Filter>> consumer)
            throws XMLStreamException {
        String name = getAttribute(filterElement, NAME_ATTRIBUTE_NAME);
        String pattern = name != null ? null : getAttribute(filterElement, PATTERN_ATTRIBUTE_NAME);
        if (name != null || pattern != null) {
            List<Metadata<SystemPropertyActivation>> systemPropertyActivations = new LinkedList<>();
            List<Metadata<ClassAvailableActivation>> classAvailableActivations = new LinkedList<>();
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (isEnd(event, EXCLUDE, SCANNING_URIS) || isEnd(event, INCLUDE, SCANNING_URIS)) {
                    Filter filter;
                    if (filterElement.getName().getNamespaceURI().equals(WELD_URI)) {
                        filter = new WeldFilterImpl(name, systemPropertyActivations, classAvailableActivations, pattern);
                    } else {
                        filter = new FilterImpl(name, systemPropertyActivations, classAvailableActivations);
                    }
                    consumer.accept(new XmlMetadata<Filter>(filterElement.getName().toString(), filter, beansXml,
                            filterElement.getLocation().getLineNumber()));
                    return;
                } else if (event.isStartElement()) {
                    StartElement element = (StartElement) event;
                    if (isStartElement(element, IF_CLASS_AVAILABLE, SCANNING_URIS)) {
                        classAvailable(element, classAvailableActivations::add, false);
                    } else if (isStartElement(element, IF_CLASS_NOT_AVAILABLE, SCANNING_URIS)) {
                        classAvailable(element, classAvailableActivations::add, true);
                    } else if (isStartElement(element, IF_SYSTEM_PROPERTY, SCANNING_URIS)) {
                        systemProperty(element, systemPropertyActivations::add);
                    }
                }
            }
        }
    }

    private void classAvailable(StartElement element, Consumer<Metadata<ClassAvailableActivation>> consumer, boolean inverse) {
        String className = getAttribute(element, NAME_ATTRIBUTE_NAME);
        Metadata<ClassAvailableActivation> classAvailableActivation = new XmlMetadata<ClassAvailableActivation>(
                element.getName().toString(),
                new ClassAvailableActivationImpl(className, inverse), beansXml, element.getLocation().getLineNumber());
        consumer.accept(classAvailableActivation);
    }

    private void systemProperty(StartElement element, Consumer<Metadata<SystemPropertyActivation>> consumer) {
        String name = getAttribute(element, NAME_ATTRIBUTE_NAME);
        String value = getAttribute(element, VALUE_ATTRIBUTE_NAME);
        Metadata<SystemPropertyActivation> activation = new XmlMetadata<SystemPropertyActivation>(element.getName().toString(),
                new SystemPropertyActivationImpl(name, value), beansXml, element.getLocation().getLineNumber());
        consumer.accept(activation);
    }

    @SuppressWarnings("rawtypes")
    private String getAttribute(StartElement element, String name) {
        Iterator attributes = element.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            if (attribute.getName().getLocalPart().equals(name)) {
                return interpolate(attribute.getValue().trim());
            }
        }
        return null;
    }

    private boolean isStartElement(XMLEvent event, String name, Set<String> namespaces) {
        if (event.isStartElement()) {
            StartElement element = event.asStartElement();
            return isLocalName(element.getName(), name) && isInNamespace(element.getName(), namespaces);
        }
        return false;
    }

    private boolean isStartElement(XMLEvent event, String name) {
        return isStartElement(event, name, JAVAEE_URIS);
    }

    private boolean isEnd(XMLEvent event, String name) {
        return isEnd(event, name, JAVAEE_URIS);
    }

    private boolean isEnd(XMLEvent event, String name, Set<String> namespaces) {
        if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            return isLocalName(element.getName(), name) && isInNamespace(element.getName(), namespaces);
        }
        return false;
    }

    private BeanDiscoveryMode parseDiscoveryMode(String value) {
        for (BeanDiscoveryMode mode : BeanDiscoveryMode.values()) {
            if (mode.toString().equals(value)) {
                return mode;
            }
        }
        throw new IllegalStateException("Unknown bean discovery mode: " + value);
    }

    private <T> List<T> orEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private boolean isInNamespace(QName name, Set<String> uris) {
        String uri = name.getNamespaceURI();
        if (uris == null || uri.isEmpty()) {
            return true;
        }
        return uris.contains(uri);
    }

    private String getTrimmedElementText(XMLEventReader reader) throws XMLStreamException {
        return interpolate(reader.getElementText().trim());
    }

    protected String interpolate(String value) {
        return interpolator.apply(value);
    }

}
