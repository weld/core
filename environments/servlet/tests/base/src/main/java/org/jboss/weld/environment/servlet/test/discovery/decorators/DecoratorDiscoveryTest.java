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
package org.jboss.weld.environment.servlet.test.discovery.decorators;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.servlet.test.discovery.Cat;
import org.jboss.weld.environment.servlet.test.discovery.Dog;
import org.jboss.weld.environment.servlet.test.discovery.Flat;
import org.jboss.weld.environment.servlet.test.discovery.House;
import org.jboss.weld.environment.servlet.test.discovery.Plant;
import org.jboss.weld.environment.servlet.test.discovery.Tree;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DecoratorDiscoveryTest {

    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive testArchive = Deployments.baseDeployment().addClass(DecoratorDiscoveryTest.class);

        JavaArchive archive01 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL).decorators(ClassicDecorator.class),
                        "beans.xml")
                .addClasses(Dog.class, Cat.class);
        JavaArchive archive02 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ANNOTATED).decorators(ClassicDecorator.class),
                        "beans.xml")
                .addClasses(Plant.class, Tree.class, ClassicDecorator.class);
        JavaArchive archive03 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.NONE).decorators(ClassicDecorator.class),
                        "beans.xml")
                .addClasses(Flat.class, House.class);
        testArchive.addAsLibraries(archive01, archive02, archive03);
        return testArchive;
    }

    @Test
    public void testAllBeanDiscovery(Cat cat, Dog dog) {
        ClassicDecorator.reset();

        cat.methodToBeDecorated();
        assertEquals(1, ClassicDecorator.called);
        dog.methodToBeDecorated();
        assertEquals(1, ClassicDecorator.called);
    }

    @Test
    public void testAnnotatedBeanDiscovery(Plant plant, Tree tree) {
        ClassicDecorator.reset();

        plant.methodToBeDecorated();
        assertEquals(1, ClassicDecorator.called);
        tree.methodToBeDecorated();
        assertEquals(1, ClassicDecorator.called);
    }

}