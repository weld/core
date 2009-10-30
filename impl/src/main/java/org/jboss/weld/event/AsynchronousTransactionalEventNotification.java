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
package org.jboss.weld.event;

import static org.jboss.weld.messages.EventMessages.ASYNC_OBSERVER_FAILURE;
import static org.jboss.weld.messages.EventMessages.ASYNC_TX_FIRE;
import static org.jboss.weld.util.log.Categories.EVENT;
import static org.jboss.weld.util.log.LoggerFactory.loggerFactory;

import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLogger.Level;

/**
 * A deferred event notification that will be delivered during the appropriate
 * transaction phase, but asynchronously from the thread which generated the
 * event.
 * 
 * @author David Allen
 *
 */
public class AsynchronousTransactionalEventNotification<T> extends DeferredEventNotification<T>
{
   private static final LocLogger log = loggerFactory().getLogger(EVENT);
   private static final XLogger xLog = loggerFactory().getXLogger(EVENT);

   public AsynchronousTransactionalEventNotification(T event, ObserverMethodImpl<T, ?> observer)
   {
      super(event, observer);
   }

   @Override
   public void run()
   {
      // Let the event be deferred again as just an asynchronous event
      try
      {
         log.trace(ASYNC_TX_FIRE, event, observer);
         observer.sendEventAsynchronously(event);
      }
      catch (Exception e)
      {
         log.error(ASYNC_OBSERVER_FAILURE, event);
         xLog.throwing(Level.DEBUG, e);
      }
   }

}
