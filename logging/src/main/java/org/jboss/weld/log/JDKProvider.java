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
package org.jboss.weld.log;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Gavin King
 *
 */
class JDKProvider implements LogProvider
{
   private final Logger logger;
   private final boolean isWrapped;

   JDKProvider(String category, boolean wrapped)
   {
      this.logger = Logger.getLogger(category);
      this.isWrapped = wrapped;
   }

   private void log(Level level, Object object, Throwable ex)
   {

      if (logger.isLoggable(level))
      {
         Throwable dummyException = new Throwable();
         StackTraceElement locations[] = dummyException.getStackTrace();
         String className = "unknown";
         String methodName = "unknown";
         int depth = isWrapped ? 3 : 2;
         if (locations != null && locations.length > depth)
         {
            StackTraceElement caller = locations[depth];
            className = caller.getClassName();
            methodName = caller.getMethodName();
         }
         if (ex == null)
         {
            logger.logp(level, className, methodName, String.valueOf(object));
         }
         else
         {
            logger.logp(level, className, methodName, String.valueOf(object), ex);
         }
      }

   }

   public void debug(Object object, Throwable t)
   {
      log(Level.FINE, object, t);
   }

   public void debug(Object object)
   {
      log(Level.FINE, object, null);
   }

   public void error(Object object, Throwable t)
   {
      log(Level.SEVERE, object, t);
   }

   public void error(Object object)
   {
      log(Level.SEVERE, object, null);
   }

   public void fatal(Object object, Throwable t)
   {
      log(Level.SEVERE, object, t);
   }

   public void fatal(Object object)
   {
      log(Level.SEVERE, object, null);
   }

   public void info(Object object, Throwable t)
   {
      log(Level.INFO, object, t);
   }

   public void info(Object object)
   {
      log(Level.INFO, object, null);
   }

   public boolean isDebugEnabled()
   {
      return logger.isLoggable(Level.FINE);
   }

   public boolean isErrorEnabled()
   {
      return logger.isLoggable(Level.SEVERE);
   }

   public boolean isFatalEnabled()
   {
      return logger.isLoggable(Level.SEVERE);
   }

   public boolean isInfoEnabled()
   {
      return logger.isLoggable(Level.INFO);
   }

   public boolean isTraceEnabled()
   {
      return logger.isLoggable(Level.FINER);
   }

   public boolean isWarnEnabled()
   {
      return logger.isLoggable(Level.WARNING);
   }

   public void trace(Object object, Throwable t)
   {
      log(Level.FINER, object, t);
   }

   public void trace(Object object)
   {
      log(Level.FINER, object, null);
   }

   public void warn(Object object, Throwable t)
   {
      log(Level.WARNING, object, t);
   }

   public void warn(Object object)
   {
      log(Level.WARNING, object, null);
   }

}
