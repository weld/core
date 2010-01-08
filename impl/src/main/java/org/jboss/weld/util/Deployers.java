/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.util;

import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static org.jboss.weld.logging.messages.BeanMessage.UNABLE_TO_PROCESS;

/**
 * Helper class for {@link org.jboss.deployers.spi.deployer.Deployer} inspections.
 * 
 * @author Marius Bogoevici
 */
public class Deployers
{
   public static Set<MethodSignature> getDecoratedMethodSignatures(BeanManagerImpl beanManager, Set<Type> decoratedTypes)
   {
      Set<MethodSignature> methodSignatures = new HashSet<MethodSignature>();
      for (Type type: decoratedTypes)
      {
         WeldClass<?> weldClass = getWeldClassOfDecoratedType(beanManager, type);
         for (WeldMethod<?, ?> method : weldClass.getWeldMethods())
         {
            if (!methodSignatures.contains(method.getSignature()))
            {
               methodSignatures.add(method.getSignature());
            }
         }
      }
      return methodSignatures;
   }

   public static WeldClass<?> getWeldClassOfDecoratedType(BeanManagerImpl beanManager, Type type)
   {
      if (type instanceof Class<?>)
      {
         return (WeldClass<?>) beanManager.createAnnotatedType((Class<?>) type);
      }
      if (type instanceof ParameterizedType && (((ParameterizedType) type).getRawType() instanceof Class))
      {
         return (WeldClass<?>) beanManager.createAnnotatedType((Class<?>) ((ParameterizedType) type).getRawType());
      }
      throw new ForbiddenStateException(UNABLE_TO_PROCESS, type);
   }
}
