package org.jboss.webbeans.test.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.SimpleBeanImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.EnterpriseBeanModel;
import org.jboss.webbeans.model.bean.SimpleBeanModel;

public class Util
{
   public static <T> SimpleBeanImpl<T> createSimpleWebBean(Class<T> clazz, ManagerImpl manager)
   {
      return new SimpleBeanImpl<T>(createSimpleModel(clazz, manager), manager);
   }

   public static <T> SimpleBeanModel<T> createSimpleModel(Class<T> clazz, ManagerImpl manager)
   {
      return new SimpleBeanModel<T>(new SimpleAnnotatedType<T>(clazz), getEmptyAnnotatedType(clazz), manager);
   }

   public static <T> SimpleBeanModel<T> createSimpleModel(Class<T> clazz, AnnotatedType<T> xmlAnnotatedType, ManagerImpl manager)
   {
      return new SimpleBeanModel<T>(new SimpleAnnotatedType<T>(clazz), xmlAnnotatedType, manager);
   }

   public static <T> EnterpriseBeanModel<T> createEnterpriseBeanModel(Class<T> clazz, ManagerImpl manager)
   {
      return new EnterpriseBeanModel<T>(new SimpleAnnotatedType<T>(clazz), getEmptyAnnotatedType(clazz), manager);
   }

   public static <T> EnterpriseBeanModel<T> createEnterpriseBeanModel(Class<T> clazz, AnnotatedType<T> xmlAnnotatedType, ManagerImpl manager)
   {
      return new EnterpriseBeanModel<T>(new SimpleAnnotatedType<T>(clazz), xmlAnnotatedType, manager);
   }
      
   public static <T> AnnotatedType<T> getEmptyAnnotatedType(Class<T> type)
   {
      return new SimpleAnnotatedType<T>(type, new HashMap<Class<? extends Annotation>, Annotation>());
   }

   
   
}
