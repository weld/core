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
package org.jboss.weld.environment.servlet.test.discovery.stereotype;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.junit.Test;

public class StereotypeDiscoveryTestBase {

    public static WebArchive baseTestArchive() {
        return Deployments.baseDeployment(new BeansXml(BeanDiscoveryMode.ANNOTATED))
                .addPackage(StereotypeDiscoveryTestBase.class.getPackage());
    }

    @Inject
    private Instance<Object> instance;

    @Test
    public void testBuiltInStereotype() {
        Instance<Bar> bar = instance.select(Bar.class);
        assertFalse(bar.isAmbiguous());
        assertFalse(bar.isUnsatisfied());
        assertNotNull(bar.get());
    }

    @Test
    public void testCustomStereotype() {
        Instance<Foo> foo = instance.select(Foo.class);
        assertFalse(foo.isAmbiguous());
        assertFalse(foo.isUnsatisfied());
        assertNotNull(foo.get());
    }

    @Test
    public void testControlSample() {
        assertTrue(instance.select(Baz.class).isUnsatisfied());
    }
}
