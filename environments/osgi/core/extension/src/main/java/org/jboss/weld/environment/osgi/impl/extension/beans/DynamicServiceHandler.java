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

import org.jboss.weld.environment.osgi.impl.extension.CDIOSGiExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;
import org.jboss.weld.environment.osgi.api.annotation.Filter;

/**
 * Handler for OSGi dynamic service in use by
 * {@link org.osgi.cdi.impl.extension.OSGiServiceBean}.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class DynamicServiceHandler implements InvocationHandler
{
   private static Logger logger = LoggerFactory.getLogger(
           DynamicServiceHandler.class);

   private final Bundle bundle;

   private final String name;

   private Filter filter;
//    private final ServiceTracker tracker;

   private final long timeout;

   private Set<Annotation> qualifiers;

   boolean stored = false;

   public DynamicServiceHandler(Bundle bundle,
                                String name,
                                Filter filter,
                                Set<Annotation> qualifiers,
                                long timeout)
   {
      logger.debug("Creation of a new DynamicServiceHandler for bundle {} "
                  + "as a {} with filter {}",
                  new Object[]
                  {
                     bundle, name, filter.value()
                  });
      this.bundle = bundle;
      this.name = name;
      this.filter = filter;
      this.timeout = timeout;
      this.qualifiers = qualifiers;
//        try {
//            if (filter != null && filter.value() != null && filter.value().length() > 0) {
//                this.tracker = new ServiceTracker(bundle.getBundleContext(),
//                    bundle.getBundleContext().createFilter(
//                        "(&(objectClass=" + name + ")" + filter.value() + ")"),
//                    null);
//            } else {
//                this.tracker = new ServiceTracker(bundle.getBundleContext(), name, null);
//            }
//        } catch (Exception e) {
//            logger.error("Unable to create the DynamicServiceHandler.",e);
//            throw new RuntimeException(e);
//        }
//        this.tracker.open();
   }

   public void closeHandler()
   {
//        this.tracker.close();
   }

   public void setStored(boolean stored)
   {
      this.stored = stored;
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable
   {
      logger.trace("Call on the DynamicServiceHandler {} for method {}",
                   this,
                   method);
      CDIOSGiExtension.currentBundle.set(bundle.getBundleId());
      if (!stored && method.getName().equals("hashCode"))
      { //intercept hashCode method
         int result = name.hashCode();
         result = 31 * result + filter.value().hashCode();
         result = 31 * result + qualifiers.hashCode();
         return result;
      }
      ServiceReference reference = null;
      if (filter != null && filter.value() != null && filter.value().length() > 0)
      {
         ServiceReference[] refs =
                            bundle.getBundleContext()
                                    .getServiceReferences(name, filter.value());
         if (refs != null && refs.length > 0)
         {
            reference = refs[0];
         }
      }
      else
      {
         reference = bundle.getBundleContext().getServiceReference(name);
      }
      if (reference == null)
      {
         throw new IllegalStateException("Can't call service "
                                         + name
                                         + ". No matching service found.");
      }
      Object instanceToUse = bundle.getBundleContext().getService(reference);
      try
      {
         return method.invoke(instanceToUse, args);
      }
      catch(Throwable t)
      {
         throw new RuntimeException(t);
      }
      finally
      {
         bundle.getBundleContext().ungetService(reference);
         CDIOSGiExtension.currentBundle.remove();
      }
//        Object instanceToUse = this.tracker.waitForService(timeout);
//        try {
//            return method.invoke(instanceToUse, args);
//        } catch(Throwable t) {
//            logger.error("Unable to find a matching service for {} with filter {} due to {}", new Object[] {name, filter.value(), t});
//            throw new RuntimeException(t);
//        } finally {
//            CDIOSGiExtension.currentBundle.remove();
//        }
   }

}
