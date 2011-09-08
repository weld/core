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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.*;
import org.jboss.weld.environment.osgi.api.BundleState;
import org.jboss.weld.environment.osgi.api.Registration;
import org.jboss.weld.environment.osgi.api.RegistrationHolder;
import org.jboss.weld.environment.osgi.api.annotation.BundleDataFile;
import org.jboss.weld.environment.osgi.api.annotation.BundleHeader;
import org.jboss.weld.environment.osgi.api.annotation.BundleHeaders;
import org.jboss.weld.environment.osgi.api.annotation.BundleName;
import org.jboss.weld.environment.osgi.api.annotation.BundleVersion;

/**
 * Producers for CDI-OSGi specific injected types.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class CDIOSGiProducer
{
   private static Logger logger = LoggerFactory.getLogger(CDIOSGiProducer.class);

   @Produces
   public BundleState getBundleState(BundleHolder holder)
   {
      logger.debug("Bundle state of bundle {} producer", holder.getBundle());
      return holder.getState();
   }

   @Produces
   public Bundle getBundle(BundleHolder holder, InjectionPoint p)
   {
      logger.debug("Bundle {} reference producer", holder.getBundle());
      return holder.getBundle();
   }

   @Produces
   @BundleName("")
   @BundleVersion("")
   public Bundle getSpecificBundle(BundleHolder holder, InjectionPoint p)
   {
      logger.debug("External bundle reference from bundle {} producer", holder.getBundle());
      Set<Annotation> qualifiers = p.getQualifiers();
      BundleName bundleName = null;
      BundleVersion bundleVersion = null;
      for (Annotation qualifier : qualifiers)
      {
         if (qualifier.annotationType().equals(BundleName.class))
         {
            bundleName = (BundleName) qualifier;
         }
         else if (qualifier.annotationType().equals(BundleVersion.class))
         {
            bundleVersion = (BundleVersion) qualifier;
         }
      }
      if (bundleName == null || bundleName.value().equals(""))
      {
         logger.warn("No bundle name provided, assuming current bundle");
         return holder.getBundle();
      }
      else
      {
         if (bundleVersion == null || bundleVersion.value().equals(""))
         {
            return (Bundle) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                   new Class[] {Bundle.class},
                                                   new BundleHandler(
                                                           bundleName.value(),
                                                           "",
                                                           holder.getContext()));
         }
         return (Bundle) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                new Class[] {Bundle.class},
                                                new BundleHandler(
                                                        bundleName.value(),
                                                        bundleVersion.value(),
                                                        holder.getContext()));
      }
   }

   @Produces
   public BundleContext getBundleContext(BundleHolder holder, InjectionPoint p)
   {
      logger.debug("Bundle {} bundle context producer", holder.getBundle());
      return holder.getContext();
   }

   @Produces
   @BundleName("")
   @BundleVersion("")
   public BundleContext getSpecificContext(BundleHolder holder, InjectionPoint p)
   {
      logger.debug("External bundle context from bundle {} producer",
                   holder.getBundle());
      return (BundleContext) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                    new Class[] {BundleContext.class},
                                                    new BundleContextHandler(
                                                            getSpecificBundle(holder, p)));
   }

   @Produces
   @BundleName("")
   @BundleVersion("")
   @BundleDataFile("")
   public File getDataFile(BundleHolder holder, InjectionPoint p)
   {
      logger.debug("Data file from bundle {} producer", holder.getBundle());
      Set<Annotation> qualifiers = p.getQualifiers();
      BundleDataFile file = null;
      for (Annotation qualifier : qualifiers)
      {
         if (qualifier.annotationType().equals(BundleDataFile.class))
         {
            file = (BundleDataFile) qualifier;
            break;
         }
      }
      if (file.value().equals(""))
      {
         logger.warn("The data file path was empty");
         return null;
      }
      BundleContext context = getSpecificContext(holder, p);
      if (context == null)
      {
         return null;
      }
      return context.getDataFile(file.value());
   }

   @Produces
   public <T> Registration<T> getRegistrations(BundleHolder bundleHolder,
                                               RegistrationHolder holder,
                                               InjectionPoint p)
   {
      logger.debug("Registrations from bundle {} producer",
                   bundleHolder.getBundle());
      Class<T> contract = ((Class<T>) ((ParameterizedType) p.getType())
                                             .getActualTypeArguments()[0]);
      return new RegistrationImpl<T>(contract,
                                     bundleHolder.getContext(),
                                     bundleHolder.getBundle(),
                                     holder);
   }

   @Produces
   @BundleName("")
   @BundleVersion("")
   @BundleHeaders
   public Map<String, String> getBundleHeaders(BundleHolder holder,
                                               InjectionPoint p)
   {
      logger.debug("Bundle headers from bundle {} producer", holder.getBundle());
      Dictionary dict = getSpecificBundle(holder, p).getHeaders();
      if (dict == null)
      {
         return null;
      }
      Map<String, String> headers = new HashMap<String, String>();
      Enumeration<String> keys = dict.keys();
      while (keys.hasMoreElements())
      {
         String key = keys.nextElement();
         headers.put(key, (String) dict.get(key));
      }
      return headers;
   }

   @Produces
   @BundleName("")
   @BundleVersion("")
   @BundleHeader("")
   public String getSpecificBundleHeader(BundleHolder holder, InjectionPoint p)
   {
      logger.debug("Bundle header from bundle {} producer", holder.getBundle());
      Set<Annotation> qualifiers = p.getQualifiers();
      BundleHeader header = null;
      for (Annotation qualifier : qualifiers)
      {
         if (qualifier.annotationType().equals(BundleHeader.class))
         {
            header = (BundleHeader) qualifier;
            break;
         }
      }
      if (header == null || header.value().equals(""))
      {
         IllegalStateException e = new IllegalStateException("The BundleHeader "
                 + "qualifier was missing or its value was null");
         logger.error("You must specify a header name. {}", e);
         throw e;
      }
      Dictionary dict = getSpecificBundle(holder, p).getHeaders();
      if (dict == null)
      {
         return null;
      }
      return (String) dict.get(header.value());
   }

   private static class BundleHandler implements InvocationHandler
   {
      private final String symbolicName;

      private final Version version;

      private final BundleContext context;

      public BundleHandler(String symbolicName, String version, BundleContext context)
      {
         logger.debug("Bundle reference {}:{} produced",
                      symbolicName,
                      version.equals("") ? "no_version_provided" : version);
         this.symbolicName = symbolicName;
         this.context = context;
         if (!version.equals(""))
         {
            this.version = new Version(version);
         }
         else
         {
            this.version = null;
         }
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args)
              throws Throwable
      {
         logger.trace("Call method {} with args {} on bundle {}:{}",
                      new Object[]
                     {
                        method, args, symbolicName,
                        version == null ? "no_version_provided" : version
                     });
         Bundle bundle = null;
         Bundle[] bundles = context.getBundles();
         if (bundles != null)
         {
            for (Bundle b : bundles)
            {
               if (b.getSymbolicName().equals(symbolicName))
               {
                  if (version != null)
                  {
                     if (version.equals(b.getVersion()))
                     {
                        bundle = b;
                        logger.warn("Bundle {}:{} found", symbolicName, version);
                        break;
                     }
                  }
                  else
                  {
                     bundle = b;
                     logger.warn("Bundle {}:{} found",
                                 symbolicName,
                                 "no_version_provided");
                     break;
                  }
               }
            }
         }
         if (bundle == null)
         {
            logger.warn("Bundle {}:{} is unavailable",
                        symbolicName,
                        version == null ? "no_version_provided" : version);
            return null;
         }
         return method.invoke(bundle, args);
      }

   }

   private static class BundleContextHandler implements InvocationHandler
   {
      Bundle bundle;

      private BundleContextHandler(Bundle bundle)
      {
         this.bundle = bundle;
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args)
              throws Throwable
      {
         int state = 0;
         try
         {
            state = bundle.getState();
         }
         catch(Exception e)
         {
            return null;
         }
         if (state != Bundle.ACTIVE)
         {
            return null;
         }
         return method.invoke(bundle.getBundleContext(), args);
      }

   }
}
