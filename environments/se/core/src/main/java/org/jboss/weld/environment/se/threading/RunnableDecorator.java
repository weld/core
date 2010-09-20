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
package org.jboss.weld.environment.se.threading;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.jboss.weld.environment.se.WeldSEBeanRegistrant;
import org.jboss.weld.environment.se.contexts.ThreadContext;

/**
 * Decorator for all beans which implements Runnable. It intercepts the call
 * to the run() method to set up the ThreadContext for the new thread so that
 * instances of @ThreadScoped beans can be correctly resolved.
 * @author Peter Royle
 */
@Decorator
public class RunnableDecorator implements Runnable {

   @Inject @Delegate Runnable runnable;

   /**
    * Set up the ThreadContet and delegate.
    */
   public void run()
   {
      // set up context for this thread
      final ThreadContext threadContext = WeldSEBeanRegistrant.THREAD_CONTEXT;
      try
      {
         threadContext.activate();
         // run the original thread
         runnable.run();
      }
      finally
      {
         threadContext.invalidate();
         threadContext.deactivate();
      }
         
   }



}
