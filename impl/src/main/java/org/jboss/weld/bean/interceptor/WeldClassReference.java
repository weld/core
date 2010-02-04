/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.bean.interceptor;

import java.util.Iterator;

import org.jboss.interceptor.model.metadata.ClassReference;
import org.jboss.interceptor.model.metadata.ImmutableIteratorWrapper;
import org.jboss.interceptor.model.metadata.MethodReference;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;

/**
 * @author Marius Bogoevici
 */
public class WeldClassReference implements ClassReference
{
   private WeldClass<?> weldClass;

   private WeldClassReference(WeldClass<?> weldClass)
   {
      this.weldClass = weldClass;
   }

   public static ClassReference of(WeldClass<?> weldClass)
   {
      return new WeldClassReference(weldClass);
   }

   public String getClassName()
   {
      return weldClass.getName();
   }

   public Iterable<MethodReference> getDeclaredMethods()
   {
      return new Iterable<MethodReference>()
      {
         public Iterator<MethodReference> iterator()
         {
            return new ImmutableIteratorWrapper<WeldMethod<?,?>>(weldClass.getDeclaredWeldMethods().iterator())
            {
               @Override protected MethodReference wrapObject(WeldMethod<?,?> weldMethod)
               {
                  return WeldMethodReference.of(weldMethod);
               }
            };
         }
      };
   }

   public Class<?> getJavaClass()
   {
      return weldClass.getJavaClass();
   }

   public ClassReference getSuperclass()
   {
      WeldClass<?> weldSuperclass = weldClass.getWeldSuperclass();
      if (weldSuperclass != null)
      {
         return WeldClassReference.of(weldSuperclass);
      }
      else
      {
         return null;
      }
   }
}
