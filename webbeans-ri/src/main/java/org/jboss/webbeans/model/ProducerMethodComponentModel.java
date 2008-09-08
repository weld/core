package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import javax.webbeans.Dependent;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.MethodConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.SimpleAnnotatedItem;

public class ProducerMethodComponentModel<T> extends AbstractProducerComponentModel<T>
{
   
   private Method type;
   private ComponentConstructor<T> constructor;
   
   private AnnotatedItem<Method> xmlAnnotatedItem = new SimpleAnnotatedItem<Method>(new HashMap<Class<? extends Annotation>, Annotation>());
   private AnnotatedMethod annotatedMethod;
   
   @SuppressWarnings("unchecked")
   public ProducerMethodComponentModel(AnnotatedMethod annotatedMethod, ContainerImpl container)
   {
      this.annotatedMethod = annotatedMethod;
      init(container);
   }
   
   @Override
   protected void init(ContainerImpl container)
   {
      super.init(container);
      checkProducerMethod();
      this.constructor = new MethodConstructor<T>(type);
   }

   @Override
   public ComponentConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   protected void checkProducerMethod()
   {
      if (Modifier.isStatic(getAnnotatedItem().getDelegate().getModifiers()))
      {
         throw new RuntimeException("Producer method cannot be static " + annotatedMethod);
      }
      // TODO Check if declaring class is a WB component
      if (Modifier.isFinal(getAnnotatedItem().getDelegate().getModifiers()) || getScopeType().annotationType().equals(Dependent.class))
      {
         throw new RuntimeException("Final producer method must have @Dependent scope " + annotatedMethod);
      }
   }
   
   @Override
   public String toString()
   {
      return "ProducerMethodComponentModel[" + getType().getName() + "]";
   }

   @Override
   protected AnnotatedItem<Method> getAnnotatedItem()
   {
      return annotatedMethod;
   }

   @Override
   protected String getDefaultName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected Method getType()
   {
      return type;
   }

   @Override
   protected AnnotatedItem<Method> getXmlAnnotatedItem()
   {
      return xmlAnnotatedItem;
   }

   @Override
   protected void initType()
   {
      this.type = annotatedMethod.getAnnotatedMethod();
   }

}
