/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.decorators.custom.prioritized;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.inject.spi.Prioritized;

/**
 *
 * @author Martin Kouba
 *
 */
public class CustomPrioritizedDecorator implements Decorator<FooDecorator>, PassivationCapable, Prioritized {

    private final Set<InjectionPoint> injectionPoints;

    private final BeanManager beanManager;

    CustomPrioritizedDecorator(BeanManager beanManager) {
        this.beanManager = beanManager;
        this.injectionPoints = Collections.singleton(new InjectionPoint() {

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public boolean isDelegate() {
                return true;
            }

            @Override
            public Type getType() {
                return Decorated.class;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return Collections.singleton(Default.Literal.INSTANCE);
            }

            @Override
            public Member getMember() {
                try {
                    return FooDecorator.class.getDeclaredField("delegate");
                } catch (NoSuchFieldException | SecurityException e) {
                    throw new IllegalStateException();
                }
            }

            @Override
            public Bean<?> getBean() {
                return CustomPrioritizedDecorator.this;
            }

            @Override
            public Annotated getAnnotated() {
                return beanManager.createAnnotatedType(FooDecorator.class).getFields().iterator().next();
            }
        });
    }

    public Class<?> getBeanClass() {
        return FooDecorator.class;
    }

    public String getId() {
        return toString();
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

    @Override
    public FooDecorator create(CreationalContext<FooDecorator> creationalContext) {
        FooDecorator fooDecorator = new FooDecorator();
        fooDecorator.delegate = (Decorated) beanManager.getInjectableReference(injectionPoints.iterator().next(),
                creationalContext);
        return fooDecorator;
    }

    @Override
    public void destroy(FooDecorator instance, CreationalContext<FooDecorator> creationalContext) {
        creationalContext.release();
    }

    @Override
    public Set<Type> getTypes() {
        return Collections.singleton(Decorated.class);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.emptySet();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public Type getDelegateType() {
        return Decorated.class;
    }

    @Override
    public Set<Annotation> getDelegateQualifiers() {
        return Collections.emptySet();
    }

    @Override
    public Set<Type> getDecoratedTypes() {
        return Collections.singleton(Decorated.class);
    }
}
