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
package org.jboss.weld.environment.osgi.impl.extension;

import org.jboss.weld.environment.osgi.impl.extension.service.CDIOSGiExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.weld.environment.osgi.api.annotation.BundleName;
import org.jboss.weld.environment.osgi.api.annotation.BundleVersion;
import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.annotation.Sent;
import org.jboss.weld.environment.osgi.api.annotation.Specification;
import org.jboss.weld.environment.osgi.api.events.AbstractBundleEvent;
import org.jboss.weld.environment.osgi.api.events.AbstractServiceEvent;
import org.jboss.weld.environment.osgi.api.events.BundleEvents;
import org.jboss.weld.environment.osgi.api.events.ServiceEvents;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;

/**
 * This is the activator of the CDI-OSGi extension part. It starts with the extension bundle.
 * <p/>
 * It seems we cannot get the BundleContext in the Extension, so to fire up OSGi Events (BundleEvent, ServiceEvent and
 * FrameworkEvent) we need to act here.
 * Thus it listens to all {@link BundleEvent}s and {@link ServiceEvent}s in order to relay them using {@link Event}s.
 *
 * @author Guillaume Sauthier
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class ExtensionActivator implements BundleActivator,
                                           SynchronousBundleListener,
                                           ServiceListener
{
   private static Logger logger = LoggerFactory.getLogger(ExtensionActivator.class);

   private BundleContext context;

   @Override
   public void start(BundleContext context) throws Exception
   {
      logger.debug("Extension part starts");
      this.context = context;
      context.addBundleListener(this);
      context.addServiceListener(this);
   }

   @Override
   public void stop(BundleContext context) throws Exception
   {
      logger.debug("Extension part stops");
   }

   @Override
   public void bundleChanged(BundleEvent event)
   {
      ServiceReference[] references = findReferences(context, Event.class);

      if (references != null)
      { //if there are some listening bean bundles
         Bundle bundle = event.getBundle();
         AbstractBundleEvent bundleEvent = null;
         switch(event.getType())
         {
            case BundleEvent.INSTALLED:
               logger.debug("Receiving a new OSGi bundle event INSTALLED");
               bundleEvent = new BundleEvents.BundleInstalled(bundle);
               break;
            case BundleEvent.LAZY_ACTIVATION:
               logger.debug("Receiving a new OSGi bundle event LAZY_ACTIVATION");
               bundleEvent = new BundleEvents.BundleLazyActivation(bundle);
               break;
            case BundleEvent.RESOLVED:
               logger.debug("Receiving a new OSGi bundle event RESOLVED");
               bundleEvent = new BundleEvents.BundleResolved(bundle);
               break;
            case BundleEvent.STARTED:
               logger.debug("Receiving a new OSGi bundle event STARTED");
               bundleEvent = new BundleEvents.BundleStarted(bundle);
               break;
            case BundleEvent.STARTING:
               logger.debug("Receiving a new OSGi bundle event STARTING");
               bundleEvent = new BundleEvents.BundleStarting(bundle);
               break;
            case BundleEvent.STOPPED:
               logger.debug("Receiving a new OSGi bundle event STOPPED");
               bundleEvent = new BundleEvents.BundleStopped(bundle);
               break;
            case BundleEvent.STOPPING:
               logger.debug("Receiving a new OSGi bundle event STOPPING");
               bundleEvent = new BundleEvents.BundleStopping(bundle);
               break;
            case BundleEvent.UNINSTALLED:
               logger.debug("Receiving a new OSGi bundle event UNINSTALLED");
               bundleEvent = new BundleEvents.BundleUninstalled(bundle);
               break;
            case BundleEvent.UNRESOLVED:
               logger.debug("Receiving a new OSGi bundle event UNRESOLVED");
               bundleEvent = new BundleEvents.BundleUnresolved(bundle);
               break;
            case BundleEvent.UPDATED:
               logger.debug("Receiving a new OSGi bundle event UPDATED");
               bundleEvent = new BundleEvents.BundleUpdated(bundle);
               break;
         }
         for (ServiceReference reference : references)
         { //broadcast event
            boolean set = CDIOSGiExtension.currentBundle.get() != null;
            CDIOSGiExtension.currentBundle.set(reference.getBundle().getBundleId());
            Event<Object> e = (Event<Object>) context.getService(reference);
            try
            {
               //broadcast the OSGi event through CDI event system
               e.select(BundleEvent.class).fire(event);
            }
            catch(Throwable t)
            {
               //t.printStackTrace();
            }
            if (bundleEvent != null)
            {
               //broadcast the corresponding CDI-OSGi event
               fireAllEvent(bundleEvent, e);
            }
            if (!set)
            {
               CDIOSGiExtension.currentBundle.remove();
            }
         }
      }
   }

   @Override
   public void serviceChanged(ServiceEvent event)
   {
      ServiceReference[] references = findReferences(context, Instance.class);

      if (references != null)
      { //if there are some listening bean bundles
         ServiceReference ref = event.getServiceReference();
         AbstractServiceEvent serviceEvent = null;
         switch(event.getType())
         {
            case ServiceEvent.MODIFIED:
               logger.debug("Receiving a new OSGi service event MODIFIED");
               serviceEvent = new ServiceEvents.ServiceChanged(ref, context);
               break;
            case ServiceEvent.REGISTERED:
               logger.debug("Receiving a new OSGi service event REGISTERED");
               serviceEvent = new ServiceEvents.ServiceArrival(ref, context);
               break;
            case ServiceEvent.UNREGISTERING:
               logger.debug("Receiving a new OSGi service event UNREGISTERING");
               serviceEvent = new ServiceEvents.ServiceDeparture(ref, context);
               break;
         }
         for (ServiceReference reference : references)
         { //broadcast event
            boolean set = CDIOSGiExtension.currentBundle.get() != null;
            CDIOSGiExtension.currentBundle.set(reference.getBundle().getBundleId());
            Instance<Object> instance = (Instance<Object>) context.getService(reference);
            try
            {
               Event<Object> e = instance.select(Event.class).get();
               //broadcast the OSGi event through CDI event system
               e.select(ServiceEvent.class).fire(event);
               if (serviceEvent != null)
               {
                  //broadcast the corresponding CDI-OSGi event
                  fireAllEvent(serviceEvent, e, instance);
               }
            }
            catch(Throwable t)
            {
               //t.printStackTrace();
            }
            if (!set)
            {
               CDIOSGiExtension.currentBundle.remove();
            }
         }
      }
   }

   private ServiceReference[] findReferences(BundleContext context, Class<?> type)
   {
      ServiceReference[] references = null;
      try
      {
         references = context.getServiceReferences(type.getName(), null);
      }
      catch(InvalidSyntaxException e)
      {// Ignored
      }
      return references;
   }

   private void fireAllEvent(AbstractServiceEvent event, Event broadcaster,
                                                         Instance<Object> instance)
   {
      List<Class<?>> classes = event.getServiceClasses(getClass());
      Class eventClass = event.getClass();
      for (Class<?> clazz : classes)
      {
         try
         {
            // here singleton issue
            broadcaster.select(eventClass,
               filteredServicesQualifiers(event,
                  new SpecificationAnnotation(clazz),
                  instance)).fire(event);
         }
         catch(Throwable t)
         {
            //t.printStackTrace();
         }
      }
   }

   private Annotation[] filteredServicesQualifiers(AbstractServiceEvent event,
                                                   SpecificationAnnotation specific,
                                                   Instance<Object> instance)
   {
      Set<Annotation> eventQualifiers = new HashSet<Annotation>();
      eventQualifiers.add(specific);
      CDIOSGiExtension extension = instance.select(CDIOSGiExtension.class).get();
      for (Annotation annotation : extension.getObservers())
      {
         String value = ((Filter) annotation).value();
         try
         {
            org.osgi.framework.Filter filter = context.createFilter(value);
            if (filter.match(event.getReference()))
            {
               eventQualifiers.add(new FilterAnnotation(value));
            }
         }
         catch(InvalidSyntaxException ex)
         {
            //ex.printStackTrace();
         }
      }
      return eventQualifiers.toArray(new Annotation[eventQualifiers.size()]);
   }

   private void fireAllEvent(AbstractBundleEvent event, Event broadcaster)
   {
      try
      {
         broadcaster.select(event.getClass(),
                            new BundleNameAnnotation(event.getSymbolicName()),
                            new BundleVersionAnnotation(event.getVersion()
                                                      .toString())).fire(event);
      }
      catch(Throwable t)
      {
         //t.printStackTrace();
      }
   }

   public static class BundleNameAnnotation
   extends AnnotationLiteral<BundleName> implements BundleName
   {
      private final String value;

      public BundleNameAnnotation(String value)
      {
         this.value = value;
      }

      @Override
      public String value()
      {
         return value;
      }

      @Override
      public Class<? extends Annotation> annotationType()
      {
         return BundleName.class;
      }

   }

   public static class BundleVersionAnnotation
   extends AnnotationLiteral<BundleVersion> implements BundleVersion
   {
      private final String value;

      public BundleVersionAnnotation(String value)
      {
         this.value = value;
      }

      @Override
      public String value()
      {
         return value;
      }

      @Override
      public Class<? extends Annotation> annotationType()
      {
         return BundleVersion.class;
      }

   }

   public static class SpecificationAnnotation
   extends AnnotationLiteral<Specification> implements Specification
   {
      private final Class value;

      public SpecificationAnnotation(Class value)
      {
         this.value = value;
      }

      @Override
      public Class value()
      {
         return value;
      }

      @Override
      public Class<? extends Annotation> annotationType()
      {
         return Specification.class;
      }

   }

   public static class SentAnnotation
   extends AnnotationLiteral<Sent> implements Sent
   {
      @Override
      public Class<? extends Annotation> annotationType()
      {
         return Sent.class;
      }

   }

   public static class FilterAnnotation
   extends AnnotationLiteral<Filter> implements Filter
   {
      private final String value;

      public FilterAnnotation(String value)
      {
         this.value = value;
      }

      @Override
      public Class<? extends Annotation> annotationType()
      {
         return Filter.class;
      }

      @Override
      public String value()
      {
         return value;
      }

   }
}
