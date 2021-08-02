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
package org.jboss.weld.tests.unit.bootstrap.xml.cdi11;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Collection;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.metadata.FilterPredicate;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.testng.annotations.Test;

public class BeansXmlParsingTest {

    private final WeldBootstrap bootstrap = new WeldBootstrap();

    private BeansXml getBeansXml(String filename) {
        return bootstrap.parse(BeansXmlParsingTest.class.getResource(filename));
    }

    @Test
    public void testNoSchemaNoAttributes() {
        BeansXml xml = getBeansXml("cdi11-beans1.xml");
        assertEquals(xml.getBeanDiscoveryMode(), BeanDiscoveryMode.ANNOTATED);
        assertNull(xml.getVersion());
    }

    @Test
    public void testExplicitVersionAndMode1() {
        BeansXml xml = getBeansXml("cdi11-beans2.xml");
        assertEquals(xml.getBeanDiscoveryMode(), BeanDiscoveryMode.NONE);
        assertEquals(xml.getVersion(), "1.1");
    }

    @Test
    public void testExplicitVersionAndMode2() {
        BeansXml xml = getBeansXml("cdi11-beans3.xml");
        assertEquals(xml.getBeanDiscoveryMode(), BeanDiscoveryMode.ANNOTATED);
        assertEquals(xml.getVersion(), "1.1");
    }

    @Test
    public void testExplicitVersionAndMode3() {
        BeansXml xml = getBeansXml("cdi11-beans3b.xml");
        assertEquals(xml.getBeanDiscoveryMode(), BeanDiscoveryMode.ANNOTATED);
        assertEquals(xml.getVersion(), "1.1");
    }

    @Test
    public void testExplicitVersionAndMode4() {
        BeansXml xml = getBeansXml("cdi11-beans4.xml");
        assertEquals(xml.getBeanDiscoveryMode(), BeanDiscoveryMode.ALL);
        assertEquals(xml.getVersion(), "1.1");
    }

    @Test
    public void testEmptyFile() {
        BeansXml xml = getBeansXml("cdi11-beans5.xml");
        assertEquals(xml.getBeanDiscoveryMode(), BeanDiscoveryMode.ANNOTATED);
        assertNull(xml.getVersion());
    }

    @Test
    public void testExclusionFilters1() {
        BeansXml xml = getBeansXml("cdi11-exclude-beans1.xml");
        Collection<Metadata<Filter>> filters = xml.getScanning().getExcludes();
        assertEquals(filters.size(), 3);
        for (Metadata<Filter> filter : filters) {
            new FilterPredicate(filter, DefaultResourceLoader.INSTANCE);
        }
    }

    @Test(expectedExceptions = Exception.class)
    public void testExclusionFilters2() {
        BeansXml xml = getBeansXml("cdi11-exclude-beans2.xml");
        Collection<Metadata<Filter>> filters = xml.getScanning().getExcludes();
        assertEquals(filters.size(), 1);
        for (Metadata<Filter> filter : filters) {
            new FilterPredicate(filter, DefaultResourceLoader.INSTANCE);
        }
    }
}
