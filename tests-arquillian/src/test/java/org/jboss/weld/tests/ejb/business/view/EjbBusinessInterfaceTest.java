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
package org.jboss.weld.tests.ejb.business.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class EjbBusinessInterfaceTest {

    @Inject
    private InjectedBean bean;

    @Inject
    private Event<MyEvent> fooEvent;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EjbBusinessInterfaceTest.class))
                .beanDiscoveryMode(BeanDiscoveryMode.ALL)
                .addPackage(EjbBusinessInterfaceTest.class.getPackage());
    }

    @Test
    public void testMethodDefinedOnSuperclass() {
        assertEquals("ping", bean.getFoo().ping("ping"));
    }

    @Test
    public void testMethodDefinedOnSuperInterface1() {
        assertEquals("ping", bean.getBarLocal().ping("ping"));
    }

    @Test
    public void testMethodDefinedOnSuperInterface2() {
        assertEquals("ping", bean.getBarSuperInterface().ping("ping"));
    }

    // See https://bugzilla.redhat.com/show_bug.cgi?id=1034776
    @Test
    public void testInterfaceInheritedMethodRecognizedAsLocalBusinessMethod() {
        fooEvent.fire(new MyEvent());
        assertTrue(BaseClass.isFooObserved());
    }
}
