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
package org.jboss.webbeans.bootstrap.spi.helpers;

import java.net.URL;
import java.util.Collection;

import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

/**
 * @author pmuir
 *
 */
public abstract class ForwardingBeanDeploymentArchive implements BeanDeploymentArchive
{

   protected abstract BeanDeploymentArchive delegate();
   
   public Collection<Class<?>> getBeanClasses()
   {
      return delegate().getBeanClasses();
   }

   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return delegate().getBeanDeploymentArchives();
   }

   public Collection<URL> getBeansXml()
   {
      return delegate().getBeansXml();
   }

   public Collection<EjbDescriptor<?>> getEjbs()
   {
      return delegate().getEjbs();
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return this == obj || delegate().equals(obj);
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }

}
