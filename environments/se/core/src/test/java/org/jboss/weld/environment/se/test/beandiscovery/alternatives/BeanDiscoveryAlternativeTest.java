/**
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
package org.jboss.weld.environment.se.test.beandiscovery.alternatives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.se.test.arquillian.WeldSEClassPath;
import org.jboss.weld.environment.se.test.beandiscovery.Cat;
import org.jboss.weld.environment.se.test.beandiscovery.Dog;
import org.jboss.weld.environment.se.test.beandiscovery.DogInterface;
import org.jboss.weld.environment.se.test.beandiscovery.Flat;
import org.jboss.weld.environment.se.test.beandiscovery.Plant;
import org.jboss.weld.environment.se.test.beandiscovery.Stone;
import org.jboss.weld.environment.se.test.beandiscovery.Tree;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanDiscoveryAlternativeTest {

    @Deployment
    public static Archive<?> getDeployment() {
        WeldSEClassPath archives = ShrinkWrap.create(WeldSEClassPath.class);
        JavaArchive archive01 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL).alternatives(AlternativeDog.class), "beans.xml")
                .addClasses(Dog.class, AlternativeDog.class, DogInterface.class, Cat.class);
        JavaArchive archive02 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ANNOTATED).alternatives(AlternativeTree.class),
                        "beans.xml")
                .addClasses(Tree.class, AlternativeTree.class, Plant.class, Stone.class, AlternativeStone.class);
        JavaArchive archive03 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.NONE).alternatives(AlternativeFlat.class), "beans.xml")
                .addClasses(Flat.class, AlternativeFlat.class);

        archives.add(archive01);
        archives.add(archive02);
        archives.add(archive03);
        return archives;
    }

    /**
     * Test alternatives for all the bean discovery modes in SE. Need to inject the representatives to get the bean manager of
     * the bean archive.
     */
    @Test
    public void testAllBeanDiscoveryAlternative(Cat representative) {
        BeanManager bm = representative.getBeanManager();
        Set<Bean<?>> beans = bm.getBeans(DogInterface.class);
        assertEquals(2, beans.size());
        assertEquals(AlternativeDog.class, bm.resolve(beans).getBeanClass());
    }

    @Test
    public void testAnnotatedBeanDiscoveryAlternative(Plant representative) {
        BeanManager bm = representative.getBeanManager();
        Set<Bean<?>> treeBeans = bm.getBeans(Tree.class);
        assertEquals(2, treeBeans.size());
        assertEquals(AlternativeTree.class, bm.resolve(treeBeans).getBeanClass());

        Set<Bean<?>> stoneBeans = bm.getBeans(Stone.class);
        assertEquals(0, stoneBeans.size());
    }

    @Test
    public void testNoneBeanDiscoveryAlternative(Flat flat) {
        assertNull(flat);
    }

}