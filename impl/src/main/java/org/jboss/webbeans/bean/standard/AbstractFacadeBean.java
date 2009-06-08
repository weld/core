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
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;

public abstract class AbstractFacadeBean<T> extends AbstractStandardBean<T>
{
   
   private static final Log log = Logging.getLog(AbstractFacadeBean.class);

   protected AbstractFacadeBean(ManagerImpl manager)
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
               return newInstance(type, fixBindings(injectionPoint.getBindings()));
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
   
   /**
    * Merges and validates the current and new bindings
    * 
    * Checks with an abstract method for annotations to exclude
    * 
    * @param currentBindings Existing bindings
    * @param newBindings New bindings
    * @return The union of the bindings
    */
   protected Set<Annotation> fixBindings(Set<? extends Annotation> bindings)
   {
      Set<Annotation> result = new HashSet<Annotation>();
      for (Annotation newAnnotation : bindings)
      {
         if (!getFilteredAnnotationTypes().contains(newAnnotation.annotationType()))
         {
            result.add(newAnnotation);
         }
      }
      return result;
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

   /**
    * Gets a set of annotation classes to ignore
    * 
    * @return A set of annotation classes to ignore
    */
   protected abstract Set<Class<? extends Annotation>> getFilteredAnnotationTypes();
   
   protected abstract T newInstance(Type type, Set<Annotation> annotations);
   
}
