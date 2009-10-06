/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.webbeans.ejb.spi;

import java.util.List;
import java.util.Collection;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.InterceptionType;
import javax.interceptor.InterceptorBinding;

/**
 * @author Marius Bogoevici
 */
public interface InterceptorBindings
{
   /**
    * Returns all interceptors that are bound to an EJB object through
    * the {@link InterceptorBinding} mechanism and are
    * enabled through the beans.xml file.
    *
    * This includes class and method-bound interceptors.The purpose of this
    * method is to indicate what interceptors does the container need to
    * interact with, for a given EJB.
    *
    * Note: in the case of an EJB, the expectation is that the interpretation
    * of {link @javax.interceptor.Interceptors} is left to the container, and
    * the interceptors provided by the binding are complementary
    */
   Collection<Interceptor<?>> getAllInterceptors();


   /**
    * Returns the interceptors that are applicable to a given {@link InterceptionType}
    * and method (bound by {@link InterceptorBinding}).
    * This includes class and method-bound interceptors, but no interceptors
    * bound by EJB-specific mechanisms.
    *
    * @param interceptionType - the interception type (non-lifecycle)
    * @param method - the method that is to be intercepted
    * @return - an immutable list of interceptors applicable to the method (empty if no such
    *          interceptors exist)
    * @throw IllegalArgumentException if interceptionType is not {@link InterceptionType.AROUND_INVOKE}
    *        or {@link InterceptionType.AROUND_TIMEOUT}
    */
   List<Interceptor<?>> getMethodInterceptors(InterceptionType interceptionType, Method method);


    /**
    * Returns the lifecycle interceptors that are applicable to a given {@link InterceptionType}
    * (bound by {@link InterceptorBinding}).
    *
    * This includes class and method-bound interceptors, but no interceptors
    * bound by EJB-specific mechanisms.
    *
    * @param interceptionType - the interception type (lifecycle)
    * @param method - the method that is to be intercepted
    * @return - an immutable list of interceptors applicable to the method (empty if no such
    *          interceptors exist)
    * @throw IllegalArgumentException if interceptionType not {@link InterceptionType.AROUND_INVOKE}
    *        or {@link InterceptionType.AROUND_TIMEOUT}
    */
   List<Interceptor<?>> getLifecycleInterceptors(InterceptionType interceptionType);

}
