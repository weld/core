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
package org.jboss.weld.mock.cluster;

import java.util.HashMap;
import java.util.Map;

import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.mock.MockEELifecycle;

public class SwitchableMockEELifecycle extends MockEELifecycle
{
   
   private final Map<Integer, BeanStore> requestBeanStores;
   private final Map<Integer, BeanStore> sessionBeanStores;
   private final Map<Integer, BeanStore> applicationBeanStores;
   
   private int id = 1;
   
   public SwitchableMockEELifecycle()
   {
      this.requestBeanStores = new HashMap<Integer, BeanStore>();
      this.sessionBeanStores = new HashMap<Integer, BeanStore>();
      this.applicationBeanStores = new HashMap<Integer, BeanStore>();
   }
   
   @Override
   protected BeanStore getRequestBeanStore()
   {
      return requestBeanStores.get(id);
   }
   
   @Override
   protected BeanStore getSessionBeanStore()
   {
      return sessionBeanStores.get(id);
   }
   
   @Override
   protected BeanStore getApplicationBeanStore()
   {
      return applicationBeanStores.get(id);
   }
   
   public void use(int id)
   {
      this.id = id;
   }

}
