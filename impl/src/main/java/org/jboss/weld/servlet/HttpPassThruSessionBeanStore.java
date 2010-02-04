/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.context.beanstore.HashMapBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.util.collections.EnumerationList;
import org.slf4j.cal10n.LocLogger;

/**
 * A BeanStore that maintains Contextuals in a hash map and writes them through
 * to a HttpSession. When this BeanStore is attached to a session, it will load
 * all the existing contextuals from the session within the naming scheme for
 * this BeanStore. All read operations are directly against the local map.
 * 
 * @author David Allen
 */
public class HttpPassThruSessionBeanStore extends HttpSessionBeanStore
{

   private static final long      serialVersionUID  = 8923580660774253915L;
   private static final LocLogger log               = loggerFactory().getLogger(CONTEXT);

   private HashMapBeanStore       delegateBeanStore = new HashMapBeanStore();
   private boolean                attachedToSession = false;
   private boolean                invalidated       = false;

   public HttpPassThruSessionBeanStore()
   {
      log.trace("New bean store created: " + this);
   }

   /**
    * Attaches this pass-through bean store to the given session.
    * 
    * @param session the HttpSession to pass contextuals to
    */
   public void attachToSession(HttpSession session)
   {
      super.attachToSession(session);
      attachedToSession = true;
      loadFromSession(session);
   }

   /**
    * Detaches this bean store from the current session. This implies that any
    * future lookups will be directly from the map maintained by this bean store
    * and no further Contextuals will be stored in a session.
    */
   protected void detachFromSession()
   {
      attachedToSession = false;
   }

   /**
    * Invalidates this session bean store, but it can still be used to service
    * the remainder of a request.
    */
   public void invalidate()
   {
      detachFromSession();
      invalidated = true;
      log.trace("Bean store " + this + " is invalidated");
   }

   /**
    * Checks if this bean store is currently attached to a session.
    * 
    * @return true if it is attached and using a session
    */
   public boolean isAttachedToSession()
   {
      return attachedToSession;
   }

   /**
    * Determines if this session bean store has been invalidated.
    * 
    * @return true if the bean store is already invalidated
    */
   public boolean isInvalidated()
   {
      return invalidated;
   }

   /**
    * Loads the map with all contextuals currently stored in the session for
    * this bean store.
    * 
    * @param newSession a new HttpSession being attached
    */
   protected void loadFromSession(HttpSession newSession)
   {
      log.trace("Loading bean store " + this + " map from session " + newSession.getId());
      try
      {
         for (String id : this.getFilteredAttributeNames())
         {
            delegateBeanStore.put(id, (ContextualInstance<?>) super.getAttribute(id));
            log.trace("Added contextual " + super.getAttribute(id) + " under ID " + id);
         }
      }
      catch (IllegalStateException e)
      {
         // There's not a lot to do here if the session is invalidated while
         // loading this map.  These beans will not be destroyed since the
         // references are lost.
         delegateBeanStore.clear();
      }
   }

   @Override
   protected Object getAttribute(String key)
   {
      return delegateBeanStore.get(key);
   }

   @Override
   protected Enumeration<String> getAttributeNames()
   {
      return Collections.enumeration(delegateBeanStore.getContextualIds());
   }

   @Override
   protected void removeAttribute(String key)
   {
      if (attachedToSession && !isInvalidated())
      {
         try
         {
            super.removeAttribute(key);
         }
         catch (IllegalStateException e)
         {
            invalidate();
         }
      }
      delegateBeanStore.delegate().remove(key);
   }

   @SuppressWarnings("unchecked")
   @Override
   protected void setAttribute(String key, Object instance)
   {
      if (attachedToSession && !isInvalidated())
      {
         try
         {
            super.setAttribute(key, instance);
            log.trace("***** Added " + key + " to session " + this.getSession().getId());
         }
         catch (IllegalStateException e)
         {
            invalidate();
         }
      }
      delegateBeanStore.put(key, (ContextualInstance<? extends Object>) instance);
      log.trace("Added instance for key " + key);
   }

   /**
    * Gets the list of attribute names that is held by the bean store
    * 
    * @return The list of attribute names
    */
   private List<String> getFilteredAttributeNames()
   {
      List<String> attributeNames = new ArrayList<String>();
      NamingScheme namingScheme = getNamingScheme();
      try
      {
         for (String attributeName : new EnumerationList<String>(super.getAttributeNames()))
         {
            if (namingScheme.acceptKey(attributeName))
            {
               attributeNames.add(attributeName);
            }
         }
      }
      catch (IllegalStateException e)
      {
         invalidate();
      }
      return attributeNames;
   }

}
