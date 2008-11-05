package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;

/**
 * Base class for implementing AnnotatedItem. This implementation assumes 
 * the annotationMap is immutable.
 * 
 * @author pmuir
 *
 */
public class SimpleAnnotatedClass<T> extends AbstractAnnotatedType<T> implements AnnotatedClass<T>
{
   
   private Class<T> clazz;
   private Type[] actualTypeArguments;
   private Set<AnnotatedField<Object>> fields;
   private Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> annotatedFields;
   private Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> metaAnnotatedFields;
   
   private Set<AnnotatedMethod<Object>> methods;
   private Map<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>> annotatedMethods;
   
   public SimpleAnnotatedClass(Class<T> annotatedClass, Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      super(annotationMap);
      this.clazz = annotatedClass;
      if (this.clazz.getGenericSuperclass() instanceof ParameterizedType)
      {
         ParameterizedType type = (ParameterizedType) this.clazz.getGenericSuperclass();
         actualTypeArguments = type.getActualTypeArguments();
      }
      else
      {
         actualTypeArguments = new Type[0];
      }
   }
   
   public SimpleAnnotatedClass(Class<T> annotatedClass)
   {
      this(annotatedClass, buildAnnotationMap(annotatedClass));
   }
   
   public Class<? extends T> getAnnotatedClass()
   {
      return clazz;
   }
   
   public Class<T> getDelegate()
   {
      return clazz;
   }
   
   public Set<AnnotatedField<Object>> getFields()
   {
      if (fields == null)
      {
         initFields();
      }
      return fields;
   }
   
   private void initFields()
   {
      this.fields = new HashSet<AnnotatedField<Object>>();
      for(Field field : clazz.getFields())
      {
         fields.add(new SimpleAnnotatedField<Object>(field));
      }
   }

   public Set<AnnotatedField<Object>> getMetaAnnotatedFields(
         Class<? extends Annotation> metaAnnotationType)
   {
      if (metaAnnotatedFields == null)
      {
         metaAnnotatedFields = new HashMap<Class<? extends Annotation>, Set<AnnotatedField<Object>>>();
      }
      if (annotatedFields == null)
      {
         initAnnotatedFields();
      }
      populateMetaAnnotatedFieldMap(metaAnnotationType, annotatedFields, metaAnnotatedFields);
      return metaAnnotatedFields.get(metaAnnotationType);
   }
   
   protected static <T extends Annotation> Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> populateMetaAnnotatedFieldMap(
         Class<T> metaAnnotationType, 
         Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> annotatedFields, 
         Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> metaAnnotatedFields)
   {
      if (!metaAnnotatedFields.containsKey(metaAnnotationType))
      {
         Set<AnnotatedField<Object>> s = new HashSet<AnnotatedField<Object>>();
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

   public Set<AnnotatedField<Object>> getAnnotatedFields(
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
      annotatedFields = new HashMap<Class<? extends Annotation>, Set<AnnotatedField<Object>>>();
      for (AnnotatedField<Object> field : fields)
      {
         for (Annotation annotation : field.getAnnotations())
         {
            if (!annotatedFields.containsKey(annotation))
            {
               annotatedFields.put(annotation.annotationType(), new HashSet<AnnotatedField<Object>>());
            }
            annotatedFields.get(annotation.annotationType()).add(field);
         }
      }
   }

   public Class<T> getType()
   {
      return clazz;
   }

   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }
   
   private void initMethods()
   {
      this.methods = new HashSet<AnnotatedMethod<Object>>();
      for (Method member : clazz.getDeclaredMethods())
      {
         methods.add(new SimpleAnnotatedMethod<Object>(member));
      }
   }
   
   public Set<AnnotatedMethod<Object>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      if (annotatedMethods == null)
      {
         initAnnotatedMethods();
      }
       
      if (!annotatedMethods.containsKey(annotationType))
      {
         return new HashSet<AnnotatedMethod<Object>>();
      }
      else
      {
         return annotatedMethods.get(annotationType);
      }
   }

   private void initAnnotatedMethods()
   {
      if (methods == null)
      {
         initMethods();
      }
      annotatedMethods = new HashMap<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>>();
      for (AnnotatedMethod<Object> member : methods)
      {
         for (Annotation annotation : member.getAnnotations())
         {
            if (!annotatedMethods.containsKey(annotation))
            {
               annotatedMethods.put(annotation.annotationType(), new HashSet<AnnotatedMethod<Object>>());
            }
            annotatedMethods.get(annotation.annotationType()).add(member);
         }
      }
   }

}