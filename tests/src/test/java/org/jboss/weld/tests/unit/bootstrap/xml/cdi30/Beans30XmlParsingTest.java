/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.bootstrap.xml.cdi30;

import static org.testng.Assert.assertEquals;

import java.util.Collection;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.metadata.FilterPredicate;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.testng.annotations.Test;

public class Beans30XmlParsingTest {

    private final WeldBootstrap bootstrap = new WeldBootstrap();

    private BeansXml getBeansXml(String filename) {
        return bootstrap.parse(Beans30XmlParsingTest.class.getResource(filename));
    }

    @Test
    public void testCdi30Namespace() {
        BeansXml xml = getBeansXml("cdi30-beans.xml");
        assertEquals(xml.getBeanDiscoveryMode(), BeanDiscoveryMode.ANNOTATED);
        assertEquals(xml.getVersion(), "3.0");
        Collection<Metadata<Filter>> filters = xml.getScanning().getExcludes();
        assertEquals(filters.size(), 3);
        for (Metadata<Filter> filter : filters) {
            new FilterPredicate(filter, DefaultResourceLoader.INSTANCE);
        }
    }
}
