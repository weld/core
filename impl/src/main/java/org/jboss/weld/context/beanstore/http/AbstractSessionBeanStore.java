package org.jboss.weld.context.beanstore.http;

import static java.util.Collections.emptyList;
import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.jboss.weld.context.beanstore.AttributeBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.util.collections.EnumerationList;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

/**
 * Base class providing an HttpSession backed, bound bean store.
 * 
 * @see LazySessionBeanStore
 * @see EagerSessionBeanStore
 * 
 * @author Pete Muir
 * @author David Allen
 * @author Nicklas Karlsson
 *
 */
public abstract class AbstractSessionBeanStore extends AttributeBeanStore
{

   private static final LocLogger log = loggerFactory().getLogger(CONTEXT);

   protected abstract HttpSession getSession(boolean create);

   public AbstractSessionBeanStore(NamingScheme namingScheme)
   {
      super(namingScheme);
   }

   protected Collection<String> getAttributeNames()
   {
      HttpSession session = getSession(false);
      if (session == null)
      {
         return emptyList();
      }
      else
      {
         return new EnumerationList<String>(Reflections.<Enumeration<String>>cast(session.getAttributeNames()));
      }
   }

   @Override
   protected void removeAttribute(String key)
   {
      HttpSession session = getSession(false);
      if (session != null)
      {
         session.removeAttribute(key);
         log.trace("Removed " + key + " from session " + this.getSession(false).getId());
      }
      else
      {
         log.trace("Unable to remove " + key + " from non-existent session");
      }
   }

   @Override
   protected void setAttribute(String key, Object instance)
   {
      HttpSession session = getSession(true);
      if (session != null)
      {
         session.setAttribute(key, instance);
         log.trace("Added " + key + " to session " + this.getSession(false).getId());
      }
      else
      {
         log.trace("Unable to add " + key + " to session " + this.getSession(false).getId() + " as no session could be obtained");
      }
   }

   @Override
   protected Object getAttribute(String prefixedId)
   {
      return getSession(false).getAttribute(prefixedId);
   }

}