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
package org.jboss.weld.environment.servlet.test.discovery;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanDiscoveryInjectionTest {

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive testArchive = Deployments.baseDeployment().addClass(BeanDiscoveryInjectionTest.class);
        JavaArchive archive01 = ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL), "beans.xml")
                .addClasses(Dog.class, Cat.class, Cow.class);
        JavaArchive archive02 = ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ANNOTATED), "beans.xml")
                .addClasses(Plant.class, Tree.class, Stone.class);
        JavaArchive archive03 = ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.NONE), "beans.xml")
                .addClasses(Flat.class, House.class);
        testArchive.addAsLibraries(archive01, archive02, archive03);
        return testArchive;
    }

    @Test
    public void testAllBeanDiscovery(BeanManager manager) {
        assertEquals(1, manager.getBeans(Dog.class).size());
        assertEquals(1, manager.getBeans(Cat.class).size());
        assertEquals(1, manager.getBeans(Cow.class).size());
    }

    @Test
    public void testAnnotatedBeanDiscovery(BeanManager manager) {
        assertEquals(1, manager.getBeans(Tree.class).size());
        assertEquals(1, manager.getBeans(Plant.class).size());
        assertEquals(0, manager.getBeans(Stone.class).size());
    }

    @Test
    public void testNoneBeanDiscovery(BeanManager manager) {
        assertEquals(0, manager.getBeans(Flat.class).size());
        assertEquals(0, manager.getBeans(House.class).size());
    }

}
