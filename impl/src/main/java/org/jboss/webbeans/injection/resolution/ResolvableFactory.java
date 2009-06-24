package org.jboss.webbeans.injection.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.util.Reflections;

public class ResolvableFactory
{

   public static Resolvable of(WBAnnotated<?, ?> element)
   {
      if (element instanceof Resolvable)
      {
         return (Resolvable) element;
      }
      else
      {
         Set<Type> types = new HashSet<Type>();
         types.add(element.getBaseType());
         return new ResolvableImpl(element.getBindings(), types);
      }
   }

   public static Resolvable of(Set<Type> typeClosure, Set<Annotation> bindings)
   {
      return new ResolvableImpl(bindings, typeClosure);
   }

   public static Resolvable of(Set<Type> typeClosure, Annotation... bindings)
   {
      return new ResolvableImpl(new HashSet<Annotation>(Arrays.asList(bindings)), typeClosure);
   }

   private ResolvableFactory() {}

   private static class ResolvableImpl implements Resolvable
   {

      private final Set<Annotation> bindings;
      private final Set<Class<? extends Annotation>> annotationTypes;
      private final Set<Type> typeClosure;

      public ResolvableImpl(Set<Annotation> bindings, Set<Type> typeClosure)
      {
         this.bindings = bindings;
         if (bindings.size() == 0)
         {
            this.bindings.add(new CurrentLiteral());
         }
         this.annotationTypes = new HashSet<Class<? extends Annotation>>();
         this.typeClosure = typeClosure;
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

      public Set<Type> getTypeClosure()
      {
         return typeClosure;
      }

      public boolean isAssignableTo(Class<?> clazz)
      {
         return Reflections.isAssignableFrom(clazz, typeClosure);
      }

      @Override
      public String toString()
      {
         return "Types: " + getTypeClosure() + "; Bindings: " + getBindings();
      }

   }

}
