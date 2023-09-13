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
package org.jboss.weld.environment.se.test.context.propagation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.weld.context.WeldAlterableContext;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.api.WeldManager;

/**
 * Util class allowing to offload tasks to another thread. Takes case of context activation/propagation for req. scope (in SE).
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class ContextPropagationSEService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    public static <T> Future<T> propagateContextsAndSubmitTask(Callable<T> task) {
        // gather all the contexts we want to propagate and the instances in them
        Map<Class<? extends Annotation>, Collection<ContextualInstance<?>>> scopeToContextualInstances = new HashMap<>();
        for (WeldAlterableContext context : WeldContainer.current().select(WeldManager.class).get()
                .getActiveWeldAlterableContexts()) {
            scopeToContextualInstances.put(context.getScope(), context.getAllContextualInstances());
        }
        // We create a task wrapper which will make sure we have contexts propagated
        Callable<T> wrappedTask = new Callable<T>() {

            @Override
            public T call() throws Exception {
                WeldContainer container = WeldContainer.current();
                WeldManager weldManager = container.select(WeldManager.class).get();
                BoundRequestContext requestContext = weldManager.instance()
                        .select(BoundRequestContext.class, BoundLiteral.INSTANCE).get();

                // we will be using bound context, prepare backing map
                Map<String, Object> requestMap = new HashMap<>();

                // activate request context
                requestContext.associate(requestMap);
                requestContext.activate();

                // propagate req. context
                if (scopeToContextualInstances.get(requestContext.getScope()) != null) {
                    requestContext.clearAndSet(scopeToContextualInstances.get(requestContext.getScope()));
                }
                // now execute the actual original task
                T result = task.call();

                // cleanup, context deactivation
                // requestContext.invalidate(); is deliberately left out so that pre destroy callbacks are not invoked
                requestContext.deactivate();

                // all done, return
                return result;
            }
        };
        return executor.submit(wrappedTask);
    }
}
