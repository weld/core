/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.decorators.broken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;

public class GlueDecoratorExtension implements Extension {

    void registerDecorator(@Observes AfterBeanDiscovery event, BeanManager manager) {
        AnnotatedType<GlueDecorator> annotatedType = manager.createAnnotatedType(GlueDecorator.class);
        final BeanAttributes<GlueDecorator> attributes = manager.createBeanAttributes(annotatedType);
        final InjectionPoint delegateInjectionPoint = manager.createInjectionPoint(annotatedType.getConstructors().iterator()
                .next().getParameters().get(0));

        Decorator<GlueDecorator> decorator = new DecoratorImpl<GlueDecorator>() {

            @Override
            public Type getDelegateType() {
                return Glue.class;
            }

            @Override
            public Set<Annotation> getDelegateQualifiers() {
                return Collections.<Annotation> singleton(Any.Literal.INSTANCE);
            }

            @Override
            public Set<Type> getDecoratedTypes() {
                return Collections.emptySet();
            }

            @Override
            public Class<?> getBeanClass() {
                return GlueDecorator.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                final Decorator<GlueDecorator> decorator = this;
                InjectionPoint wrappedInjectionPoint = new ForwardingInjectionPoint() {

                    @Override
                    public Bean<?> getBean() {
                        return decorator;
                    }

                    @Override
                    protected InjectionPoint delegate() {
                        return delegateInjectionPoint;
                    }
                };
                return Collections.singleton(wrappedInjectionPoint);
            }

            @Override
            public GlueDecorator create(CreationalContext<GlueDecorator> creationalContext) {
                return new GlueDecorator(null);
            }

            @Override
            public void destroy(GlueDecorator instance, CreationalContext<GlueDecorator> creationalContext) {
                creationalContext.release();
            }

            @Override
            protected BeanAttributes<GlueDecorator> attributes() {
                return attributes;
            }
        };
        event.addBean(decorator);
    }

    private abstract static class DecoratorImpl<T> extends ForwardingBeanAttributes<T> implements Decorator<T> {
    }
}
