/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.event;

import static org.jboss.weld.logging.Category.EVENT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.EventMessage.ASYNC_FIRE;
import static org.jboss.weld.logging.messages.EventMessage.ASYNC_OBSERVER_FAILURE;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.Lifecycle;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLogger.Level;

/**
 * A task that will notify the observer of a specific event at some future time.
 * 
 * @author David Allen
 */
public class DeferredEventNotification<T> implements Runnable
{
   private static final LocLogger log = loggerFactory().getLogger(EVENT);
   private static final XLogger xLog = loggerFactory().getXLogger(EVENT);
   
   // The observer
   protected final ObserverMethodImpl<T, ?> observer;
   // The event object
   protected final T event;

   /**
    * Creates a new deferred event notifier.
    * 
    * @param observer The observer to be notified
    * @param event The event being fired
    */
   public DeferredEventNotification(T event, ObserverMethodImpl<T, ?> observer)
   {
      this.observer = observer;
      this.event = event;
   }

   public void run()
   {
      try
      {
         log.debug(ASYNC_FIRE, event, observer);
         new RunInRequest()
         {
            
            @Override
            protected void execute()
            {
               observer.sendEvent(event);
            }
            
         }.run();
         
      }
      catch (Exception e)
      {
         log.error(ASYNC_OBSERVER_FAILURE, event);
         xLog.throwing(Level.DEBUG, e);
      }
   }

   @Override
   public String toString()
   {
      return "Deferred event [" + event + "] for [" + observer + "]";
   }
   
   private abstract static class RunInRequest
   {
      
      protected abstract void execute();
      
      public void run()
      {
         Lifecycle lifecycle = Container.instance().services().get(ContextLifecycle.class);
         boolean requestActive = lifecycle.isRequestActive();
         BeanStore requestBeanStore = new ConcurrentHashMapBeanStore();
         try
         {
            if (!requestActive)
            {
               lifecycle.beginRequest("async invocation", requestBeanStore);
            }
            execute();
         }
         finally
         {
            if (!requestActive)
            {
               lifecycle.endRequest("async invocation", requestBeanStore);
            }
         }
      }
      
   }
   
}
