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
package org.jboss.weld.injection.producer;

import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;

/**
 * Carries extended metadata - {@link EnhancedAnnotatedType} for a given {@link InjectionTarget}. This object is dropped after
 * {@link InjectionTarget} validation to safe memory.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class InjectionTargetInitializationContext<T> {

    private final EnhancedAnnotatedType<T> enhancedAnnotatedType;
    private final BasicInjectionTarget<T> injectionTarget;

    public InjectionTargetInitializationContext(EnhancedAnnotatedType<T> enhancedAnnotatedType,
            BasicInjectionTarget<T> injectionTarget) {
        this.enhancedAnnotatedType = enhancedAnnotatedType;
        this.injectionTarget = injectionTarget;
    }

    public void initialize() {
        injectionTarget.initializeAfterBeanDiscovery(enhancedAnnotatedType);
    }

    public BasicInjectionTarget<T> getInjectionTarget() {
        return injectionTarget;
    }

    @Override
    public String toString() {
        return "InjectionTargetInitializationContext for " + injectionTarget;
    }
}
