package org.jboss.webbeans.log;

public class Logging
{
   private static final boolean isLog4JAvailable;

   static
   {
      boolean available;
      try
      {
         Class.forName("org.apache.log4j.Logger");
         available = true;
      }
      catch (ClassNotFoundException cnfe)
      {
         available = false;
      }
      isLog4JAvailable = available;
   }

   public static Log getLog(String category)
   {
      return new LogImpl(category);
   }

   public static Log getLog(Class<?> clazz)
   {
      return new LogImpl(clazz.getName());
   }

   static LogProvider getLogProvider(String category, boolean wrapped)
   {
      return isLog4JAvailable ? new Log4JProvider(category, wrapped) : new JDKProvider(category, wrapped);
   }

   public static LogProvider getLogProvider(Class<?> clazz)
   {
      return getLogProvider(clazz.getName(), false);
   }

}
