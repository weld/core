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
package org.jboss.webbeans.bean.ee;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.resources.spi.ResourceServices;

/**
 * Proxy for persistence unit Java EE resources
 * 
 * @author Pete Muir
 *
 */
public class ResourceMethodHandler extends AbstractResourceMethodHandler
{
   
   private static final long serialVersionUID = -8835529913294253208L;

   public ResourceMethodHandler(String jndiName, String mappedName)
   {
      super(jndiName, mappedName);
   }

   @Override
   protected Object getProxiedInstance(Class<?> declaringClass)
   {
      return CurrentManager.rootManager().getServices().get(ResourceServices.class).resolveResource(getJndiName(), getMappedName());
   }

}
