/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.osgi.tests.cdispi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import java.lang.reflect.Field;
import java.util.Set;

@ApplicationScoped
public class TestExtension implements Extension {

    <X> void processInjectionTarget(@Observes ProcessInjectionTarget<X> processInjectionTarget) {

        final InjectionTarget<X> old = processInjectionTarget.getInjectionTarget();
        final AnnotatedType<X> annotatedType = processInjectionTarget.getAnnotatedType();

        InjectionTarget<X> wrapped = new InjectionTarget<X>() {
            @Override
            public void inject(X instance, CreationalContext<X> ctx) {
                old.inject(instance, ctx);
                for (Field field : annotatedType.getJavaClass().getFields()) {
                    if (field.getType() == String.class) {
                        try {
                            field.setAccessible(true);
                            field.set(instance, "Hacked by extension !");
                        } catch (Exception e) {
                            throw new InjectionException(e);
                        }
                    }
                }
            }

            @Override
            public void postConstruct(X instance) {
                old.postConstruct(instance);
            }

            @Override
            public void preDestroy(X instance) {
                old.preDestroy(instance);
            }

            @Override
            public X produce(CreationalContext<X> ctx) {
                return old.produce(ctx);
            }

            @Override
            public void dispose(X instance) {
                old.dispose(instance);
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return old.getInjectionPoints();
            }
        };

        processInjectionTarget.setInjectionTarget(wrapped);
    }
}
