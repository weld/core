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

package org.jboss.webbeans.util;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil
{
   // The logging filter property
   public static final String FILTER_PROPERTY = "org.jboss.webbeans.logger.filter";
   // The logging level property
   public static final String LEVEL_PROPERTY = "org.jboss.webbeans.logger.level";
   // The Web Bean logger
   public static final String WEBBEANS_LOGGER = "javax.webbeans.";
   // The current filter
   private static Filter filter;
   // The current level
   private static Level level;

   /*
    * Static init block
    */
   static
   {
      String filterClassName = System.getProperty(FILTER_PROPERTY);
      if (filterClassName != null && !"".equals(filterClassName))
         try
         {
            filter = (Filter) Class.forName(filterClassName).newInstance();
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException("Unable to instantiate logging filter");
         }
      String levelProperty = System.getProperty(LEVEL_PROPERTY);
      if (levelProperty != null && !"".equals(levelProperty))
      {
         level = Level.parse(levelProperty);
      }

      if (level != null)
      {
         for (Handler handler : Logger.getLogger("").getHandlers())
         {
            handler.setLevel(level);
         }
      }

   }

   /**
    * Gets a logger
    * 
    * @param name The name of the logger
    * @return A logger implementation
    */
   public static Logger getLogger(String name)
   {
      name = WEBBEANS_LOGGER + name;
      Logger logger = Logger.getLogger(name);
      if (filter != null)
      {
         logger.setFilter(filter);
      }
      if (level != null)
      {
         logger.setLevel(level);
      }
      return logger;
   }

}
