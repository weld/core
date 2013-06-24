/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.beanManager.injectionTarget.ejb;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class InjectionTargetWrappingExtension implements Extension {

    public static Set<String> invokedPostConstructs = new HashSet<String>();

    public <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {
        pit.setInjectionTarget(new InjectionTargetWrapper<T>(pit.getInjectionTarget()));
    }

    private static class InjectionTargetWrapper<T> implements InjectionTarget<T> {

        private InjectionTarget<T> delegate;

        public InjectionTargetWrapper(InjectionTarget<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void dispose(T instance) {
            delegate.dispose(instance);
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return delegate.getInjectionPoints();
        }

        @Override
        public T produce(CreationalContext<T> ctx) {
            return delegate.produce(ctx);
        }

        @Override
        public void inject(T instance, CreationalContext<T> ctx) {
            delegate.inject(instance, ctx);
        }

        @Override
        public void postConstruct(T instance) {
            invokedPostConstructs.add(instance.getClass().getName());
            delegate.postConstruct(instance);
        }

        @Override
        public void preDestroy(T instance) {
            delegate.preDestroy(instance);
        }
    }
}
