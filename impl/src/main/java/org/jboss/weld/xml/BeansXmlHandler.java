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

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.BeansXmlRecord;
import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.SystemPropertyActivation;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.BeansXmlRecordBuilder;
import org.jboss.weld.metadata.ClassAvailableActivationImpl;
import org.jboss.weld.metadata.FilterImpl;
import org.jboss.weld.metadata.ScanningImpl;
import org.jboss.weld.metadata.SystemPropertyActivationImpl;
import org.slf4j.cal10n.LocLogger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

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

        private final String uri;
        private final String localName;
        private final Collection<String> nestedElements;

        public Container(String uri, String localName, String... nestedElements) {
            this.uri = uri;
            this.localName = localName;
            this.nestedElements = asList(nestedElements);
        }

        public String getLocalName() {
            return localName;
        }

        public String getUri() {
            return uri;
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

        protected boolean isInNamespace(String uri) {
            return uri.length() == 0 || uri.equals(getUri());
        }
    }

    private abstract class SpecContainer extends Container {

        private BeansXmlRecordBuilder builder;
        private final Collection<Metadata<BeansXmlRecord>> records;

        public SpecContainer(Collection<Metadata<BeansXmlRecord>> records, String localName, String... nestedElements) {
            super(JAVAEE_URI, localName, nestedElements);
            this.records = records;
        }

        /**
         * Indicates whether this {@link Container} is accepting a given element.
         */
        protected boolean isAccepting(String uri, String localName) {
            return isInNamespace(uri) && getNestedElements().contains(localName);
        }

        @Override
        public void processStartChildElement(String uri, String localName, String qName, Attributes attributes) {
            if (!isAccepting(uri, localName)) {
                return;
            }
            if (builder != null) {
                throw new IllegalStateException(BeansXmlRecordBuilder.class.getName() + " not cleaned up");
            }
            builder = new BeansXmlRecordBuilder();

            String enabled = interpolate(trim(attributes.getValue(JAVAEE_URI, "enabled")));
            String priority = interpolate(trim(attributes.getValue(JAVAEE_URI, "priority")));

            if (enabled != null) {
                builder.setEnabled(Boolean.valueOf(enabled));
            }
            if (priority != null) {
                builder.setPriority(Integer.valueOf(priority));
            }
            builder.setStereotype("stereotype".equals(localName));
        }

        @Override
        public void processEndChildElement(String uri, String localName, String qName, String nestedText) {
            if (!isAccepting(uri, localName)) {
                return;
            }
            if (builder == null) {
                throw new IllegalStateException(BeansXmlRecordBuilder.class.getName() + " not set");
            }
            builder.setValue(interpolate(trim(nestedText)));
            records.add(buildRecord(builder, qName));
            builder = null;
        }

        protected Metadata<BeansXmlRecord> buildRecord(BeansXmlRecordBuilder builder, String qName) {
            return new SpecXmlMetadata(qName, builder.create(), file, locator.getLineNumber());
        }
    }

    public static final String WELD_URI = "http://jboss.org/schema/weld/beans";
    public static final String JAVAEE_URI = "http://java.sun.com/xml/ns/javaee";

    /*
    * The containers we are parsing
    */
    private final Collection<Container> containers;

    /*
    * Storage for parsed info
    */
    private final List<Metadata<BeansXmlRecord>> interceptors;
    private final List<Metadata<BeansXmlRecord>> decorators;
    private final List<Metadata<BeansXmlRecord>> alternatives;
    private final List<Metadata<Filter>> includes;
    private final List<Metadata<Filter>> excludes;
    protected final URL file;

    /*
    * Parser State
    */
    private Collection<Container> seenContainers;
    private Container currentContainer;
    private StringBuilder buffer;
    private Locator locator;

    public BeansXmlHandler(final URL file) {
        this.file = file;
        this.interceptors = new ArrayList<Metadata<BeansXmlRecord>>();
        this.decorators = new ArrayList<Metadata<BeansXmlRecord>>();
        this.alternatives = new ArrayList<Metadata<BeansXmlRecord>>();
        this.includes = new ArrayList<Metadata<Filter>>();
        this.excludes = new ArrayList<Metadata<Filter>>();
        this.seenContainers = new ArrayList<Container>();
        this.containers = new ArrayList<Container>();
        containers.add(new SpecContainer(interceptors, "interceptors", "class") {

            @Override
            public void handleMultiple() {
                throw new DefinitionException(MULTIPLE_INTERCEPTORS, file + "@" + locator.getLineNumber());
            }
        });
        containers.add(new SpecContainer(decorators, "decorators", "class") {

            @Override
            public void handleMultiple() {
                throw new DefinitionException(MULTIPLE_DECORATORS, file + "@" + locator.getLineNumber());
            }
        });
        containers.add(new SpecContainer(alternatives, "alternatives", "class", "stereotype") {

            @Override
            public void handleMultiple() {
                throw new DefinitionException(MULTIPLE_ALTERNATIVES, file + "@" + locator.getLineNumber());
            }
        });
        containers.add(new Container(WELD_URI, "scan") {

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
                } else if (isInNamespace(uri) && "if-system-property".equals(localName)) {
                    String systemPropertyName = interpolate(trim(attributes.getValue("name")));
                    String systemPropertyValue = interpolate(trim(attributes.getValue("value")));
                    Metadata<SystemPropertyActivation> systemPropertyActivation = new XmlMetadata<SystemPropertyActivation>(qName, new SystemPropertyActivationImpl(systemPropertyName, systemPropertyValue), file, locator.getLineNumber());
                    systemPropertyActivations.add(systemPropertyActivation);
                } else if (isInNamespace(uri) && "if-class-available".equals(localName)) {
                    String className = interpolate(trim(attributes.getValue("name")));
                    Metadata<ClassAvailableActivation> classAvailableActivation = new XmlMetadata<ClassAvailableActivation>(qName, new ClassAvailableActivationImpl(className), file, locator.getLineNumber());
                    classAvailableActivations.add(classAvailableActivation);
                }
            }

            @Override
            public void processEndChildElement(String uri, String localName, String qName, String nestedText) {
                if (isFilterElement(uri, localName)) {
                    Metadata<Filter> filter = new XmlMetadata<Filter>(qName, new FilterImpl(pattern, name, systemPropertyActivations, classAvailableActivations), file, locator.getLineNumber());
                    if ("include".equals(localName)) {
                        includes.add(filter);
                    } else if ("exclude".equals(localName)) {
                        excludes.add(filter);
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
                if (container.getLocalName().equals(localName) && container.getUri().equals(uri)) {
                    return container;
                }
            }
        }
        return null;
    }

    public BeansXml createBeansXml() {
        return new BeansXmlImpl(alternatives, decorators, interceptors, new ScanningImpl(includes, excludes), file);
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
