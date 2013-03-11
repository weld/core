package org.jboss.weld.xml;

import static java.util.Arrays.asList;
import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_ALTERNATIVES;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_DECORATORS;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_INTERCEPTORS;
import static org.jboss.weld.logging.messages.XmlMessage.MULTIPLE_SCANNING;
import static org.jboss.weld.logging.messages.XmlMessage.XSD_VALIDATION_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.XSD_VALIDATION_WARNING;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.SystemPropertyActivation;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.ClassAvailableActivationImpl;
import org.jboss.weld.metadata.FilterImpl;
import org.jboss.weld.metadata.ScanningImpl;
import org.jboss.weld.metadata.SystemPropertyActivationImpl;
import org.jboss.weld.metadata.WeldFilterImpl;
import org.slf4j.cal10n.LocLogger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ImmutableSet;

/**
 * An implementation of the beans.xml parser written using SAX
 * <p/>
 * This class is NOT threadsafe, and should only be called in a single thread
 *
 * @author Pete Muir
 */
public class BeansXmlHandler extends DefaultHandler {

    static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);

    protected static String trim(String str) {
        if (str == null) {
            return null;
        } else {
            return str.trim();
        }
    }

    public abstract class Container {

        private final Set<String> uris;
        private final String localName;
        private final Collection<String> nestedElements;

        public Container(Set<String> uris, String localName, String... nestedElements) {
            this.uris = uris;
            this.localName = localName;
            this.nestedElements = asList(nestedElements);
        }

        public String getLocalName() {
            return localName;
        }

        public Set<String> getUris() {
            return uris;
        }

        /**
         * Called by startElement, the nested content is not available
         */
        public void processStartChildElement(String uri, String localName, String qName, Attributes attributes) {
        }

        /**
         * Called by endElement, the nested content is not available
         */
        public void processEndChildElement(String uri, String localName, String qName, String nestedText) {
        }

        public void handleMultiple() {

        }

        public Collection<String> getNestedElements() {
            return nestedElements;
        }

        @Override
        public String toString() {
            return "<" + localName + " />";
        }

        protected boolean isInNamespace(String namespace) {
            if (namespace.length() == 0) {
                return true;
            }
            return getUris().contains(namespace);
        }
    }

    private abstract class SpecContainer extends Container {

        public SpecContainer(String localName, String... nestedElements) {
            super(JAVAEE_URIS, localName, nestedElements);
        }
    }


    public static final String JAVAEE_LEGACY_URI = "http://java.sun.com/xml/ns/javaee";
    public static final String JAVAEE_URI = "http://xmlns.jcp.org/xml/ns/javaee";
    public static final Set<String> JAVAEE_URIS = ImmutableSet.of(JAVAEE_LEGACY_URI, JAVAEE_URI);

    public static final String WELD_URI = "http://jboss.org/schema/weld/beans";
    public static final Set<String> SCANNING_URI = ImmutableSet.of(WELD_URI, JAVAEE_URI, JAVAEE_LEGACY_URI);

    private static final String VERSION_ATTRIBUTE_NAME = "version";
    private static final String BEAN_DISCOVERY_MODE_ATTRIBUTE_NAME = "bean-discovery-mode";
    private static final String ROOT_ELEMENT_NAME = "beans";

    private static final String IF_CLASS_AVAILABLE = "if-class-available";
    private static final String IF_CLASS_NOT_AVAILABLE = "if-class-not-available";
    private static final String IF_SYSTEM_PROPERTY = "if-system-property";

    /*
    * The containers we are parsing
    */
    private final Collection<Container> containers;

    /*
    * Storage for parsed info
    */
    private final List<Metadata<String>> interceptors;
    private final List<Metadata<String>> decorators;
    private final List<Metadata<String>> alternativesClasses;
    private final List<Metadata<String>> alternativeStereotypes;
    private final List<Metadata<Filter>> includes;
    private final List<Metadata<Filter>> excludes;
    protected final URL file;
    private BeanDiscoveryMode discoveryMode;
    private String version;

    /*
    * Parser State
    */
    private Collection<Container> seenContainers;
    private Container currentContainer;
    private StringBuilder buffer;
    private Locator locator;

    public BeansXmlHandler(final URL file) {
        this.file = file;
        this.interceptors = new ArrayList<Metadata<String>>();
        this.decorators = new ArrayList<Metadata<String>>();
        this.alternativesClasses = new ArrayList<Metadata<String>>();
        this.alternativeStereotypes = new ArrayList<Metadata<String>>();
        this.includes = new ArrayList<Metadata<Filter>>();
        this.excludes = new ArrayList<Metadata<Filter>>();
        this.seenContainers = new ArrayList<Container>();
        this.containers = new ArrayList<Container>();
        this.discoveryMode = BeanDiscoveryMode.ALL; // this is the default value for a beans.xml file
        containers.add(new SpecContainer("interceptors", "class") {

            @Override
            public void processEndChildElement(String uri, String localName, String qName, String nestedText) {
                if (isInNamespace(uri) && "class".equals(localName)) {
                    interceptors.add(new XmlMetadata<String>(qName, trim(nestedText), file, locator.getLineNumber()));
                }
            }

            @Override
            public void handleMultiple() {
                throw new DefinitionException(MULTIPLE_INTERCEPTORS, file + "@" + locator.getLineNumber());
            }
        });
        containers.add(new SpecContainer("decorators", "class") {

            @Override
            public void processEndChildElement(String uri, String localName, String qName, String nestedText) {
                if (isInNamespace(uri) && "class".equals(localName)) {
                    decorators.add(new XmlMetadata<String>(qName, trim(nestedText), file, locator.getLineNumber()));
                }
            }

            @Override
            public void handleMultiple() {
                throw new DefinitionException(MULTIPLE_DECORATORS, file + "@" + locator.getLineNumber());
            }
        });
        containers.add(new SpecContainer("alternatives", "class", "stereotype") {

            @Override
            public void processEndChildElement(String uri, String localName, String qName, String nestedText) {
                if (isInNamespace(uri) && "class".equals(localName)) {
                    alternativesClasses.add(new XmlMetadata<String>(qName, trim(nestedText), file, locator.getLineNumber()));
                } else if (isInNamespace(uri) && "stereotype".equals(localName)) {
                    alternativeStereotypes.add(new XmlMetadata<String>(qName, trim(nestedText), file, locator.getLineNumber()));
                }
            }

            @Override
            public void handleMultiple() {
                throw new DefinitionException(MULTIPLE_ALTERNATIVES, file + "@" + locator.getLineNumber());
            }
        });
        containers.add(new Container(SCANNING_URI, "scan") {

            String name;
            String pattern;
            Collection<Metadata<SystemPropertyActivation>> systemPropertyActivations;
            Collection<Metadata<ClassAvailableActivation>> classAvailableActivations;

            @Override
            public void processStartChildElement(String uri, String localName, String qName, Attributes attributes) {
                if (isFilterElement(uri, localName)) {
                    name = interpolate(trim(attributes.getValue("name")));
                    pattern = interpolate(trim(attributes.getValue("pattern")));
                    systemPropertyActivations = new ArrayList<Metadata<SystemPropertyActivation>>();
                    classAvailableActivations = new ArrayList<Metadata<ClassAvailableActivation>>();
                } else if (isInNamespace(uri)) {
                    if (IF_CLASS_AVAILABLE.equals(localName) || IF_CLASS_NOT_AVAILABLE.equals(localName)) {
                        String className = interpolate(trim(attributes.getValue("name")));
                        Metadata<ClassAvailableActivation> classAvailableActivation = new XmlMetadata<ClassAvailableActivation>(qName, new ClassAvailableActivationImpl(className, IF_CLASS_NOT_AVAILABLE.equals(localName)), file, locator.getLineNumber());
                        classAvailableActivations.add(classAvailableActivation);
                    } else if (IF_SYSTEM_PROPERTY.equals(localName)) {
                        String systemPropertyName = interpolate(trim(attributes.getValue("name")));
                        String systemPropertyValue = interpolate(trim(attributes.getValue("value")));
                        Metadata<SystemPropertyActivation> systemPropertyActivation = new XmlMetadata<SystemPropertyActivation>(qName, new SystemPropertyActivationImpl(systemPropertyName, systemPropertyValue), file, locator.getLineNumber());
                        systemPropertyActivations.add(systemPropertyActivation);
                    }
                }
            }

            @Override
            public void processEndChildElement(String uri, String localName, String qName, String nestedText) {
                if (isFilterElement(uri, localName)) {
                    Filter filter = null;
                    if (WELD_URI.equals(uri)) {
                        filter = new WeldFilterImpl(name, systemPropertyActivations, classAvailableActivations, pattern);
                    } else {
                        filter = new FilterImpl(name, systemPropertyActivations, classAvailableActivations);
                    }
                    Metadata<Filter> filterMetadata = new XmlMetadata<Filter>(qName, filter, file, locator.getLineNumber());
                    if ("include".equals(localName)) {
                        includes.add(filterMetadata);
                    } else if ("exclude".equals(localName)) {
                        excludes.add(filterMetadata);
                    }
                    // reset
                    name = null;
                    pattern = null;
                    systemPropertyActivations = null;
                    classAvailableActivations = null;
                }
            }

            private boolean isFilterElement(String uri, String localName) {
                return isInNamespace(uri) && ("include".equals(localName) || "exclude".equals(localName));
            }

            @Override
            public void handleMultiple() {
                throw new DefinitionException(MULTIPLE_SCANNING, file + "@" + locator.getLineNumber());
            }

        });
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals(ROOT_ELEMENT_NAME) && (uri == "" || JAVAEE_URIS.contains(uri))) {
            processRootElement(attributes);
            return;
        }

        if (currentContainer == null) {
            Container container = getContainer(uri, localName);
            if (container != null) {
                if (seenContainers.contains(container)) {
                    container.handleMultiple();
                }
                currentContainer = container;
            }
        } else {
            currentContainer.processStartChildElement(uri, localName, qName, attributes);
            // The current container is interested in the content of this element
            if (currentContainer.getNestedElements().contains(localName)) {
                buffer = new StringBuilder();
            }
        }
    }

    private void processRootElement(Attributes attributes) {
        String discoveryMode = attributes.getValue(BEAN_DISCOVERY_MODE_ATTRIBUTE_NAME);
        if (discoveryMode != null) {
            this.discoveryMode = BeanDiscoveryMode.valueOf(discoveryMode.toUpperCase());
        }
        String version = attributes.getValue(VERSION_ATTRIBUTE_NAME);
        if (version != null) {
            this.version = version;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (currentContainer != null) {
            currentContainer.processEndChildElement(uri, localName, qName, buffer != null ? buffer.toString() : null);

            // The current container was interested in this element
            if (currentContainer.getNestedElements().contains(localName)) {
                buffer = null;
            }
            Container container = getContainer(uri, localName);
            if (container != null) {
                // We are exiting the container, record it so we know it's already been declared (for error reporting)
                seenContainers.add(container);
                // And stop work until we find another container of interest
                currentContainer = null;
            }
        }
    }

    private Container getContainer(String uri, String localName) {
        return getContainer(uri, localName, containers);
    }

    private static Container getContainer(String uri, String localName, Collection<Container> containers) {
        for (Container container : containers) {
            if (uri.length() == 0) {
                if (container.getLocalName().equals(localName)) {
                    return container;
                }
            } else {
                if (container.getLocalName().equals(localName) && container.getUris().contains(uri)) {
                    return container;
                }
            }
        }
        return null;
    }

    public BeansXml createBeansXml() {
        return new BeansXmlImpl(alternativesClasses, alternativeStereotypes, decorators, interceptors, new ScanningImpl(includes, excludes), file, discoveryMode, version);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (buffer != null) {
            buffer.append(ch, start, length);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        log.warn(XSD_VALIDATION_WARNING, file, e.getLineNumber(), e.getMessage());
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        if (e.getMessage().equals("cvc-elt.1: Cannot find the declaration of element 'beans'.")) {
            // Ignore the errors we get when there is no schema defined
            return;
        }
        log.warn(XSD_VALIDATION_ERROR, file, e.getLineNumber(), e.getMessage());
    }

    protected String interpolate(String string) {
        return string; // noop by default, integrators may override
    }

}
