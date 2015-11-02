/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.conversation.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.event.Observes;
import javax.servlet.ServletRequest;

@ApplicationScoped
public class DestroyedConversationObserver {

    private final Set<String> disassociatedConversationIds = Collections.synchronizedSet(new HashSet<String>());
    private final Set<String> associatedConversationIds = Collections.synchronizedSet(new HashSet<String>());
    private final List<Message> destroyedMessages = Collections.synchronizedList(new ArrayList<Message>());

    void observeDestroyedConversation(@Observes @Destroyed(ConversationScoped.class) String id) {
        disassociatedConversationIds.add(id);
    }

    void observeDestroyedAssociatedConversation(@Observes @Destroyed(ConversationScoped.class) ServletRequest request) {
        String cid = request.getParameter("cid");
        if (cid != null) {
            associatedConversationIds.add(cid);
        }
    }

    public Set<String> getDisassociatedConversationIds() {
        return Collections.unmodifiableSet(disassociatedConversationIds);
    }

    public Set<String> getAssociatedConversationIds() {
        return Collections.unmodifiableSet(associatedConversationIds);
    }

    public void addMessage(Message message) {
        destroyedMessages.add(message);
    }

    public List<Message> getDestroyedMessages() {
        return destroyedMessages;
    }

    void reset() {
        disassociatedConversationIds.clear();
        associatedConversationIds.clear();
        destroyedMessages.clear();
    }

}
