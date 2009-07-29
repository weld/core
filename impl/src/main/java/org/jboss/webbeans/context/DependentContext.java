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
package org.jboss.webbeans.context;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.context.api.ContexutalInstance;

/**
 * The dependent context
 * 
 * @author Nicklas Karlsson
 */
public class DependentContext extends AbstractContext implements Service
{

   /**
    * Constructor
    */
   public DependentContext()
   {
      super(Dependent.class);
   }

   /**
    * Overridden method always creating a new instance
    * 
    * @param contextual The bean to create
    * @param create Should a new one be created
    */
   public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      if (creationalContext != null)
      {
         T instance = contextual.create(creationalContext);
         if (creationalContext instanceof CreationalContextImpl<?>)
         {
            CreationalContextImpl<T> creationalContextImpl = (CreationalContextImpl<T>) creationalContext;
            ContexutalInstance<T> beanInstance = new BeanInstanceImpl<T>(contextual, instance, creationalContext);
            creationalContextImpl.getParentDependentInstancesStore().addDependentInstance(beanInstance);
         }
         return instance;
      }
      else
      {
         return null;
      }
   }

   public <T> T get(Contextual<T> contextual)
   {
      return get(contextual, null);
   }

   @Override
   public String toString()
   {
      return "dependent context";
   }
   
   @Override
   public boolean isActive()
   {
      return true;
   }

}
