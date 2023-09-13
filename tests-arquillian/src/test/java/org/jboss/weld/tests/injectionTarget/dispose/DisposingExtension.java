/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.injectionTarget.dispose;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

/**
 * Helps test fix for <a
 * href="https://issues.jboss.org/browse/WELD-2580">WELD-2580</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 *         target="_parent">Laird Nelson</a>
 */
public class DisposingExtension implements Extension {

    static boolean disposeCalled;

    public DisposingExtension() {
        super();
    }

    private final void processInjectionTarget(@Observes final ProcessInjectionTarget<Widget> event) {
        final InjectionTarget<Widget> delegate = event.getInjectionTarget();
        event.setInjectionTarget(new InjectionTarget<Widget>() {

            @Override
            public final Widget produce(final CreationalContext<Widget> cc) {
                return delegate.produce(cc);
            }

            @Override
            public final void inject(final Widget instance, final CreationalContext<Widget> cc) {
                delegate.inject(instance, cc);
            }

            @Override
            public final void postConstruct(final Widget instance) {
                delegate.postConstruct(instance);
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return delegate.getInjectionPoints();
            }

            @Override
            public final void preDestroy(final Widget instance) {
                delegate.preDestroy(instance);
            }

            @Override
            public final void dispose(final Widget instance) {
                delegate.dispose(instance);
                disposeCalled = true;
            }

        });
    }

}
