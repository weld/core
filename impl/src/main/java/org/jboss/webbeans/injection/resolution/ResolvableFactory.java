package org.jboss.webbeans.injection.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.util.Reflections;

public class ResolvableFactory
{
   
   public static Resolvable of(AnnotatedItem<?, ?> element)
   {
      if (element instanceof Resolvable)
      {
         return (Resolvable) element;
      }
      else
      {
         Set<Type> types = new HashSet<Type>();
         types.add(element.getType());
         return new ResolvableImpl(element.getBindings(), types);
      }
   }
   
   private ResolvableFactory() {}
   
   private static class ResolvableImpl implements Resolvable
   {
      
      private final Set<Annotation> bindings;
      private final Set<Class<? extends Annotation>> annotationTypes;
      private final Set<Type> types;
      
      public ResolvableImpl(Set<Annotation> bindings, Set<Type> types)
      {
         this.bindings = bindings;
         this.annotationTypes = new HashSet<Class<? extends Annotation>>();
         this.types = types;
         for (Annotation annotation : bindings)
         {
            annotationTypes.add(annotation.annotationType());
         }
      }

      public Set<Annotation> getBindings()
      {
         return bindings;
      }

      public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
      {
         return annotationTypes.contains(annotationType);
      }
      
      public Set<Type> getTypes()
      {
         return types;
      }
      
      public boolean isAssignableTo(Class<?> clazz)
      {
         return Reflections.isAssignableFrom(clazz, types);
      }
      
   }

}
