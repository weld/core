package org.jboss.weld.environment.osgi.tests.util;

import org.jboss.weld.environment.osgi.spi.CDIContainer;
import org.jboss.weld.environment.osgi.spi.CDIContainerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class WeldOSGiWait {
    
    public static void waitForContainersToStart(BundleContext ctx, Bundle... bundles) {
        ServiceReference s = ctx.getServiceReference(CDIContainerFactory.class.getName());
        if (s != null) {
            CDIContainerFactory fact = (CDIContainerFactory) ctx.getService(s);
            if (fact != null) {
                for (Bundle bundle : bundles) {
                    CDIContainer container = fact.container(bundle);
                    if (container != null) {
                        container.waitToBeReady();
                    }
                }
                ctx.ungetService(s);
            }
        }
    }
}
