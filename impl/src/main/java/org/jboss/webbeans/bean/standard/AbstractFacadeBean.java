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
package org.jboss.webbeans.bean.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;

public abstract class AbstractFacadeBean<T> extends AbstractStandardBean<T>
{
   
   private static final Log log = Logging.getLog(AbstractFacadeBean.class);

   protected AbstractFacadeBean(BeanManagerImpl manager)
   {
      super(manager);
   }

   public T create(CreationalContext<T> creationalContext)
   {
      try
      {
         DependentContext.instance().setActive(true);
         InjectionPoint injectionPoint = this.getManager().getInjectionPoint();
         if (injectionPoint != null)
         {
            Type genericType = injectionPoint.getType();
            if (genericType instanceof ParameterizedType )
            {
               Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
               return newInstance(type, injectionPoint.getBindings());
            }
            else
            {
               throw new IllegalStateException("Must have concrete type argument " + injectionPoint);
            }
         }
         else
         {
            log.warn("Dynamic lookup of " + toString() + " is not supported");
            return null;
         }
      }
      finally
      {
         DependentContext.instance().setActive(false);
      }
   }
   
   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      // TODO Auto-generated method stub
   }
   
   @Override
   public boolean isSerializable()
   {
      return true;
   }
   
   protected abstract T newInstance(Type type, Set<Annotation> annotations);
   
}
