/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
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
package org.jboss.webbeans;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.TypeLiteral;

import org.jboss.webbeans.bootstrap.BeanDeployment;
import org.jboss.webbeans.bootstrap.api.Singleton;
import org.jboss.webbeans.bootstrap.api.SingletonProvider;
import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;


/**
 * Access point for getting/setting current Manager 
 * 
 * @author Gavin King
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Pete Muir
 */
public class CurrentManager 
{

   private static class IntegerMangerImplMap extends TypeLiteral<Map<Integer, BeanManagerImpl>> {}
   
   private static class BeanDeploymentArchiveManagerImplMap extends TypeLiteral<Map<BeanDeploymentArchive, BeanManagerImpl>> {}
   
   // The root manager instance
   private static Singleton<BeanManagerImpl> rootManager = SingletonProvider.instance().create(BeanManagerImpl.class);
   // A map of managers keyed by ID, used for activities
   private final static Singleton<Map<Integer, BeanManagerImpl>> keyedManagers = SingletonProvider.instance().create(new IntegerMangerImplMap().getRawType());
   
   private final static Singleton<Map<BeanDeploymentArchive, BeanManagerImpl>> beanDeploymentArchives = SingletonProvider.instance().create(new BeanDeploymentArchiveManagerImplMap().getRawType());

   public static void clear()
   {
      keyedManagers.get().clear();
      rootManager.clear();
      keyedManagers.clear();
      beanDeploymentArchives.clear();
   }
   
   public static boolean isAvailable()
   {
      return rootManager.isSet() && beanDeploymentArchives.isSet();
   }
   
   /**
    * Gets the root manager
    * 
    * @return The root manager
    */
   public static BeanManagerImpl rootManager()
   {
      return rootManager.get();
   }
   
   /**
    * Sets the root manager
    * 
    * @param managerImpl The root manager
    */
   public static void setRootManager(BeanManagerImpl managerImpl) 
   {
      rootManager.set(managerImpl);
      if (!keyedManagers.isSet()) 
      {
          keyedManagers.set(new ConcurrentHashMap<Integer, BeanManagerImpl>());
      }
      keyedManagers.get().put(managerImpl.getId(), managerImpl);
   }
   
   public static void setBeanDeploymentArchives(Map<BeanDeploymentArchive, BeanDeployment> beanDeployments)
   {
      beanDeploymentArchives.set(new ConcurrentHashMap<BeanDeploymentArchive, BeanManagerImpl>());
      for (Entry<BeanDeploymentArchive, BeanDeployment> entry : beanDeployments.entrySet())   
      {
         beanDeploymentArchives.get().put(entry.getKey(), entry.getValue().getBeanManager());
         add(entry.getValue().getBeanManager());
      }
   }
   
   public static Map<BeanDeploymentArchive, BeanManagerImpl> getBeanDeploymentArchives()
   {
      return beanDeploymentArchives.get();
   }
   
   public static BeanManagerImpl get(Integer key)
   {
      return keyedManagers.get().get(key);
   }
   
   public static Integer add(BeanManagerImpl manager)
   {
      Integer id = manager.getId();
      keyedManagers.get().put(id, manager);
      return id;
   }
   
}
