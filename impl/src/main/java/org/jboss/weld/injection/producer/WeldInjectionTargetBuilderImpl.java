/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.producer;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldInjectionTarget;
import org.jboss.weld.manager.api.WeldInjectionTargetBuilder;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.InjectionTargets;

/**
 * Default {@link WeldInjectionTargetBuilder} implementation.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class WeldInjectionTargetBuilderImpl<T> implements WeldInjectionTargetBuilder<T> {

    private final InjectionTargetService injectionTargetService;

    private boolean resourceInjectionEnabled = true;
    private boolean targetClassLifecycleCallbacksEnabled = true;
    private boolean interceptorsEnabled = true;
    private boolean decorationEnabled = true;
    private Bean<T> bean;

    private final EnhancedAnnotatedType<T> type;
    private final BeanManagerImpl manager;

    public WeldInjectionTargetBuilderImpl(AnnotatedType<T> type, BeanManagerImpl manager) {
        this.manager = manager;
        this.type = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(type, manager.getId());
        this.injectionTargetService = manager.getServices().get(InjectionTargetService.class);
    }

    @Override
    public WeldInjectionTargetBuilder<T> setResourceInjectionEnabled(boolean value) {
        this.resourceInjectionEnabled = value;
        return this;
    }

    @Override
    public WeldInjectionTargetBuilder<T> setTargetClassLifecycleCallbacksEnabled(boolean value) {
        this.targetClassLifecycleCallbacksEnabled = value;
        return this;
    }

    @Override
    public WeldInjectionTargetBuilder<T> setInterceptionEnabled(boolean value) {
        this.interceptorsEnabled = value;
        return this;
    }

    @Override
    public WeldInjectionTargetBuilder<T> setDecorationEnabled(boolean value) {
        this.decorationEnabled = value;
        return this;
    }

    @Override
    public WeldInjectionTargetBuilder<T> setBean(Bean<T> bean) {
        this.bean = bean;
        return this;
    }

    @Override
    public WeldInjectionTarget<T> build() {
        BasicInjectionTarget<T> injectionTarget = buildInternal();
        injectionTargetService.addInjectionTargetToBeInitialized(type, injectionTarget);
        injectionTargetService.validateProducer(injectionTarget);
        return injectionTarget;
    }

    private BasicInjectionTarget<T> buildInternal() {
        final Injector<T> injector = buildInjector();
        final LifecycleCallbackInvoker<T> invoker = buildInvoker();
        NonProducibleInjectionTarget<T> nonProducible = InjectionTargets.createNonProducibleInjectionTarget(type, bean,
                injector, invoker, manager);
        if (nonProducible != null) {
            return nonProducible;
        }
        if (!interceptorsEnabled && !decorationEnabled) {
            return BasicInjectionTarget.create(type, bean, manager, injector, invoker);
        } else if (interceptorsEnabled && decorationEnabled) {
            return new BeanInjectionTarget<T>(type, bean, manager, injector, invoker);
        }
        throw new IllegalStateException(
                "Unsupported combination: [interceptorsEnabled=" + interceptorsEnabled + ", decorationEnabled="
                        + decorationEnabled + "]");
    }

    private Injector<T> buildInjector() {
        if (resourceInjectionEnabled) {
            return ResourceInjector.of(type, bean, manager);
        } else {
            return DefaultInjector.of(type, bean, manager);
        }
    }

    private LifecycleCallbackInvoker<T> buildInvoker() {
        if (targetClassLifecycleCallbacksEnabled) {
            return DefaultLifecycleCallbackInvoker.of(type);
        } else {
            return NoopLifecycleCallbackInvoker.<T> getInstance();
        }
    }

    @Override
    public String toString() {
        return "WeldInjectionTargetBuilderImpl for " + type;
    }

}
