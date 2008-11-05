package org.jboss.webbeans.model.bean;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.webbeans.Dependent;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.MethodConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;

public class ProducerExpressionBeanModel<T> extends AbstractProducerBeanModel<T>
{
   
   private AnnotatedItem<T, Method> xmlAnnotatedItem;
   private AnnotatedItem<T, Method> annotatedItem = null /*new SimpleAnnotatedItem<T, Method>(new HashMap<Class<? extends Annotation>, Annotation>())*/;
   private String location;

   public ProducerExpressionBeanModel(AnnotatedItem<T, Method> xmlAnnotatedMethod, ManagerImpl container)
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

   public MethodConstructor<T> getConstructor()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected String getDefaultName()
   {
      throw new RuntimeException(getLocation() + " Cannot set a default name on producer expressions");
   }

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
