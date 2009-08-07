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
package org.jboss.webbeans.mock;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

/**
 * @author pmuir
 *
 */
public class MockBeanDeploymentArchive implements BeanDeploymentArchive
{
   

   private Set<Class<?>> beanClasses = new HashSet<Class<?>>();

   private Collection<URL> webBeansXmlFiles = new HashSet<URL>();

   public Collection<Class<?>> getBeanClasses()
   {
      return beanClasses;
   }

   public Collection<URL> getBeansXml()
   {
      return webBeansXmlFiles;
   }
   
   public void setWebBeansXmlFiles(Iterable<URL> webBeansXmlFiles)
   {
      this.webBeansXmlFiles.clear();
      for (URL url : webBeansXmlFiles)
      {
         this.webBeansXmlFiles.add(url);
      }
   }

   public List<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return Collections.emptyList();
   }
   
   private List<EjbDescriptor<?>> ejbs;

   public void setBeanClasses(Iterable<Class<?>> beanClasses)
   {
      this.beanClasses.clear();
      for (Class<?> clazz : beanClasses)
      {
         this.beanClasses.add(clazz);
      }
      ejbs = new ArrayList<EjbDescriptor<?>>();
      for (Class<?> ejbClass : discoverEjbs(getBeanClasses()))
      {
         ejbs.add(MockEjbDescriptor.of(ejbClass));
      }
   }
   
   public Collection<EjbDescriptor<?>> getEjbs()
   {
      return ejbs;
   }
   
   protected static Iterable<Class<?>> discoverEjbs(Iterable<Class<?>> webBeanClasses)
   {
      Set<Class<?>> ejbs = new HashSet<Class<?>>();
      for (Class<?> clazz : webBeanClasses)
      {
         if (clazz.isAnnotationPresent(Stateless.class) || clazz.isAnnotationPresent(Stateful.class) || clazz.isAnnotationPresent(MessageDriven.class) || clazz.isAnnotationPresent(Singleton.class)) 
         {
            ejbs.add(clazz);
         }
      }
      return ejbs;
   }

}
