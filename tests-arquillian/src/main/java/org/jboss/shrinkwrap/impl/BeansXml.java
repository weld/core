package org.jboss.shrinkwrap.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.shrinkwrap.api.BeansXmlClass;
import org.jboss.shrinkwrap.api.BeansXmlStereotype;
import org.jboss.shrinkwrap.api.asset.Asset;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class BeansXml implements Asset {
    private List<BeansXmlClass> alternatives = new ArrayList<BeansXmlClass>();
    private List<BeansXmlClass> interceptors = new ArrayList<BeansXmlClass>();
    private List<BeansXmlClass> decorators = new ArrayList<BeansXmlClass>();

    public BeansXml() {
    }

    public BeansXml alternatives(BeansXmlClass... alternatives) {
        this.alternatives.addAll(Arrays.asList(alternatives));
        return this;
    }

    public BeansXml alternatives(Class<?>... alternatives) {
        this.alternatives.addAll(Lists.transform(Arrays.asList(alternatives), ClassToBeansXmlClass.INSTANCE));
        return this;
    }

    public BeansXml interceptors(BeansXmlClass... interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
        return this;
    }

    public BeansXml interceptors(Class<?>... interceptors) {
        this.interceptors.addAll(Lists.transform(Arrays.asList(interceptors), ClassToBeansXmlClass.INSTANCE));
        return this;
    }

    public BeansXml decorators(BeansXmlClass... decorators) {
        this.decorators.addAll(Arrays.asList(decorators));
        return this;
    }

    public BeansXml decorators(Class<?>... decorators) {
        this.decorators.addAll(Lists.transform(Arrays.asList(decorators), ClassToBeansXmlClass.INSTANCE));
        return this;
    }

    public BeansXml stereotypes(Class<?>... stereotypes) {
        this.alternatives.addAll(Lists.transform(Arrays.asList(stereotypes), ClassToBeansXmlStereotype.INSTANCE));
        return this;
    }

    public InputStream openStream() {
        StringBuilder xml = new StringBuilder();
        xml.append("<beans>\n");
        appendSection("alternatives", alternatives, xml);
        appendSection("interceptors", interceptors, xml);
        appendSection("decorators", decorators, xml);
        xml.append("</beans>");

        return new ByteArrayInputStream(xml.toString().getBytes());
    }

    private void appendSection(String name, List<BeansXmlClass> classes, StringBuilder xml) {
        if (classes.size() > 0) {
            xml.append("<").append(name).append(">\n");
            appendClasses(classes, xml);
            xml.append("</").append(name).append(">\n");
        }
    }

    private void appendClasses(List<BeansXmlClass> classes, StringBuilder xml) {
        for (BeansXmlClass clazz : classes) {
            xml.append(clazz.asXmlElement()).append("\n");
        }
    }

    private static class ClassToBeansXmlClass implements Function<Class<?>, BeansXmlClass> {

        private static final ClassToBeansXmlClass INSTANCE = new ClassToBeansXmlClass();

        @Override
        public BeansXmlClass apply(Class<?> input) {
            return new BeansXmlClass(input);
        }
    }

    private static class ClassToBeansXmlStereotype implements Function<Class<?>, BeansXmlStereotype> {

        private static final ClassToBeansXmlStereotype INSTANCE = new ClassToBeansXmlStereotype();

        @Override
        public BeansXmlStereotype apply(Class<?> input) {
            return new BeansXmlStereotype(input);
        }
    }
}
