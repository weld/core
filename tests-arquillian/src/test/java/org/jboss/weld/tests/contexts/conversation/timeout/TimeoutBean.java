/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.contexts.conversation.timeout;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class TimeoutBean {

    @Inject
    private Conversation conversation;

    @Inject
    private TimeoutConversationScopedBean bean;

    public String beginConversation() {
        conversation.begin();
        conversation.setTimeout(500);

        bean.setValue("foo");
        return conversation.getId();
    }

    public String testConversation() {
        String value = bean.getValue();
        if (!"foo".equals(value)) {
            throw new IllegalStateException("bean's value was not foo");
        }
        return conversation.getId();
    }
}
