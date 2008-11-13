package org.jboss.webbeans.introspector.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;

/**
 * Base class for implementing AnnotatedItem.
 * 
 * @author pmuir
 *
 */
public class AnnotatedClassImpl<T> extends AbstractAnnotatedType<T> implements AnnotatedClass<T>
{
   
   private Class<T> clazz;
   private Type[] actualTypeArguments;
   
   private Set<AnnotatedField<Object>> fields;
   private Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> annotatedFields;
   private Map<Class<? extends Annotation>, Set<AnnotatedField<Object>>> metaAnnotatedFields;
   
   private Set<AnnotatedMethod<Object>> methods;
   private Map<Class<? extends Annotation>, Set<AnnotatedMethod<Object>>> annotatedMethods;
   
   private Set<AnnotatedConstructor<T>> constructors;
   private Map<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>> annotatedConstructors;
   private Map<List<Class<?>>, AnnotatedConstructor<T>> constructorsByArgumentMap;
   
   public AnnotatedClassImpl(Class<T> rawType, Type type, Annotation[] annotations)
   {
      super(buildAnnotationMap(annotations));
      this.clazz = rawType;
      if (type instanceof ParameterizedType)
      {
         actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
      }
      else
      {
         actualTypeArguments = new Type[0];
      }
   }
   
   public AnnotatedClassImpl(Class<T> clazz)
   {
      this(clazz, clazz, clazz.getAnnotations());
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
   
   public Set<AnnotatedConstructor<T>> getConstructors()
   {
      if (constructors == null)
      {
         initConstructors();
      }
      return constructors;
   }
   
   private void initFields()
   {
      this.fields = new HashSet<AnnotatedField<Object>>();
      for(Class c=clazz;c!=Object.class;c=c.getSuperclass())
      {
         for(Field field : clazz.getDeclaredFields())
         {
            if ( !field.isAccessible() ) field.setAccessible(true);
            fields.add(new AnnotatedFieldImpl<Object>(field, this));
         }
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
      for(Class c=clazz;c!=Object.class;c=c.getSuperclass())
      {
         for (Method method : clazz.getDeclaredMethods())
         {
            if (!method.isAccessible()) method.setAccessible(true);
            methods.add(new AnnotatedMethodImpl<Object>(method, this));
         }
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
            if (!annotatedMethods.containsKey(annotation.annotationType()))
            {
               annotatedMethods.put(annotation.annotationType(), new HashSet<AnnotatedMethod<Object>>());
            }
            annotatedMethods.get(annotation.annotationType()).add(member);
         }
      }
   }
   
   private void initConstructors()
   {
      this.constructors = new HashSet<AnnotatedConstructor<T>>();
      this.constructorsByArgumentMap = new HashMap<List<Class<?>>, AnnotatedConstructor<T>>();
      for (Constructor<T> constructor : clazz.getDeclaredConstructors())
      {
         AnnotatedConstructor<T> annotatedConstructor = new AnnotatedConstructorImpl<T>(constructor, this);
         if (!constructor.isAccessible()) constructor.setAccessible(true);
         constructors.add(annotatedConstructor);
         constructorsByArgumentMap.put(Arrays.asList(constructor.getParameterTypes()), annotatedConstructor);
      }
   }
   
   public Set<AnnotatedConstructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotationType)
   {
      if (annotatedConstructors == null)
      {
         initAnnotatedConstructors();
      }
       
      if (!annotatedConstructors.containsKey(annotationType))
      {
         return new HashSet<AnnotatedConstructor<T>>();
      }
      else
      {
         return annotatedConstructors.get(annotationType);
      }
   }

   private void initAnnotatedConstructors()
   {
      if (constructors == null)
      {
         initConstructors();
      }
      annotatedConstructors = new HashMap<Class<? extends Annotation>, Set<AnnotatedConstructor<T>>>();
      for (AnnotatedConstructor<T> constructor : constructors)
      {
         for (Annotation annotation : constructor.getAnnotations())
         {
            if (!annotatedConstructors.containsKey(annotation.annotationType()))
            {
               annotatedConstructors.put(annotation.annotationType(), new HashSet<AnnotatedConstructor<T>>());
            }
            annotatedConstructors.get(annotation.annotationType()).add(constructor);
         }
      }
   }
   
   public AnnotatedConstructor<T> getConstructor(List<Class<?>> arguments)
   {
      return constructorsByArgumentMap.get(arguments);
   }

}