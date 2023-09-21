package org.jboss.weld.tck;

import jakarta.enterprise.context.spi.Contextual;

import org.jboss.cdi.tck.spi.CreationalContexts;
import org.jboss.weld.contexts.CreationalContextImpl;

public class CreationalContextsImpl implements CreationalContexts {
    @Override
    public <T> Inspectable<T> create(Contextual<T> contextual) {
        return new InspectableCreationalContext<>(contextual);
    }

    static class InspectableCreationalContext<T> extends CreationalContextImpl<T> implements Inspectable<T> {
        private Object lastBeanPushed = null;
        private boolean pushCalled = false;
        private boolean releaseCalled = false;

        public InspectableCreationalContext(Contextual<T> contextual) {
            super(contextual);
        }

        public void push(T incompleteInstance) {
            pushCalled = true;
            lastBeanPushed = incompleteInstance;
            super.push(incompleteInstance);
        }

        public Object getLastBeanPushed() {
            return lastBeanPushed;
        }

        public boolean isPushCalled() {
            return pushCalled;
        }

        public boolean isReleaseCalled() {
            return releaseCalled;
        }

        /**
         * We need to override this method because internally, Weld uses this enhanced method to release CCs so long
         * as the CC is instance of {@link org.jboss.weld.contexts.WeldCreationalContext} which will hold true for
         * this impl as well.
         */
        public void release(Contextual<T> contextual, T instance) {
            releaseCalled = true;
            super.release(contextual, instance);
        }

        public void release() {
            releaseCalled = true;
            super.release();
        }

    }
}
