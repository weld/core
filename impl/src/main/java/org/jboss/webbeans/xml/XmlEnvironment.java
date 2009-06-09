/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.xml;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.ee.AbstractJavaEEResourceBean;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.resources.spi.ResourceLoader;

public class XmlEnvironment
{
   
   private final List<AnnotatedClass<?>> classes;
   private final List<AnnotatedAnnotation<?>> annotations;
   private final ServiceRegistry serviceRegistry;
   private final List<Class<? extends Annotation>> enabledDeploymentTypes;
   private final Iterable<URL> beansXmlUrls;
   private final EjbDescriptorCache ejbDescriptors;
   private final Set<AbstractJavaEEResourceBean<?>> resourceBeans;
   private final BeanManagerImpl manager;
   
   public XmlEnvironment(ServiceRegistry serviceRegistry, EjbDescriptorCache ejbDescriptors, BeanManagerImpl manager)
   {
      this(serviceRegistry, serviceRegistry.get(WebBeanDiscovery.class).discoverWebBeansXml(), ejbDescriptors, manager);
   }
   
   protected XmlEnvironment(ServiceRegistry serviceRegistry, Iterable<URL> beanXmlUrls, EjbDescriptorCache ejbDescriptors, BeanManagerImpl manager)
   {
      this.classes = new ArrayList<AnnotatedClass<?>>();
      this.annotations = new ArrayList<AnnotatedAnnotation<?>>();
      this.enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      this.resourceBeans = new HashSet<AbstractJavaEEResourceBean<?>>();
      this.serviceRegistry = serviceRegistry;
      this.beansXmlUrls = beanXmlUrls;
      this.ejbDescriptors = ejbDescriptors;
      this.manager = manager;
   }
   
   public List<AnnotatedClass<?>> getClasses()
   {
      return classes;
   }
   
   public List<AnnotatedAnnotation<?>> getAnnotations()
   {
      return annotations;
   }
   
   public Iterable<URL> getBeansXmlUrls()
   {
      return beansXmlUrls;
   }
   
   public <T> AnnotatedClass<? extends T> loadClass(String className, Class<T> expectedType)
   {
      return serviceRegistry.get(ClassTransformer.class).classForName((serviceRegistry.get(ResourceLoader.class).classForName(className).asSubclass(expectedType)));
   }
   
   public <T extends Annotation> Class<? extends T> loadAnnotation(String className, Class<T> expectedType)
   {
      return serviceRegistry.get(ResourceLoader.class).classForName(className).asSubclass(expectedType);
   }

   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return enabledDeploymentTypes;
   }
   
   public EjbDescriptorCache getEjbDescriptors() 
   {
      return ejbDescriptors;
   }
   
   public URL loadFileByUrn(String urn, String fileName)
   {
      char separator = '/';
      String packageName = urn.replaceFirst(XmlConstants.URN_PREFIX, "");
      String path = packageName.replace('.', separator);
      String filePath = path + separator + fileName;
      return serviceRegistry.get(ResourceLoader.class).getResource(filePath);
   }
   
   public ServiceRegistry getServices()
   {
      return serviceRegistry;
   }
   
   public Set<AbstractJavaEEResourceBean<?>> getResourceBeans()
   {
      return resourceBeans;
   }
   
   public BeanManagerImpl getManager()
   {
      return manager;
   }
   
}
