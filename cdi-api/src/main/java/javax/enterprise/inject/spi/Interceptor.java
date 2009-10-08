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

package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.interceptor.InvocationContext;

/**
 * Main interface for all Bean objects representing an interceptor.
 * 
 * @author Gavin King
 * @author Pete Muir
 * @author David Allen
 *
 * @param <T>
 */
public interface Interceptor<T> extends Bean<T>
{

   /**
    * The interceptor bindings used to bind an interceptor to a bean
    * 
    * @return the interceptor bindings
    */
   public Set<Annotation> getInterceptorBindingTypes();
   
   /**
    * Tests if this intercepts callbacks or business methods of the given type
    * 
    * @param type The type of interception
    * @return true if this intercepts the given type of methods
    */
   public boolean intercepts(InterceptionType type); 
   
   /**
    * Invokes the specified kind of lifecycle callback or business method upon the 
    * given instance
    * 
    * @param type the interception type
    * @param instance the instance to invoke
    * @param ctx the context for the invocation
    * @return the return value from the invocation
    */
   public Object intercept(InterceptionType type, T instance, InvocationContext ctx); 


}