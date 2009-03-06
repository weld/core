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
public interface LogProvider
{
   public void trace(Object object);
   public void trace(Object object, Throwable t);
   public void debug(Object object);
   public void debug(Object object, Throwable t);
   public void info(Object object);
   public void info(Object object, Throwable t);
   public void warn(Object object);
   public void warn(Object object, Throwable t);
   public void error(Object object);
   public void error(Object object, Throwable t);
   public void fatal(Object object);
   public void fatal(Object object, Throwable t);
   public boolean isTraceEnabled();
   public boolean isDebugEnabled();
   public boolean isInfoEnabled();
   public boolean isWarnEnabled();
   public boolean isErrorEnabled();
   public boolean isFatalEnabled();
}
