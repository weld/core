package org.jboss.webbeans.context;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.conversation.ConversationManager;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ConversationBeanStore;

/**
 * An implementation of the Web Beans lifecycle that supports restoring
 * and destroying all the built in contexts
 * 
 * @author Pete Muir
 * 
 */
public class ContextLifecycle
{

   private static ContextLifecycle instance;

   public static ContextLifecycle instance()
   {
      return instance;
   }

   protected static void setInstance(ContextLifecycle instance)
   {
      ContextLifecycle.instance = instance;
   }

   private static LogProvider log = Logging.getLogProvider(ContextLifecycle.class);

   protected void restoreSession(String id, BeanStore sessionBeanStore)
   {
      log.trace("Restoring session " + id);
      SessionContext.INSTANCE.setBeanStore(sessionBeanStore);
      SessionContext.INSTANCE.setActive(true);
   }

   protected void endSession(String id, BeanStore sessionBeanStore)
   {
      log.trace("Ending session " + id);
      SessionContext.INSTANCE.setActive(true);
      ConversationManager conversationManager = CurrentManager.rootManager().getInstanceByType(ConversationManager.class);
      conversationManager.destroyAllConversations();
      SessionContext.INSTANCE.destroy();
      SessionContext.INSTANCE.setBeanStore(null);
      SessionContext.INSTANCE.setActive(false);
   }

   public void beginRequest(String id, BeanStore requestBeanStore)
   {
      log.trace("Starting request " + id);
      RequestContext.INSTANCE.setBeanStore(requestBeanStore);
      RequestContext.INSTANCE.setActive(true);
      DependentContext.INSTANCE.setActive(true);
   }

   public void endRequest(String id, BeanStore requestBeanStore)
   {
      log.trace("Ending request " + id);
      RequestContext.INSTANCE.setBeanStore(requestBeanStore);
      DependentContext.INSTANCE.setActive(false);
      RequestContext.INSTANCE.destroy();
      RequestContext.INSTANCE.setActive(false);
   }

   protected void restoreConversation(String id, BeanStore conversationBeanStore)
   {
      log.trace("Starting conversation " + id);
      ConversationContext.INSTANCE.setBeanStore(conversationBeanStore);
      ConversationContext.INSTANCE.setActive(true);
   }

   protected void destroyConversation(String id, ConversationBeanStore conversationBeanStore)
   {
      log.trace("Ending conversation " + id);
      ConversationContext destructionContext = new ConversationContext();
      destructionContext.setBeanStore(conversationBeanStore);
      destructionContext.destroy();
   }

}
