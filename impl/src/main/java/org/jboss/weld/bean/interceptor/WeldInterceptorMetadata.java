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

import org.jboss.interceptor.model.InterceptorMetadata;
import org.jboss.interceptor.model.metadata.AbstractInterceptorMetadata;
import org.jboss.interceptor.model.metadata.AbstractInterceptorMetadataSerializationProxy;
import org.jboss.interceptor.model.metadata.ClassReference;
import org.jboss.interceptor.model.metadata.MethodReference;
import org.jboss.weld.Container;
import org.jboss.weld.exceptions.WeldException;

/**
 * @author Marius Bogoevici
 */
public class WeldInterceptorMetadata extends AbstractInterceptorMetadata
{
   public WeldInterceptorMetadata(ClassReference interceptorClass, boolean targetClass)
   {
      super(interceptorClass, targetClass);
   }

   private Object writeReplace()
   {
      return createSerializableProxy();
   }

   @Override protected Object createSerializableProxy()
   {
      return new WeldInterceptorMetadataSerializationProxy(getInterceptorClass().getClassName(), isTargetClass());
   }

   public static class WeldInterceptorMetadataSerializationProxy extends AbstractInterceptorMetadataSerializationProxy
   {
      protected WeldInterceptorMetadataSerializationProxy(String className, boolean interceptionTargetClass)
      {
         super(className, interceptionTargetClass);
      }

      @Override protected InterceptorMetadata loadInterceptorMetadata() throws ClassNotFoundException
      {
         //Class<?> clazz = Container.instance().services().get(ResourceLoader.class).classForName(getClassName());

         return Container.instance().services().get(InterceptionMetadataService.class)
               .getInterceptorMetadataRegistry().getInterceptorClassMetadata(new ClassReferenceStub(), isInterceptionTargetClass());
      }

      private Object readResolve()
      {
         try
         {
            return loadInterceptorMetadata();
         }
         catch (ClassNotFoundException e)
         {
            throw new WeldException(e);
         }
      }

      /**
       * ClassReference stub - we are assuming that the interceptor metadata is loaded already
       */
      private class ClassReferenceStub implements ClassReference
      {
         public String getClassName()
         {
            return WeldInterceptorMetadataSerializationProxy.this.getClassName();
         }

         public Iterable<MethodReference> getDeclaredMethods()
         {
            throw new UnsupportedOperationException("");
         }

         public Class<?> getJavaClass()
         {
            throw new UnsupportedOperationException("");
         }

         public ClassReference getSuperclass()
         {
            throw new UnsupportedOperationException("");
         }
      }
   }
}
