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

package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.model.AnnotationModel;
import org.jboss.webbeans.model.BindingTypeModel;
import org.jboss.webbeans.model.ScopeModel;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.util.Strings;

import com.google.common.collect.ForwardingMap;

public class MetaDataCache
{

   private abstract class AnnotationModelMap<T extends AnnotationModel<?>> extends ForwardingMap<Class<? extends Annotation>, T>
   {

      Map<Class<? extends Annotation>, T> delegate;

      public AnnotationModelMap()
      {
         delegate = new ConcurrentHashMap<Class<? extends Annotation>, T>();
      }

      public <S extends Annotation> T putIfAbsent(Class<S> key)
      {
         if (!containsKey(key))
         {
            T model = createAnnotationModel(key);
            super.put(key, model);
            return model;
         }
         return (T) super.get(key);
      }

      protected abstract <S extends Annotation> T createAnnotationModel(Class<S> type);

      @Override
      protected Map<Class<? extends Annotation>, T> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("AnnotationModelMap (annotation -> ?): ", delegate);
      }

   }

   @SuppressWarnings("unchecked")
   private class ScopeModelMap extends AnnotationModelMap<ScopeModel<?>>
   {

      @Override
      public <S extends Annotation> ScopeModel<S> putIfAbsent(Class<S> key)
      {
         return (ScopeModel<S>) super.putIfAbsent(key);
      }

      @Override
      protected <S extends Annotation> ScopeModel<?> createAnnotationModel(Class<S> type)
      {
         return new ScopeModel<S>(type);
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("ScopeModelMap (annotation -> ScopeModel): ", delegate);
      }

   }

   @SuppressWarnings("unchecked")
   private class BindingTypeModelMap extends AnnotationModelMap<BindingTypeModel<?>>
   {

      @Override
      public <S extends Annotation> BindingTypeModel<S> putIfAbsent(Class<S> key)
      {
         return (BindingTypeModel<S>) super.putIfAbsent(key);
      }

      @Override
      protected <S extends Annotation> BindingTypeModel<?> createAnnotationModel(Class<S> type)
      {
         return new BindingTypeModel<S>(type);
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("BindingTypeModelMap (annotation -> BindingTypeModel): ", delegate);
      }
   }

   private class EjbMetaDataMap extends ForwardingMap<Class<?>, EjbMetaData<?>>
   {

      private Map<Class<?>, EjbMetaData<?>> delegate;

      public EjbMetaDataMap()
      {
         delegate = new HashMap<Class<?>, EjbMetaData<?>>();
      }

      @Override
      protected Map<Class<?>, EjbMetaData<?>> delegate()
      {
         return delegate;
      }

      @SuppressWarnings("unchecked")
      public <T> EjbMetaData<T> putIfAbsent(Class<T> key)
      {
         if (!containsKey(key))
         {
            EjbMetaData<T> ejbMetaData = new EjbMetaData<T>(key);
            super.put(key, ejbMetaData);
            return ejbMetaData;
         }
         return (EjbMetaData<T>) super.get(key);
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("EJBMetadataMap (class -> EJBMetaData): ", delegate);
      }

   }

   private Map<Class<? extends Annotation>, StereotypeModel<?>> stereotypes = new HashMap<Class<? extends Annotation>, StereotypeModel<?>>();

   private ScopeModelMap scopes = new ScopeModelMap();

   private BindingTypeModelMap bindingTypes = new BindingTypeModelMap();

   private EjbMetaDataMap ejbMetaDataMap = new EjbMetaDataMap();

   public void addStereotype(StereotypeModel<?> stereotype)
   {
      stereotypes.put(stereotype.getType(), stereotype);
   }

   public StereotypeModel<?> getStereotype(Class<? extends Annotation> annotationType)
   {
      return stereotypes.get(annotationType);
   }

   public <T extends Annotation> ScopeModel<T> getScopeModel(Class<T> scopeType)
   {
      return scopes.putIfAbsent(scopeType);
   }

   public <T extends Annotation> BindingTypeModel<T> getBindingTypeModel(Class<T> bindingType)
   {
      return bindingTypes.putIfAbsent(bindingType);
   }

   public <T> EjbMetaData<T> getEjbMetaData(Class<T> clazz)
   {
      return ejbMetaDataMap.putIfAbsent(clazz);
   }

   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("====================\n");
      buffer.append("Metadata cache\n");
      buffer.append("====================\n");
      buffer.append(bindingTypes.toString() + "\n");
      buffer.append(ejbMetaDataMap.toString() + "\n");
      buffer.append(scopes.toString() + "\n");
      buffer.append("Stereotypes: " + stereotypes.size() + "\n");
      int i = 0;
      for (Entry<Class<? extends Annotation>, StereotypeModel<?>> entry : stereotypes.entrySet())
      {
         buffer.append(++i + " - " + entry.getKey().getName() + ": " + entry.getValue().toString() + "\n");
      }
      buffer.append("====================\n");
      return buffer.toString();
   }

}
