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

package javax.inject.manager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * The contract between the manager and a interceptor.
 * 
 * This interface should not be called directly by the application.
 * 
 * @author Pete Muir
 * 
 */
public abstract class Interceptor extends Bean<Object>
{
   
   /**
    * Create an interceptor bean
    * 
    * @param manager
    *           the manager to create the interceptor for
    */
   protected Interceptor(Manager manager)
   {
      super(manager);
   }
   
   /**
    * The interceptor bindings used to bind an interceptor to a bean
    * 
    * @return the interceptor bindings
    */
   public abstract Set<Annotation> getInterceptorBindingTypes();
   
   /**
    * The interceptor method for the specified lifecycle callback or business
    * method
    * 
    * @param type
    *           the interception type
    * @return the method, or null if the interceptor does not intercept
    *         lifecycle callbacks or business methods
    */
   public abstract Method getMethod(InterceptionType type);
   
}
