/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

public class WoodlandExtension implements Extension {

    private static boolean injectCalled;
    private static boolean postConstructCalled;
    private static boolean preDestroyCalled;
    private static boolean produceCalled;

    public void cleanup(@Observes BeforeShutdown shutdown) {
        reset();
        Woodland.reset();
    }

    public void enhanceWoodland(final @Observes ProcessInjectionTarget<Woodland> processWoodland) {
        final InjectionTarget<Woodland> it = processWoodland.getInjectionTarget();
        processWoodland.setInjectionTarget(new InjectionTarget<Woodland>() {

            public void inject(Woodland instance, CreationalContext<Woodland> ctx) {
                injectCalled = true;
                it.inject(instance, ctx);
            }

            public void postConstruct(Woodland instance) {
                postConstructCalled = true;
                it.postConstruct(instance);
            }

            public void preDestroy(Woodland instance) {
                preDestroyCalled = true;
                it.preDestroy(instance);
            }

            public void dispose(Woodland instance) {
                // No-op for class bean

            }

            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

            public Woodland produce(CreationalContext<Woodland> ctx) {
                produceCalled = true;
                return it.produce(ctx);
            }

        });
    }

    public static void reset() {
        injectCalled = false;
        postConstructCalled = false;
        preDestroyCalled = false;
        produceCalled = false;
    }

    public static boolean isInjectCalled() {
        return injectCalled;
    }

    public static boolean isPostConstructCalled() {
        return postConstructCalled;
    }

    public static boolean isPreDestroyCalled() {
        return preDestroyCalled;
    }

    public static boolean isProduceCalled() {
        return produceCalled;
    }

}
