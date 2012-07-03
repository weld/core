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

import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.ManagedConversation;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.slf4j.cal10n.LocLogger;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.jboss.weld.logging.Category.CONVERSATION;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ConversationMessage.BEGIN_CALLED_ON_LONG_RUNNING_CONVERSATION;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_ID_ALREADY_IN_USE;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_LOCKED;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_UNAVAILBLE;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_UNLOCKED;
import static org.jboss.weld.logging.messages.ConversationMessage.DEMOTED_LRC;
import static org.jboss.weld.logging.messages.ConversationMessage.END_CALLED_ON_TRANSIENT_CONVERSATION;
import static org.jboss.weld.logging.messages.ConversationMessage.ILLEGAL_CONVERSATION_UNLOCK_ATTEMPT;
import static org.jboss.weld.logging.messages.ConversationMessage.PROMOTED_TRANSIENT;

/**
 * @author Nicklas Karlsson
 */
@SuppressWarnings(value = "SE_BAD_FIELD", justification = "InstanceImpl, which we actually use, is serializable")
public class ConversationImpl implements ManagedConversation, Serializable {

    private static final long serialVersionUID = 8873338254645033645L;

    private static final LocLogger log = loggerFactory().getLogger(CONVERSATION);

    private String id;
    private boolean _transient;
    private long timeout;

    private ReentrantLock concurrencyLock;
    private long lastUsed;

    private final Instance<ConversationContext> conversationContexts;

    @Inject
    public ConversationImpl(Instance<ConversationContext> conversationContexts) {
        this.conversationContexts = conversationContexts;
        this._transient = true;
        ConversationContext conversationContext = getConversationContext();
        if (conversationContext != null) {
            this.timeout = conversationContext.getDefaultTimeout();
        } else {
            this.timeout = 0;
        }
        this.concurrencyLock = new ReentrantLock();
        touch();
    }

    private ConversationContext getConversationContext() {
        for (ConversationContext conversationContext : conversationContexts) {
            if (conversationContext.isActive()) {
                return conversationContext;
            }
        }
        return null;
    }

    public void begin() {
        verifyConversationContextActive();
        if (!_transient) {
            throw new IllegalStateException(BEGIN_CALLED_ON_LONG_RUNNING_CONVERSATION);
        }
        _transient = false;
        if (this.id == null) {
            // This a conversation that was made transient previously in this request
            this.id = getConversationContext().generateConversationId();
        }
        log.debug(PROMOTED_TRANSIENT, id);
    }

    public void begin(String id) {
        verifyConversationContextActive();
        if (!_transient) {
            throw new IllegalStateException(BEGIN_CALLED_ON_LONG_RUNNING_CONVERSATION);
        }
        if (getConversationContext().getConversation(id) != null) {
            throw new IllegalArgumentException(CONVERSATION_ID_ALREADY_IN_USE, id);
        }
        _transient = false;
        this.id = id;
        log.debug(PROMOTED_TRANSIENT, id);
    }


    public void end() {
        verifyConversationContextActive();
        if (_transient) {
            throw new IllegalStateException(END_CALLED_ON_TRANSIENT_CONVERSATION);
        }
        log.debug(DEMOTED_LRC, id);
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
            log.trace(CONVERSATION_LOCKED, this);
        } else {
            log.warn(CONVERSATION_UNAVAILBLE, timeout, this);
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
            log.trace(CONVERSATION_UNLOCKED, this);
        } else {
            log.warn(ILLEGAL_CONVERSATION_UNLOCK_ATTEMPT, this, "not owner");
        }
        return !concurrencyLock.isLocked();
    }

    private void verifyConversationContextActive() {
        ConversationContext ctx = getConversationContext();
        if (ctx == null) {
            throw new ContextNotActiveException("Conversation Context not active when method called on conversation " + this);
        }
    }

}
