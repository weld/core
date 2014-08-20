/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.deployment.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;

/**
 * A class used to store information about the bean archive and to build the {@link org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive}
 * instance from the gathered information.
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public class BeanArchiveBuilder {

    private final Map<String, Object> attributes;

    private final Set<String> classes;

    private BeansXml beansXml;

    private String id;

    public BeanArchiveBuilder() {
        this.attributes = new HashMap<String, Object>();
        this.classes = new HashSet<String>();
    }

    /**
     *
     * @return the bean deployment archive
     */
    public WeldBeanDeploymentArchive build() {
        if (id == null) {
            throw new IllegalStateException("ID must be set");
        }
        return new WeldBeanDeploymentArchive(id, classes, getBeansXml());
    }

    public BeansXml getBeansXml() {
        return beansXml;
    }

    public BeanArchiveBuilder setBeansXml(BeansXml beansXml) {
        this.beansXml = beansXml;
        return this;
    }

    public String getId() {
        return id;
    }

    public BeanArchiveBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public BeanArchiveBuilder addClass(String className) {
        classes.add(className);
        return this;
    }

    public BeanArchiveBuilder clearClasses() {
        classes.clear();
        return this;
    }

    public Set<String> getClasses() {
        return classes;
    }

    public BeanArchiveBuilder setAttribute(String key, Object value) {
        attributes.put(key, value);        return this;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Iterator<String> getClassIterator() {
        return classes.iterator();
    }

}
