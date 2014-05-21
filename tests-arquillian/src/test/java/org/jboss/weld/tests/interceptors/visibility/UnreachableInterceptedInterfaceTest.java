/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.visibility;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.tests.interceptors.visibility.MyPanel.YetAnotherPanel;
import org.jboss.weld.tests.interceptors.visibility.unreachable.AbstractPanel;
import org.jboss.weld.tests.interceptors.visibility.unreachable.AbstractPanel2.AnotherPanel;
import org.jboss.weld.tests.interceptors.visibility.unreachable.Foo;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class UnreachableInterceptedInterfaceTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).intercept(PanelInterceptor.class)
                .addPackage(UnreachableInterceptedInterfaceTest.class.getPackage())
                .addPackage(AbstractPanel.class.getPackage());
    }

    @Test
    public void testInterceptorInvoked(MyPanel panel) {
        assertNotNull(panel);
        PanelInterceptor.called = false;
        panel.drawPanel();
        assertTrue(PanelInterceptor.called);
    }

    @Test
    public void testClientProxyCreation(AnotherPanel anotherPanel, YetAnotherPanel yetAnotherPanel, LastPanel lastPanel) {
        assertNotNull(anotherPanel);
        assertNotNull(yetAnotherPanel);
        assertNotNull(lastPanel);
        anotherPanel.foo();
        anotherPanel.bar();
        yetAnotherPanel.foo();
        lastPanel.anotherDraw();
    }

    @Test
    public void testClientProxyTypecast(Foo foo, LastPanel lastPanel) {
        assertNotNull(foo);
        assertNotNull(lastPanel);
        foo.testTypecast();
        ((AnotherPackagePrivateInterface)lastPanel).anotherDraw();
    }

}