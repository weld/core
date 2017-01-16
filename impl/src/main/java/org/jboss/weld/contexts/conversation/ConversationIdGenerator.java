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
package org.jboss.weld.contexts.conversation;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The default conversation id generator
 *
 * @author Nicklas Karlsson
 */
public class ConversationIdGenerator implements Callable<String>, Serializable {

    public static final String CONVERSATION_ID_GENERATOR_ATTRIBUTE_NAME = ConversationIdGenerator.class.getName();

    private static final long serialVersionUID = 8489811313900825684L;

    // The next conversation ID
    private final AtomicInteger id;

    /**
     * Creates a new conversation ID generator
     */
    public ConversationIdGenerator() {
        this.id = new AtomicInteger(1);
    }

    public String call() {
        int nextId = id.getAndIncrement();
        return String.valueOf(nextId);
    }

}
