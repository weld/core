package org.jboss.webbeans.log;

public interface Log
{
   public boolean isDebugEnabled();
   public boolean isErrorEnabled();
   public boolean isFatalEnabled();
   public boolean isInfoEnabled();
   public boolean isTraceEnabled();
   public boolean isWarnEnabled();
   public void trace(Object object, Object... params);
   public void trace(Object object, Throwable t, Object... params);
   public void debug(Object object, Object... params);
   public void debug(Object object, Throwable t, Object... params);
   public void info(Object object, Object... params);
   public void info(Object object, Throwable t, Object... params);
   public void warn(Object object, Object... params);
   public void warn(Object object, Throwable t, Object... params);
   public void error(Object object, Object... params);
   public void error(Object object, Throwable t, Object... params);
   public void fatal(Object object, Object... params);
   public void fatal(Object object, Throwable t, Object... params);

}