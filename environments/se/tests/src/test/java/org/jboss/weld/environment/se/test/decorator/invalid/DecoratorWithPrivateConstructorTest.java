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
package org.jboss.weld.environment.se.test.decorator.invalid;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.exceptions.DeploymentException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * See WELD-2273 for more information
 * Relaxed mode should have no effect on this particular problem, therefore we test same assumption with and without it
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class DecoratorWithPrivateConstructorTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class)
                        .addPackage(DecoratorWithPrivateConstructorTest.class.getPackage()))
                .build();
    }

    @Test(expected = DeploymentException.class)
    public void testExceptionIsThrownWithoutRelaxedMode() {
        Weld weld = new Weld();
        try (WeldContainer container = weld.disableDiscovery()
                .addPackages(DecoratorWithPrivateConstructorTest.class.getPackage())
                // NOTE: in SE this is by default true
                .property("org.jboss.weld.construction.relaxed", false)
                .initialize()) {
            container.select(ImplementingBean.class).get().ping();
        }
    }

    @Test(expected = DeploymentException.class)
    public void testExceptionIsThrownWithRelaxedMode() {
        Weld weld = new Weld();
        try (WeldContainer container = weld.disableDiscovery()
                .addPackages(DecoratorWithPrivateConstructorTest.class.getPackage())
                .property("org.jboss.weld.construction.relaxed", true)
                .initialize()) {
            container.select(ImplementingBean.class).get().ping();
        }
    }
}
