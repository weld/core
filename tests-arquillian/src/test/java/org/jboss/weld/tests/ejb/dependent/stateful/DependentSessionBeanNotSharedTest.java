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
package org.jboss.weld.tests.ejb.dependent.stateful;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * See http://lists.jboss.org/pipermail/cdi-dev/2014-December/005800.html for details
 *
 * @author Steve Millidge
 *
 */
public abstract class DependentSessionBeanNotSharedTest {

    @Inject
    private Injection injection;

    public static Archive<?> getDeployment(boolean optimizationEnabled) {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(DependentSessionBeanNotSharedTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(DependentSessionBeanNotSharedTest.class, StatefulService.class, Injection.class)
                .addAsResource(
                        PropertiesBuilder.newBuilder()
                                .set(ConfigurationKey.INJECTABLE_REFERENCE_OPTIMIZATION.get(), "" + optimizationEnabled)
                                .build(),
                        "weld.properties");
    }

    @Test
    public void testDependentStatefulSessionBeanNotSharedBetweenInjectionPoints() {
        Assert.assertEquals("init", injection.invokeStatefulService());
    }
}
