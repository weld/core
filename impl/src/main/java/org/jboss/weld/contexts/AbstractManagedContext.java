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
package org.jboss.weld.contexts;

import org.jboss.weld.context.ManagedContext;
import org.jboss.weld.logging.ContextLogger;

/**
 *
 * @author Pete Muir
 */
public abstract class AbstractManagedContext extends AbstractContext implements ManagedContext {

    private final ThreadLocal<ManagedState> state;

    public AbstractManagedContext(String contextId, boolean multithreaded) {
        super(contextId, multithreaded);
        this.state = new ThreadLocal<ManagedState>();
    }

    public boolean isActive() {
        ManagedState managedState = state.get();
        return managedState != null ? managedState.isActive() : false;
    }

    protected void setActive(boolean active) {
        getManagedState().setActive(active);
    }

    public void invalidate() {
        getManagedState().setValid(false);
    }

    public void activate() {
        setActive(true);
    }

    public boolean isValid() {
        ManagedState managedState = state.get();
        return managedState != null ? managedState.isValid() : false;
    }

    public void deactivate() {
        if (!isValid()) {
            destroy();
        }
        removeState();
    }

    /**
     * The managed state should be always removed during deactivation. Note that there are some special cases where the context
     * is not deactivated through
     * {@link #deactivate()} method. E.g. {@link AbstractConversationContext#destroy(Object)}.
     */
    protected void removeState() {
        ContextLogger.LOG.tracev("State thread-local removed: {0}", this);
        state.remove();
    }

    private ManagedState getManagedState() {
        ManagedState managedState = state.get();
        if (managedState == null) {
            managedState = new ManagedState();
            state.set(managedState);
        }
        return managedState;
    }

    private static class ManagedState {

        private boolean isActive;

        private boolean isValid;

        private ManagedState() {
            isActive = false;
            isValid = true;
        }

        boolean isActive() {
            return isActive;
        }

        void setActive(boolean isActive) {
            this.isActive = isActive;
        }

        boolean isValid() {
            return isValid;
        }

        void setValid(boolean isValid) {
            this.isValid = isValid;
        }

    }

}
