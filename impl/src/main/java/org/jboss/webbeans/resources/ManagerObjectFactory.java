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
package org.jboss.webbeans.resources;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.jboss.webbeans.CurrentManager;

public class ManagerObjectFactory implements ObjectFactory
{
   
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
   {
      // Temp hack for JBoss Flat Deployment
      if (CurrentManager.getBeanDeploymentArchives().size() == 1)
      {
         return CurrentManager.getBeanDeploymentArchives().entrySet().iterator().next().getValue().getCurrent();
      }
      else
      {
         throw new UnsupportedOperationException("Unable to determine which manager to return");
      }
   }
   
}
