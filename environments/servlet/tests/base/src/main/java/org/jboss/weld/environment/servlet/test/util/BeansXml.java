package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeansXml implements Asset {

    public static final String SCHEMA = "<beans>\n";
    public static final String FULL_SCHEMA =
            "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\" \n" +
            "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
            "       xmlns:weld=\"http://jboss.org/schema/weld/beans\" \n" +
            "       xsi:schemaLocation=\"\n" +
            "          http://java.sun.com/xml/ns/javaee http://docs.jboss.org/cdi/beans_1_0.xsd\n" +
            "          http://jboss.org/schema/weld/beans http://jboss.org/schema/weld/beans_1_1.xsd\">\n";

    private String schema = SCHEMA;

    private List<Class<?>> alternatives = new ArrayList<Class<?>>();
    private List<Class<?>> interceptors = new ArrayList<Class<?>>();
    private List<Class<?>> decorators = new ArrayList<Class<?>>();
    private List<Class<?>> stereotypes = new ArrayList<Class<?>>();

    public BeansXml() {
    }

    public void setSchema(String schema) {
        this.schema = schema;
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

    public InputStream openStream() {
        StringBuilder xml = new StringBuilder();
        xml.append(schema);
        appendAlternatives(alternatives, stereotypes, xml);
        appendSection("interceptors", "class", interceptors, xml);
        appendSection("decorators", "class", decorators, xml);
        appendExternal(xml);
        xml.append("</beans>");

        return new ByteArrayInputStream(xml.toString().getBytes());
    }

    @SuppressWarnings({"UnusedParameters"})
    protected void appendExternal(StringBuilder xml) {
    }

    private void appendAlternatives(List<Class<?>> alternatives, List<Class<?>> stereotypes, StringBuilder xml) {
        if (alternatives.size() > 0 || stereotypes.size() > 0) {
            xml.append("<").append("alternatives").append(">\n");
            appendClasses("class", alternatives, xml);
            appendClasses("stereotype", stereotypes, xml);
            xml.append("</").append("alternatives").append(">\n");
        }
    }

    private void appendSection(String name, String subName, List<Class<?>> classes, StringBuilder xml) {
        if (classes.size() > 0) {
            xml.append("<").append(name).append(">\n");
            appendClasses(subName, classes, xml);
            xml.append("</").append(name).append(">\n");
        }
    }

    private void appendClasses(String name, List<Class<?>> classes, StringBuilder xml) {
        for (Class<?> clazz : classes) {
            xml.append("<").append(name).append(">").append(clazz.getName()).append("</").append(name).append(">\n");
        }
    }
}
