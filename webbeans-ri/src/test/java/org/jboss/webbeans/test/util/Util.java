package org.jboss.webbeans.test.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.ProducerBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedMethod;
import org.jboss.webbeans.model.bean.EnterpriseBeanModel;
import org.jboss.webbeans.model.bean.ProducerMethodBeanModel;
import org.jboss.webbeans.model.bean.SimpleBeanModel;

public class Util
{
   public static <T> SimpleBean<T> createSimpleWebBean(Class<T> clazz, ManagerImpl manager)
   {
      return new SimpleBean<T>(createSimpleModel(clazz, manager), manager);
   }

   public static <T> SimpleBeanModel<T> createSimpleModel(Class<T> clazz, ManagerImpl manager)
   {
      return new SimpleBeanModel<T>(new SimpleAnnotatedClass<T>(clazz), null, manager);
   }

   public static <T> SimpleBeanModel<T> createSimpleModel(Class<T> clazz, AnnotatedClass<T> xmlAnnotatedType, ManagerImpl manager)
   {
      return new SimpleBeanModel<T>(new SimpleAnnotatedClass<T>(clazz), xmlAnnotatedType, manager);
   }

   public static <T> EnterpriseBeanModel<T> createEnterpriseBeanModel(Class<T> clazz, ManagerImpl manager)
   {
      return new EnterpriseBeanModel<T>(new SimpleAnnotatedClass<T>(clazz), null, manager);
   }

   public static <T> EnterpriseBeanModel<T> createEnterpriseBeanModel(Class<T> clazz, AnnotatedClass<T> xmlAnnotatedType, ManagerImpl manager)
   {
      return new EnterpriseBeanModel<T>(new SimpleAnnotatedClass<T>(clazz), xmlAnnotatedType, manager);
   }
   
   public static <T> ProducerBean<T> createProducerMethodBean(Class<T> type, Method method, ManagerImpl manager)
   {
      return new ProducerBean<T>(createProducerModel(type, method, null, manager), manager);
   }
   
   public static <T> ProducerMethodBeanModel<T> createProducerModel(Class<T> type, Method method, ManagerImpl manager)
   {
      return createProducerModel(type, method, null, manager);
   }
   
   public static <T> ProducerMethodBeanModel<T> createProducerModel(Class<T> type, Method method, AnnotatedMethod<T> xmlAnnotatedMethod, ManagerImpl manager)
   {
      return new ProducerMethodBeanModel<T>(new SimpleAnnotatedMethod<T>(method), xmlAnnotatedMethod, manager);
   }
      
   @Deprecated
   public static <T> AnnotatedClass<T> getEmptyAnnotatedType(Class<T> type)
   {
      return new SimpleAnnotatedClass<T>(type, new HashMap<Class<? extends Annotation>, Annotation>());
   }

   
   
}
