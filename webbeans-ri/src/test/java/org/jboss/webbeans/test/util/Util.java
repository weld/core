package org.jboss.webbeans.test.util;

import java.lang.reflect.Method;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.bean.XmlEnterpriseBean;
import org.jboss.webbeans.bean.XmlSimpleBean;

public class Util
{
   public static <T> SimpleBean<T> createSimpleBean(Class<T> clazz, ManagerImpl manager)
   {
      return new SimpleBean<T>(clazz, manager);
   }

   public static <T> XmlSimpleBean<T> createXmlSimpleBean(Class<T> clazz, ManagerImpl manager)
   {
      return new XmlSimpleBean<T>(clazz, manager);
   }

   public static <T> EnterpriseBean<T> createEnterpriseBean(Class<T> clazz, ManagerImpl manager)
   {
      return new EnterpriseBean<T>(clazz, manager);
   }

   public static <T> XmlEnterpriseBean<T> createXmlEnterpriseBean(Class<T> clazz, ManagerImpl manager)
   {
      return new XmlEnterpriseBean<T>(clazz, manager);
   }

   public static <T> ProducerMethodBean<T> createProducerMethodBean(Class<T> type, Method method, ManagerImpl manager, AbstractClassBean<?> declaringBean)
   {
      return new ProducerMethodBean<T>(method, declaringBean, manager);
   }

}
