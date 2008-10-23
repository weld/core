package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for implementing AnnotatedItem. This implementation assumes 
 * the annotationMap is immutable.
 * 
 * @author pmuir
 *
 */
public class SimpleAnnotatedType<T> extends AbstractAnnotatedItem<T, Class<T>> implements AnnotatedType<T>
{
   
   private Class<T> clazz;
   private Set<AnnotatedField<?>> fields;
   private Map<Class<? extends Annotation>, Set<AnnotatedField<?>>> annotatedFields;
   private Map<Class<? extends Annotation>, Set<AnnotatedField<?>>> metaAnnotatedFields;
   
   public SimpleAnnotatedType(Class<T> annotatedClass, Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      super(annotationMap);
      this.clazz = annotatedClass;
   }
   
   public SimpleAnnotatedType(Class<T> annotatedClass)
   {
      this(annotatedClass, buildAnnotationMap(annotatedClass));
   }
   
   public Class<? extends T> getAnnotatedClass()
   {
      return clazz;
   }
   
   @Override
   public String toString()
   {
      return clazz + " " + super.getAnnotationMap().toString();
   }
   
   public Class<T> getDelegate()
   {
      return clazz;
   }
   
   public Set<AnnotatedField<?>> getFields()
   {
      if (fields == null)
      {
         initFields();
      }
      return fields;
   }
   
   private void initFields()
   {
      this.fields = new HashSet<AnnotatedField<?>>();
      for(Field field : clazz.getFields())
      {
         fields.add(new SimpleAnnotatedField<Object>(field));
      }
   }

   public Set<AnnotatedField<?>> getMetaAnnotatedFields(
         Class<? extends Annotation> metaAnnotationType)
   {
      if (metaAnnotatedFields == null)
      {
         metaAnnotatedFields = new HashMap<Class<? extends Annotation>, Set<AnnotatedField<?>>>();
      }
      if (annotatedFields == null)
      {
         initAnnotatedFields();
      }
      populateMetaAnnotatedFieldMap(metaAnnotationType, annotatedFields, metaAnnotatedFields);
      return metaAnnotatedFields.get(metaAnnotationType);
   }
   
   protected static <T extends Annotation> Map<Class<? extends Annotation>, Set<AnnotatedField<?>>> populateMetaAnnotatedFieldMap(
         Class<T> metaAnnotationType, 
         Map<Class<? extends Annotation>, Set<AnnotatedField<?>>> annotatedFields, 
         Map<Class<? extends Annotation>, Set<AnnotatedField<?>>> metaAnnotatedFields)
   {
      if (!metaAnnotatedFields.containsKey(metaAnnotationType))
      {
         Set<AnnotatedField<?>> s = new HashSet<AnnotatedField<?>>();
         for (Class<? extends Annotation> annotationType: annotatedFields.keySet())
         {
            if (annotationType.isAnnotationPresent(metaAnnotationType))
            {
               s.addAll(annotatedFields.get(annotationType));
            }
         }
         metaAnnotatedFields.put(metaAnnotationType, s);
      }
      return metaAnnotatedFields;
   }

   public Set<AnnotatedField<?>> getAnnotatedField(
         Class<? extends Annotation> annotationType)
   {
      if (annotatedFields == null)
      {
         initAnnotatedFields();
      }
      return annotatedFields.get(annotationType);
   }

   private void initAnnotatedFields()
   {
      if (fields == null)
      {
         initFields();
      }
      annotatedFields = new HashMap<Class<? extends Annotation>, Set<AnnotatedField<?>>>();
      for (AnnotatedField<?> field : fields)
      {
         for (Annotation annotation : field.getAnnotations())
         {
            if (!annotatedFields.containsKey(annotation))
            {
               annotatedFields.put(annotation.annotationType(), new HashSet<AnnotatedField<?>>());
            }
            annotatedFields.get(annotation.annotationType()).add(field);
         }
      }
   }

   public Class<? extends T> getType()
   {
      return clazz;
   }

}