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
package org.jboss.weld.event;

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.Container;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLogger.Level;

import static org.jboss.weld.logging.Category.EVENT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.EventMessage.ASYNC_FIRE;
import static org.jboss.weld.logging.messages.EventMessage.ASYNC_OBSERVER_FAILURE;

/**
 * A task that will notify the observer of a specific event at some future time.
 *
 * @author David Allen
 * @author Jozef Hartinger
 */
public class DeferredEventNotification<T> implements Runnable {
    private static final LocLogger log = loggerFactory().getLogger(EVENT);
    private static final XLogger xLog = loggerFactory().getXLogger(EVENT);

    // The observer
    protected final ObserverMethod<? super T> observer;
    // The event object
    protected final T event;

    private final String contextId;

    /**
     * Creates a new deferred event notifier.
     *
     * @param observer The observer to be notified
     * @param event    The event being fired
     */
    public DeferredEventNotification(T event, ObserverMethod<? super T> observer, String contextId) {
        this.observer = observer;
        this.event = event;
        this.contextId = contextId;
    }

    public void run() {
        try {
            log.debug(ASYNC_FIRE, event, observer);
            new RunInRequest(contextId) {

                @Override
                protected void execute() {
                    observer.notify(event);
                }

            }.run();

        } catch (Exception e) {
            log.error(ASYNC_OBSERVER_FAILURE, event);
            xLog.throwing(Level.DEBUG, e);
        }
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
                RequestContext requestContext = Container.instance(contextId).deploymentManager().instance().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
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
            for (RequestContext requestContext : Container.instance(contextId).deploymentManager().instance().select(RequestContext.class)) {
                if (requestContext.isActive()) {
                    return true;
                }
            }
            return false;
        }

    }

}
