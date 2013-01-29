/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.environment.osgi.tests.service;

import javax.inject.Inject;

import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.jboss.weld.osgi.tests.bundle1.api.AbstractService;
import org.jboss.weld.osgi.tests.bundle1.util.AbstractServiceProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import static org.jboss.weld.environment.osgi.tests.util.Environment.toMavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(JUnit4TestRunner.class)
public class ConcreteServiceTest {

    @Configuration
    public static Option[] configure() {
        return options(
                Environment.toCDIOSGiEnvironment(
                        toMavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-bundle1"),
                        toMavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-bundle2"),
                        toMavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-bundle3")
                )
        );
    }

    @Inject
    private BundleContext context;

    @SuppressWarnings("unchecked")
    @Test
    public void testConcreteClassTest() throws Exception {
        Environment.waitForEnvironment(context);

        ServiceReference[] asRefs = context.getServiceReferences(AbstractService.class.getName(), null);
        Assert.assertNotNull("The property service reference array was null", asRefs);
        Assert.assertEquals("The number of service property service implementations was wrong", 1, asRefs.length);

        AbstractService as = (AbstractService) context.getService(asRefs[0]);
        Assert.assertEquals("Foo", as.foo());

        ServiceReference[] aspRefs = context.getServiceReferences(AbstractServiceProvider.class.getName(), null);
        Assert.assertNotNull("The property service reference array was null", aspRefs);
        Assert.assertEquals("The number of service property service implementations was wrong", 1, aspRefs.length);

        AbstractServiceProvider asp = (AbstractServiceProvider) context.getService(aspRefs[0]);
        Assert.assertEquals(as.foo(), asp.getService().foo());
    }
}
