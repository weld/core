package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class SimpleAnnotatedAnnotation<T extends Annotation> extends AbstractAnnotatedItem<T, Class<T>> implements AnnotatedAnnotation<T>
{
   
   private Class<T> clazz;
   
   private Set<AnnotatedMethod<?>> members;
   
   public SimpleAnnotatedAnnotation(Class<T> annotationType)
   {
      super(buildAnnotationMap(annotationType));
      this.clazz = annotationType;
   }
   
   public Type[] getActualTypeArguements()
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

   public Class<? extends T> getType()
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
   
}
