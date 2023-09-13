/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.application.event;

import jakarta.servlet.ServletContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Verifies that an observer is not notified of a non-visible {@link ServletContext}.
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class MultiWarTest {

    @Deployment
    public static EnterpriseArchive getDeployment() {
        JavaArchive lib = ShrinkWrap.create(BeanArchive.class).addClasses(AbstractObserver.class, EventRepository.class,
                MultiObserver4.class);
        WebArchive war1 = Testable.archiveToTest(ShrinkWrap.create(WebArchive.class, "test1.war")
                .addClasses(MultiObserver1.class, MultiWarTest.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml"));
        WebArchive war2 = ShrinkWrap.create(WebArchive.class, "test2.war").addClasses(MultiObserver2.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        JavaArchive ejb1 = ShrinkWrap.create(BeanArchive.class, "ejb1.jar").addClasses(MultiObserver3.class, TestEjb1.class);
        JavaArchive ejb2 = ShrinkWrap.create(BeanArchive.class, "ejb2.jar").addClasses(MultiObserver5.class, TestEjb2.class);
        return ShrinkWrap
                .create(EnterpriseArchive.class, Utils.getDeploymentNameAsHash(MultiWarTest.class, Utils.ARCHIVE_TYPE.EAR))
                .addAsModules(war1, war2, ejb1, ejb2)
                .addAsLibrary(lib);
    }

    @Test
    public void testServletContextObservers() {
        Assert.assertEquals(2, EventRepository.SERVLET_CONTEXTS.size());
        Assert.assertTrue(EventRepository.SERVLET_CONTEXTS.contains("test1"));
        Assert.assertTrue(EventRepository.SERVLET_CONTEXTS.contains("test2"));
    }

    @Test
    public void testObject() {
        Assert.assertEquals(EventRepository.OBJECTS.toString(), 5, EventRepository.OBJECTS.size());
        Assert.assertTrue(EventRepository.OBJECTS.contains("test1"));
        Assert.assertTrue(EventRepository.OBJECTS.contains("test2"));
        Assert.assertTrue(EventRepository.OBJECTS.contains("lib"));
        Assert.assertTrue(EventRepository.OBJECTS.contains("ejb1"));
        Assert.assertTrue(EventRepository.OBJECTS.contains("ejb2"));
    }
}
