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

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.util.Preconditions;

/**
 * A class used to store information about the bean archive and to build the
 * {@link org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive}
 * instance from the gathered information.
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public class BeanArchiveBuilder {

    private final Map<String, Object> attributes;

    private final Set<String> beanClasses;

    private BeansXml beansXml;

    private String id;

    private Set<String> knownClasses;

    public BeanArchiveBuilder() {
        this.attributes = new HashMap<>();
        this.beanClasses = new HashSet<>();
    }

    /**
     *
     * @return the bean deployment archive
     */
    public WeldBeanDeploymentArchive build() {
        Preconditions.checkArgumentNotNull(id, "id");
        return new WeldBeanDeploymentArchive(id, beanClasses, knownClasses, getBeansXml());
    }

    public BeansXml getBeansXml() {
        return beansXml;
    }

    public BeanArchiveBuilder setBeansXml(BeansXml beansXml) {
        this.beansXml = beansXml;
        if (beansXml != null && BeanDiscoveryMode.ANNOTATED.equals(beansXml.getBeanDiscoveryMode())) {
            this.knownClasses = new HashSet<>(beanClasses);
        }
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
        beanClasses.add(className);
        return this;
    }

    public BeanArchiveBuilder clearClasses() {
        beanClasses.clear();
        return this;
    }

    public Set<String> getClasses() {
        return beanClasses;
    }

    public BeanArchiveBuilder setAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Iterator<String> getClassIterator() {
        return beanClasses.iterator();
    }

}
