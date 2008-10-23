package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import javax.webbeans.Dependent;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.SimpleAnnotatedItem;

public class ProducerExpressionComponent<T> extends AbstractProducerComponentModel<T>
{
   
   private AnnotatedItem<T, Method> xmlAnnotatedItem;
   private AnnotatedItem<T, Method> annotatedItem = new SimpleAnnotatedItem<T, Method>(new HashMap<Class<? extends Annotation>, Annotation>());
   private String location;

   public ProducerExpressionComponent(AnnotatedItem<T, Method> xmlAnnotatedMethod, ManagerImpl container)
   {
      this.xmlAnnotatedItem = xmlAnnotatedMethod;
      init(container);
   }
   
   protected void checkApiType()
   {
      if (!getScopeType().equals(Dependent.class))
      {
         if (Modifier.isFinal(getType().getModifiers()))
         {
            throw new RuntimeException(getLocation() + "Final producer method must have @Dependent scope");
         }
      }
      
   }
   
   @Override
   protected void init(ManagerImpl container)
   {
      super.init(container);
      initInjectionPoints();
   }
   

   @Override
   protected AnnotatedItem<T, Method> getAnnotatedItem()
   {
      return annotatedItem;
   }

   @Override
   public ComponentConstructor<T> getConstructor()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected String getDefaultName()
   {
      throw new RuntimeException(getLocation() + " Cannot set a default name on producer expressions");
   }

   @Override
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Producer Expression; declaring document: TODO;";
      }
      return location;
   }

   @Override
   protected AnnotatedItem<T, Method> getXmlAnnotatedItem()
   {
      return xmlAnnotatedItem;
   }

   @Override
   protected void initType()
   {
      // TODO Auto-generated method stub

   }

}
