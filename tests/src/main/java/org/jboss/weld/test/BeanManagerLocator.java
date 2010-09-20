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

import java.util.Map.Entry;

import javax.naming.InitialContext;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;

public class BeanManagerLocator
{

   public static BeanManagerLocator INSTANCE = new BeanManagerLocator();

   private BeanManagerLocator()
   {
      // TODO Auto-generated constructor stub
   }

   public BeanManagerImpl locate()
   {
      try
      {
         return (BeanManagerImpl) new InitialContext().lookup("java:comp/BeanManager");
      }
      catch (Exception e)
      {
         // Try the next method
      }
      // Locate via the BDA id "test"
      for (Entry<BeanDeploymentArchive, BeanManagerImpl> entry : Container.instance().beanDeploymentArchives().entrySet())
      {
         if (entry.getKey().getId().equals("test"))
         {
            return entry.getValue();
         }
      }
      throw new IllegalStateException();
   }

}
