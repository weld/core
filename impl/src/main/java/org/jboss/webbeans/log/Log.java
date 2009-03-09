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

/**
 * <p>A <code>Log</code> object is used by RI classes to log messages.
 * They will be logged in any environment that has setup a logging service
 * such as Log4J or the standard JDK logging facilities.  In fact, this
 * logging interface is very similar to the other facilities with the
 * difference that logging methods also take any number of optional
 * parameters beyond the message object for later interpolation
 * into the message.</p>
 * 
 * <p>The idea of using interpolation parameters is very important for
 * performance reasons in production code.  Normally developers write
 * logging messages similar to this one:
 * <pre>
 *    log.debug("Started processing of " + obj1 + " with action " + obj2);
 * </pre>
 * The problem that arises at runtime in production systems, is that DEBUG
 * level logging may not be enabled.  And even if this logging is going to
 * be a no-op call, Java must still build the string dynamically that is the
 * argument to the call.  It is the building of this string that can be quite
 * time consuming.  The more complex the objects being concatenated are, the
 * worse the time penalty incurred is.  And this time is completely wasted
 * since the string may never be used.</p>
 * 
 * <p>Normally to combat the problem of making this call and building the
 * string dynamically, the developer uses a conditional statement to invoke
 * the call only if the corresponding logging level is enabled.  So the above
 * call may end up looking like:
 * <pre>
 *    if (log.isDebugEnabled())
 *    {
 *       log.debug("Started processing of " + obj1 + " with action " + obj2);
 *    }
 * </pre>
 * Ideally, this structure should always be used to avoid the cost of building the
 * dynamic string (concatenation) and making the unnecessary call.  The only 
 * drawback to this is that code can become less readable.  In some cases, there
 * might be more code devoted to logging than the actual behavior required by a
 * method.</p>
 * 
 * <p>A cleaner way to do the logging is to use this interface where simple
 * objects without any concatenation are passed as arguments.  Albeit the call
 * itself may still be a no-op when the logging level is not enabled, this is
 * still much smaller than the concatenation process with dynamic strings.  Each
 * of the methods defined here will first check to see if the logging level is enabled,
 * if that feature exists in the underlying logging system.  If and only if that logging
 * level is enabled, will the implementation proceed with concatenation of the strings
 * and objects passed.  So the above code using this interface becomes:
 * <pre>
 *    log.debug("Started processing of {0} with action {1}, obj1, obj2);
 * </pre>
 * </p>
 * 
 * <p>The interpolation of parameters into the message string are done using
 * {@link java.text.MessageFormat}.  See the documentation on that class for
 * more ideas of interpolation possibilities.  In the above code, <code>obj1</code>
 * and <code>obj2</code> simply have their <code>toString()</code> methods invoked
 * to produce a string which is then concatenated.</p>
 * 
 * @author Gavin King
 * @author David Allen
 *
 */
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