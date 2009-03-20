package org.jboss.webbeans.bean.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.event.Event;

import org.jboss.webbeans.injection.resolution.AnnotatedItemTransformer;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.ForwardingAnnotatedItem;

public class FacadeBeanAnnotatedItemTransformer implements AnnotatedItemTransformer
{
   
   private final Class<?> clazz;
   private final Annotation annotation;
   private final Set<Annotation> annotations;
   private final Set<? extends Type> flattenedTypes;
   
   public FacadeBeanAnnotatedItemTransformer(Class<?> clazz, Annotation annotation)
   {
      this.clazz = clazz;
      this.annotation = annotation;
      this.annotations = new HashSet<Annotation>(Arrays.asList(annotation));
      Type[] types = {Object.class, Event.class};
      this.flattenedTypes = new HashSet<Type>(Arrays.asList(types));
   }

   public <T, S> AnnotatedItem<T, S> transform(final AnnotatedItem<T, S> element)
   {
      if (clazz.isAssignableFrom(element.getRawType()))
      {
         if (element.isAnnotationPresent(annotation.annotationType()))
         {
            
            return new ForwardingAnnotatedItem<T, S>()
            {
               
               @Override
               public Type[] getActualTypeArguments()
               {
                  return new Type[0];
               }
               
               @Override
               public Set<Annotation> getBindings()
               {
                  return annotations;
               }
               
               @SuppressWarnings("unchecked")
               @Override
               public Class<T> getRawType()
               {
                  return (Class<T>) clazz;
               }
               
               @Override
               public Type getType()
               {
                  return clazz;
               }
               
               @Override
               public Set<? extends Type> getFlattenedTypeHierarchy()
               {
                  return flattenedTypes;
               }

               @Override
               public AnnotatedItem<T, S> delegate()
               {
                  return element;
               }
               
               @Override
               public boolean isAssignableFrom(Set<? extends Type> types)
               {
                  return types.contains(clazz);
               }

            };
         }
      }
      return element;
   }
   
}
