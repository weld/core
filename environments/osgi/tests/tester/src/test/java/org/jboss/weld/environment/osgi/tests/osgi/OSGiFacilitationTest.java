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

package org.jboss.weld.environment.osgi.tests.osgi;

import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.jboss.weld.osgi.tests.bundle1.util.BundleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.io.File;
import java.util.Dictionary;
import java.util.Map;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(JUnit4TestRunner.class)
public class OSGiFacilitationTest {

    @Configuration
    public static Option[] configure() {
        return options(
                Environment.CDIOSGiEnvironment(
                        mavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-bundle1").version("1.1.6-SNAPSHOT"),
                        mavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-bundle2").version("1.1.6-SNAPSHOT"),
                        mavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-bundle3").version("1.1.6-SNAPSHOT")
                )
        );
    }

    @Test
    //@Ignore
    public void osgiUtilitiesTest(BundleContext context) throws InterruptedException, InvalidSyntaxException {
        Environment.waitForEnvironment(context);

        Bundle bundle1 = null, bundle2 = null;
        for (Bundle b : context.getBundles()) {
            if (b.getSymbolicName().equals("org.jboss.weld.osgi.tests.weld-osgi-bundle1")) {
                bundle1 = b;
                break;
            }
        }

        ServiceReference[] bundleProviderServiceReferences = context.getServiceReferences(BundleProvider.class.getName(), null);
        Assert.assertNotNull("The bundle provider reference array was null", bundleProviderServiceReferences);
        Assert.assertEquals("The number of bundle provider implementations was wrong", 1, bundleProviderServiceReferences.length);
        BundleProvider bundleProvider = (BundleProvider) context.getService(bundleProviderServiceReferences[0]);
        Assert.assertNotNull("The bundle provider was null", bundleProvider);

        Bundle injectedBundle = bundleProvider.getBundle();
        Assert.assertNotNull("The injected bundle was null", injectedBundle);
        Assert.assertEquals("The injected bundle was not the bundle1", bundle1, injectedBundle);

        BundleContext injectedContext = bundleProvider.getBundleContext();
        Assert.assertNotNull("The injected bundle context was null", injectedBundle);
        Assert.assertEquals("The injected bundle context was not the bundle1 bundle context", bundle1, injectedContext.getBundle());

        Map<String, String> metadata = bundleProvider.getMetadata();
        Dictionary headers = bundle1.getHeaders();
        Assert.assertNotNull("The injected bundle metadata was null", metadata);
        Assert.assertEquals("The injected bundle metadata had the wrong size", headers.size(), metadata.size());
        for (String s : metadata.keySet()) {
            Assert.assertEquals("The injected metadata header was not the bundle1 header", headers.get(s), metadata.get(s));
        }

        String symbolicName = bundleProvider.getSymbolicName();
        Assert.assertNotNull("The injected bundle symbolic name was null", symbolicName);
        Assert.assertEquals("The injected symbolic name was not the bundle1 symbolic name", bundle1.getSymbolicName(), symbolicName);

        File file = bundleProvider.getFile();
        Assert.assertNotNull("The injected bundle file was null", file);
        Assert.assertEquals("The injected bundle file was not the bundle1 file", injectedContext.getDataFile("test.txt"), file);
    }
}
