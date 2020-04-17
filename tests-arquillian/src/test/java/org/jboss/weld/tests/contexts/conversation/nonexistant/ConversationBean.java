/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.contexts.conversation.nonexistant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.NonexistentConversationException;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

@ApplicationScoped
public class ConversationBean {

    public static int TIMES_INIT_INVOKED = 0;
    public static int TIMES_DESTROYED_INVOKED = 0;

    @Inject
    private Conversation conversation;

    @Inject
    private ConversationScopedBean bean;

    public void begin(ServletRequest request) {
        try {
            if (conversation.isTransient()) {
                conversation.begin();
                bean.getMsg();
            }
        } catch (NonexistentConversationException e) {
            String cidParam = request.getParameter("cid");
            conversation.begin(cidParam);
            bean.getMsg();
        }
    }

    public void end() {
        conversation.end();
    }

    void onInitialized(@Observes @Initialized(ConversationScoped.class) Object obj, HttpServletRequest request) {
        TIMES_INIT_INVOKED++;
    }

    void onDestroyed(@Observes @Destroyed(ConversationScoped.class) ServletRequest req) {
        TIMES_DESTROYED_INVOKED++;
    }
}
