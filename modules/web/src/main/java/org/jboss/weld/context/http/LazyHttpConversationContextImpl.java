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
package org.jboss.weld.context.http;

import java.util.function.Consumer;

import javax.enterprise.context.BusyConversationException;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.NonexistentConversationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.logging.ConversationLogger;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.jboss.weld.servlet.ConversationContextActivator;

/**
 * An implementation of {@link HttpConversationContext} that is capable of lazy initialization. By default, the context is associated with a request and the active flag
 * is set to true in the beginning of the request processing but the context is not initialized (cid not read and the state not restored) until the conversation context is first
 * accessed. As a result, {@link BusyConversationException} or {@link NonexistentConversationException} may be thrown late in the request processing and any component invoking
 * methods on {@link ConversationScoped} beans should be ready to catch these exceptions.
 *
 * Lazy initialization is mostly a workaround for https://issues.jboss.org/browse/CDI-411.
 *
 * @author Jozef Hartinger
 *
 */
public class LazyHttpConversationContextImpl extends HttpConversationContextImpl {

    private final ThreadLocal<Consumer<HttpServletRequest>> initializationCallback;

    private final ThreadLocal<Object> initialized;

    public LazyHttpConversationContextImpl(String contextId, BeanIdentifierIndex beanIdentifierIndex) {
        super(contextId, beanIdentifierIndex);
        this.initialized = new ThreadLocal<Object>();
        this.initializationCallback = new ThreadLocal<>();
    }

    /**
     *
     * @param initializationCallback This callback will be executed during initialization
     */
    public void activate(Consumer<HttpServletRequest> initializationCallback) {
        activate();
        if (initializationCallback != null) {
            this.initializationCallback.set(initializationCallback);
        } else {
            // For the case the deactivation was not performed properly
            this.initializationCallback.set(null);
        }
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
                // Only deactivate the context
                super.setActive(false);
            }
        } finally {
            this.initializationCallback.set(null);
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
            initialize(cid);
            if (cid == null) { // transient conversation
                Consumer<HttpServletRequest> callback = initializationCallback.get();
                if(callback != null) {
                    callback.accept(request);
                }
            }
        }
    }
}
