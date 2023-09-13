/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.discovery.isolation;

import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class IsolationDisabledTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        JavaArchive common = ShrinkWrap.create(JavaArchive.class).addClasses(IsolationDisabledTest.class, FooInterceptor.class,
                FooBinding.class);
        JavaArchive bda1 = ShrinkWrap.create(BeanArchive.class).addClass(Rorschach.class);
        JavaArchive bda2 = ShrinkWrap.create(BeanArchive.class).intercept(FooInterceptor.class).addClass(Comedian.class);
        return ClassPath.builder().add(common, bda1, bda2).build();
    }

    @Test
    public void testDiscovery() {
        try (WeldContainer container = new Weld().disableIsolation().initialize()) {
            FooInterceptor.INVOKED.set(false);
            container.select(Comedian.class).get().ping();
            assertTrue(FooInterceptor.INVOKED.get());
            FooInterceptor.INVOKED.set(false);
            container.select(Rorschach.class).get().ping();
            assertTrue(FooInterceptor.INVOKED.get());
        }
    }

}
