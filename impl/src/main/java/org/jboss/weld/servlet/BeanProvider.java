/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.servlet;

import static org.jboss.weld.servlet.ServletHelper.getModuleBeanManager;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;
import javax.servlet.ServletContext;

import org.jboss.weld.conversation.ConversationIdName;
import org.jboss.weld.conversation.ConversationImpl;
import org.jboss.weld.conversation.ConversationManager;
import org.jboss.weld.manager.BeanManagerImpl;

public class BeanProvider
{

   private static class ConversationIdNameLiteral extends AnnotationLiteral<ConversationIdName> implements ConversationIdName
   {
      
      public static final ConversationIdName INSTANCE = new ConversationIdNameLiteral();
      
      private ConversationIdNameLiteral() {}
      
   }
   
   public static ConversationManager conversationManager(ServletContext servletContext)
   {
      BeanManagerImpl beanManager = getModuleBeanManager(servletContext);
      Bean<?> bean = beanManager.resolve(beanManager.getBeans(ConversationManager.class));
      return (ConversationManager) beanManager.getReference(bean, ConversationManager.class, beanManager.createCreationalContext(bean));
   }
   
   public static HttpSessionManager httpSessionManager(ServletContext servletContext)
   {
      BeanManagerImpl beanManager = getModuleBeanManager(servletContext);
      Bean<?> bean = beanManager.resolve(beanManager.getBeans(HttpSessionManager.class));
      return (HttpSessionManager) beanManager.getReference(bean, HttpSessionManager.class, beanManager.createCreationalContext(bean));
   }
   
   public static ConversationImpl conversation(ServletContext servletContext)
   {
      BeanManagerImpl beanManager = getModuleBeanManager(servletContext);
      Bean<?> bean = beanManager.resolve(beanManager.getBeans(ConversationImpl.class));
      return (ConversationImpl) beanManager.getReference(bean, ConversationImpl.class, beanManager.createCreationalContext(bean));
   }
   
   public static String conversationIdName(ServletContext servletContext)
   {
      BeanManagerImpl beanManager = getModuleBeanManager(servletContext);
      Bean<?> bean = beanManager.resolve(beanManager.getBeans(String.class, ConversationIdNameLiteral.INSTANCE));
      return (String) beanManager.getReference(bean, String.class, beanManager.createCreationalContext(bean));
   }
   
   private BeanProvider() {}
   
}
