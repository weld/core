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
package org.jboss.weld.environment.se.discovery.url;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;

/**
 * A class used to store information about the bean archive and to build the {@link org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive}
 * instance from the gathered information.
 *
 * @author Matej Briškár
 */
class BeanArchiveBuilder {

    private Object index;
    private List<String> classes;
    private URL beansXmlUrl;
    private BeansXml beansXml = null;
    private Bootstrap bootstrap;
    private String id;

    public BeanArchiveBuilder(String id, Object index, List<String> classes, URL beansXmlUrl, Bootstrap bootstrap) {
        this.id = id;
        this.index = index;
        this.classes = classes;
        this.beansXmlUrl = beansXmlUrl;
        this.bootstrap = bootstrap;
    }

    public BeansXml parseBeansXml() {
        beansXml = bootstrap.parse(beansXmlUrl);
        return beansXml;
    }

    public BeansXml getParsedBeansXml() {
        if (beansXml == null) {
            return parseBeansXml();
        } else {
            return beansXml;
        }
    }

    public WeldSEBeanDeploymentArchive build() {
        if (id == null) {
            throw new IllegalStateException("ID not set");
        }
        return new WeldSEBeanDeploymentArchive(id, classes, getParsedBeansXml());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void clearClasses() {
        classes.clear();
    }

    public void removeClass(String className) {
        classes.remove(className);
    }

    public Object getIndex() {
        return index;
    }

    public List<String> getClasses() {
        return classes;
    }

    public URL getBeansXmlUrl() {
        return beansXmlUrl;
    }

    public Iterator<String> getClassIterator() {
        return classes.iterator();
    }
}
