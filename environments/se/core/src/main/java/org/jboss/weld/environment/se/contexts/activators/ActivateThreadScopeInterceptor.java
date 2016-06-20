/*
* JBoss, Home of Professional Open Source
* Copyright 2016, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.se.contexts.activators;

import javax.annotation.Priority;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.environment.se.WeldSEBeanRegistrant;
import org.jboss.weld.environment.se.contexts.ThreadContext;

/**
 * @author Tomas Remes
 */

@Vetoed
@Interceptor
@ActivateThreadScope
@SuppressWarnings("checkstyle:magicnumber")
@Priority(Interceptor.Priority.APPLICATION + 100)
public class ActivateThreadScopeInterceptor {

    private final ThreadContext threadContext;

    @Inject
    public ActivateThreadScopeInterceptor(WeldSEBeanRegistrant registrant) {
        this.threadContext = registrant.getThreadContext();
    }

    @AroundInvoke
    Object intercept(InvocationContext invocationContext) throws Exception {

        if (!threadContext.isActive()) {
            try {
                threadContext.activate();
                return invocationContext.proceed();
            } finally {
                threadContext.invalidate();
                threadContext.deactivate();
            }
        } else {
            return invocationContext.proceed();
        }
    }

}
