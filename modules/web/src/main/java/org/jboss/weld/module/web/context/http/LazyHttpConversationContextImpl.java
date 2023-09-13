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
package org.jboss.weld.module.web.context.http;

import java.util.function.Consumer;

import jakarta.enterprise.context.BusyConversationException;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.NonexistentConversationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.logging.ConversationLogger;
import org.jboss.weld.module.web.servlet.ConversationContextActivator;

/**
 * An implementation of {@link HttpConversationContext} that is capable of lazy initialization. By default, the context is
 * associated with a request and the
 * active flag is set to true in the beginning of the request processing but the context is not initialized (cid not read and
 * the state not restored) until the
 * conversation context is first accessed. As a result, {@link BusyConversationException} or
 * {@link NonexistentConversationException} may be thrown late in the
 * request processing and any component invoking methods on {@link ConversationScoped} beans should be ready to catch these
 * exceptions.
 *
 * Lazy initialization is mostly a workaround for https://issues.jboss.org/browse/CDI-411.
 *
 * @author Jozef Hartinger
 *
 */
public class LazyHttpConversationContextImpl extends HttpConversationContextImpl {

    private final ThreadLocal<Consumer<HttpServletRequest>> transientConversationInitializationCallback;

    private final ThreadLocal<Object> initialized;

    public LazyHttpConversationContextImpl(String contextId, ServiceRegistry services) {
        super(contextId, services);
        this.initialized = new ThreadLocal<Object>();
        this.transientConversationInitializationCallback = new ThreadLocal<>();
    }

    /**
     *
     * @param transientConversationInitializationCallback This callback will be executed during initialization
     */
    public void activateLazily(Consumer<HttpServletRequest> transientConversationInitializationCallback) {
        activate();
        // Always set the callback - the deactivation might not be performed properly
        this.transientConversationInitializationCallback.set(transientConversationInitializationCallback);
    }

    @Override
    public void activate() {
        if (!isAssociated()) {
            throw ConversationLogger.LOG.mustCallAssociateBeforeActivate();
        }
        if (!isActive()) {
            super.setActive(true);
        } else {
            ConversationLogger.LOG.contextAlreadyActive(getRequest());
        }
        // Reset the initialized flag - a thread which is not cleaned up properly (e.g. async processing on
        // Tomcat) may break the lazy initialization otherwise
        this.initialized.set(null);
    }

    public boolean isInitialized() {
        return initialized.get() != null;
    }

    @Override
    protected void initialize(String cid) {
        this.initialized.set(Boolean.TRUE);
        super.initialize(cid);
    }

    @Override
    public void deactivate() {
        try {
            if (isInitialized()) {
                try {
                    super.deactivate();
                } finally {
                    this.initialized.set(null);
                }
            } else {
                // Only deactivate the context, i.e. remove state threadlocal
                removeState();
            }
        } finally {
            this.transientConversationInitializationCallback.set(null);
        }
    }

    @Override
    public boolean destroy(HttpSession session) {
        if (isAssociated()) {
            checkContextInitialized();
        }
        return super.destroy(session);
    }

    @Override
    protected void checkContextInitialized() {
        if (!isInitialized()) {
            HttpServletRequest request = getRequest();
            String cid = ConversationContextActivator.determineConversationId(request, getParameterName());
            try {
                initialize(cid);
            } catch (NonexistentConversationException e) {
                // new conversation is associated in this case, but we need to fire init event and rethrow exception
                fireInitEvent(request);
                throw e;
            }
            // new conversation, fire init event
            if (cid == null || cid.isEmpty()) { // transient conversation
                fireInitEvent(request);
            }
        }
    }

    private void fireInitEvent(HttpServletRequest request) {
        Consumer<HttpServletRequest> callback = transientConversationInitializationCallback.get();
        if (callback != null) {
            callback.accept(request);
        }
    }
}
