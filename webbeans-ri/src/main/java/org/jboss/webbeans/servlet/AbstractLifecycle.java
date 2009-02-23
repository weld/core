package org.jboss.webbeans.servlet;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.beanmap.BeanMap;
import org.jboss.webbeans.conversation.ConversationManager;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * A generic implementation of the Web Beans lifecycle that supports restoring
 * and destroying all the built in contexts
 * 
 * @author Pete Muir
 * 
 */
public abstract class AbstractLifecycle
{

   private static AbstractLifecycle instance;

   public static AbstractLifecycle instance()
   {
      return instance;
   }

   protected static void setInstance(AbstractLifecycle instance)
   {
      AbstractLifecycle.instance = instance;
   }

   private static LogProvider log = Logging.getLogProvider(AbstractLifecycle.class);

   protected void initialize()
   {
      ManagerImpl manager = CurrentManager.rootManager();
      if (manager == null)
      {
         throw new IllegalStateException("Manager has not been initialized, check that Bootstrap.initialize() has run");
      }
      manager.addContext(DependentContext.create());
      manager.addContext(RequestContext.create());
      manager.addContext(SessionContext.create());
      manager.addContext(ApplicationContext.create());
      manager.addContext(ConversationContext.create());
   }

   protected void beginApplication(String id, BeanMap applicationBeanMap)
   {
      log.trace("Starting application " + id);
      ApplicationContext.INSTANCE.setBeanMap(applicationBeanMap);
      ApplicationContext.INSTANCE.setActive(true);

   }

   protected void beginDeploy(BeanMap requestBeanMap)
   {
      RequestContext.INSTANCE.setBeanMap(requestBeanMap);
      RequestContext.INSTANCE.setActive(true);
   }

   protected void endDeploy(BeanMap requestBeanMap)
   {
      RequestContext.INSTANCE.setBeanMap(null);
      RequestContext.INSTANCE.setActive(false);
   }

   protected void endApplication(String id, BeanMap applicationBeanMap)
   {
      log.trace("Ending application " + id);
      ApplicationContext.INSTANCE.destroy();
      ApplicationContext.INSTANCE.setActive(false);
      ApplicationContext.INSTANCE.setBeanMap(null);
   }

   protected void beginSession(String id, BeanMap sessionBeanMap)
   {
      log.trace("Starting session " + id);
      SessionContext.INSTANCE.setBeanMap(sessionBeanMap);
      SessionContext.INSTANCE.setActive(true);
   }

   protected void endSession(String id, BeanMap sessionBeanMap)
   {
      log.trace("Ending session " + id);
      ConversationManager conversationManager = CurrentManager.rootManager().getInstanceByType(ConversationManager.class);
      conversationManager.destroyAllConversations();
      SessionContext.INSTANCE.destroy();
      SessionContext.INSTANCE.setBeanMap(null);
      SessionContext.INSTANCE.setActive(false);
   }

   public void beginRequest(String id, BeanMap requestBeanMap)
   {
      log.trace("Starting request " + id);
      RequestContext.INSTANCE.setBeanMap(requestBeanMap);
      RequestContext.INSTANCE.setActive(true);
      DependentContext.INSTANCE.setActive(true);
   }

   public void endRequest(String id, BeanMap requestBeanMap)
   {
      log.trace("Ending request " + id);
      RequestContext.INSTANCE.setBeanMap(requestBeanMap);
      DependentContext.INSTANCE.setActive(false);
      RequestContext.INSTANCE.destroy();
      RequestContext.INSTANCE.setActive(false);
   }

   protected void restoreConversation(String id, BeanMap conversationBeanMap)
   {
      log.trace("Starting conversation " + id);
      ConversationContext.INSTANCE.setBeanMap(conversationBeanMap);
      ConversationContext.INSTANCE.setActive(true);
   }

   protected void destroyConversation(String id, ConversationBeanMap conversationBeanMap)
   {
      log.trace("Ending conversation " + id);
      ConversationContext destructionContext = new ConversationContext();
      destructionContext.setBeanMap(conversationBeanMap);
      destructionContext.destroy();
   }

}
