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
