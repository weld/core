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

import org.junit.Ignore;
import org.jboss.weld.environment.osgi.spi.CDIContainer;
import org.jboss.weld.environment.osgi.spi.CDIContainerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.osgi.framework.*;

import java.util.Collection;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(JUnit4TestRunner.class)
public class BundleScannerTest  {

    @Configuration
    public static Option[] configure() {
        return options(
                Environment.CDIOSGiEnvironment(
                        mavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-scanner").version("1.1.5-SNAPSHOT"),
                        mavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-innerscanner").version("1.1.5-SNAPSHOT")
                                              )
        );
    }

    @Test
    //@Ignore
    public void bundleScannerTest(BundleContext context) throws InterruptedException, BundleException, InvalidSyntaxException {
        Environment.waitForEnvironment(context);

        Bundle bundleScanner = null, bundleScannerInner = null;
        for(Bundle b : context.getBundles()) {
            Assert.assertEquals("Bundle" + b.getSymbolicName() + " is not ACTIVE", Bundle.ACTIVE, b.getState());
            if(b.getSymbolicName().equals("org.jboss.weld.osgi.tests.weld-osgi-scanner")) {
                bundleScanner=b;
            }
            else if(b.getSymbolicName().equals("org.jboss.weld.osgi.tests.weld-osgi-innerscanner")) {
                bundleScannerInner=b;
            }
        }
        Assert.assertNotNull("The bundleScanner was not retrieved",bundleScanner);
        Assert.assertNotNull("The bundleScannerInner was not retrieved",bundleScannerInner);
        ServiceReference factoryReference = context.getServiceReference(CDIContainerFactory.class.getName());
        CDIContainerFactory factory = (CDIContainerFactory) context.getService(factoryReference);
        Collection<CDIContainer> containers = factory.containers();

        Assert.assertEquals("The container collection had the wrong number of containers",2,containers.size());

        CDIContainer container1 = factory.container(bundleScanner);
        CDIContainer container2 = factory.container(bundleScannerInner);
        Assert.assertNotNull("The container for bundleScanner was null",container1);
        Assert.assertNotNull("The container for bundleScannerInner was null", container2);
        Assert.assertTrue("The container for bundleScanner was not started",container1.isStarted());
        Assert.assertTrue("The container for bundleScannerInner was not started",container2.isStarted());

        Collection<String> classScanner = container1.getBeanClasses();
        Collection<String> classScannerInner = container2.getBeanClasses();
        Assert.assertNotNull("The bean class collection for bundleScanner was null",classScanner);
        Assert.assertNotNull("The bean class collection for bundleScannerInner was null", classScannerInner);
        Assert.assertEquals("The bean class collection size for bundleScanner was wrong",2,classScanner.size());
        Assert.assertEquals("The bean class collection size for bundleScannerInner was wrong",1,classScannerInner.size());
        Assert.assertTrue("The class com.sample.ScannerClass was not registered for bundleScanner", classScanner.contains("org.jboss.weld.osgi.tests.ScannerClass"));
        Assert.assertTrue("The class com.sample.ScannerInnerClass was not registered for bundleScanner", classScanner.contains("org.jboss.weld.osgi.tests.ScannerInnerClass"));
        Assert.assertTrue("The class com.sample.ScannerInnerClass was not registered for bundleScanner",classScannerInner.contains("org.jboss.weld.osgi.tests.ScannerInnerClass"));
    }
}
