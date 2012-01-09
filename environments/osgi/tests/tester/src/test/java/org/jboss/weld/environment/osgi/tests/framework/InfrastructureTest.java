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

package org.jboss.weld.environment.osgi.tests.framework;

import junit.framework.Assert;
import org.jboss.weld.environment.osgi.api.BundleState;
import org.jboss.weld.environment.osgi.api.annotation.BundleDataFile;
import org.jboss.weld.environment.osgi.api.events.AbstractBundleContainerEvent;
import org.jboss.weld.environment.osgi.spi.CDIContainer;
import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(JUnit4TestRunner.class)
public class InfrastructureTest {

    @Configuration
    public static Option[] configure() {
        return options(
                Environment.CDIOSGiEnvironment(
                        mavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-importing").version("1.1.6-SNAPSHOT")
                )
        );
    }

    @Test
    //@Ignore
    public void fiveBundlesTest(BundleContext context) throws InterruptedException, BundleException {
        Environment.waitForEnvironment(context);

        Bundle extAPI = null, intAPI = null, extImpl = null, intImpl = null, mand = null;
        for (Bundle b : context.getBundles()) {
            if (b.getSymbolicName().equals("org.jboss.weld.osgi.weld-osgi-core-api")) {
                extAPI = b;
            } else if (b.getSymbolicName().equals("org.jboss.weld.osgi.weld-osgi-core-spi")) {
                intAPI = b;
            } else if (b.getSymbolicName().equals("org.jboss.weld.osgi.weld-osgi-core-extension")) {
                extImpl = b;
            } else if (b.getSymbolicName().equals("org.jboss.weld.osgi.weld-osgi-core-integration")) {
                intImpl = b;
            } else if (b.getSymbolicName().equals("org.jboss.weld.osgi.weld-osgi-core-mandatory")) {
                mand = b;
            }
        }
        Assert.assertNotNull("Extension API bundle is not present", extAPI);
        Assert.assertNotNull("Integration API bundle is not present", intAPI);
        Assert.assertNotNull("Extension Impl bundle is not present", extImpl);
        Assert.assertNotNull("Integration Impl bundle is not present", intImpl);
        Assert.assertNotNull("Mandatory bundle is not present", mand);
    }

    @Test
    //@Ignore
    public void interactionsTest(BundleContext context) throws InterruptedException {
        Environment.waitForEnvironment(context);

        Bundle importingBundle = null;
        for (Bundle b : context.getBundles()) {
            if (b.getSymbolicName().equals("org.jboss.weld.osgi.tests.weld-osgi-importing")) {
                importingBundle = b;
            }
        }
        Assert.assertNotNull("Importing bundle is not present", importingBundle);

        try {
            Assert.assertEquals("Unable to access the package org.jboss.weld.environment.osgi.api", "org.jboss.weld.environment.osgi.api", importingBundle.loadClass(BundleState.class.getName()).getPackage().getName());
        } catch (ClassNotFoundException e) {
            Assert.fail("Unable to access the package org.jboss.weld.environment.osgi.api " + e.getMessage());
        }
        try {
            Assert.assertEquals("Unable to access the package org.jboss.weld.environment.osgi.api.annotation", "org.jboss.weld.environment.osgi.api.annotation", importingBundle.loadClass(BundleDataFile.class.getName()).getPackage().getName());
        } catch (ClassNotFoundException e) {
            Assert.fail("Unable to access the package org.jboss.weld.environment.osgi.api.annotation " + e.getMessage());
        }
        try {
            Assert.assertEquals("Unable to access the package org.jboss.weld.environment.osgi.api.events", "org.jboss.weld.environment.osgi.api.events", importingBundle.loadClass(AbstractBundleContainerEvent.class.getName()).getPackage().getName());
        } catch (ClassNotFoundException e) {
            Assert.fail("Unable to access the package org.jboss.weld.environment.osgi.api.events " + e.getMessage());
        }

        try {
            Assert.assertEquals("Unable to access the package org.jboss.weld.environment.osgi.spi", "org.jboss.weld.environment.osgi.spi", importingBundle.loadClass(CDIContainer.class.getName()).getPackage().getName());
        } catch (ClassNotFoundException e) {
            Assert.fail("Unable to access the package org.jboss.weld.environment.osgi.spi " + e.getMessage());
        }
    }
}
