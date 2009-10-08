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
package org.jboss.weld.introspector;

import static org.jboss.weld.introspector.WeldAnnotated.MAPPED_METAANNOTATIONS;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Qualifier;

import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.metadata.TypeStore;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

public class AnnotationStore
{

   // The array of default binding types
   private static final Annotation[] DEFAULT_BINDING_ARRAY = { new DefaultLiteral() };
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
   public static AnnotationStore of(AnnotatedElement annotatedElement, TypeStore typeStore)
   {
      return new AnnotationStore(buildAnnotationMap(annotatedElement.getAnnotations()), buildAnnotationMap(annotatedElement.getDeclaredAnnotations()), typeStore);
   }
   
   public static AnnotationStore of(AnnotatedElement annotatedElement, Set<Annotation> extraAnnotations, Set<Annotation> extraDeclaredAnnotations, TypeStore typeStore)
   {
      Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      annotationMap.putAll(buildAnnotationMap(annotatedElement.getAnnotations()));
      annotationMap.putAll(buildAnnotationMap(extraAnnotations));
      
      Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      declaredAnnotationMap.putAll(buildAnnotationMap(annotatedElement.getDeclaredAnnotations()));
      declaredAnnotationMap.putAll(buildAnnotationMap(extraDeclaredAnnotations));
      
      return new AnnotationStore(annotationMap, declaredAnnotationMap, typeStore);
   }
   
   public static AnnotationStore of(Annotation[] annotations, Annotation[] declaredAnnotations, TypeStore typeStore)
   {
      return new AnnotationStore(buildAnnotationMap(annotations), buildAnnotationMap(declaredAnnotations), typeStore);
   }
   
   public static AnnotationStore of(Set<Annotation> annotations, Set<Annotation> declaredAnnotations, TypeStore typeStore)
   {
      return new AnnotationStore(buildAnnotationMap(annotations), buildAnnotationMap(declaredAnnotations), typeStore);
   }
   
   public static AnnotationStore wrap(AnnotationStore annotationStore, Set<Annotation> annotations, Set<Annotation> declaredAnnotations, TypeStore typeStore)
   {
      Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      annotationMap.putAll(buildAnnotationMap(annotations));
      annotationMap.putAll(annotationStore.getAnnotationMap());
      
      Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      declaredAnnotationMap.putAll(buildAnnotationMap(declaredAnnotations));
      declaredAnnotationMap.putAll(annotationStore.getDeclaredAnnotationMap());
      
      return new AnnotationStore(annotationMap, declaredAnnotationMap, typeStore);
   }
   
   // The annotation map (annotation type -> annotation) of the item
   private final Map<Class<? extends Annotation>, Annotation> annotationMap;
   // The meta-annotation map (annotation type -> set of annotations containing
   // meta-annotation) of the item
   private final SetMultimap<Class<? extends Annotation>, Annotation> metaAnnotationMap;
   // The set of all annotations on the item
   private final Set<Annotation> annotationSet;
   
   // The annotation map (annotation type -> annotation) of the item
   private final Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap;
   // The meta-annotation map (annotation type -> set of annotations containing
   // meta-annotation) of the item
   private final SetMultimap<Class<? extends Annotation>, Annotation> declaredMetaAnnotationMap;
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
   protected AnnotationStore(Map<Class<? extends Annotation>, Annotation> annotationMap, Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, TypeStore typeStore)
   {
      if (annotationMap == null)
      {
         throw new NullPointerException("annotationMap cannot be null");
      }
      this.annotationMap = annotationMap;
      this.annotationSet = new HashSet<Annotation>();
      this.metaAnnotationMap = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<Annotation>>(), new Supplier<Set<Annotation>>()
      {

         public Set<Annotation> get()
         {
            return new HashSet<Annotation>();
         }
      });
      for (Annotation annotation : annotationMap.values())
      {
         addMetaAnnotations(metaAnnotationMap, annotation, annotation.annotationType().getAnnotations());
         addMetaAnnotations(metaAnnotationMap, annotation, typeStore.get(annotation.annotationType()));
         annotationSet.add(annotation);
      }
      
      if (declaredAnnotationMap == null)
      {
         throw new NullPointerException("declaredAnnotationMap cannot be null");
      }
      this.declaredAnnotationMap = declaredAnnotationMap;
      this.declaredAnnotationSet = new HashSet<Annotation>();
      this.declaredMetaAnnotationMap = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<Annotation>>(), new Supplier<Set<Annotation>>()
      {

         public Set<Annotation> get()
         {
            return new HashSet<Annotation>();
         }
      });
      for (Annotation declaredAnnotation : declaredAnnotationMap.values())
      {
         addMetaAnnotations(declaredMetaAnnotationMap, declaredAnnotation, declaredAnnotation.annotationType().getAnnotations());
         addMetaAnnotations(declaredMetaAnnotationMap, declaredAnnotation, typeStore.get(declaredAnnotation.annotationType()));
         declaredAnnotationSet.add(declaredAnnotation);
      }
   }
   
   private static void addMetaAnnotations(SetMultimap<Class<? extends Annotation>, Annotation> metaAnnotationMap, Annotation annotation, Annotation[] metaAnnotations)
   {
      for (Annotation metaAnnotation : metaAnnotations)
      {
         addMetaAnnotation(metaAnnotationMap, annotation, metaAnnotation.annotationType());
      }
   }
   
   private static void addMetaAnnotations(SetMultimap<Class<? extends Annotation>, Annotation> metaAnnotationMap, Annotation annotation, Iterable<Annotation> metaAnnotations)
   {
      for (Annotation metaAnnotation : metaAnnotations)
      {
         addMetaAnnotation(metaAnnotationMap, annotation, metaAnnotation.annotationType());
      }
   }
   
   private static void addMetaAnnotation(SetMultimap<Class<? extends Annotation>, Annotation> metaAnnotationMap, Annotation annotation, Class<? extends Annotation> metaAnnotationType)
   {
      // Only map meta-annotations we are interested in
      if (MAPPED_METAANNOTATIONS.contains(metaAnnotationType))
      {
         metaAnnotationMap.put(metaAnnotationType, annotation);
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
      if (getMetaAnnotations(Qualifier.class).size() > 0)
      {
         return Collections.unmodifiableSet(getMetaAnnotations(Qualifier.class));
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
