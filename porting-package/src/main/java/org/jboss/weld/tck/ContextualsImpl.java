package org.jboss.weld.tck;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.cdi.tck.spi.Contextuals;

public class ContextualsImpl implements Contextuals {
    @Override
    public <T> Inspectable<T> create(T instance, Context context) {
        return new InspectableContextual<>(instance);
    }

    static class InspectableContextual<T> implements Inspectable<T> {
        private final T instanceToReturn;

        private CreationalContext<T> createCC;
        private T destroyInstance;
        private CreationalContext<T> destroyCC;

        InspectableContextual(T instanceToReturn) {
            this.instanceToReturn = instanceToReturn;
        }

        @Override
        public T create(CreationalContext<T> creationalContext) {
            createCC = creationalContext;
            return instanceToReturn;
        }

        @Override
        public void destroy(T instance, CreationalContext<T> creationalContext) {
            destroyInstance = instance;
            destroyCC = creationalContext;
        }

        @Override
        public CreationalContext<T> getCreationalContextPassedToCreate() {
            return createCC;
        }

        @Override
        public T getInstancePassedToDestroy() {
            return destroyInstance;
        }

        @Override
        public CreationalContext<T> getCreationalContextPassedToDestroy() {
            return destroyCC;
        }
    }
}
