package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.introspector.AnnotatedMethod;

public class ProducerMethodComponentModel<T> extends AbstractComponentModel<T>
{
   
   @SuppressWarnings("unchecked")
   public ProducerMethodComponentModel(AnnotatedMethod annotatedItem, ContainerImpl container)
   {
      checkProducerMethod(annotatedItem, container);
   }

   @Override
   public ComponentConstructor getConstructor()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public static void checkProducerMethod(AnnotatedMethod annotatedItem, ContainerImpl container)
   {
      if (Modifier.isStatic(annotatedItem.getAnnotatedMethod().getModifiers()))
      {
         throw new RuntimeException("Producer method cannot be static " + annotatedItem);
      }
      // TODO Check if declaring class is a WB component
      
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Annotation getDeploymentType()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Annotation getScopeType()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected Class<? extends T> getType()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
