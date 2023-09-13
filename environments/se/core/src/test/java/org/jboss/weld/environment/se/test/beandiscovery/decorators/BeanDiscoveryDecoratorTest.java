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
package org.jboss.weld.environment.se.test.beandiscovery.decorators;

import static org.junit.Assert.assertEquals;

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
import org.jboss.weld.environment.se.test.beandiscovery.Flat;
import org.jboss.weld.environment.se.test.beandiscovery.House;
import org.jboss.weld.environment.se.test.beandiscovery.Plant;
import org.jboss.weld.environment.se.test.beandiscovery.Stone;
import org.jboss.weld.environment.se.test.beandiscovery.Tree;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanDiscoveryDecoratorTest {

    @Deployment
    public static Archive<?> getDeployment() {
        WeldSEClassPath archives = ShrinkWrap.create(WeldSEClassPath.class);
        JavaArchive archive01 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(
                        new BeansXml(BeanDiscoveryMode.ALL).decorators(ClassicRepresentDecorator.class,
                                ScopedRepresentDecorator.class),
                        "beans.xml")
                .addClasses(Dog.class, Cat.class, ClassicRepresentDecorator.class, ScopedRepresentDecorator.class);
        JavaArchive archive02 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(
                        new BeansXml(BeanDiscoveryMode.ANNOTATED).decorators(ClassicRepresentDecorator.class,
                                ScopedRepresentDecorator.class),
                        "beans.xml")
                .addClasses(Plant.class, Tree.class, Stone.class);
        JavaArchive archive03 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(
                        new BeansXml(BeanDiscoveryMode.NONE).decorators(ClassicRepresentDecorator.class,
                                ScopedRepresentDecorator.class),
                        "beans.xml")
                .addClasses(Flat.class, House.class);
        archives.add(archive01);
        archives.add(archive02);
        archives.add(archive03);
        return archives;
    }

    /**
     * Test bean discovery modes with decorators in SE.
     */
    @Test
    public void testAllBeanDiscovery(Cat cat, Dog dog) {
        int classicDecoratorCalls = ClassicRepresentDecorator.called;
        int scopedDecoratorCalls = ScopedRepresentDecorator.called;
        cat.methodToBeDecorated();
        assertEquals(classicDecoratorCalls + 1, ClassicRepresentDecorator.called);
        assertEquals(scopedDecoratorCalls + 1, ScopedRepresentDecorator.called);
        dog.methodToBeDecorated();
        assertEquals(classicDecoratorCalls + 1, ClassicRepresentDecorator.called);
        assertEquals(scopedDecoratorCalls + 1, ScopedRepresentDecorator.called);
    }

    @Test
    public void testAnnotatedBeanDiscovery(Plant plant, Tree tree) {
        int classicDecoratorCalls = ClassicRepresentDecorator.called;
        int scopedDecoratorCalls = ScopedRepresentDecorator.called;
        plant.methodToBeDecorated();
        assertEquals(classicDecoratorCalls + 1, ClassicRepresentDecorator.called);
        assertEquals(scopedDecoratorCalls + 1, ScopedRepresentDecorator.called);
        tree.methodToBeDecorated();
        assertEquals(classicDecoratorCalls + 1, ClassicRepresentDecorator.called);
        assertEquals(scopedDecoratorCalls + 1, ScopedRepresentDecorator.called);

    }

}