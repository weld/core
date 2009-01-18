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
 * 
 * @author Gavin King
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