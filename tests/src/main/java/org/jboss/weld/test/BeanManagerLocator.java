/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.test;

import org.jboss.testharness.impl.runner.servlet.ServletTestRunner;
import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.servlet.ServletHelper;

public class BeanManagerLocator
{
   
   public static BeanManagerLocator INSTANCE = new BeanManagerLocator();
   
   private BeanManagerLocator()
   {
      // TODO Auto-generated constructor stub
   }
   
   private BeanDeploymentArchive testArchive;
   
   public BeanManagerImpl locate()
   {
      if (ServletTestRunner.getCurrentServletContext() != null)
      {
         return ServletHelper.getModuleBeanManager(ServletTestRunner.getCurrentServletContext()).getCurrent();
      }
      else if (getTestArchive() != null)
      {
         return Container.instance().beanDeploymentArchives().get(getTestArchive()).getCurrent();
      }
      else
      {
         throw new IllegalStateException();
      }
   }
   
   private BeanDeploymentArchive getTestArchive()
   {
      if (testArchive == null)
      {
         try
         {
            testArchive = (BeanDeploymentArchive) Class.forName("org.jboss.weld.mock.MockBeanDeploymentArchive").newInstance();
         }
         catch (InstantiationException e)
         {
            throw new IllegalStateException(e);
         }
         catch (IllegalAccessException e)
         {
            throw new IllegalStateException(e);
         }
         catch (ClassNotFoundException e)
         {
            throw new IllegalStateException(e);
         }
      }
      return testArchive;
   }

}
