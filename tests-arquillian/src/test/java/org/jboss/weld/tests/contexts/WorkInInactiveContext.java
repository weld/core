package org.jboss.weld.tests.contexts;

import org.jboss.weld.context.ManagedContext;

public abstract class WorkInInactiveContext {

    private final ManagedContext context;

    public WorkInInactiveContext(ManagedContext context) {
        this.context = context;
    }

    public void run() {
        boolean wasActive = false;
        try {
            wasActive = context.isActive();
            if (wasActive) {
                context.deactivate();
            }
            work();
        } finally {
            if (wasActive) {
                context.activate();
            }
        }
    }

    protected abstract void work();

    public ManagedContext getContext() {
        return context;
    }

}
