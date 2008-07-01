package org.jboss.webbeans.util;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil
{

   public static final String FILTER_PROPERTY = "org.jboss.webbeans.logger.filter";
   public static final String LEVEL_PROPERTY = "org.jboss.webbeans.logger.level";

   public static final String WEBBEANS_LOGGER = "javax.webbeans.";

   private static Filter filter;
   
   private static Level level;

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
