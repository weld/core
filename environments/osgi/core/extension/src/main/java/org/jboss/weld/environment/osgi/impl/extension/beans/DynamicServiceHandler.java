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
package org.jboss.weld.environment.osgi.impl.extension.beans;

import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.impl.extension.service.WeldOSGiExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;
import org.jboss.weld.environment.osgi.impl.extension.OSGiServiceBean;
import org.osgi.framework.BundleContext;

/**
 * Handler for proxy used by {@link OSGiServiceBean}. Dynamicaly lookup for a
 * matching OSGi service at method call. Automaticaly release the services after
 * each use.
 * <p/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class DynamicServiceHandler implements InvocationHandler {

    private static Logger logger =
                          LoggerFactory.getLogger(DynamicServiceHandler.class);

    private final BundleContext ctx;

    private final String name;

    private Filter filter;

    /*private final ServiceTracker tracker;*/
    private final long timeout;

    private Set<Annotation> qualifiers;

    boolean stored = false;

    public DynamicServiceHandler(BundleContext ctx,
                                 String name,
                                 Filter filter,
                                 Set<Annotation> qualifiers,
                                 long timeout) {
        logger.trace("Entering DynamicServiceHandler : "
                     + "DynamicServiceHandler() with parameters {} | {} | {} | {}",
                     new Object[] {name, filter, qualifiers, timeout});
        this.ctx = ctx;
        this.name = name;
        this.filter = filter;
        this.timeout = timeout;
        this.qualifiers = qualifiers;
        /* ServiceTracker usage, currently fails
        try {
        if (filter != null && filter.value() != null && filter.value().length() > 0) {
        this.tracker = new ServiceTracker(bundle.getBundleContext(),
        bundle.getBundleContext().createFilter(
        "(&(objectClass=" + name + ")" + filter.value() + ")"),
        null);
        } else {
        this.tracker = new ServiceTracker(bundle.getBundleContext(), name, null);
        }
        } catch (Exception e) {
        logger.error("Unable to create the DynamicServiceHandler.",e);
        throw new RuntimeException(e);
        }
        this.tracker.open();
         */
        logger.debug("New DynamicServiceHandler constructed {}", this);
    }

    /*public void closeHandler() {
    this.tracker.close();
    }*/
    public void setStored(boolean stored) {
        this.stored = stored;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        logger.trace("Call on the DynamicServiceHandler {} for method {}",
                     this,
                     method);
        WeldOSGiExtension.currentBundle.set(ctx.getBundle().getBundleId());
        //intercept HashCode method when the handler is not allready registered
        //map.put() need a correct hashCode() method to use
        //see OSGiServiceBean
        if (!stored && method.getName().equals("hashCode")) {
            int result = name.hashCode();
            result = 31 * result + filter.value().hashCode();
            result = 31 * result + qualifiers.hashCode();
            result = 31 * result + (int) timeout;
            return result;
        }
        ServiceReference reference = null;
        if (filter != null
            && filter.value() != null
            && filter.value().length() > 0) {
            ServiceReference[] refs =
                               ctx.getServiceReferences(name, filter.value());
            if (refs != null && refs.length > 0) {
                reference = refs[0];
            }
        }
        else {
            reference = ctx.getServiceReference(name);
        }
        if (reference == null) {
            throw new IllegalStateException("Can't call service "
                                            + name
                                            + ". No matching service found.");
        }
        Object instanceToUse = ctx.getService(reference);
        try {
            return method.invoke(instanceToUse, args);
        }
        catch(Throwable t) {
            throw new RuntimeException(t);
        }
        finally {
            ctx.ungetService(reference);
            WeldOSGiExtension.currentBundle.remove();
        }
        /*Object instanceToUse = this.tracker.waitForService(timeout);
        try {
        return method.invoke(instanceToUse, args);
        } catch(Throwable t) {
        logger.error("Unable to find a matching service for {} with filter {} due to {}", new Object[] {name, filter.value(), t});
        throw new RuntimeException(t);
        } finally {
        WeldOSGiExtension.currentBundle.remove();
        }*/
    }

}
