package org.jboss.webbeans.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

final class Log4JProvider implements LogProvider
{
   private final Logger logger;
   private final boolean isWrapped;

   private static final String LOG_IMPL_FQCN = LogImpl.class.getName();
   private static final String LOG_PROVIDER_FQCN = Log4JProvider.class.getName();

   private static final Level TRACE;
   static
   {
      Object trace;
      try
      {
         trace = Level.class.getDeclaredField("TRACE").get(null);
      }
      catch (Exception e)
      {
         trace = Level.DEBUG;
      }
      TRACE = (Level) trace;
   }

   Log4JProvider(String category, boolean wrapped)
   {
      logger = Logger.getLogger(category);
      isWrapped = wrapped;
   }

   private String getFQCN()
   {
      return isWrapped ? LOG_IMPL_FQCN : LOG_PROVIDER_FQCN;
   }

   public void debug(Object object)
   {
      logger.log(getFQCN(), Level.DEBUG, object, null);
   }

   public void debug(Object object, Throwable t)
   {
      logger.log(getFQCN(), Level.DEBUG, object, t);
   }

   public void error(Object object)
   {
      logger.log(getFQCN(), Level.ERROR, object, null);
   }

   public void error(Object object, Throwable t)
   {
      logger.log(getFQCN(), Level.ERROR, object, t);
   }

   public void fatal(Object object)
   {
      logger.log(getFQCN(), Level.FATAL, object, null);
   }

   public void fatal(Object object, Throwable t)
   {
      logger.log(getFQCN(), Level.FATAL, object, t);
   }

   public void info(Object object)
   {
      logger.log(getFQCN(), Level.INFO, object, null);
   }

   public void info(Object object, Throwable t)
   {
      logger.log(getFQCN(), Level.INFO, object, t);
   }

   public boolean isDebugEnabled()
   {
      return logger.isEnabledFor(Level.DEBUG);
   }

   public boolean isErrorEnabled()
   {
      return logger.isEnabledFor(Level.ERROR);
   }

   public boolean isFatalEnabled()
   {
      return logger.isEnabledFor(Level.FATAL);
   }

   public boolean isInfoEnabled()
   {
      return logger.isEnabledFor(Level.INFO);
   }

   public boolean isTraceEnabled()
   {
      return logger.isEnabledFor(TRACE);
   }

   public boolean isWarnEnabled()
   {
      return logger.isEnabledFor(Level.WARN);
   }

   public void trace(Object object)
   {
      logger.log(getFQCN(), TRACE, object, null);
   }

   public void trace(Object object, Throwable t)
   {
      logger.log(getFQCN(), TRACE, object, t);
   }

   public void warn(Object object)
   {
      logger.log(getFQCN(), Level.WARN, object, null);
   }

   public void warn(Object object, Throwable t)
   {
      logger.log(getFQCN(), Level.WARN, object, t);
   }

}
