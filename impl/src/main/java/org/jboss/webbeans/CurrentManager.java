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
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.TypeLiteral;

import org.jboss.webbeans.bootstrap.api.Singleton;
import org.jboss.webbeans.bootstrap.api.SingletonProvider;


/**
 * Access point for getting/setting current Manager 
 * 
 * @author Gavin King
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Pete Muir
 */
public class CurrentManager 
{

   private static class IntegerMangerImplMap extends TypeLiteral<Map<Integer, ManagerImpl>> {}
   
   // The root manager instance
   private static Singleton<ManagerImpl> rootManager = SingletonProvider.instance().create(ManagerImpl.class);
   
   private final static Singleton<Map<Integer, ManagerImpl>> managers = SingletonProvider.instance().create(new IntegerMangerImplMap().getRawType());

   public static void cleanup()
   {
      rootManager.set(null);
      managers.get().clear();
   }
   
   /**
    * Gets the root manager
    * 
    * @return The root manager
    */
   public static ManagerImpl rootManager()
   {
      return rootManager.get();
   }
   
   /**
    * Sets the root manager
    * 
    * @param managerImpl The root manager
    */
   public static void setRootManager(ManagerImpl managerImpl) 
   {
      rootManager.set(managerImpl);
      if (managers.get() == null) 
      {
          managers.set(new ConcurrentHashMap<Integer, ManagerImpl>());
      }
      managers.get().put(managerImpl.getId(), managerImpl);
   }
   
   public static ManagerImpl get(Integer key)
   {
      return managers.get().get(key);
   }
   
   public static Integer add(ManagerImpl manager)
   {
      Integer id = manager.getId();
      managers.get().put(id, manager);
      return id;
   }
   
}
