/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.io.IOException;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@RequestScoped
@Named("controller")
public class TimeoutController {

    @Inject
    private Conversation conversation;

    @Inject
    private TimeoutConversationScopedBean bean;

    public void beginConversation(int timeout) {
        conversation.begin();
        conversation.setTimeout(timeout);
        bean.setValue("foo");
    }

    public void ping() {
        bean.getValue();
    }

    public String makeLongRequest() throws IOException {
        try {
            conversation.setTimeout(1000);
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Unable to sleep thread for long request", e);
        }
        return "doRedirect";
    }

}
