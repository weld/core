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
package org.jboss.webbeans.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 
 * @author Gavin King
 *
 */
class LogImpl implements Log, Externalizable
{
   private transient LogProvider log;
   private String category;

   public LogImpl()
   {
   }

   LogImpl(String category)
   {
      this.category = category;
      this.log = Logging.getLogProvider(category, true);
   }

   public boolean isDebugEnabled()
   {
      return log.isDebugEnabled();
   }

   public boolean isErrorEnabled()
   {
      return log.isErrorEnabled();
   }

   public boolean isFatalEnabled()
   {
      return log.isFatalEnabled();
   }

   public boolean isInfoEnabled()
   {
      return log.isInfoEnabled();
   }

   public boolean isTraceEnabled()
   {
      return log.isTraceEnabled();
   }

   public boolean isWarnEnabled()
   {
      return log.isWarnEnabled();
   }

   public void trace(Object object, Object... params)
   {
      if (isTraceEnabled())
      {
         log.trace(interpolate(object, params));
      }
   }

   public void trace(Object object, Throwable t, Object... params)
   {
      if (isTraceEnabled())
      {
         log.trace(interpolate(object, params), t);
      }
   }

   public void debug(Object object, Object... params)
   {
      if (isDebugEnabled())
      {
         log.debug(interpolate(object, params));
      }
   }

   public void debug(Object object, Throwable t, Object... params)
   {
      if (isDebugEnabled())
      {
         log.debug(interpolate(object, params), t);
      }
   }

   public void info(Object object, Object... params)
   {
      if (isInfoEnabled())
      {
         log.info(interpolate(object, params));
      }
   }

   public void info(Object object, Throwable t, Object... params)
   {
      if (isInfoEnabled())
      {
         log.info(interpolate(object, params), t);
      }
   }

   public void warn(Object object, Object... params)
   {
      if (isWarnEnabled())
      {
         log.warn(interpolate(object, params));
      }
   }

   public void warn(Object object, Throwable t, Object... params)
   {
      if (isWarnEnabled())
      {
         log.warn(interpolate(object, params), t);
      }
   }

   public void error(Object object, Object... params)
   {
      if (isErrorEnabled())
      {
         log.error(interpolate(object, params));
      }
   }

   public void error(Object object, Throwable t, Object... params)
   {
      if (isErrorEnabled())
      {
         log.error(interpolate(object, params), t);
      }
   }

   public void fatal(Object object, Object... params)
   {
      if (isFatalEnabled())
      {
         log.fatal(interpolate(object, params));
      }
   }

   public void fatal(Object object, Throwable t, Object... params)
   {
      if (isFatalEnabled())
      {
         log.fatal(interpolate(object, params), t);
      }
   }

   private Object interpolate(Object object, Object... params)
   {
      if (object instanceof String)
      {
         return object;
         // TODO: interpolation
         // return Interpolator.instance().interpolate( (String) object, params
         // );
      }
      else
      {
         return object;
      }
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      category = (String) in.readObject();
      log = Logging.getLogProvider(category, true);
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeObject(category);
   }

}
