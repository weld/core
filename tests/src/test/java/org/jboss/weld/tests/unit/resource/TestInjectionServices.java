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
package org.jboss.weld.tests.unit.resource;

import java.lang.reflect.Field;

import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.injection.spi.InjectionContext;
import org.jboss.weld.injection.spi.InjectionServices;

class TestInjectionServices implements InjectionServices {

    public static final String RESOURCE_NAME = "injected resource";

    private Field field;

    @Override
    public <T> void registerInjectionTarget(InjectionTarget<T> injectionTarget, AnnotatedType<T> annotatedType) {
        if (annotatedType.getJavaClass().equals(InjectedClass.class)) {
            for (AnnotatedField<?> field : annotatedType.getFields()) {
                if (field.getJavaMember().getName().equals("resource")) {
                    this.field = field.getJavaMember();
                    this.field.setAccessible(true);
                }
            }
        }
    }

    private void setField(Object value) {
        if (field == null) {
            throw new IllegalStateException("field is null");
        }
        try {
            this.field.set(null, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void aroundInject(InjectionContext<T> injectionContext) {
        if (injectionContext.getTarget() == null
                && injectionContext.getAnnotatedType().getJavaClass().equals(InjectedClass.class)) {
            setField(new SpecialResource(RESOURCE_NAME));
        }
    }

    @Override
    public void cleanup() {
        setField(null);
    }
}
