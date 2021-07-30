package org.jboss.shrinkwrap.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.asset.Asset;

public class BeansXml implements Asset {

    public static final BeansXml SUPPRESSOR = new BeansXml(Collections.<Class<?>> emptyList(), Collections.<Class<?>> emptyList(), Collections.<Class<?>> emptyList(), Collections.<Class<?>> emptyList(), Collections.<Exclude> emptyList()) {
        @Override
        public BeanDiscoveryMode getBeanDiscoveryMode() {
            return BeanDiscoveryMode.NONE;
        }
    };

    private static final String CLOSING_TAG_PREFIX = "</";
    private static final String OPENING_TAG_PREFIX = "<";
    private static final String TAG_SUFFIX = ">";
    private static final String TAG_SUFFIX_NEW_LINE = ">\n";
    private static final String TAG_SUFFIX_SELF_CLOSE_NEW_LINE = " />\n";
    private static final String ALTERNATIVES_ELEMENT_NAME = "alternatives";
    private static final String CLASS = "class";

    private static final String SCAN_ELEMENT_NAME = "scan";
    private static final String EXCLUDE_ELEMENT_NAME = "exclude";
    private static final String IF_SYSTEM_PROPERTY_ELEMENT_NAME = "if-system-property";
    private static final String IF_CLASS_AVAILABLE_ELEMENT_NAME = "if-class-available";
    private static final String IF_CLASS_NOT_AVAILABLE_ELEMENT_NAME = "if-class-not-available";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String VALUE_ATTRIBUTE_NAME = "value";

    private final List<Class<?>> alternatives;
    private final List<Class<?>> interceptors;
    private final List<Class<?>> decorators;
    private final List<Class<?>> stereotypes;
    private final List<Exclude> excludeFilters;

    private BeanDiscoveryMode mode = BeanDiscoveryMode.ANNOTATED;

    public static class Exclude {

        private final String classFilter;
        private final List<Condition> conditions;

        private Exclude(String classFilter) {
            this.classFilter = classFilter;
            conditions = new ArrayList<Condition>();
        }

        public static Exclude match(String classFilter) {
            return new Exclude(classFilter);
        }

        public static Exclude exact(Class<?> clazz) {
            return new Exclude(clazz.getName());
        }

        public Exclude ifSystemProperty(String name) {
            conditions.add(new IfSystemProperty(name, null));
            return this;
        }

        public Exclude ifSystemProperty(String name, String value) {
            conditions.add(new IfSystemProperty(name, value));
            return this;
        }

        public Exclude ifClassAvailable(Class<?> clazz) {
            return ifClassAvailable(clazz.getName());
        }

        public Exclude ifClassAvailable(String className) {
            conditions.add(new IfClassAvailable(className));
            return this;
        }

        public Exclude ifClassNotAvailable(Class<?> clazz) {
            return ifClassNotAvailable(clazz.getName());
        }

        public Exclude ifClassNotAvailable(String className) {
            conditions.add(new IfClassNotAvailable(className));
            return this;
        }

        public String getClassFilter() {
            return this.classFilter;
        }

        public List<Condition> getConditions() {
            return this.conditions;
        }

        private static class Condition {
            private final String nameParam;
            private final String tagName;

            public Condition(String tagName, String nameParam) {
                this.tagName = tagName;
                this.nameParam = nameParam;
            }

            public String getName() {
                return this.nameParam;
            }

            public String getTagName() {
                return this.tagName;
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append(OPENING_TAG_PREFIX).append(getTagName());
                appendAttribute(NAME_ATTRIBUTE_NAME, getName(), sb);
                sb.append(TAG_SUFFIX_SELF_CLOSE_NEW_LINE);
                return sb.toString();
            }

        }

        private static class IfSystemProperty extends Condition {

            private final String value;

            public IfSystemProperty(String name, String value) {
                super(IF_SYSTEM_PROPERTY_ELEMENT_NAME, name);
                this.value = value;
            }

            public String getValue() {
                return this.value;
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append(OPENING_TAG_PREFIX).append(getTagName());
                appendAttribute(NAME_ATTRIBUTE_NAME, getName(), sb);
                if (value != null) {
                    appendAttribute(VALUE_ATTRIBUTE_NAME, getValue(), sb);
                }
                sb.append(TAG_SUFFIX_SELF_CLOSE_NEW_LINE);
                return sb.toString();
            }

        }

        private static class IfClassAvailable extends Condition {

            public IfClassAvailable(String className) {
                super(IF_CLASS_AVAILABLE_ELEMENT_NAME, className);
            }

        }

        private static class IfClassNotAvailable extends Condition {

            public IfClassNotAvailable(String className) {
                super(IF_CLASS_NOT_AVAILABLE_ELEMENT_NAME, className);
            }
        }

    }

    public BeansXml() {
        this(new ArrayList<Class<?>>(), new ArrayList<Class<?>>(), new ArrayList<Class<?>>(), new ArrayList<Class<?>>(), new ArrayList<Exclude>());
    }

