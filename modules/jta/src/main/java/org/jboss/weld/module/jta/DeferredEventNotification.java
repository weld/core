/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.jta;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.Container;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.jboss.weld.event.CurrentEventMetadata;
import org.jboss.weld.injection.ThreadLocalStack.ThreadLocalStackReference;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.util.Observers;

/**
 * A task that will notify the observer of a specific event at some future time.
 *
 * @author David Allen
 * @author Jozef Hartinger
 */
class DeferredEventNotification<T> implements Runnable {

    // The observer
    protected final ObserverMethod<? super T> observer;
    // The event object
    private final T event;
    protected final EventMetadata metadata;
    private final CurrentEventMetadata currentEventMetadata;
    private final String contextId;

    private final Status status;
    private final boolean before;

    /**
     * Creates a new deferred event notifier.
     *
     * @param observer The observer to be notified
     * @param metadata The event being fired
     */
    public DeferredEventNotification(String contextId, T event, EventMetadata metadata, ObserverMethod<? super T> observer,
            CurrentEventMetadata currentEventMetadata, Status status, boolean before) {
        this.contextId = contextId;
        this.observer = observer;
        this.event = event;
        this.metadata = metadata;
        this.currentEventMetadata = currentEventMetadata;
        this.status = status;
        this.before = before;
    }

    public void run() {
        try {
            EventLogger.LOG.asyncFire(metadata, observer);
            new RunInRequest(contextId) {

                @Override
                protected void execute() {
                    final ThreadLocalStackReference<EventMetadata> stack = currentEventMetadata.pushIfNotNull(metadata);
                    try {
                        Observers.notify(observer, event, metadata);
                    } finally {
                        stack.pop();
                    }
                }

            }.run();

        } catch (Exception e) {
            EventLogger.LOG.asyncObserverFailure(observer, metadata, e.getCause() != null ? e.getCause() : e);
            EventLogger.LOG.catchingDebug(e);
        }
    }

    public Status getStatus() {
        return status;
    }

    public boolean isBefore() {
        return before;
    }

    @Override
    public String toString() {
        return "Deferred event [" + event + "] for [" + observer + "]";
    }

    private abstract static class RunInRequest {

        private final String contextId;

        public RunInRequest(String contextId) {
            this.contextId = contextId;
        }

        protected abstract void execute();

        public void run() {

            if (isRequestContextActive()) {
                execute();
            } else {
                RequestContext requestContext = Container.instance(contextId).deploymentManager().instance()
                        .select(RequestContext.class, UnboundLiteral.INSTANCE).get();
                try {
                    requestContext.activate();
                    execute();
                } finally {
                    requestContext.invalidate();
                    requestContext.deactivate();
                }
            }
        }

        private boolean isRequestContextActive() {
            return Container.instance(contextId).deploymentManager().isContextActive(RequestScoped.class);
        }

    }
}
