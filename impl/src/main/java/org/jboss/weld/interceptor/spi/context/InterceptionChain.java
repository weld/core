/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.interceptor.spi.context;

import javax.interceptor.InvocationContext;

/**
 * Represents a chain of interceptor methods to be invoked as part of interception of a business/timeout method or a lifecycle event.
 * <p>
 * The chain tracks the position of the last-invoked interceptor method internally.
 *
 * @author Marius Bogoevici
 */
public interface InterceptionChain {

    /**
     * Invokes the next interceptor method in the chain. Generally this method should be called once {@link InvocationContext#proceed()} is invoked by the
     * interceptor.
     *
     * @param invocationContext the invocation context to be passed to the interceptor method
     * @return the value returned by the interceptor
     * @throws Exception
     */
    Object invokeNextInterceptor(InvocationContext invocationContext) throws Exception;

    /**
     * Indicates whether there are not-yet-invoked interceptor methods in the chain.
     * @return true if the chain has not been completed yet, false otherwise
     */
    boolean hasNextInterceptor();
}
