/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.se.test.extension.build.compatible;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies invocation of BCE and PE and the ability to override BCE via provided PE
 */
@RunWith(Arquillian.class)
public class BuildCompatibleExtensionSmokeTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ClassPath.builder().add(ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(BuildCompatibleExtensionSmokeTest.class))
                .addPackage(BuildCompatibleExtensionSmokeTest.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class, StandardBuildCompatibleExtension.class,
                        OverridenBuildCompatibleExtension.class)
                .addAsServiceProvider(Extension.class, OverridingPortableExtension.class, StandardPortableExtension.class))
                .build();
    }

    @Test
    public void testExtensionsCanCoexist() {
        try (WeldContainer container = new Weld().initialize()) {
            // assert the deployment is fine, DummyBean should be resolvable
            Assert.assertTrue(container.select(DummyBean.class).isResolvable());

            // assert that standard BCE was invoked correctly
            Assert.assertEquals(5, StandardBuildCompatibleExtension.TIMES_INVOKED);

            // assert that overriden BCE was not invoked
            Assert.assertEquals(0, OverridenBuildCompatibleExtension.TIMES_INVOKED);

            // assert that overriding portable extension was invoked
            Assert.assertEquals(3, OverridingPortableExtension.TIMES_INVOKED);

            // assert that standard portable extension was invoked
            Assert.assertTrue(StandardPortableExtension.INVOKED);
        }
    }
}
