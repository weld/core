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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.webbeans.TypeLiteral;

public class AnnotatedItemImpl<T, S> extends AbstractAnnotatedItem<T, S>
{

   private Type[] actualTypeArguments = new Type[0];
   private Class<T> type;
   private Annotation[] actualAnnotations;
   
   private AnnotatedItemImpl(AnnotationMap annotationMap)
   {
      super(annotationMap);
   }
   
   private AnnotatedItemImpl(AnnotationMap annotationMap, Class<T> type)
   {
      super(annotationMap);
      this.type = type;
   }
   
   private AnnotatedItemImpl(AnnotationMap annotationMap, TypeLiteral<T> apiType)
   {
      super(annotationMap);
      this.type = apiType.getRawType();
      if (apiType.getType() instanceof ParameterizedType)
      {
         actualTypeArguments = ((ParameterizedType) apiType.getType()).getActualTypeArguments();
      }
   }
   
   private AnnotatedItemImpl(AnnotationMap annotationMap, Class<T> type, Type[] actualTypeArguments)
   {
      this(annotationMap, type);
      this.actualTypeArguments = actualTypeArguments;
   }
   
   public AnnotatedItemImpl(Annotation[] annotations)
   {
      this(buildAnnotationMap(annotations));
      this.actualAnnotations = annotations;
   }
   
   public AnnotatedItemImpl(Annotation[] annotations, Class<T> type)
   {
      this(buildAnnotationMap(annotations), type);
      this.actualAnnotations = annotations;
   }
   
   public AnnotatedItemImpl(Annotation[] annotations, TypeLiteral<T> apiType)
   {
      this(buildAnnotationMap(annotations), apiType);
      this.actualAnnotations = annotations;
   }
   
   public AnnotatedItemImpl(Annotation[] annotations, Class<T> type, Type[] actualTypeArguments)
   {
      this(buildAnnotationMap(annotations), type, actualTypeArguments);
      this.actualAnnotations = annotations;
   }

   public S getDelegate()
   {
      return null;
   }
   
   public Class<T> getType()
   {
      return type;
   }

   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }
   
   public Annotation[] getActualAnnotations()
   {
      return actualAnnotations;
   }
   
   public boolean isStatic()
   {
      return false;
   }
   
   public boolean isFinal()
   {
      return false;
   }

   public String getName()
   {
      throw new IllegalArgumentException("Unable to determine name");
   }
   
}
