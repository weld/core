/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.contexts.activators;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.context.bound.BoundRequestContext;

/**
 * @author Tomas Remes
 */
@Vetoed
@Interceptor
@ActivateRequestScope
@SuppressWarnings("checkstyle:magicnumber")
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 100)
public class ActivateRequestScopeInterceptor {

    private final BoundRequestContext boundRequestContext;

    @Inject
    public ActivateRequestScopeInterceptor(BoundRequestContext boundRequestContext) {
        this.boundRequestContext = boundRequestContext;
    }

    @AroundInvoke
    Object invoke(InvocationContext invocationContext) throws Exception {

        if (!boundRequestContext.isActive()) {
            Map<String, Object> storage = new HashMap<>();
            try {
                boundRequestContext.associate(storage);
                boundRequestContext.activate();
                return invocationContext.proceed();
            } finally {
                boundRequestContext.invalidate();
                boundRequestContext.deactivate();
                boundRequestContext.dissociate(storage);
                storage.clear();
            }
        } else {
            return invocationContext.proceed();
        }
    }
}
