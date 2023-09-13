/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.propagation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;

import org.jboss.weld.context.WeldAlterableContext;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequest;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.bound.MutableBoundRequest;
import org.jboss.weld.manager.api.WeldManager;

/**
 * Util class allowing to offload tasks to another thread. Takes care of context activation/propagation.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class ContextPropagationService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    public static <T> Future<T> propagateContextsAndSubmitTask(Callable<T> task) {
        // gather all the contexts we want to propagate and the instances in them
        Map<Class<? extends Annotation>, Collection<ContextualInstance<?>>> scopeToContextualInstances = new HashMap<>();
        for (WeldAlterableContext context : CDI.current().select(WeldManager.class).get().getActiveWeldAlterableContexts()) {
            scopeToContextualInstances.put(context.getScope(), context.getAllContextualInstances());
        }
        // We create a task wrapper which will make sure we have contexts propagated
        Callable<T> wrappedTask = new Callable<T>() {

            @Override
            public T call() throws Exception {
                WeldManager weldManager = CDI.current().select(WeldManager.class).get();
                BoundRequestContext requestContext = weldManager.instance()
                        .select(BoundRequestContext.class, BoundLiteral.INSTANCE).get();
                BoundSessionContext sessionContext = weldManager.instance()
                        .select(BoundSessionContext.class, BoundLiteral.INSTANCE).get();
                BoundConversationContext conversationContext = weldManager.instance()
                        .select(BoundConversationContext.class, BoundLiteral.INSTANCE).get();

                // we will be using bound contexts, prepare backing structures for contexts
                Map<String, Object> sessionMap = new HashMap<>();
                Map<String, Object> requestMap = new HashMap<>();
                BoundRequest boundRequest = new MutableBoundRequest(requestMap, sessionMap);

                // activate contexts
                requestContext.associate(requestMap);
                requestContext.activate();
                sessionContext.associate(sessionMap);
                sessionContext.activate();
                conversationContext.associate(boundRequest);
                conversationContext.activate();

                // propagate all contexts that have some bean in them
                if (scopeToContextualInstances.get(requestContext.getScope()) != null) {
                    requestContext.clearAndSet(scopeToContextualInstances.get(requestContext.getScope()));
                }
                if (scopeToContextualInstances.get(sessionContext.getScope()) != null) {
                    sessionContext.clearAndSet(scopeToContextualInstances.get(sessionContext.getScope()));
                }
                if (scopeToContextualInstances.get(conversationContext.getScope()) != null) {
                    conversationContext.clearAndSet(scopeToContextualInstances.get(conversationContext.getScope()));
                }

                // now execute the actual original task
                T result = task.call();

                // cleanup, context deactivation
                // context.invalidate() is deliberately left out so as to avoid calling all pre destroy/disposer callbacks
                // propagators might choose to invoke them but it could lead to these methods being invoked multiple times
                // while the bean is still 'alive' in yet another thread into which it was propagated
                requestContext.deactivate();
                conversationContext.deactivate();
                sessionContext.deactivate();

                // all done, return
                return result;
            }
        };
        return executor.submit(wrappedTask);
    }

    public static <T> Integer wrapAndRunOnTheSameThread(Callable<T> task) {
        // gather all the contexts we want to propagate and the instances in them
        Map<Class<? extends Annotation>, Collection<ContextualInstance<?>>> scopeToContextualInstances = new HashMap<>();
        for (WeldAlterableContext context : CDI.current().select(WeldManager.class).get().getActiveWeldAlterableContexts()) {
            scopeToContextualInstances.put(context.getScope(), context.getAllContextualInstances());
        }
        // We create a task wrapper which will make sure we have contexts propagated
        Callable<T> wrappedTask = new Callable<T>() {

            @Override
            public T call() throws Exception {
                WeldManager weldManager = CDI.current().select(WeldManager.class).get();

                //verify we have req context active already, in arq. this will be BoundRequestContext
                Collection<WeldAlterableContext> activeContexts = weldManager.getActiveWeldAlterableContexts();
                WeldAlterableContext requestContext = null;
                for (WeldAlterableContext activeContext : activeContexts) {
                    if (activeContext.getScope().equals(RequestScoped.class)) {
                        requestContext = activeContext;
                    }
                }
                if (requestContext == null) {
                    throw new IllegalStateException("RequestContext is expected to be active on current thread.");
                }
                // clear up the context
                requestContext.clearAndSet(Collections.emptySet());

                // now execute the actual original task, bean should be recreated, return result
                return task.call();
            }
        };
        Integer result = -1;
        try {
            result = (Integer) wrappedTask.call();
        } catch (Exception e) {
            throw new IllegalStateException("An exception occurred when executing the task on current thread: " + e);
        }
        return result;
    }
}
