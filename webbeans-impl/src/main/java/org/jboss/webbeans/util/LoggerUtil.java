package org.jboss.webbeans.util;

import java.util.logging.Filter;
import java.util.logging.Logger;

public class LoggerUtil
{

   public static final String FILTER_PROPERTY = "org.jboss.webbeans.logger.filter";

   public static final String WEBBEANS_LOGGER = "javax.webbeans.";

   private static Filter filter;

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

   }

   public static Logger getLogger(String name)
   {
      name = WEBBEANS_LOGGER + name;
      Logger logger = Logger.getLogger(name);
      logger.setFilter(filter);
      return logger;
   }

}
