package org.jboss.webbeans.test.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;


public class Util
{
   public static <T> BeanImpl<T> createSimpleWebBean(Class<T> clazz, ManagerImpl manager)
   {
      return new BeanImpl<T>(createSimpleModel(clazz, manager), manager);
   }
   
   public static <T> SimpleComponentModel<T> createSimpleModel(Class<T> clazz, ManagerImpl manager)
   {
      return new SimpleComponentModel<T>(new SimpleAnnotatedType<T>(clazz), getEmptyAnnotatedItem(clazz), manager);
   }
   
   public static <T> AnnotatedType<T> getEmptyAnnotatedItem(Class<T> type)
   {
      return new SimpleAnnotatedType<T>(type, new HashMap<Class<? extends Annotation>, Annotation>());
   }
   
   
   
}
