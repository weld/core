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

package org.jboss.webbeans;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Access point for getting/setting current Managager 
 * 
 * @author Gavin King
 */
public class CurrentManager 
{
   
   // The root manager instance
   private static Integer rootManagerId;
   
   private final static Map<Integer, ManagerImpl> managers = new ConcurrentHashMap<Integer, ManagerImpl>();

   /**
    * Gets the root manager
    * 
    * @return The root manager
    */
   public static ManagerImpl rootManager()
   {
      return managers.get(rootManagerId);
   }
   
   /**
    * Sets the root manager
    * 
    * @param managerImpl The root manager
    */
   public static void setRootManager(ManagerImpl managerImpl) 
   {
      if (managerImpl == null)
      {
         rootManagerId = null;
      }
      else
      {
         rootManagerId = add(managerImpl);
      }
   }
   
   public static ManagerImpl get(Integer key)
   {
      return managers.get(key);
   }
   
   public static Integer add(ManagerImpl manager)
   {
      Integer id = manager.getId();
      managers.put(id, manager);
      return id;
   }
   
}
