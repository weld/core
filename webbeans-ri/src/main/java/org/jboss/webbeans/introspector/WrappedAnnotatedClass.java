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
package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

public class WrappedAnnotatedClass<T> extends ForwardingAnnotatedClass<T>
{
   
   private final AnnotatedClass<T> delegate;
   private AnnotationStore annotationStore;
   
   public WrappedAnnotatedClass(AnnotatedClass<T> clazz, Set<Annotation> extraAnnotations, Set<Annotation> extraDeclaredAnnotations)
   {
      this.delegate = clazz;
      this.annotationStore = AnnotationStore.wrap(clazz.getAnnotationStore(), extraAnnotations, extraDeclaredAnnotations);
   }
   
   @Override
   protected AnnotatedClass<T> delegate()
   {
      return delegate;
   }
   
   @Override
   public AnnotationStore getAnnotationStore()
   {
      return annotationStore;
   }
   
}
