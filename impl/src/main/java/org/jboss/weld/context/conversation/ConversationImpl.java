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
package org.jboss.weld.context.conversation;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;

import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.ManagedConversation;
import org.jboss.weld.logging.ConversationLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author Nicklas Karlsson
 * @author Marko Luksa
 */
public class ConversationImpl implements ManagedConversation, Serializable {

    private static final long serialVersionUID = -5566903049468084035L;

    private String id;
    private boolean _transient;
    private long timeout;

    private ReentrantLock concurrencyLock;
    private long lastUsed;

    private BeanManagerImpl manager;

    @Inject
    public ConversationImpl(BeanManagerImpl manager) {
        this.manager = manager;
        this._transient = true;
        this.timeout = isContextActive() ? getActiveConversationContext().getDefaultTimeout() : 0;
        this.concurrencyLock = new ReentrantLock();
        touch();
    }

    public void begin() {
        verifyConversationContextActive();
        if (!_transient) {
            throw ConversationLogger.LOG.beginCalledOnLongRunningConversation();
        }
        _transient = false;
        if (this.id == null) {
            // This a conversation that was made transient previously in this request
            this.id = getActiveConversationContext().generateConversationId();
        }
        ConversationLogger.LOG.promotedTransient(id);
    }

    public void begin(String id) {
        verifyConversationContextActive();
        if (!_transient) {
            throw ConversationLogger.LOG.beginCalledOnLongRunningConversation();
        }
        if (getActiveConversationContext().getConversation(id) != null) {
            throw ConversationLogger.LOG.conversationIdAlreadyInUse(id);
        }
        _transient = false;
        this.id = id;
        ConversationLogger.LOG.promotedTransient(id);
    }


    public void end() {
        verifyConversationContextActive();
        if (_transient) {
            throw ConversationLogger.LOG.endCalledOnTransientConversation();
        }
        ConversationLogger.LOG.demotedLrc(id);
        _transient = true;
    }

    public String getId() {
        verifyConversationContextActive();
        if (!_transient) {
            return id;
        } else {
            return null;
        }
    }

    public long getTimeout() {
        verifyConversationContextActive();
        return timeout;
    }

    public void setTimeout(long timeout) {
        verifyConversationContextActive();
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        if (_transient) {
            return "Transient conversation";
        } else {
            return "Conversation with id: " + id;
        }
    }

    public boolean isTransient() {
        verifyConversationContextActive();
        return _transient;
    }

    public long getLastUsed() {
        verifyConversationContextActive();
        return lastUsed;
    }

    public void touch() {
        verifyConversationContextActive();
        lastUsed = System.currentTimeMillis();
    }

    public boolean lock(long timeout) {
        verifyConversationContextActive();
        boolean success;
        try {
            success = concurrencyLock.tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            success = false;
        }
        if (success) {
            ConversationLogger.LOG.conversationLocked(this);
        } else {
            ConversationLogger.LOG.conversationUnavailable(timeout, this);
        }
        return success;
    }

    public boolean unlock() {
        verifyConversationContextActive();
        if (!concurrencyLock.isLocked()) {
            return true;
        }
        if (concurrencyLock.isHeldByCurrentThread()) {
            concurrencyLock.unlock();
            ConversationLogger.LOG.conversationUnlocked(this);
        } else {
            ConversationLogger.LOG.illegalConversationUnlockAttempt(this, "not owner");
        }
        return !concurrencyLock.isLocked();
    }

    private void verifyConversationContextActive() {
        if (!isContextActive()) {
            throw new ContextNotActiveException("Conversation Context not active when method called on conversation " + this);
        }
    }

    public boolean isContextActive() {
        return manager.isContextActive(ConversationScoped.class);
    }

    private ConversationContext getActiveConversationContext() {
        return (ConversationContext) manager.getUnwrappedContext(ConversationScoped.class);
    }

}
