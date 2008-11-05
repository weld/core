package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedMethod;

public class SimpleAnnotatedAnnotation<T extends Annotation> extends AbstractAnnotatedType<T> implements AnnotatedAnnotation<T>
{
   
   private Map<Class<? extends Annotation>, Set<AnnotatedMethod<?>>> annotatedMembers;
   
   private Class<T> clazz;
   
   private Set<AnnotatedMethod<?>> members;
   
   public SimpleAnnotatedAnnotation(Class<T> annotationType)
   {
      super(buildAnnotationMap(annotationType));
      this.clazz = annotationType;
   }
   
   public Type[] getActualTypeArguments()
   {
      return new Type[0];
   }

   public Set<AnnotatedMethod<?>> getMembers()
   {
      if (members == null)
      {
         initMembers();
      }
      return members;
   }

   public Class<T> getDelegate()
   {
      return clazz;
   }

   public Class<T> getType()
   {
      return clazz;
   }
   
   private void initMembers()
   {
      this.members = new HashSet<AnnotatedMethod<?>>();
      for (Method member : clazz.getDeclaredMethods())
      {
         members.add(new SimpleAnnotatedMethod<Object>(member));
      }
   }

   public Set<AnnotatedMethod<?>> getAnnotatedMembers(Class<? extends Annotation> annotationType)
   {
      if (annotatedMembers == null)
      {
         initAnnotatedMembers();
      }
       
      if (!annotatedMembers.containsKey(annotationType))
      {
         return new HashSet<AnnotatedMethod<?>>();
      }
      else
      {
         return annotatedMembers.get(annotationType);
      }
   }

   private void initAnnotatedMembers()
   {
      if (members == null)
      {
         initMembers();
      }
      annotatedMembers = new HashMap<Class<? extends Annotation>, Set<AnnotatedMethod<?>>>();
      for (AnnotatedMethod<?> member : members)
      {
         for (Annotation annotation : member.getAnnotations())
         {
            if (!annotatedMembers.containsKey(annotation))
            {
               annotatedMembers.put(annotation.annotationType(), new HashSet<AnnotatedMethod<?>>());
            }
            annotatedMembers.get(annotation.annotationType()).add(member);
         }
      }
   }
   
}
