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

import org.junit.Ignore;
import org.jboss.weld.osgi.tests.bundle1.api.PropertyService;
import org.jboss.weld.osgi.tests.bundle1.api.TestPublished;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(JUnit4TestRunner.class)
public class MetaFilterTest {

    @Configuration
    public static Option[] configure() {
        return options(
                Environment.CDIOSGiEnvironment(
                        mavenBundle("org.jboss.weld.osgi.tests","weld-osgi-bundle1").version("1.1.5-SNAPSHOT"),
                        mavenBundle("org.jboss.weld.osgi.tests","weld-osgi-bundle2").version("1.1.5-SNAPSHOT"),
                        mavenBundle("org.jboss.weld.osgi.tests","weld-osgi-bundle3").version("1.1.5-SNAPSHOT")
                )
        );
    }

    @Test
    //@Ignore
    public void metaFilterTest(BundleContext context) throws InterruptedException, InvalidSyntaxException, BundleException {
        Environment.waitForEnvironment(context);
        ServiceReference ref = context.getServiceReference(TestPublished.class.getName());
        TestPublished test = (TestPublished) context.getService(ref);
        if (test != null) {
            PropertyService serv1 = test.getService();
            PropertyService serv2 = test.getService2();
            Assert.assertNotNull(serv1.whoAmI());
            Assert.assertNotNull(serv2.whoAmI());
            Assert.assertTrue(serv1.whoAmI().equals(serv2.whoAmI()));
        } else {
            Assert.fail("No test bean available");
        }
    }
}
