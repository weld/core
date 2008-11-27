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

package org.jboss.webbeans.introspector.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.webbeans.BindingType;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public class AnnotatedParameterImpl<T> extends AbstractAnnotatedItem<T, Object> implements AnnotatedParameter<T>
{

   private Class<T> type;
   private Type[] actualTypeArguments = new Type[0];
   private boolean _final;
   private boolean _static;

   public AnnotatedParameterImpl(Annotation[] annotations, Class<T> type)
   {
      super(buildAnnotationMap(annotations));
      this.type = type;
   }

   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   public Object getDelegate()
   {
      return null;
   }

   public Class<T> getType()
   {
      return type;
   }

   public boolean isFinal()
   {
      return _final;
   }

   public boolean isStatic()
   {
      return _static;
   }
   
   public T getValue(ManagerImpl manager)
   {
      return manager.getInstanceByType(getType(), getMetaAnnotationsAsArray(BindingType.class));
   }

   public String getName()
   {
      throw new IllegalArgumentException("Unable to determine name of parameter");
   }
   
}
