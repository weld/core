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
import java.util.concurrent.Callable;

import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.model.BindingTypeModel;
import org.jboss.webbeans.model.ScopeModel;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.util.ConcurrentCache;
import org.jboss.webbeans.util.Strings;

public class MetaDataCache
{

   private ConcurrentCache<Class<? extends Annotation>, StereotypeModel<?>> stereotypes = new ConcurrentCache<Class<? extends Annotation>, StereotypeModel<?>>();

   private ConcurrentCache<Class<? extends Annotation>, ScopeModel<?>> scopes = new ConcurrentCache<Class<? extends Annotation>, ScopeModel<?>>();

   private ConcurrentCache<Class<? extends Annotation>, BindingTypeModel<?>> bindingTypes = new ConcurrentCache<Class<? extends Annotation>, BindingTypeModel<?>>();

   private ConcurrentCache<Class<?>, EjbMetaData<?>> ejbMetaDataMap = new ConcurrentCache<Class<?>, EjbMetaData<?>>();

   public <T extends Annotation> StereotypeModel<T> getStereotype(final Class<T> stereotype)
   {
      return stereotypes.putIfAbsent(stereotype, new Callable<StereotypeModel<T>>()
      {

         public StereotypeModel<T> call() throws Exception
         {
            return new StereotypeModel<T>(stereotype);
         }
      });
   }

   public <T extends Annotation> ScopeModel<T> getScopeModel(final Class<T> scopeType)
   {
      return scopes.putIfAbsent(scopeType, new Callable<ScopeModel<T>>()
      {

         public ScopeModel<T> call() throws Exception
         {
            return new ScopeModel<T>(scopeType);
         }

      });
   }

   public <T extends Annotation> BindingTypeModel<T> getBindingTypeModel(final Class<T> bindingType)
   {
      return bindingTypes.putIfAbsent(bindingType, new Callable<BindingTypeModel<T>>()
      {

         public BindingTypeModel<T> call() throws Exception
         {
            return new BindingTypeModel<T>(bindingType);
         }

      });
   }

   public <T> EjbMetaData<T> getEjbMetaData(final Class<T> clazz)
   {
      return ejbMetaDataMap.putIfAbsent(clazz, new Callable<EjbMetaData<T>>()
      {

         public EjbMetaData<T> call() throws Exception
         {
            return new EjbMetaData<T>(clazz);
         }

      });
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
      buffer.append(Strings.mapToString("Stereotypes: ", stereotypes));
      buffer.append("====================\n");
      return buffer.toString();
   }

}
