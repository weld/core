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

import static org.jboss.webbeans.introspector.AnnotatedItem.MAPPED_METAANNOTATIONS;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.BindingType;

import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.util.collections.multi.SetHashMultiMap;
import org.jboss.webbeans.util.collections.multi.SetMultiMap;

public class AnnotationStore
{

   // The array of default binding types
   private static final Annotation[] DEFAULT_BINDING_ARRAY = { new CurrentLiteral() };
   // The set of default binding types
   private static final Set<Annotation> DEFAULT_BINDING = new HashSet<Annotation>(Arrays.asList(DEFAULT_BINDING_ARRAY));
   
   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

   /**
    * Builds the annotation map (annotation type -> annotation)
    * 
    * @param annotations The array of annotations to map
    * @return The annotation map
    */
   protected static Map<Class<? extends Annotation>, Annotation> buildAnnotationMap(Annotation[] annotations)
   {
      Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      for (Annotation annotation : annotations)
      {
         annotationMap.put(annotation.annotationType(), annotation);
      }
      return annotationMap;
   }
   
   /**
    * Builds the annotation map (annotation type -> annotation)
    * 
    * @param annotations The array of annotations to map
    * @return The annotation map
    */
   protected static Map<Class<? extends Annotation>, Annotation> buildAnnotationMap(Iterable<Annotation> annotations)
   {
      Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      for (Annotation annotation : annotations)
      {
         annotationMap.put(annotation.annotationType(), annotation);
      }
      return annotationMap;
   }
   
   /**
    * Build an AnnotatedItemHelper from a class
    * 
    * @param annotatedElement
    * @return
    */
   public static AnnotationStore of(AnnotatedElement annotatedElement)
   {
      return new AnnotationStore(buildAnnotationMap(annotatedElement.getAnnotations()), buildAnnotationMap(annotatedElement.getDeclaredAnnotations()));
   }
   
   public static AnnotationStore of(Annotation[] annotations, Annotation[] declaredAnnotations)
   {
      return new AnnotationStore(buildAnnotationMap(annotations), buildAnnotationMap(declaredAnnotations));
   }
   
   public static AnnotationStore wrap(AnnotationStore annotationStore, Set<Annotation> annotations, Set<Annotation> declaredAnnotations)
   {
      Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      annotationMap.putAll(buildAnnotationMap(annotations));
      annotationMap.putAll(annotationStore.getAnnotationMap());
      
      Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      declaredAnnotationMap.putAll(buildAnnotationMap(declaredAnnotations));
      declaredAnnotationMap.putAll(annotationStore.getDeclaredAnnotationMap());
      
      return new AnnotationStore(annotationMap, declaredAnnotationMap);
   }
   
   // The annotation map (annotation type -> annotation) of the item
   private final Map<Class<? extends Annotation>, Annotation> annotationMap;
   // The meta-annotation map (annotation type -> set of annotations containing
   // meta-annotation) of the item
   private final SetMultiMap<Class<? extends Annotation>, Annotation> metaAnnotationMap;
   // The set of all annotations on the item
   private final Set<Annotation> annotationSet;
   
   // The annotation map (annotation type -> annotation) of the item
   private final Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap;
   // The meta-annotation map (annotation type -> set of annotations containing
   // meta-annotation) of the item
   private final SetMultiMap<Class<? extends Annotation>, Annotation> declaredMetaAnnotationMap;
   // The set of all annotations on the item
   private final Set<Annotation> declaredAnnotationSet;
   
   /**
    * Constructor
    * 
    * Also builds the meta-annotation map. Throws a NullPointerException if
    * trying to register a null map
    * 
    * @param annotationMap A map of annotation to register
    * 
    */
   protected AnnotationStore(Map<Class<? extends Annotation>, Annotation> annotationMap, Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap)
   {
      if (annotationMap == null)
      {
         throw new NullPointerException("annotationMap cannot be null");
      }
      this.annotationMap = annotationMap;
      this.annotationSet = new HashSet<Annotation>();
      this.metaAnnotationMap = new SetHashMultiMap<Class<? extends Annotation>, Annotation>();
      for (Annotation annotation : annotationMap.values())
      {
         for (Annotation metaAnnotation : annotation.annotationType().getAnnotations())
         {
            // Only map meta-annotations we are interested in
            if (MAPPED_METAANNOTATIONS.contains(metaAnnotation.annotationType()))
            {
               metaAnnotationMap.put(metaAnnotation.annotationType(), annotation);
            }
         }
         annotationSet.add(annotation);
      }
      
      if (declaredAnnotationMap == null)
      {
         throw new NullPointerException("declaredAnnotationMap cannot be null");
      }
      this.declaredAnnotationMap = declaredAnnotationMap;
      this.declaredAnnotationSet = new HashSet<Annotation>();
      this.declaredMetaAnnotationMap = new SetHashMultiMap<Class<? extends Annotation>, Annotation>();
      for (Annotation annotation : declaredAnnotationMap.values())
      {
         for (Annotation metaAnnotation : annotation.annotationType().getAnnotations())
         {
            // Only map meta-annotations we are interested in
            if (MAPPED_METAANNOTATIONS.contains(metaAnnotation.annotationType()))
            {
               declaredMetaAnnotationMap.put(metaAnnotation.annotationType(), annotation);
            }
         }
         declaredAnnotationSet.add(annotation);
      }
   }
   
   public Set<Annotation> getAnnotations()
   {
      return Collections.unmodifiableSet(annotationSet);
   }

   public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return Collections.unmodifiableSet(metaAnnotationMap.get(metaAnnotationType));
   }
   
   public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return Collections.unmodifiableSet(declaredMetaAnnotationMap.get(metaAnnotationType));
   }

   public Annotation[] getMetaAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType)
   {
      return getMetaAnnotations(metaAnnotationType).toArray(EMPTY_ANNOTATION_ARRAY);
   }

   @Deprecated
   public Set<Annotation> getBindings()
   {
      if (getMetaAnnotations(BindingType.class).size() > 0)
      {
         return Collections.unmodifiableSet(getMetaAnnotations(BindingType.class));
      }
      else
      {
         return Collections.unmodifiableSet(DEFAULT_BINDING);
      }
   }

   @Deprecated
   public Annotation[] getBindingsAsArray()
   {
      return getBindings().toArray(EMPTY_ANNOTATION_ARRAY);
   }

   public <A extends Annotation> A getAnnotation(Class<? extends A> annotationType)
   {
      return annotationType.cast(annotationMap.get(annotationType));
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return annotationMap.containsKey(annotationType);
   }
   
   public boolean isDeclaredAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return declaredAnnotationMap.containsKey(annotationType);
   }
   
   Map<Class<? extends Annotation>, Annotation> getAnnotationMap()
   {
      return annotationMap;
   }
   
   Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap()
   {
      return declaredAnnotationMap;
   }
   
}
