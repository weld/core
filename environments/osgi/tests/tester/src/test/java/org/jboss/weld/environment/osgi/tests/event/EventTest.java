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

package org.jboss.weld.environment.osgi.tests.event;

import javax.inject.Inject;

import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.jboss.weld.osgi.tests.bundle1.api.MovingService;
import org.jboss.weld.osgi.tests.bundle1.util.EventListener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import static org.jboss.weld.environment.osgi.tests.util.Environment.toMavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(JUnit4TestRunner.class)
public class EventTest {

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
    public void eventTest() throws InterruptedException, InvalidSyntaxException, BundleException {
        Environment.waitForEnvironment(context);

        Bundle bundle1 = null, bundle2 = null, bundle3 = null;
        for (Bundle b : context.getBundles()) {
            Assert.assertEquals("Bundle" + b.getSymbolicName() + " is not ACTIVE", Bundle.ACTIVE, b.getState());
            if (b.getSymbolicName().equals("org.jboss.weld.osgi.tests.weld-osgi-bundle1")) {
                bundle1 = b;
            } else if (b.getSymbolicName().equals("org.jboss.weld.osgi.tests.weld-osgi-bundle2")) {
                bundle2 = b;
            } else if (b.getSymbolicName().equals("org.jboss.weld.osgi.tests.weld-osgi-bundle3")) {
                bundle3 = b;
            }
        }
        Assert.assertNotNull("The bundle1 was not retrieved", bundle1);
        Assert.assertNotNull("The bundle2 was not retrieved", bundle2);
        Assert.assertNotNull("The bundle3 was not retrieved", bundle3);

        ServiceReference[] eventListenerReferences = context.getServiceReferences(EventListener.class.getName(), null);
        Assert.assertNotNull("The event listener reference array was null", eventListenerReferences);
        Assert.assertEquals("The number of event listener implementations was wrong", 1, eventListenerReferences.length);
        EventListener eventListener = (EventListener) context.getService(eventListenerReferences[0]);
        Assert.assertNotNull("The event listener was null", eventListener);

        Assert.assertEquals("The number of listened BundleContainerInitialized event was wrong", 1, eventListener.getStart());
        Assert.assertEquals("The number of listened BundleContainerShutdown event was wrong", 0, eventListener.getStop());

        int serviceArrival = eventListener.getServiceArrival();
        int serviceChanged = eventListener.getServiceChanged();
        int serviceDeparture = eventListener.getServiceDeparture();
        Assert.assertTrue("The number of listened ServiceArrival event was wrong", serviceArrival > 0);
        Assert.assertEquals("The number of listened ServiceChanged event was wrong", 0, serviceChanged);
        Assert.assertEquals("The number of listened ServiceDeparture event was wrong", 0, serviceDeparture);

        ServiceRegistration registration = context.registerService(MovingService.class.getName(), eventListener.getMovingServiceInstance(), null);
        Assert.assertEquals("The second number of listened ServiceArrival event was wrong", serviceArrival + 1, eventListener.getServiceArrival());
        Assert.assertEquals("The second number of listened ServiceChanged event was wrong", serviceChanged, eventListener.getServiceChanged());
        Assert.assertEquals("The second number of listened ServiceDeparture event was wrong", serviceDeparture, eventListener.getServiceDeparture());

        registration.setProperties(null);
        Assert.assertEquals("The third number of listened ServiceArrival event was wrong", serviceArrival + 1, eventListener.getServiceArrival());
        Assert.assertEquals("The third number of listened ServiceChanged event was wrong", serviceChanged + 1, eventListener.getServiceChanged());
        Assert.assertEquals("The third number of listened ServiceDeparture event was wrong", serviceDeparture, eventListener.getServiceDeparture());

        registration.unregister();
        Assert.assertEquals("The forth number of listened ServiceArrival event was wrong", serviceArrival + 1, eventListener.getServiceArrival());
        Assert.assertEquals("The forth number of listened ServiceChanged event was wrong", serviceChanged + 1, eventListener.getServiceChanged());
        Assert.assertEquals("The forth number of listened ServiceDeparture event was wrong", serviceDeparture + 1, eventListener.getServiceDeparture());

        int bundleInstalled = eventListener.getBundleInstalled();
        int bundleUninstalled = eventListener.getBundleUninstalled();
        int bundleResolved = eventListener.getBundleResolved();
        int bundleUnresolved = eventListener.getBundleUnresolved();
        int bundleStarting = eventListener.getBundleStarting();
        int bundleStarted = eventListener.getBundleStarted();
        int bundleStopping = eventListener.getBundleStopping();
        int bundleStopped = eventListener.getBundleStopped();
        int bundleUpdated = eventListener.getBundleUpdated();
        int bundleLazyActivation = eventListener.getBundleLazyActivation();

        // can't listen its own BundleResolved or BundleStarting (left us with bundle2 and bundle3's)
        Assert.assertEquals("The number of listened BundleInstalled event was wrong", 0, bundleInstalled);
        Assert.assertEquals("The number of listened BundleUninstalled event was wrong", 0, bundleUninstalled);
        Assert.assertEquals("The number of listened BundleResolved event was wrong", 2, bundleResolved);
        Assert.assertEquals("The number of listened BundleUnresolved event was wrong", 0, bundleUnresolved);
        Assert.assertEquals("The number of listened BundleStarting event was wrong", 2, bundleStarting);
        Assert.assertEquals("The number of listened BundleStarted event was wrong", 3, bundleStarted);
        Assert.assertEquals("The number of listened BundleStopping event was wrong", 0, bundleStopping);
        Assert.assertEquals("The number of listened BundleStopped event was wrong", 0, bundleStopped);
        Assert.assertEquals("The number of listened BundleUpdated event was wrong", 0, bundleUpdated);
        Assert.assertEquals("The number of listened BundleLazyActivation event was wrong", 0, bundleLazyActivation);

        bundle3.stop();
        Environment.waitForState(bundle3, Bundle.RESOLVED);
        Assert.assertEquals("The second number of listened BundleInstalled event was wrong", bundleInstalled, eventListener.getBundleInstalled());
        Assert.assertEquals("The second number of listened BundleUninstalled event was wrong", bundleUninstalled, eventListener.getBundleUninstalled());
        Assert.assertEquals("The second number of listened BundleResolved event was wrong", bundleResolved, eventListener.getBundleResolved());
        Assert.assertEquals("The second number of listened BundleUnresolved event was wrong", bundleUnresolved, eventListener.getBundleUnresolved());
        Assert.assertEquals("The second number of listened BundleStarting event was wrong", bundleStarting, eventListener.getBundleStarting());
        Assert.assertEquals("The second number of listened BundleStarted event was wrong", bundleStarted, eventListener.getBundleStarted());
        Assert.assertEquals("The second number of listened BundleStopping event was wrong", bundleStopping + 1, eventListener.getBundleStopping());
        Assert.assertEquals("The second number of listened BundleStopped event was wrong", bundleStopped + 1, eventListener.getBundleStopped());
        Assert.assertEquals("The second number of listened BundleUpdated event was wrong", bundleUpdated, eventListener.getBundleUpdated());
        Assert.assertEquals("The second number of listened BundleLazyActivation event was wrong", bundleLazyActivation, eventListener.getBundleLazyActivation());

        bundle3.start();
        Environment.waitForState(bundle3, Bundle.ACTIVE);
        Assert.assertEquals("The third number of listened BundleInstalled event was wrong", bundleInstalled, eventListener.getBundleInstalled());
        Assert.assertEquals("The third number of listened BundleUninstalled event was wrong", bundleUninstalled, eventListener.getBundleUninstalled());
        Assert.assertEquals("The third number of listened BundleResolved event was wrong", bundleResolved, eventListener.getBundleResolved());
        Assert.assertEquals("The third number of listened BundleUnresolved event was wrong", bundleUnresolved, eventListener.getBundleUnresolved());
        Assert.assertEquals("The third number of listened BundleStarting event was wrong", bundleStarting + 1, eventListener.getBundleStarting());
        Assert.assertEquals("The third number of listened BundleStarted event was wrong", bundleStarted + 1, eventListener.getBundleStarted());
        Assert.assertEquals("The third number of listened BundleStopping event was wrong", bundleStopping + 1, eventListener.getBundleStopping());
        Assert.assertEquals("The third number of listened BundleStopped event was wrong", bundleStopped + 1, eventListener.getBundleStopped());
        Assert.assertEquals("The third number of listened BundleUpdated event was wrong", bundleUpdated, eventListener.getBundleUpdated());
        Assert.assertEquals("The third number of listened BundleLazyActivation event was wrong", bundleLazyActivation, eventListener.getBundleLazyActivation());

        bundle3.update();
        Environment.waitForState(bundle3, Bundle.ACTIVE);
        // unresolved -> stopping -> stopped -> starting -> resolved -> started
        Assert.assertEquals("The forth number of listened BundleInstalled event was wrong", bundleInstalled, eventListener.getBundleInstalled());
        Assert.assertEquals("The forth number of listened BundleUninstalled event was wrong", bundleUninstalled, eventListener.getBundleUninstalled());
        Assert.assertEquals("The forth number of listened BundleResolved event was wrong", bundleResolved + 1, eventListener.getBundleResolved());
        Assert.assertEquals("The forth number of listened BundleUnresolved event was wrong", bundleUnresolved + 1, eventListener.getBundleUnresolved());
        Assert.assertEquals("The forth number of listened BundleStarting event was wrong", bundleStarting + 2, eventListener.getBundleStarting());
        Assert.assertEquals("The forth number of listened BundleStarted event was wrong", bundleStarted + 2, eventListener.getBundleStarted());
        Assert.assertEquals("The forth number of listened BundleStopping event was wrong", bundleStopping + 2, eventListener.getBundleStopping());
        Assert.assertEquals("The forth number of listened BundleStopped event was wrong", bundleStopped + 2, eventListener.getBundleStopped());
        Assert.assertEquals("The forth number of listened BundleUpdated event was wrong", bundleUpdated + 1, eventListener.getBundleUpdated());
        Assert.assertEquals("The forth number of listened BundleLazyActivation event was wrong", bundleLazyActivation, eventListener.getBundleLazyActivation());

        String location = bundle3.getLocation();
        bundle3.uninstall();
        Environment.waitForState(bundle3, Bundle.UNINSTALLED);
        // unresolved -> stopping -> stopped -> uninstalled
        Assert.assertEquals("The fifth number of listened BundleInstalled event was wrong", bundleInstalled, eventListener.getBundleInstalled());
        Assert.assertEquals("The fifth number of listened BundleUninstalled event was wrong", bundleUninstalled + 1, eventListener.getBundleUninstalled());
        Assert.assertEquals("The fifth number of listened BundleResolved event was wrong", bundleResolved + 1, eventListener.getBundleResolved());
        Assert.assertEquals("The fifth number of listened BundleUnresolved event was wrong", bundleUnresolved + 2, eventListener.getBundleUnresolved());
        Assert.assertEquals("The fifth number of listened BundleStarting event was wrong", bundleStarting + 2, eventListener.getBundleStarting());
        Assert.assertEquals("The fifth number of listened BundleStarted event was wrong", bundleStarted + 2, eventListener.getBundleStarted());
        Assert.assertEquals("The fifth number of listened BundleStopping event was wrong", bundleStopping + 3, eventListener.getBundleStopping());
        Assert.assertEquals("The fifth number of listened BundleStopped event was wrong", bundleStopped + 3, eventListener.getBundleStopped());
        Assert.assertEquals("The fifth number of listened BundleUpdated event was wrong", bundleUpdated + 1, eventListener.getBundleUpdated());
        Assert.assertEquals("The fifth number of listened BundleLazyActivation event was wrong", bundleLazyActivation, eventListener.getBundleLazyActivation());

        context.installBundle(location);
        Environment.waitForState(context, bundle3.getSymbolicName(), Bundle.INSTALLED);
        Assert.assertEquals("The sixth number of listened BundleInstalled event was wrong", bundleInstalled + 1, eventListener.getBundleInstalled());
        Assert.assertEquals("The sixth number of listened BundleUninstalled event was wrong", bundleUninstalled + 1, eventListener.getBundleUninstalled());
        Assert.assertEquals("The sixth number of listened BundleResolved event was wrong", bundleResolved + 1, eventListener.getBundleResolved());
        Assert.assertEquals("The sixth number of listened BundleUnresolved event was wrong", bundleUnresolved + 2, eventListener.getBundleUnresolved());
        Assert.assertEquals("The sixth number of listened BundleStarting event was wrong", bundleStarting + 2, eventListener.getBundleStarting());
        Assert.assertEquals("The sixth number of listened BundleStarted event was wrong", bundleStarted + 2, eventListener.getBundleStarted());
        Assert.assertEquals("The sixth number of listened BundleStopping event was wrong", bundleStopping + 3, eventListener.getBundleStopping());
        Assert.assertEquals("The sixth number of listened BundleStopped event was wrong", bundleStopped + 3, eventListener.getBundleStopped());
        Assert.assertEquals("The sixth number of listened BundleUpdated event was wrong", bundleUpdated + 1, eventListener.getBundleUpdated());
        Assert.assertEquals("The sixth number of listened BundleLazyActivation event was wrong", bundleLazyActivation, eventListener.getBundleLazyActivation());

        int bundleValid = eventListener.getBundleValid();
        int bundleInvalid = eventListener.getBundleInvalid();
        Assert.assertEquals("The number of listened BundleValid event was wrong", 1, bundleValid);
        Assert.assertEquals("The number of listened BundleInvalid event was wrong", 1, bundleInvalid);

        bundle2.stop();
        Environment.waitForState(bundle2, Bundle.RESOLVED);
        Assert.assertEquals("The new number of listened BundleValid event was wrong", bundleValid, eventListener.getBundleValid());
        Assert.assertEquals("The new number of listened BundleInvalid event was wrong", bundleInvalid + 1, eventListener.getBundleInvalid());

        bundle2.start();
        Environment.waitForState(bundle2, Bundle.ACTIVE);
        Thread.sleep(2000);
        Assert.assertEquals("The new number of listened BundleValid event was wrong", bundleValid + 1, eventListener.getBundleValid());
        Assert.assertEquals("The new number of listened BundleInvalid event was wrong", bundleInvalid + 1, eventListener.getBundleInvalid());
    }
}
