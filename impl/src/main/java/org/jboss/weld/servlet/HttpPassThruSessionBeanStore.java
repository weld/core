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

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;

/**
 * A BeanStore that maintains Contextuals in a hash map and writes them through
 * to a HttpSession. It also has the capability to reload the hash map from an
 * existing session or to rewrite the map entries into a session.
 * 
 * @author David Allen
 */
public class HttpPassThruSessionBeanStore extends HttpSessionBeanStore
{

   private static final long          serialVersionUID       = 8923580660774253915L;

   private ConcurrentHashMapBeanStore delegateBeanStore      = new ConcurrentHashMapBeanStore();
   private boolean                    attachedToSession      = false;
   private boolean                    invalidated            = false;
   private static final String        SESSION_ATTRIBUTE_NAME = HttpPassThruSessionBeanStore.class.getName() + ".sessionBeanStore";

   /**
    * Attaches this pass-through bean store to the given session.
    * 
    * @param session the HttpSession to pass contextuals to
    */
   public void attachToSession(HttpSession session)
   {
      super.attachToSession(session);
      loadFromSession(session);
      attachedToSession = true;
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
    * Loads the map from the given session into this map store, if it already
    * exists in this session. If it does not already exist, then a new map is
    * created since the session does not already have any contextuals stored in
    * it.
    * 
    * @param newSession a new HttpSession being attached
    */
   protected void loadFromSession(HttpSession newSession)
   {
      Object map = newSession.getAttribute(SESSION_ATTRIBUTE_NAME);
      if (map != null)
      {
         delegateBeanStore = (ConcurrentHashMapBeanStore) map;
      }
      else
      {
         newSession.setAttribute(SESSION_ATTRIBUTE_NAME, delegateBeanStore);
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
         super.removeAttribute(key);
      }
      delegateBeanStore.delegate().remove(key);
   }

   @SuppressWarnings("unchecked")
   @Override
   protected void setAttribute(String key, Object instance)
   {
      if (attachedToSession && !isInvalidated())
      {
         super.setAttribute(key, instance);
      }
      delegateBeanStore.put(key, (ContextualInstance<? extends Object>) instance);
   }
}
