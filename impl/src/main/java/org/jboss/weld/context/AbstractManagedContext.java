package org.jboss.weld.context;

import static java.lang.Boolean.FALSE;

public abstract class AbstractManagedContext extends AbstractContext implements ManagedContext {

    private final ThreadLocal<Boolean> active;
    private final ThreadLocal<Boolean> valid;

    public AbstractManagedContext(String contextId, boolean multithreaded) {
        super(contextId, multithreaded);
        this.active = new ThreadLocal<Boolean>();
        this.valid = new ThreadLocal<Boolean>();

    }

    public boolean isActive() {
        Boolean active = this.active.get();
        return active == null ? false : active.booleanValue();
    }

    protected void setActive(boolean active) {
        this.active.set(active);
    }

    public void invalidate() {
        this.valid.set(FALSE);
    }

    public void activate() {
        setActive(true);
    }

    private boolean isValid() {
        Boolean valid = this.valid.get();
        return valid == null ? true : valid.booleanValue();
    }

    public void deactivate() {
        if (!isValid()) {
            destroy();
        }
        active.remove();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        active.remove();
        valid.remove();
    }

}