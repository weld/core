/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.interceptor.spi.metadata;

import org.jboss.weld.interceptor.proxy.InterceptorInvocation;
import org.jboss.weld.interceptor.spi.model.InterceptionType;

/**
 * Metadata about a type that may have interceptor methods. This may either be an interceptor class or a component's target
 * class.
 *
 * @author Jozef Hartinger
 *
 */
public interface InterceptorMetadata {

    /**
     * Indicates, whether the given interception type is supported.
     *
     * @param interceptionType The {@link org.jboss.weld.interceptor.spi.model.InterceptionType}
     * @return true if the given interception type is supported, false otherwise
     */
    boolean isEligible(InterceptionType interceptionType);

    /**
     * Creates a {@link InterceptorInvocation} object representing invocation of interceptor methods of a given type on a given
     * interceptor instance.
     *
     * @param interceptorInstance the given instance of the interceptor
     * @param interceptionType the given interception type
     * @return
     */
    InterceptorInvocation getInterceptorInvocation(Object interceptorInstance, InterceptionType interceptionType);

}
