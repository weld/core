/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.webbeans.util;

import java.lang.reflect.Method;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.bean.XmlEnterpriseBean;
import org.jboss.webbeans.bean.XmlSimpleBean;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;

/**
 * Utility class for creating Web Beans
 * 
 * @author Pete Muir
 */
public class BeanFactory
{
   /**
    * Creates a simple, annotation defined Web Bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @return A Web Bean
    */
   public static <T> SimpleBean<T> createSimpleBean(Class<T> clazz)
   {
      return new SimpleBean<T>(clazz, ManagerImpl.rootManager());
   }

   /**
    * Creates a simple, XML defined Web Bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @return A Web Bean
    */
   public static <T> XmlSimpleBean<T> createXmlSimpleBean(Class<T> clazz)
   {
      return new XmlSimpleBean<T>(clazz, ManagerImpl.rootManager());
   }

   /**
    * Creates a simple, annotation defined Enterprise Web Bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @return An Enterprise Web Bean
    */
   public static <T> EnterpriseBean<T> createEnterpriseBean(Class<T> clazz)
   {
      return new EnterpriseBean<T>(clazz, ManagerImpl.rootManager());
   }

   /**
    * Creates a simple, XML defined Enterprise Web Bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @return An Enterprise Web Bean
    */
   public static <T> XmlEnterpriseBean<T> createXmlEnterpriseBean(Class<T> clazz)
   {
      return new XmlEnterpriseBean<T>(clazz, ManagerImpl.rootManager());
   }

   /**
    * Creates a producer method Web Bean
    * 
    * @param <T> The type
    * @param type The class
    * @param method The underlying method
    * @param declaringBean The declaring bean abstraction
    * @return A producer Web Bean
    */
   public static <T> ProducerMethodBean<T> createProducerMethodBean(Class<T> type, Method method, AbstractClassBean<?> declaringBean)
   {
      return new ProducerMethodBean<T>(method, declaringBean, ManagerImpl.rootManager());
   }

   /**
    * Creates a producer method Web Bean
    * 
    * @param method The underlying method abstraction
    * @param declaringBean The declaring bean abstraction
    * @return A producer Web Bean
    */
   public static <T> ProducerMethodBean<T> createProducerMethodBean(AnnotatedMethod<T> method, AbstractClassBean<?> declaringBean)
   {
      return new ProducerMethodBean<T>(method, declaringBean, ManagerImpl.rootManager());
   }

   /**
    * Creates an event Web Bean
    * 
    * @param field The observer field abstraction
    * @param declaringBean The declaring bean abstraction
    * @return An event Web Bean
    */
   public static <T> EventBean<T> createEventBean(AnnotatedField<T> field)
   {
      return new EventBean<T>(field, ManagerImpl.rootManager());
   }

}
