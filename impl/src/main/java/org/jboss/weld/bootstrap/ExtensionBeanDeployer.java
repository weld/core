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
package org.jboss.weld.bootstrap;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.introspector.WBClass;
import org.jboss.weld.resources.ClassTransformer;

/**
 * @author pmuir
 *
 */
public class ExtensionBeanDeployer extends AbstractBeanDeployer<ExtensionBeanDeployerEnvironment>
{
   
   
   private final Set<Extension> extensions;
   
   public ExtensionBeanDeployer(BeanManagerImpl manager, ExtensionBeanDeployerEnvironment environment)
   {
      super(manager, environment);
      this.extensions = new HashSet<Extension>();
   }
   
   public ExtensionBeanDeployer createBeans()
   {
      ClassTransformer classTransformer = Container.instance().deploymentServices().get(ClassTransformer.class);
      for (Extension extension : extensions)
      {
         @SuppressWarnings("unchecked")
         WBClass<Extension> clazz = (WBClass<Extension>) classTransformer.loadClass(extension.getClass());
         
         ExtensionBean bean = new ExtensionBean(getManager(), clazz, extension);
         this.
         getEnvironment().addBean(bean);
         createObserverMethods(bean, clazz);
      }
      return this;
   }
   
   public void addExtensions(Iterable<Extension> extensions)
   {
      for (Extension extension : extensions)
      {
         addExtension(extension);
      }
   }
   
   public void addExtension(Extension extension)
   {
      this.extensions.add(extension);
   }

}