    public BeansXml(BeanDiscoveryMode mode) {
        this();
        setBeanDiscoveryMode(mode);
    }

    private BeansXml(List<Class<?>> alternatives, List<Class<?>> interceptors, List<Class<?>> decorators, List<Class<?>> stereotypes, List<Exclude> excludeFilters) {
        this.alternatives = alternatives;
        this.interceptors = interceptors;
        this.decorators = decorators;
        this.stereotypes = stereotypes;
        this.excludeFilters = excludeFilters;
    }

    public BeansXml alternatives(Class<?>... alternatives) {
        this.alternatives.addAll(Arrays.asList(alternatives));
        return this;
    }

    public BeansXml interceptors(Class<?>... interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
        return this;
    }

    public BeansXml decorators(Class<?>... decorators) {
        this.decorators.addAll(Arrays.asList(decorators));
        return this;
    }

    public BeansXml stereotype(Class<?>... stereotypes) {
        this.stereotypes.addAll(Arrays.asList(stereotypes));
        return this;
    }

    public BeansXml excludeFilters(Exclude... filters) {
        this.excludeFilters.addAll(Arrays.asList(filters));
        return this;
    }

    public BeanDiscoveryMode getBeanDiscoveryMode() {
        return mode;
    }

    public void setBeanDiscoveryMode(BeanDiscoveryMode mode) {
        this.mode = mode;
    }

    @Override
    public InputStream openStream() {
        StringBuilder xml = new StringBuilder();
        xml.append("<beans version=\"1.1\" bean-discovery-mode=\"");
        xml.append(getBeanDiscoveryMode().getValue());
        xml.append("\">\n");
        appendExcludeFilters(excludeFilters, xml);
        appendAlternatives(alternatives, stereotypes, xml);
        appendSection("interceptors", CLASS, interceptors, xml);
        appendSection("decorators", CLASS, decorators, xml);
        xml.append("</beans>");

        return new ByteArrayInputStream(xml.toString().getBytes());
    }

    private void appendExcludeFilters(List<Exclude> filters, StringBuilder xml) {
        if (filters.size() > 0) {
            xml.append(OPENING_TAG_PREFIX).append(SCAN_ELEMENT_NAME).append(TAG_SUFFIX_NEW_LINE);
            for(Exclude ex : filters) {
                xml.append(OPENING_TAG_PREFIX).append(EXCLUDE_ELEMENT_NAME);
                appendAttribute(NAME_ATTRIBUTE_NAME, ex.getClassFilter(), xml);
                List<Exclude.Condition> conditions = ex.getConditions();
                if(conditions.isEmpty()) {
                    xml.append(TAG_SUFFIX_SELF_CLOSE_NEW_LINE);
                } else {
                    xml.append(TAG_SUFFIX_NEW_LINE);
                    for (Exclude.Condition c : conditions) {
                         xml.append(c.toString());
                    }
                    xml.append(CLOSING_TAG_PREFIX).append(EXCLUDE_ELEMENT_NAME).append(TAG_SUFFIX_NEW_LINE);
                }
            }
            xml.append(CLOSING_TAG_PREFIX).append(SCAN_ELEMENT_NAME).append(TAG_SUFFIX_NEW_LINE);
        }
    }

    private static void appendAttribute(String name, String value, StringBuilder xml) {
        xml.append(" ").append(name).append("=\"").append(value).append("\"");
    }

    private static void appendAlternatives(List<Class<?>> alternatives, List<Class<?>> stereotypes, StringBuilder xml) {
        if (alternatives.size() > 0 || stereotypes.size() > 0) {
            xml.append(OPENING_TAG_PREFIX).append(ALTERNATIVES_ELEMENT_NAME).append(TAG_SUFFIX_NEW_LINE);
            appendClasses(CLASS, alternatives, xml);
            appendClasses("stereotype", stereotypes, xml);
            xml.append(CLOSING_TAG_PREFIX).append(ALTERNATIVES_ELEMENT_NAME).append(TAG_SUFFIX_NEW_LINE);
        }
    }

    private static void appendSection(String name, String subName, List<Class<?>> classes, StringBuilder xml) {
        if (classes.size() > 0) {
            xml.append(OPENING_TAG_PREFIX).append(name).append(TAG_SUFFIX_NEW_LINE);
            appendClasses(subName, classes, xml);
            xml.append(CLOSING_TAG_PREFIX).append(name).append(TAG_SUFFIX_NEW_LINE);
        }
    }

    private static void appendClasses(String name, List<Class<?>> classes, StringBuilder xml) {
        for (Class<?> clazz : classes) {
            xml.append(OPENING_TAG_PREFIX).append(name).append(TAG_SUFFIX).append(clazz.getName()).append(CLOSING_TAG_PREFIX).append(name).append(TAG_SUFFIX_NEW_LINE);
        }
    }
}
