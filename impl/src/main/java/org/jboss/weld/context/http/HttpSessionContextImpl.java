package org.jboss.weld.context.http;

import java.lang.annotation.Annotation;

import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.Container;
import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.beanstore.http.EagerSessionBeanStore;
import org.jboss.weld.context.beanstore.http.LazySessionBeanStore;

public class HttpSessionContextImpl extends AbstractBoundContext<HttpServletRequest> implements HttpSessionContext
{

   private static final String IDENTIFIER = HttpSessionContextImpl.class.getName();

   private final NamingScheme namingScheme;

   public HttpSessionContextImpl()
   {
      super(true);
      this.namingScheme = new SimpleNamingScheme(HttpSessionContext.class.getName());
   }

   public boolean associate(HttpServletRequest request)
   {
      if (request.getAttribute(IDENTIFIER) == null)
      {
         // Don't reassociate
         setBeanStore(new LazySessionBeanStore(request, namingScheme));
         request.setAttribute(IDENTIFIER, IDENTIFIER);
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean dissociate(HttpServletRequest request)
   {
      if (request.getAttribute(IDENTIFIER) != null)
      {
         setBeanStore(null);
         request.removeAttribute(IDENTIFIER);
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean destroy(HttpSession session)
   {
      HttpConversationContext conversationContext = getConversationContext();
      if (getBeanStore() == null)
      {
         setBeanStore(new EagerSessionBeanStore(namingScheme, session));
         activate();
         invalidate();
         conversationContext.destroy(session);
         deactivate();
         setBeanStore(null);
         return true;
      }
      else
      {
         // We are in a request, invalidate it
         invalidate();
         if (conversationContext.isActive())
         {
            getConversationContext().invalidate();
         }
         else
         {
            // In a request, with no coversations, so destroy now
            getConversationContext().destroy(session);
         }
         return false;
      }
   }

   public Class<? extends Annotation> getScope()
   {
      return SessionScoped.class;
   }
   
   protected HttpConversationContext getConversationContext()
   {
      return Container.instance().deploymentManager().instance().select(HttpConversationContext.class).get();
   }

}
