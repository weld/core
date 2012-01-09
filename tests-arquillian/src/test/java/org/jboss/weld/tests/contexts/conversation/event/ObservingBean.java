/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.contexts.conversation.event;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.servlet.ServletRequestEvent;

@ApplicationScoped
public class ObservingBean {

    private final AtomicInteger initializedConversationCount = new AtomicInteger();
    private final AtomicInteger destroyedConversationCount = new AtomicInteger();

    public void observeConversationInitialized(@Observes @Initialized(ConversationScoped.class) ServletRequestEvent event) {
        initializedConversationCount.incrementAndGet();
    }

    public void observeConversationDestroyed(@Observes @Destroyed(ConversationScoped.class) ServletRequestEvent event) {
        destroyedConversationCount.incrementAndGet();
    }

    public AtomicInteger getInitializedConversationCount() {
        return initializedConversationCount;
    }

    public AtomicInteger getDestroyedConversationCount() {
        return destroyedConversationCount;
    }
}
