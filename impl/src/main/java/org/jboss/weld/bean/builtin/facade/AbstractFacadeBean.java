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
package org.jboss.weld.bean.builtin.facade;

import static org.jboss.weld.messages.BeanMessages.DYNAMIC_LOOKUP_OF_BUILT_IN_NOT_ALLOWED;
import static org.jboss.weld.util.log.Categories.BEAN;
import static org.jboss.weld.util.log.LoggerFactory.loggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.slf4j.cal10n.LocLogger;

public abstract class AbstractFacadeBean<T> extends AbstractBuiltInBean<T>
{
   
   private static final LocLogger log = loggerFactory().getLogger(BEAN);

   protected AbstractFacadeBean(String idSuffix, BeanManagerImpl manager)
   {
      super(idSuffix, manager);
   }

   public T create(CreationalContext<T> creationalContext)
   {
      InjectionPoint injectionPoint = this.getManager().getCurrentInjectionPoint();
      if (injectionPoint != null)
      {
         Type genericType = injectionPoint.getType();
         if (genericType instanceof ParameterizedType )
         {
            Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            return newInstance(type, injectionPoint.getQualifiers());
         }
         else
         {
            throw new IllegalStateException("Must have concrete type argument " + injectionPoint);
         }
      }
      else
      {
         log.warn(DYNAMIC_LOOKUP_OF_BUILT_IN_NOT_ALLOWED, toString());
         return null;
      }
   }
   
   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      // TODO Auto-generated method stub
   }
   
   protected abstract T newInstance(Type type, Set<Annotation> annotations);
   
}
