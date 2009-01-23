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

import javax.webbeans.BindingType;

import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.util.Strings;

import com.google.common.collect.ForwardingMap;

public class AnnotationStore
{

   // The array of default binding types
   private static final Annotation[] DEFAULT_BINDING_ARRAY = { new CurrentLiteral() };
   // The set of default binding types
   private static final Set<Annotation> DEFAULT_BINDING = new HashSet<Annotation>(Arrays.asList(DEFAULT_BINDING_ARRAY));
   
   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
   
   /**
    * Represents a mapping from a annotation type to an annotation
    * implementation
    */
   public static class AnnotationMap extends ForwardingMap<Class<? extends Annotation>, Annotation>
   {
      private final Map<Class<? extends Annotation>, Annotation> delegate;

      public AnnotationMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Annotation>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Annotation> delegate()
      {
         return delegate;
      }

      /**
       * Gets a string representation of the Map
       * 
       * @return A string representation
       */
      @Override
      public String toString()
      {
         return Strings.mapToString("AnnotationMap (annotation type -> annotation): ", delegate);
      }

   }

   /**
    * Represents a mapping from a annotation (meta-annotation) to a set of
    * annotations
    * 
    */
   private static class MetaAnnotationMap extends ForwardingMap<Class<? extends Annotation>, Set<Annotation>>
   {
      private final Map<Class<? extends Annotation>, Set<Annotation>> delegate;

      public MetaAnnotationMap()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<Annotation>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<Annotation>> delegate()
      {
         return delegate;
      }

      /**
       * Gets the set of annotations matching the given annotation type
       * 
       * @param key The meta-annotation to match
       * @returns The set of matching annotations containing this
       *          meta-annotation
       */
      @Override
      public Set<Annotation> get(Object key)
      {
         Set<Annotation> annotations = super.get(key);
         return annotations != null ? annotations : new HashSet<Annotation>();
      }

      /**
       * Adds an annotation under the meta-annotation type key
       * 
       * @param key The meta-annotation type
       * @param value The annotation
       */
      public void put(Class<? extends Annotation> key, Annotation value)
      {
         Set<Annotation> annotations = super.get(key);
         if (annotations == null)
         {
            annotations = new HashSet<Annotation>();
            super.put(key, annotations);
         }
         annotations.add(value);
      }

      /**
       * Gets a string representation of the Map
       * 
       * @return A string representation
       */
      @Override
      public String toString()
      {
         return Strings.mapToString("MetaAnnotationMap (annotation type -> annotation set: ", delegate);
      }

   }

   /**
    * Builds the annotation map (annotation type -> annotation)
    * 
    * @param annotations The array of annotations to map
    * @return The annotation map
    */
   protected static AnnotationMap buildAnnotationMap(Annotation[] annotations)
   {
      AnnotationMap annotationMap = new AnnotationMap();
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
   protected static AnnotationMap buildAnnotationMap(Iterable<Annotation> annotations)
   {
      AnnotationMap annotationMap = new AnnotationMap();
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
      AnnotationMap annotationMap = new AnnotationMap();
      annotationMap.putAll(buildAnnotationMap(annotations));
      annotationMap.putAll(annotationStore.getAnnotationMap());
      
      AnnotationMap declaredAnnotationMap = new AnnotationMap();
      declaredAnnotationMap.putAll(buildAnnotationMap(declaredAnnotations));
      declaredAnnotationMap.putAll(annotationStore.getDeclaredAnnotationMap());
      
      return new AnnotationStore(annotationMap, declaredAnnotationMap);
   }
   
   // The annotation map (annotation type -> annotation) of the item
   private final AnnotationMap annotationMap;
   // The meta-annotation map (annotation type -> set of annotations containing
   // meta-annotation) of the item
   private final MetaAnnotationMap metaAnnotationMap;
   // The set of all annotations on the item
   private final Set<Annotation> annotationSet;
   
   // The annotation map (annotation type -> annotation) of the item
   private final AnnotationMap declaredAnnotationMap;
   // The meta-annotation map (annotation type -> set of annotations containing
   // meta-annotation) of the item
   private final MetaAnnotationMap declaredMetaAnnotationMap;
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
   protected AnnotationStore(AnnotationMap annotationMap, AnnotationMap declaredAnnotationMap)
   {
      if (annotationMap == null)
      {
         throw new NullPointerException("annotationMap cannot be null");
      }
      this.annotationMap = annotationMap;
      this.annotationSet = new HashSet<Annotation>();
      this.metaAnnotationMap = new MetaAnnotationMap();
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
      this.declaredMetaAnnotationMap = new MetaAnnotationMap();
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
   public Set<Annotation> getBindingTypes()
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
   public Annotation[] getBindingTypesAsArray()
   {
      return getBindingTypes().toArray(EMPTY_ANNOTATION_ARRAY);
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
   
   AnnotationMap getAnnotationMap()
   {
      return annotationMap;
   }
   
   AnnotationMap getDeclaredAnnotationMap()
   {
      return declaredAnnotationMap;
   }
   
}
