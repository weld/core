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
package org.jboss.webbeans.el;

import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.DependentInstancesStore;
import org.jboss.webbeans.context.DependentStorageRequest;

abstract class RunInDependentContext
{
   
   private final DependentStorageRequest dependentStorageRequest;
   
   public RunInDependentContext()
   {
      dependentStorageRequest = DependentStorageRequest.of(new DependentInstancesStore(), new Object());
   }
   
   private void setup()
   {
      DependentContext.instance().setActive(true);
      DependentContext.instance().startCollectingDependents(dependentStorageRequest);
   }
   
   private void cleanup()
   {
      DependentContext.instance().stopCollectingDependents(dependentStorageRequest);
      // TODO kinky
      dependentStorageRequest.getDependentInstancesStore().destroyDependentInstances(dependentStorageRequest.getKey());
      DependentContext.instance().setActive(false);
   }
   
   protected abstract void execute() throws Exception;
   
   public final void run() throws Exception
   {
      try
      {
         setup();
         execute();
      }
      finally
      {
         cleanup();
      }
   }
   
}